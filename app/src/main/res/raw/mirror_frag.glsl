#version 310 es
// mirrored_frag.glsl

precision mediump float;

uniform vec4 vColor;
uniform vec3 vCamPos;
uniform sampler2D tex0;
uniform sampler2D texAmbientOcclusion;
uniform samplerCube cubeMap;


in vec2 texCoord;
in vec3 normal;
in vec4 position;

out vec4 gl_FragColor;

void main() {
  vec3 incident = position.xyz - vCamPos;
  gl_FragColor = texture(cubeMap, reflect(incident, normal));
}

//texture(tex0, TexCoord);
//texture(cubemap, normalize(reflect(incident, normal)));