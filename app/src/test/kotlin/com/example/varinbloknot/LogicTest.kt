package com.example.varinbloknot

import com.example.varinbloknot.data.DesignItem
import com.example.varinbloknot.data.SortItem
import com.example.varinbloknot.data.Task
import com.example.varinbloknot.logic.*
import org.junit.Assert.*
import org.junit.Test

class LogicTest {
    private val items = listOf(
        DesignItem("Лампа", 1500, "лампу", listOf("lamp", "lampglow")),
        DesignItem("Полка", 1200, "полку", listOf("shelf")),
        DesignItem("Коврик", 2000, "коврик", listOf("rug")),
        DesignItem("Шторы", 1400, "шторы", listOf("curtains")),
    )
    private val plan = Task(
        "plan", "Сколько накопить?", priority = listOf("Коврик", "Шторы", "Лампа", "Полка"),
        texts = mapOf(
            "qNeed" to "{target}: {price}, осталось {left}. Сколько отложить?",
            "qHave" to "{target}: {price}, осталось {left}. Сколько останется?",
            "eNeed" to "{price} − {left} = {need}, {weeks} нед.",
            "eHave" to "{left} − {price} = {rest}",
            "hintNeed" to "отними", "hintHave" to "вычти",
        ),
    )

    // --- магазин ---
    @Test fun shopMath_andHiddenLayers() {
        val bought = listOf("Лампа", "Шторы")
        assertEquals(2900, Shop.spent(items, bought))
        assertEquals(2100, Shop.left(5000, items, bought))
        assertEquals(setOf("shelf", "rug"), Shop.hiddenLayers(5000, items, bought))
        assertTrue(Shop.hiddenLayers(null, items, emptyList()).isEmpty())   // без бюджета всё на месте
    }

    @Test fun cannotBuyOverBudget() {
        val bought = listOf("Коврик", "Лампа")      // 3 500, осталось 1 500
        assertFalse(Shop.canBuy(5000, items, bought, items[1].copy(price = 1600)))
        assertTrue(Shop.canBuy(5000, items, bought, items[3].copy(price = 1500)))
        assertTrue(Shop.canBuy(null, items, bought, items[2]))
    }

    // --- подстановки ---
    @Test fun fillText_usesWhatPlayerBought() {
        val t = "Ты купила {bought}. Осталось {left}."
        assertEquals("Ты купила лампу и шторы. Осталось 2 100 ₽.", fillText(t, 5000, items, listOf("Лампа", "Шторы")))
        assertEquals("Ты купила пока ничего. Осталось 5 000 ₽.", fillText(t, 5000, items, emptyList()))
        assertEquals("лампу, полку и шторы", fillText("{bought}", 5000, items, listOf("Лампа", "Полка", "Шторы")))
    }

    // --- «Сколько накопить?» ---
    @Test fun plan_needMoreSavings() {
        val q = buildPlan(plan, 5000, items, listOf("Лампа", "Полка", "Шторы"))!!   // 4 100 потрачено, 900 осталось
        assertTrue(q.text.startsWith("Коврик: 2 000 ₽"))
        assertEquals("1 100 ₽", q.options[q.correct])
        assertEquals(3, q.options.toSet().size)
        assertEquals("2 000 ₽ − 900 ₽ = 1 100 ₽, 11 нед.", q.explain)
    }

    @Test fun plan_enoughLeft_asksWhatRemains() {
        val q = buildPlan(plan, 5000, items, emptyList())!!                       // осталось 5 000, коврик 2 000
        assertEquals("3 000 ₽", q.options[q.correct])
        assertEquals("5 000 ₽ − 2 000 ₽ = 3 000 ₽", q.explain)
    }

    @Test fun plan_targetSkipsBoughtItems_andNullWhenAllBought() {
        val q = buildPlan(plan, 5000, items, listOf("Коврик"))!!                  // следующий по приоритету — шторы
        assertTrue(q.text.startsWith("Шторы"))
        assertNull(buildPlan(plan, 99999, items, items.map { it.name }))
    }

    @Test fun plan_optionsAreDistinctAndPositive_forManyStates() {
        val names = items.map { it.name }
        for (mask in 0 until (1 shl names.size)) {
            val bought = names.filterIndexed { i, _ -> mask and (1 shl i) != 0 }
            val q = buildPlan(plan, 7000, items, bought, seed = mask) ?: continue
            assertEquals(3, q.options.toSet().size)
            assertTrue(q.correct in 0..2)
            assertFalse(q.options.any { it.startsWith("-") || it.startsWith("0 ") })
        }
    }

    // --- «Нужно или хочется?» ---
    private val cards = listOf(
        SortItem("Автобус", "Нужно", "без него не доехать", "подумай о дороге", "", ""),
        SortItem("Конфеты", "Хочется", "можно обойтись", "можно ли без них?", "", ""),
    )

    @Test fun sort_correctFirstTry_thenFinished() {
        val g = SortGame(cards)
        assertTrue(g.answer("Нужно") is SortOutcome.Correct)
        assertTrue(g.canAdvance)
        g.advance()
        assertEquals("Конфеты", g.current?.name)
        assertEquals(SortOutcome.Correct("можно обойтись", true), g.answer("Хочется"))
        g.advance()
        assertTrue(g.finished)
        assertEquals(2, g.firstTryCount)
        assertEquals(listOf("Автобус"), g.inBin("Нужно").map { it.name })
    }

    @Test fun sort_wrongOnce_hint_thenCorrect_notFirstTry() {
        val g = SortGame(cards)
        assertEquals(SortOutcome.Wrong("подумай о дороге"), g.answer("Хочется"))
        assertFalse(g.canAdvance)                       // пока не ответил верно — дальше нельзя
        val o = g.answer("Нужно") as SortOutcome.Correct
        assertFalse(o.firstTry)
        assertEquals(0, g.firstTryCount)
    }

    @Test fun sort_wrongTwice_revealsAnswer_noDeadEnd() {
        val g = SortGame(cards)
        g.answer("Хочется")
        val o = g.answer("Хочется")
        assertEquals(SortOutcome.Revealed("Нужно", "без него не доехать"), o)
        assertTrue(g.canAdvance)
        g.advance()
        assertEquals("Конфеты", g.current?.name)
        assertEquals(listOf("Автобус"), g.inBin("Нужно").map { it.name })   // карточка легла в правильную стопку
    }

    @Test fun sort_answerAfterAnswerIsIgnored_andAdvanceNeedsAnswer() {
        val g = SortGame(cards)
        g.advance()
        assertEquals(0, g.index)                        // нельзя пролистнуть без ответа
        g.answer("Нужно")
        assertNull(g.answer("Хочется"))                 // повторный ответ на ту же карточку игнорируется
    }

    // --- викторина ---
    @Test fun quiz_secondChance_thenReveal() {
        val q = QuizGame(3, 1)
        assertEquals(QuizOutcome.Wrong(0), q.choose(0))
        assertTrue(q.isDisabled(0))
        assertNull(q.choose(0))                         // тот же вариант повторно нельзя
        assertEquals(QuizOutcome.Revealed, q.choose(2))
        assertTrue(q.done)
        val ok = QuizGame(3, 1)
        assertEquals(QuizOutcome.Correct, ok.choose(1))
    }

    @Test fun nearestOpaque_findsPixelOnObject_notInTheGap() {
        // «шторы»: непрозрачны только левая и правая полоски, центр пуст
        val p = nearestOpaque(100, 40, 50, 20) { x, _ -> x < 10 || x >= 90 }!!
        assertTrue(p.first < 10 || p.first >= 90)
    }
}
