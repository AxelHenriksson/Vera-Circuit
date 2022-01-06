#version 310 es
// ui.frag

precision mediump float;

uniform sampler2D tex0;

in vec2 texCoord;

out vec4 gl_FragColor;

void main() {
    gl_FragColor = texture(tex0, texCoord);
}
