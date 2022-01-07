#version 310 es
// ui.frag

precision mediump float;

uniform sampler2D tex0;
uniform sampler2D texPressed;
uniform int isPressed;

in vec2 texCoord;


out vec4 gl_FragColor;

void main() {
    if (isPressed > 0) {
        gl_FragColor = texture(texPressed, texCoord);
    } else {
        gl_FragColor = texture(tex0, texCoord);
    }
}
