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
        merge_link(source, caption) if "http" in source else caption
        for source, caption in zip(original, translated)
    ]


def merge_link(source, caption):
    url = source.split(URL_SEPARATOR, 1)[0]
    return f"{url}{URL_SEPARATOR}{caption}" if caption else url


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
