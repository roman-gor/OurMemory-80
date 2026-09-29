# Перевод контента Firebase на белорусский, английский и китайский

## Статус на 2026-09-29

Сделано и закоммичено:
- переведены все 15 ветеранов, захоронения `b_004` и `b_005` (у остальных захоронений нет описаний) и экскурсия `t_heroes` со всеми остановками;
- исходники лежат в `firebase/content/translations/`;
- сборщик — `tools/translations/build_translations.py`. Для ссылки с пустой подписью он оставляет URL без `|`;
- `firebase/content/translations.json` собран: 22 пути, все оканчиваются на `/translations`;
- в `CLAUDE.md` описаны узел `translations`, сборщик и команда импорта.

Осталось:
1. **Импорт в базу.** Пользователь выполняет `! npx -y firebase-tools login`, затем я пересобираю файл и выполняю `database:update` (шаг 4).
2. **Ошибки в исходных данных.** Их нужно исправить в админке, потом перевести заново и пересобрать файл:
   - у `veteran7` (Ахманов) в `veteransInfo[0]` лежит биография Чибисова (`veteran4`), и перевод повторяет её же;
   - у `veteran8` (Михайлов) `baseInfo` и `allInfo` называют его генерал-майором авиации, а в биографии он гвардии лейтенант-артиллерист;
   - у `veteran8` нет подписей к пяти ссылкам.
3. **iOS.** Удалить `claude/translations-wip/` и добавить в `CLAUDE.md` iOS тот же абзац про `translations`.
4. **Проверка** из раздела «Проверка». Сборка Gradle не запускалась: Kotlin не менялся, а на диске свободно около 1 ГБ.

## Контекст

Код под переводы уже готов и лежит в `main` (Android `d7b3d0e`, `ed7a8b0`, `5f5fa9d`, `2d6e552`, iOS тоже). У `Veterans`, `Burials`, `Tours` и остановок экскурсий есть необязательный узел `translations/{be|en|zh}`. Приложения показывают перевод, а если его нет, показывают русский оригинал. Веб-админка (`ourmemory-admin`) тоже знает про `translations` и не стирает его при сохранении.

При этом в самой базе переводов нет: проверка через `curl` показала, что ни у одного из 15 ветеранов нет `translations`. Поэтому на en/zh/be интерфейс переведён, а биографии, награды и экскурсии остаются русскими. Осталось перевести контент и импортировать его.

Старый план — `claude/localization-plan.ru.md` (статус на 2026-09-25). Что изменилось с тех пор:
- всё запушено, так что шаг «Push» из старого плана не нужен;
- заготовки переводов лежат не в Android, а в iOS-репо: `~/Personal/OurMemory-ios/claude/translations-wip/` (`_burials_tours.json`, `veteran1.json`, `show_veteran.py`), там они не под git;
- `CLAUDE.md` в Android до сих пор пишет, что строки есть только в `values/` и `values-be/`, и ничего не говорит про узел `translations`.

**Важно до начала:** на диске свободно 1,2 ГБ, и часть команд уже падала с `ENOSPC`. Перед работой диск нужно освободить (например, `~/.gradle/caches` или DerivedData Xcode). Без этого не соберутся ни `detektAll`, ни `assembleDebug`.

## Объём

Сколько символов нужно перевести (без ссылок) по живой базе:

| Ветеран | Символов | Статус |
|---|---|---|
| veteran1 | 7 311 | готов (`veteran1.json`) |
| veteran2 (Купала) | 12 759 | нет |
| veteran3 (Колас) | 7 089 | нет |
| veteran4–15 | ≈36 000 | нет |

Захоронения `b_004`, `b_005` и экскурсия `t_heroes` готовы. Нужно ещё проверить, появились ли описания у `b_001–b_003`, `b_006`, `b_007`.

Всего осталось ≈56 тыс. символов на каждый из трёх языков. Переводить буду сам, по 1–2 ветерана за шаг, с сохранением на диск после каждого, чтобы работа не пропала при сжатии контекста.

## Шаг 1. Сохранить план и перенести заготовки в Android-репо

### Почему

В iOS-репо заготовки не под git, и искать их там неудобно. Импорт делается из Android-репо, поэтому исходники переводов должны лежать рядом с ним и попасть в коммит. Тогда при правке текста ветерана перевод можно будет пересобрать.

### Код

Структура после переноса:

```text
firebase/content/
├── translations/
│   ├── _burials_tours.json
│   ├── veteran1.json
│   └── … veteran15.json
└── translations.json
tools/translations/
├── build_translations.py
└── show_veteran.py
```

Первое действие после одобрения — обновить `claude/localization-plan.ru.md` этим планом.

## Шаг 2. Перевести veteran2–15 и недостающие захоронения

### Почему

Без этих файлов у 14 из 15 ветеранов на en/zh/be так и останется русский текст.

### Формат (как у `veteran1.json`)

```json
{
  "be": {"name": "Філіпскіх Яўген Фёдаравіч", "baseInfo": "…", "allInfo": "…", "veteransInfo": ["Абзац…", "Узнагародны ліст"]},
  "en": {"name": "Filipskikh Yevgeny Fyodorovich", "baseInfo": "…", "allInfo": "…", "veteransInfo": ["Paragraph…", "Award sheet"]},
  "zh": {"name": "菲利普斯基赫·叶夫根尼·费奥多罗维奇", "baseInfo": "…", "allInfo": "…", "veteransInfo": ["段落…", "奖励表"]}
}
```

Правила:
- `veteransInfo` той же длины, что оригинал;
- на месте ссылки пишется только переведённая подпись, а URL подставит сборщик;
- фамилия идёт первой, как в оригинале;
- английский — по BGN/PCGN, китайский — традиционной транскрипцией, белорусский — по-белорусски (Янка Купала — «Луцэвіч Іван Дамінікавіч»).

Исходник смотрю так: `curl -s …/OurMemory/Veterans.json > $SCRATCH/Veterans.json && python3 tools/translations/show_veteran.py $SCRATCH/Veterans.json veteranN`.

## Шаг 3. Сборщик `tools/translations/build_translations.py`

### Почему

`database:update` принимает один JSON с мульти-путями. Сборщик нужен, чтобы:
- склеить файлы;
- подставить URL-ы из живого оригинала;
- не пропустить в импорт ничего, кроме узлов `translations`. Ошибочный ключ перезаписал бы запись целиком.

### Код

```python
import json
import sys
import urllib.request
from pathlib import Path

DATABASE_URL = "https://chatroom-85fb8-default-rtdb.firebaseio.com/OurMemory/Veterans.json"
SOURCE_DIR = Path("firebase/content/translations")
OUTPUT_FILE = Path("firebase/content/translations.json")
LANGUAGES = ("be", "en", "zh")
URL_SEPARATOR = "|"
TRANSLATIONS_SUFFIX = "/translations"


def as_list(info):
    return [info[key] for key in sorted(info, key=int)] if isinstance(info, dict) else list(info or [])


def merge_links(original, translated, veteran_id, language):
    if len(original) != len(translated):
        sys.exit(f"{veteran_id}/{language}: veteransInfo {len(translated)} != {len(original)}")
    return [
        f"{source.split(URL_SEPARATOR, 1)[0]}{URL_SEPARATOR}{caption}" if "http" in source else caption
        for source, caption in zip(original, translated)
    ]


def main():
    veterans = json.load(urllib.request.urlopen(DATABASE_URL))
    update = json.loads((SOURCE_DIR / "_burials_tours.json").read_text())
    for path in sorted(SOURCE_DIR.glob("veteran*.json")):
        veteran_id = path.stem
        original = as_list(veterans[veteran_id].get("veteransInfo"))
        translations = json.loads(path.read_text())
        for language in LANGUAGES:
            entry = translations[language]
            entry["veteransInfo"] = merge_links(original, entry["veteransInfo"], veteran_id, language)
        update[f"Veterans/{veteran_id}{TRANSLATIONS_SUFFIX}"] = translations
    wrong_keys = [key for key in update if not key.endswith(TRANSLATIONS_SUFFIX)]
    if wrong_keys:
        sys.exit(f"Not a translations path: {wrong_keys}")
    OUTPUT_FILE.write_text(json.dumps(update, ensure_ascii=False, indent=2))
    print(f"{len(update)} paths -> {OUTPUT_FILE}")


main()
```

## Шаг 4. Импорт

### Почему

Нужен именно `database:update` по пути `/OurMemory`: он меняет только перечисленные узлы `translations`. `database:set` перезаписал бы весь `OurMemory`.

```bash
python3 tools/translations/build_translations.py
npx -y firebase-tools database:update /OurMemory firebase/content/translations.json \
  --project chatroom-85fb8 --instance chatroom-85fb8-default-rtdb
```

Для этого пользователь должен сам выполнить `! npx -y firebase-tools login`. Это запись в общую продовую базу, поэтому перед импортом я покажу итоговые ключи и попрошу подтверждения.

## Шаг 5. Документация

### Почему

Без этого следующий разработчик (или я) не узнает про узел `translations` и команду импорта, а `CLAUDE.md` будет врать про два языка.

### Код (`CLAUDE.md`, раздел Localization и таблица Firebase)

```markdown
**Localization.** The interface is in `values/` (Russian, default), `values-be/`, `values-en/` and `values-zh/`; all four must be updated together. Content is translated through an optional `translations/{be|en|zh}` node on `Veterans`, `Burials`, `Tours` and tour stops; a missing field falls back to Russian. Translation sources live in `firebase/content/translations/`, `tools/translations/build_translations.py` builds `firebase/content/translations.json`, imported with `npx -y firebase-tools database:update /OurMemory firebase/content/translations.json --project chatroom-85fb8 --instance chatroom-85fb8-default-rtdb`.
```

Такую же строку добавлю в `CLAUDE.md` iOS-репо. Файлы `claude/translations-wip/` из iOS удалю после переноса.

## Шаг 6. Коммит

Коммит в `main` (Android): переводы, сборщик, `translations.json`, документация. Сообщение без упоминаний ИИ:

```text
Translate veteran, burial and tour content into Belarusian, English and Chinese

- Add translation sources and a builder for the multi-path database update
- Document the translations node and the import command
```

## Проверка

- `python3 tools/translations/build_translations.py`: выводит «N paths», ошибок длины `veteransInfo` нет, все ключи оканчиваются на `/translations`.
- После импорта: `curl -s '…/OurMemory/Veterans/veteran2/translations/en/name.json'` возвращает английское имя, а `…/veteran2/name.json` по-прежнему русское.
- `./gradlew detektAll testDebugUnitTest assembleDebug` зелёные (Kotlin не меняется, это контроль).
- В приложении проверяю руками по очереди на English, 中文 и Беларуская в «Ещё»:
  - список ветеранов и поиск;
  - карточку Купалы (абзацы, подписи к фото открывают правильные ссылки);
  - экскурсию `t_heroes`;
  - описание захоронения на карте.
- Проверяю, что русский язык не изменился.
- В админке (Android и веб) открываю ветерана, сохраняю без правок и проверяю через `curl`, что `translations` на месте.
