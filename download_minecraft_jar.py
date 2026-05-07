#!/usr/bin/env python3
import os, sys, subprocess, zipfile, tempfile, shutil
from pathlib import Path

MC_VERSION = "1.21.1"
JAR_PATH = Path(f"minecraft-{MC_VERSION}-client.jar")

def main():
    if not JAR_PATH.exists():
        print(f"Baixando minecraft-{MC_VERSION}-client.jar...")
        # Usa o gradle para garantir que o JAR está na cache
        subprocess.run(["./gradlew", "build", "--no-daemon"], check=False)
        # Procura na cache do Loom
        cache_dir = Path.home() / ".gradle/caches/fabric-loom"
        candidates = list(cache_dir.rglob(f"minecraft-{MC_VERSION}-client.jar"))
        if not candidates:
            # Tenta achar na cache do Gradle normal
            candidates = list(Path.home().rglob(f".gradle/caches/**/minecraft-{MC_VERSION}-client.jar"))
        if not candidates:
            sys.exit("JAR não encontrado. Rode ./gradlew build primeiro.")
        shutil.copy(candidates[0], JAR_PATH)
        print(f"JAR copiado: {candidates[0]}")
    else:
        print(f"JAR já existe: {JAR_PATH}")

    # Extrai texturas
    with zipfile.ZipFile(JAR_PATH, 'r') as zf:
        png_files = [name for name in zf.namelist() if name.startswith("assets/minecraft/textures/") and name.endswith(".png")]
        for png in png_files:
            dest = Path("textures_source") / png.replace("assets/minecraft/textures/", "")
            dest.parent.mkdir(parents=True, exist_ok=True)
            with zf.open(png) as src, open(dest, 'wb') as dst:
                dst.write(src.read())
    print(f"Texturas extraídas: {len(png_files)} PNGs")

if __name__ == "__main__":
    main()
