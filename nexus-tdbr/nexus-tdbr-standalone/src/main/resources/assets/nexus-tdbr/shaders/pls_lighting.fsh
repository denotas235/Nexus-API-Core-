#version 300 es
#extension GL_EXT_shader_pixel_local_storage : require
precision mediump float;

in vec2 vTexCoord;

uniform vec3  uLightDir;
uniform vec3  uLightColor;
uniform float uAmbient;

__pixel_local_inEXT FragDataPLS {
    layout(rgba8)          highp vec4 albedo;
    layout(rgba8_snorm)    highp vec4 normal;
    layout(r11f_g11f_b10f) highp vec4 material;
} pls;

out vec4 fragColor;

void main() {
    vec3 albedo   = pls.albedo.rgb;
    vec3 normal   = normalize(pls.normal.rgb * 2.0 - 1.0);
    vec3 lightDir = normalize(uLightDir);
    float diff    = max(dot(normal, lightDir), 0.0);
    vec3 color    = (uAmbient + diff) * uLightColor * albedo;
    fragColor     = vec4(color, 1.0);
}
