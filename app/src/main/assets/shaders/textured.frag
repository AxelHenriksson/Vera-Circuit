#version 310 es
// mtl.frag

precision mediump float;

uniform vec4 vColor;
uniform sampler2D tex0;
uniform sampler2D tex_ao;


in vec2 texCoord;
in vec3 normal;
in vec4 position;

out vec4 gl_FragColor;

void main() {
  gl_FragColor = texture(tex_ao, texCoord);
}

//texture(tex0, TexCoord);
//texture(cubemap, normalize(reflect(incident, normal)));