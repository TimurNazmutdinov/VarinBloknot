# Сборка и релиз (Варин блокнот)

Репозиторий: `TimurNazmutdinov/VarinBloknot` (публичный). Релизы лежат в нём же (`releases/latest` читает само приложение), контент — в `content/` ветки `main`.

## Ключ подписи (уже создан)
`C:\Projects\keys\varin-release.jks`, четыре строки `VARIN_*` в `%USERPROFILE%\.gradle\gradle.properties`. **Сделать 2 копии `.jks` и пароля в разные места:** с другим ключом обновить установленное приложение поверх нельзя.

## Сборка (запускает пользователь, Android Studio закрыть)
```powershell
powershell -ExecutionPolicy Bypass -File scripts\build-apk.ps1            # версия из version.properties
powershell -ExecutionPolicy Bypass -File scripts\build-apk.ps1 -Bump patch # сначала версия +1
```
Результат: `release\VarinBloknot-<версия>.apk`, размер, SHA-256, подпись. `versionCode` растёт при каждом `-Bump`.

## Релиз
1. Проверить файл (размер, SHA-256, версия, подпись).
2. `.\publish.ps1 app -Notes "что изменилось"` — коммит, тег `v<версия>`, push, GitHub Release с APK.
3. `curl -sIL <apkUrl>` → 200 и тот же размер.

## Только контент (без APK)
Править `content/scenes/*.json`, `content/art/*.webp`, затем `.\publish.ps1 content -Message "..."`. Скрипт пересчитает `content/manifest.json` (contentVersion +1, если файлы изменились) и запушит.
Если контенту нужна новая версия программы — поднять `minAppVersion` в манифесте.

## Проверка автообновления
0. USB-отладки нет: v0.1.0 скопировать на телефон (кабель как флешка, Telegram или облако) и установить вручную, разрешив установку из этого источника. Дальше всё обновляется из самого приложения.
1. Открыть v0.1.0.
2. Выпустить v0.1.1 (`-Bump patch`), на телефоне: главный экран → «Проверить обновления» → «Обновить» → «Установить».
3. Изменить текст в `content/scenes/scene_01.json`, `publish.ps1 content` → на телефоне «Проверить обновления» → «Контент обновлён» → текст новый без переустановки.
