#version 330 core
in vec2 TexCoord;
in vec3 FragPos;
in vec3 Normal;
in vec3 Tangent;
in vec3 Bitangent;
uniform sampler2D u_TextureUnit;
uniform sampler2D normalMap;
uniform sampler2D metallicMap;
uniform sampler2D roughnessMap;
uniform sampler2D aoMap;
uniform int baseColorMapEnable;
uniform int normalMapEnable;
uniform int metallicMapEnable;
uniform int roughnessMapEnable;
uniform int aoMapEnable;
uniform vec3 baseColor;
uniform float metallic;
uniform float roughness;
uniform float ambientOcclusion;
uniform vec3 viewPos;
uniform int dLightNum;
struct DirectionalLight { vec3 color; vec3 direction; float diffuse; float specular; };
struct AmbientLight { vec3 color; };
uniform DirectionalLight dLights[4];
uniform AmbientLight aLight;
out vec4 FragColor;
void main() {
    vec3 albedo = baseColor * (baseColorMapEnable == 1 ? texture(u_TextureUnit, TexCoord).rgb : vec3(1.0));
    vec3 normal = normalize(Normal);
    if (normalMapEnable == 1) {
        vec3 tangent = normalize(Tangent - normal * dot(normal, Tangent));
        float handedness = dot(cross(normal, tangent), Bitangent) < 0.0 ? -1.0 : 1.0;
        vec3 bitangent = normalize(cross(normal, tangent)) * handedness;
        normal = normalize(mat3(tangent, bitangent, normal) * (texture(normalMap, TexCoord).rgb * 2.0 - 1.0));
    }
    float metal = clamp(metallicMapEnable == 1 ? texture(metallicMap, TexCoord).r : metallic, 0.0, 1.0);
    float rough = clamp(roughnessMapEnable == 1 ? texture(roughnessMap, TexCoord).r : roughness, 0.04, 1.0);
    float ao = clamp(aoMapEnable == 1 ? texture(aoMap, TexCoord).r : ambientOcclusion, 0.0, 1.0);
    vec3 viewDirection = normalize(viewPos - FragPos);
    vec3 diffuseColor = albedo * (1.0 - metal);
    vec3 specularColor = mix(vec3(0.04), albedo, metal);
    float exponent = mix(128.0, 4.0, rough * rough);
    vec3 lighting = aLight.color * albedo * ao;
    for (int i = 0; i < dLightNum && i < 4; i++) {
        vec3 lightDirection = normalize(-dLights[i].direction);
        float diffuse = max(dot(normal, lightDirection), 0.0) * dLights[i].diffuse;
        vec3 halfDirection = normalize(lightDirection + viewDirection);
        float specular = pow(max(dot(normal, halfDirection), 0.0), exponent) * dLights[i].specular;
        lighting += dLights[i].color * (diffuseColor * diffuse + specularColor * specular);
    }
    FragColor = vec4(lighting, 1.0);
}
