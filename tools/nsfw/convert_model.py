"""Download the GantMan NSFW MobileNet V2 model and convert it to a float16 TFLite asset.

Run once with Python 3.11 (TensorFlow has no wheels for newer versions):

    uv run --python 3.11 --with-requirements tools/nsfw/requirements.txt tools/nsfw/convert_model.py

The resulting file is committed to app/src/main/assets, so the Gradle build does not need Python.
"""

import argparse
import io
import tempfile
import zipfile
from pathlib import Path

import requests
import tensorflow as tf

MODEL_URL = "https://github.com/GantMan/nsfw_model/releases/download/1.1.0/nsfw_mobilenet_v2_140_224.zip"
SAVED_MODEL_DIR = "mobilenet_v2_140_224"
REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_OUTPUT = REPO_ROOT / "app/src/main/assets/nsfw_mobilenet_v2_224.tflite"


def download_saved_model(target: Path) -> Path:
    response = requests.get(MODEL_URL, timeout=120)
    response.raise_for_status()
    with zipfile.ZipFile(io.BytesIO(response.content)) as archive:
        archive.extractall(target)
    return target / SAVED_MODEL_DIR


def convert(saved_model: Path) -> bytes:
    converter = tf.lite.TFLiteConverter.from_saved_model(str(saved_model))
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]
    return converter.convert()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()

    with tempfile.TemporaryDirectory() as workdir:
        model = convert(download_saved_model(Path(workdir)))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_bytes(model)
    print(f"Wrote {args.output} ({len(model) / 1_000_000:.1f} MB)")


if __name__ == "__main__":
    main()
