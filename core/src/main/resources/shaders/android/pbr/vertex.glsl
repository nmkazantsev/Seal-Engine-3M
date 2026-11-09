#version 300 es
precision highp float;
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 normalVec;
layout (location = 3) in vec3 aT;
layout (location = 4) in vec3 aB;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
out vec2 TexCoord;
out vec3 FragPos;
out vec3 Normal;
out vec3 Tangent;
out vec3 Bitangent;
void main() {
    vec4 worldPosition = model * vec4(aPos, 1.0);
    mat3 normalMatrix = transpose(inverse(mat3(model)));
    FragPos = worldPosition.xyz;
    Normal = normalize(normalMatrix * normalVec);
    Tangent = normalize(mat3(model) * aT);
    Bitangent = normalize(mat3(model) * aB);
    TexCoord = aTexCoord;
    gl_Position = projection * view * worldPosition;
}
