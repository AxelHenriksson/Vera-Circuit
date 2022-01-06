#version 310 es
// shadow.frag

precision mediump float;

uniform vec4 vColor;

in vec2 texCoord;

out vec4 gl_FragColor;

void main() {
  float luminance = -texCoord.x*texCoord.x+texCoord.x-texCoord.y*texCoord.y+texCoord.y;
  gl_FragColor = vColor * vec4(3.0f*luminance-1.0f, 3.0f*luminance-1.0f, 3.0f*luminance-1.0f, 1.0f);
}