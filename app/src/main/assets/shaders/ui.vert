#version 310 es
// ui.vert

uniform mat4 mTransform;

in vec4 vPosition;

out vec2 texCoord;



void main() {
    gl_Position = mTransform * vPosition;

    texCoord = (vPosition.xy+vec2(1.0f, 1.0f)) / 2.0f;
}