#version 300 es
precision mediump float;

in vec3 vNormal;
in vec2 vTexCoord;

uniform sampler2D uAlbedoTex;

layout(location = 0) out vec4 gAlbedo;
layout(location = 1) out vec4 gNormal;
layout(location = 2) out vec4 gMaterial;

void main() {
    gAlbedo   = texture(uAlbedoTex, vTexCoord);
    gNormal   = vec4(vNormal * 0.5 + 0.5, 1.0);
    gMaterial = vec4(0.0, 0.5, 0.0, 1.0);
}
