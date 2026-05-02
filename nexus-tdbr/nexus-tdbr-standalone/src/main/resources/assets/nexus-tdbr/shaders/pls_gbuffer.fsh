#version 300 es
#extension GL_EXT_shader_pixel_local_storage : require
precision mediump float;

in vec3 vNormal;
in vec2 vTexCoord;

uniform sampler2D uAlbedoTex;

__pixel_local_outEXT FragDataPLS {
    layout(rgba8)          highp vec4 albedo;
    layout(rgba8_snorm)    highp vec4 normal;
    layout(r11f_g11f_b10f) highp vec4 material;
} pls;

void main() {
    pls.albedo   = texture(uAlbedoTex, vTexCoord);
    pls.normal   = vec4(vNormal * 0.5 + 0.5, 1.0);
    pls.material = vec4(0.0, 0.5, 0.0, 1.0);
}
