#version 300 es
precision mediump float;

in vec2 vTexCoord;

uniform sampler2D uAlbedo;
uniform sampler2D uNormal;
uniform sampler2D uMaterial;

uniform vec3  uLightDir;
uniform vec3  uLightColor;
uniform float uAmbient;

out vec4 fragColor;

void main() {
    vec3 albedo   = texture(uAlbedo,   vTexCoord).rgb;
    vec3 normal   = normalize(texture(uNormal, vTexCoord).rgb * 2.0 - 1.0);
    vec3 lightDir = normalize(uLightDir);
    float diff    = max(dot(normal, lightDir), 0.0);
    vec3 color    = (uAmbient + diff) * uLightColor * albedo;
    fragColor     = vec4(color, 1.0);
}
