package com.example.varinbloknot.logic

import com.example.varinbloknot.data.Choice
import com.example.varinbloknot.data.DesignItem
import com.example.varinbloknot.data.Line
import com.example.varinbloknot.data.SortItem
import com.example.varinbloknot.data.Task
import kotlin.math.abs

/** Чистая логика игры (без Android и Compose) — её проверяют обычные тесты. */

fun formatMoney(n: Int): String = n.toString().reversed().chunked(3).joinToString(" ").reversed().replace("- ", "-") + " ₽"

// ---------- магазин ----------

object Shop {
    /** Предмет надо купить, чтобы он появился (есть бюджет и цена). */
    fun needsBuying(budget: Int?, item: DesignItem, bought: Collection<String>) =
        budget != null && item.price != null && item.name !in bought

    fun spent(items: List<DesignItem>, bought: Collection<String>): Int =
        items.filter { it.name in bought }.sumOf { it.price ?: 0 }

    fun left(budget: Int?, items: List<DesignItem>, bought: Collection<String>): Int = (budget ?: 0) - spent(items, bought)

    fun canBuy(budget: Int?, items: List<DesignItem>, bought: Collection<String>, item: DesignItem): Boolean =
        budget == null || item.price == null || spent(items, bought) + item.price <= budget

    /** Слои, скрытые, пока предмет не куплен. */
    fun hiddenLayers(budget: Int?, items: List<DesignItem>, bought: Collection<String>): Set<String> =
        items.filter { needsBuying(budget, it, bought) }.flatMap { it.layers }.toSet()
}

// ---------- подстановки в тексты ----------

private fun joinRu(a: List<String>) = when (a.size) {
    0 -> ""
    1 -> a[0]
    else -> a.dropLast(1).joinToString(", ") + " и " + a.last()
}

/** Подставляет в реплику то, что игрок сделал: {bought}, {left}, {spent}. */
fun fillText(text: String, budget: Int?, items: List<DesignItem>, bought: Collection<String>): String {
    val boughtItems = items.filter { it.name in bought }
    return text
        .replace("{bought}", if (boughtItems.isEmpty()) "пока ничего" else joinRu(boughtItems.map { it.acc }))
        .replace("{notBought}", joinRu(items.filter { it.price != null && it.name !in bought }.map { it.acc }))
        .replace("{left}", formatMoney(Shop.left(budget, items, bought)))
        .replace("{spent}", formatMoney(Shop.spent(items, bought)))
}

// ---------- диалог: условия и порядок реплик ----------

/**
 * Условие показа реплики. Пусто — всегда. Части через «&» (все должны выполняться):
 * bought:Лампа / !bought:Лампа, none (ничего не куплено), some (что-то куплено),
 * unbought (есть некупленное), left>=1000, left>0, left<1000, left==0.
 */
fun evalCond(cond: String, budget: Int?, items: List<DesignItem>, bought: Collection<String>): Boolean {
    if (cond.isBlank()) return true
    val left = Shop.left(budget, items, bought)
    return cond.split("&").map { it.trim() }.all { t ->
        when {
            t == "none" -> bought.isEmpty()
            t == "some" -> bought.isNotEmpty()
            t == "unbought" -> items.any { it.price != null && it.name !in bought }
            t.startsWith("!bought:") -> t.removePrefix("!bought:") !in bought
            t.startsWith("bought:") -> t.removePrefix("bought:") in bought
            t.startsWith("left") -> {
                val m = Regex("""left(>=|<=|>|<|==)(-?\d+)""").matchEntire(t)
                if (m == null) false else {
                    val n = m.groupValues[2].toInt()
                    when (m.groupValues[1]) { ">=" -> left >= n; "<=" -> left <= n; ">" -> left > n; "<" -> left < n; else -> left == n }
                }
            }
            else -> false
        }
    }
}

/** Один шаг диалога на экране. lineIdx — номер реплики в сценарии (ключ для выбора игрока). */
class DlgStep(val who: String, val text: String, val camera: String, val choices: List<Choice>, val lineIdx: Int)

/**
 * Собирает реплики под то, что сделал игрок: пропускает реплики с невыполненным условием,
 * выбирает нужный вариант текста, после выбора игрока добавляет ответную реплику.
 * Шаг с выбором без ответа (picks) — последний: дальше идти нельзя, пока не выберет.
 */
fun buildSteps(lines: List<Line>, picks: Map<Int, String>, budget: Int?, items: List<DesignItem>, bought: Collection<String>): List<DlgStep> {
    val out = ArrayList<DlgStep>()
    lines.forEachIndexed { i, l ->
        if (!evalCond(l.cond, budget, items, bought)) return@forEachIndexed
        val text = l.variants.firstOrNull { evalCond(it.cond, budget, items, bought) }?.text ?: l.text
        val ch = l.choices.filter { evalCond(it.cond, budget, items, bought) }
        if (ch.isEmpty() && text.isBlank()) return@forEachIndexed
        if (ch.isEmpty()) { out.add(DlgStep(l.who, text, l.camera, emptyList(), i)); return@forEachIndexed }
        if (text.isNotBlank()) out.add(DlgStep(l.who, text, l.camera, emptyList(), i))
        val picked = ch.firstOrNull { it.text == picks[i] }
        if (picked == null) { out.add(DlgStep(l.who, "", l.camera, ch, i)); return out }
        if (picked.replyText.isNotBlank()) out.add(DlgStep(picked.replyWho.ifBlank { l.who }, picked.replyText, l.camera, emptyList(), i))
    }
    return out
}

// ---------- задание «Сколько накопить?» ----------

class PlanQuestion(val text: String, val options: List<String>, val correct: Int, val explain: String, val hint: String)

/** Собирает вопрос из чисел игрока. Если купил всё нужное — null (задание пропускается). */
fun buildPlan(task: Task, budget: Int, items: List<DesignItem>, bought: Collection<String>, seed: Int = 1): PlanQuestion? {
    val left = Shop.left(budget, items, bought)
    val target = task.priority.firstNotNullOfOrNull { n -> items.firstOrNull { it.name == n && it.name !in bought } }
        ?: items.firstOrNull { it.name !in bought } ?: return null
    val price = target.price ?: return null
    val need = price - left
    val have = left >= price
    val correct = if (have) left - price else need
    val wrongs = LinkedHashSet<Int>()
    if (have) listOf(left, left + price, price) else listOf(price, left, price + left, need * 2)
        .forEach { v -> if (v > 0 && v != correct) wrongs.add(v) }
    var extra = correct + 100
    while (wrongs.size < 2) { if (extra != correct) wrongs.add(extra); extra += 100 }
    val opts = (listOf(correct) + wrongs.take(2)).shuffled(java.util.Random(seed.toLong()))
    val weeks = if (need > 0) (need + 99) / 100 else 0
    fun fill(k: String) = (task.texts[k] ?: "")
        .replace("{target}", target.name).replace("{acc}", target.acc)
        .replace("{price}", formatMoney(price)).replace("{left}", formatMoney(left))
        .replace("{need}", formatMoney(maxOf(need, 0))).replace("{rest}", formatMoney(maxOf(left - price, 0)))
        .replace("{weeks}", weeks.toString())
    return PlanQuestion(
        text = fill(if (have) "qHave" else "qNeed"),
        options = opts.map { formatMoney(it) },
        correct = opts.indexOf(correct),
        explain = fill(if (have) "eHave" else "eNeed"),
        hint = fill(if (have) "hintHave" else "hintNeed"),
    )
}

// ---------- «Нужно или хочется?» ----------

sealed class SortOutcome {
    data class Correct(val why: String, val firstTry: Boolean) : SortOutcome()
    data class Wrong(val hint: String) : SortOutcome()
    data class Revealed(val correctBin: String, val why: String) : SortOutcome()
}

/** Колода карточек: две попытки на карточку, застрять нельзя. */
class SortGame(val items: List<SortItem>) {
    var index = 0
        private set
    private var wrong = 0
    private var answered = false
    private val placed = LinkedHashMap<Int, String>()
    private val firstTry = HashSet<Int>()

    val current: SortItem? get() = items.getOrNull(index)
    val finished: Boolean get() = index >= items.size
    val canAdvance: Boolean get() = answered
    val firstTryCount: Int get() = firstTry.size

    fun answer(bin: String): SortOutcome? {
        val it = current ?: return null
        if (answered) return null
        if (bin == it.bin) {
            answered = true
            placed[index] = it.bin
            val ft = wrong == 0
            if (ft) firstTry.add(index)
            return SortOutcome.Correct(it.why, ft)
        }
        wrong++
        if (wrong >= 2) {
            answered = true
            placed[index] = it.bin
            return SortOutcome.Revealed(it.bin, it.why)
        }
        return SortOutcome.Wrong(it.hint)
    }

    fun advance() {
        if (!answered) return
        index++
        wrong = 0
        answered = false
    }

    fun inBin(bin: String): List<SortItem> = placed.filter { it.value == bin }.keys.map { items[it] }
}

// ---------- викторина с двумя попытками ----------

sealed class QuizOutcome {
    object Correct : QuizOutcome()
    data class Wrong(val option: Int) : QuizOutcome()
    object Revealed : QuizOutcome()
}

class QuizGame(val optionCount: Int, val correct: Int) {
    private val disabled = HashSet<Int>()
    var done = false
        private set
    var wrongCount = 0
        private set

    fun isDisabled(i: Int) = i in disabled

    fun choose(i: Int): QuizOutcome? {
        if (done || i in disabled || i !in 0 until optionCount) return null
        if (i == correct) { done = true; return QuizOutcome.Correct }
        disabled.add(i)
        wrongCount++
        if (wrongCount >= 2) { done = true; return QuizOutcome.Revealed }
        return QuizOutcome.Wrong(i)
    }
}

/** Ближайшее непрозрачное значение к точке (для значков «можно покрасить»). */
fun nearestOpaque(w: Int, h: Int, cx: Int, cy: Int, opaque: (Int, Int) -> Boolean): Pair<Int, Int>? {
    var best: Pair<Int, Int>? = null
    var bd = Int.MAX_VALUE
    val step = maxOf(1, minOf(w, h) / 80)
    var y = 0
    while (y < h) {
        var x = 0
        while (x < w) {
            if (opaque(x, y)) {
                val d = abs(x - cx) * abs(x - cx) + abs(y - cy) * abs(y - cy)
                if (d < bd) { bd = d; best = x to y }
            }
            x += step
        }
        y += step
    }
    return best
}
