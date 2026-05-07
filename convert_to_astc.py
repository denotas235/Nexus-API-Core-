#!/usr/bin/env python3
import os, subprocess, json, time
from pathlib import Path

QUALITY = os.environ.get("QUALITY", "medium")  # medium, thorough, fast
MANIFEST = {}

def convert_folder(folder, block_size):
    src_dir = Path("textures_source") / folder
    if not src_dir.exists():
        return
    out_dir = Path("src/main/resources/assets/maliopt/textures_astc") / folder
    out_dir.mkdir(parents=True, exist_ok=True)
    for png in src_dir.rglob("*.png"):
        rel = png.relative_to(Path("textures_source"))
        astc = out_dir / rel.with_suffix(".astc")
        astc.parent.mkdir(parents=True, exist_ok=True)
        print(f"  {rel} -> {astc}")
        start = time.time()
        subprocess.run([
            "astcenc", "-cs",
            str(png), str(astc),
            block_size, f"-{QUALITY}"
        ], check=True)
        elapsed = time.time() - start
        orig_size = png.stat().st_size
        astc_size = astc.stat().st_size if astc.exists() else 0
        MANIFEST[str(rel.with_suffix(""))] = {
            "astc": str(astc.relative_to(Path("src/main/resources"))),
            "block": block_size,
            "quality": QUALITY,
            "original_kb": orig_size // 1024,
            "size_kb": astc_size // 1024,
            "conversion_s": round(elapsed, 2)
        }

def main():
    for folder, block in [("block", "6x6"), ("item", "4x4"), ("gui", "4x4"),
                          ("entity", "5x5"), ("environment", "10x10"), ("particle", "8x8")]:
        convert_folder(folder, block)
    manifest_path = Path("src/main/resources/assets/maliopt/astc_manifest.json")
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    with open(manifest_path, "w") as f:
        json.dump(MANIFEST, f, indent=2)
    print(f"Manifesto salvo: {manifest_path} ({len(MANIFEST)} texturas)")

if __name__ == "__main__":
    main()
