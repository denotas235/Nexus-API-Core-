#!/bin/bash
set -e
if command -v astcenc &> /dev/null; then
    echo "astcenc já instalado"
    exit 0
fi
curl -L -o astcenc.zip https://github.com/ARM-software/astc-encoder/releases/download/5.4.0/astcenc-5.4.0-linux-x64.zip
unzip -o astcenc.zip
chmod +x bin/astcenc-sse4.1
sudo cp bin/astcenc-sse4.1 /usr/local/bin/astcenc
rm astcenc.zip bin/ -r
echo "astcenc instalado"
