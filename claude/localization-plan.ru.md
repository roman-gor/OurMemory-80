# Английский и китайский (Android + iOS), включая контент

Полный план: `~/.claude/plans/humming-finding-sparrow.md`. Кратко: интерфейс на en/zh, необязательный узел `translations/{be|en|zh}` у `Veterans`, `Burials`, `Tours` и остановок экскурсий, переключатель языка контента в редакторах админки, перевод существующего контента и импорт в базу.

## Статус на 2026-09-25

Готово и закоммичено, **не запушено**:
- Android (`~/Personal/OurMemory-80`): `d7b3d0e` интерфейс en/zh, `ed7a8b0` модель переводов, `5f5fa9d` редактор ветерана, `2d6e552` редакторы захоронения и экскурсии. `detektAll testDebugUnitTest assembleDebug` зелёные.
- iOS: `9d54dde` интерфейс en/zh, `fac6f4f` модель переводов, `3e75795` редакторы, `b4266ae` английские тексты разрешений в `Info.plist` как запасной вариант. Сборка `build-for-testing` зелёная, тесты не прогонялись: по просьбе пользователя, прогон занимает больше 6 минут.

## Осталось

1. **Перевод контента.** Исходники читаются только на чтение: `curl https://chatroom-85fb8-default-rtdb.firebaseio.com/OurMemory/{Veterans,Burials,Tours}.json`.
   - Готово в `claude/translations-wip/`: `_burials_tours.json` (описания b_004 и b_005, экскурсия t_heroes со всеми остановками) и `veteran1.json`.
   - Осталось 14 ветеранов (≈56 тыс. символов × be/en/zh): veteran2 (Купала, ≈12,7 тыс.), 3 (Колас, ≈7 тыс.), 4–15.
   - Формат файла ветерана: `{"be"|"en"|"zh": {name, baseInfo, allInfo, veteransInfo: [...]}}`. `veteransInfo` той же длины, что и оригинал. На месте ссылки пишется только переведённая подпись, URL подставляет сборщик.
   - Посмотреть исходник: `python3 claude/translations-wip/show_veteran.py Veterans.json veteranN`.
   - Имена: фамилия первой, как в оригинале. Английский по BGN/PCGN, китайский традиционной транскрипцией, белорусский по-белорусски (Янка Купала — «Луцэвіч Іван Дамінікавіч»).
2. **Сборщик.** Скрипт склеивает файлы в Android-репо `firebase/content/translations.json`, мульти-путевое обновление вида `{"Veterans/veteranN/translations": {...}, "Burials/b_004/translations": {...}, "Tours/t_heroes/stops/0/translations": {...}}`. Проверить, что все ключи оканчиваются на `/translations`.
3. **Импорт** после коммитов: `npx -y firebase-tools database:update /OurMemory firebase/content/translations.json --project chatroom-85fb8 --instance chatroom-85fb8-default-rtdb`. Нужен `! npx -y firebase-tools login` от пользователя.
4. **Документация.** Этот файл (и копия в Android `claude/`); в `CLAUDE.md` обоих репозиториев — список языков, узел `translations`, файл переводов и команда импорта; в Android — строки в пяти `values*`, на iOS — синхронизация скриптом.
5. **Push** `origin master` в обоих репозиториях.
6. **Проверка** в симуляторе: English/中文 в «Ещё», карточка ветерана, экскурсия, редакторы админки. Старые сборки админки при сохранении стирают `translations`, поэтому админам нужно обновиться.
