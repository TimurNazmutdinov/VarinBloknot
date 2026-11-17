"""Одноразово: приводит content/scenes/scene_01..03.json к формату 0.3.0 (paint, shop, задания)."""
import json
import os

os.chdir(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'))
PAL = ['#e85d4a', '#f2a23c', '#f6d04d', '#7cc56f', '#4fb3c8', '#6e8fe0', '#b07ad9', '#f28bb0']


def load(n):
    return json.load(open(f'content/scenes/scene_0{n}.json', encoding='utf8'))


def save(n, d):
    open(f'content/scenes/scene_0{n}.json', 'w', encoding='utf8', newline='\n').write(json.dumps(d, ensure_ascii=False, indent=1) + '\n')


def tint(layer, label):
    return {'id': layer, 'label': label, 'layer': layer, 'mode': 'tint'}


def mask(oid, layer, label):
    return {'id': oid, 'label': label, 'layer': layer, 'mode': 'mask', 'overlay': oid + '_paint'}


# --- сцена 1
d = load(1)
d['story'] = 'Родители отдают Варе маленькую комнату. Денег на ремонт — ровно 5 000 рублей. Что купить первым: лампу, полку, шторы или коврик?'
d['design'] = {
    'hint': 'Купи нужное, потом раскрась',
    'palette': PAL,
    'budget': 5000,
    'items': [
        {'name': 'Лампа', 'price': 1500, 'acc': 'лампу', 'layers': ['lamp', 'lampglow']},
        {'name': 'Полка', 'price': 1200, 'acc': 'полку', 'layers': ['shelf']},
        {'name': 'Коврик', 'price': 2000, 'acc': 'коврик', 'layers': ['rug']},
        {'name': 'Шторы', 'price': 1400, 'acc': 'шторы', 'layers': ['curtains']},
    ],
    'paint': [tint('quilt', 'Одеяло'), tint('pillows', 'Подушки'), tint('curtains', 'Шторы'), tint('rug', 'Коврик'), mask('lamp1', 'lamp', 'Лампа')],
    'shopHint': 'У тебя {budget}. На всё не хватит — выбери самое нужное',
    'paintHint': 'Выбери карандаш и нажми на предмет',
}
d['task'] = {
    'type': 'plan',
    'title': 'Сколько накопить?',
    'priority': ['Коврик', 'Шторы', 'Лампа', 'Полка'],
    'texts': {
        'qNeed': '{target} стоит {price}. После покупок у тебя осталось {left}. Сколько ещё нужно отложить?',
        'qHave': '{target} стоит {price}. У тебя осталось {left}. Сколько денег останется, если купить?',
        'eNeed': 'Верно! {price} − {left} = {need}. Если откладывать по 100 ₽ в неделю, {acc} получится купить через {weeks} нед.',
        'eHave': 'Верно! {left} − {price} = {rest}. Так работает бюджет: остаток можно отложить на другое.',
        'hintNeed': 'Подумай: сколько не хватает до цены? От цены нужно отнять то, что уже есть.',
        'hintHave': 'Подумай: из того, что есть, нужно вычесть цену покупки.',
    },
}
save(1, d)

# --- сцена 2
d = load(2)
d['design'] = {
    'hint': 'Нажми на предмет и выбери цвет',
    'palette': PAL,
    'budget': None,
    'items': [],
    'paint': [tint('curtains', 'Шторы'), mask('piggy', 'piggy', 'Копилка'), mask('cup', 'cup', 'Стакан'), mask('lamp2', 'lamp', 'Лампа')],
    'paintHint': 'Укрась Варин стол: выбери карандаш и нажми на предмет',
}
d['task'] = {
    'type': 'sort',
    'title': 'Нужно или хочется?',
    'bins': ['Нужно', 'Хочется'],
    'items': [
        {'name': 'Проезд в автобусе', 'bin': 'Нужно', 'why': 'Без автобуса не доехать до музыкальной школы.', 'hint': 'Подумай: как Варя добирается на занятия?', 'caption': 'туда и обратно', 'emoji': '🚌'},
        {'name': 'Тетрадь для музыки', 'bin': 'Нужно', 'why': 'Без неё не сделать задание по музыке.', 'hint': 'Подумай: что нужно для учёбы?', 'caption': 'для занятий', 'emoji': '📓'},
        {'name': 'Пирожок', 'bin': 'Нужно', 'why': 'Во вторник Варя забыла обед дома, и пирожок был её едой до вечера.', 'hint': 'Подумай: что делать, если забыл обед?', 'caption': 'вторник · забыла обед', 'emoji': '🥐'},
        {'name': 'Конфеты', 'bin': 'Хочется', 'why': 'Приятно, но без конфет можно обойтись.', 'hint': 'Подумай: можно ли прожить без этого?', 'caption': 'каждый день', 'emoji': '🍬'},
        {'name': 'Наклейки', 'bin': 'Хочется', 'why': 'Красиво, но с покупкой можно подождать.', 'hint': 'Подумай: без чего Варя точно обойдётся?', 'caption': '', 'emoji': '⭐'},
        {'name': 'Шоколадка', 'bin': 'Хочется', 'why': 'Вкусно, но это лакомство, а не еда.', 'hint': 'Подумай: нужна ли она для учёбы или еды?', 'caption': '', 'emoji': '🍫'},
    ],
}
save(2, d)

# --- сцена 3
d = load(3)
d['design'] = {
    'hint': 'Нажми на предмет и выбери цвет',
    'palette': PAL,
    'budget': None,
    'items': [],
    'paint': [tint('curtains', 'Шторы'), mask('table', 'table', 'Скатерть'), mask('stools', 'stools', 'Табуретки'), mask('kettle', 'kettle', 'Чайник'),
              mask('pot', 'kettle', 'Кастрюля'), mask('bowl', 'bowl', 'Миска'), mask('lamp3', 'lamp', 'Лампа')],
    'paintHint': 'Укрась кухню: выбери карандаш и нажми на предмет',
}
d['task']['hint'] = 'Подумай: что делают живые существа, когда едят?'
save(3, d)
print('ok')
