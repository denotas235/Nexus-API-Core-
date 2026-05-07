#!/bin/bash
set -e
MINECRAFT_JAR="$HOME/storage/downloads/Minecraft/1.jar"
ASTC_PACK="MaliOpt_ASTC_Pack.zip"
TEMP_DIR="$HOME/tmp/astc_gen"
BLOCK_SIZE="4x4"
QUALITY="-fast"

rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR/texturas"
mkdir -p "$TEMP_DIR/output"

echo "Extraindo texturas PNG do jar..."
cd "$TEMP_DIR"
unzip -q "$MINECRAFT_JAR" "assets/minecraft/textures/**/*.png" -d texturas 2>/dev/null

echo "Convertendo para ASTC..."
find texturas -name "*.png" | while read f; do
    rel_path="${f#texturas/assets/minecraft/textures/}"
    astc_file="output/assets/minecraft/textures/$rel_path.astc"
    mkdir -p "$(dirname "$astc_file")"
    astcenc -cl "$f" "$astc_file" "$BLOCK_SIZE" "$QUALITY"
    echo "  OK: $rel_path"
done

echo "Criando resource pack..."
cd output
echo '{"pack": {"pack_format": 34, "description": "MaliOpt ASTC Textures"}}' > pack.mcmeta
zip -r "$HOME/$ASTC_PACK" .
echo "Pack criado: $HOME/$ASTC_PACK"
rm -rf "$TEMP_DIR"
