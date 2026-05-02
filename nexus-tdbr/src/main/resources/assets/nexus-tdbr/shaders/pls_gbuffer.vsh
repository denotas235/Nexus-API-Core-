#version 300 es
in vec3 aPos;
in vec3 aNormal;
in vec2 aTexCoord;

out vec3 vNormal;
out vec2 vTexCoord;

uniform mat4 uMVP;
uniform mat3 uNormalMatrix;

void main() {
    vNormal     = normalize(uNormalMatrix * aNormal);
    vTexCoord   = aTexCoord;
    gl_Position = uMVP * vec4(aPos, 1.0);
}
