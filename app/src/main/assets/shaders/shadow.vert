#version 310 es
// shadow.vert

uniform mat4 mModel;
uniform mat4 mView;
uniform mat4 mProjection;

in vec4 vPosition;
in vec2 vTexCoord;

out vec2 texCoord;

void main() {
  gl_Position = mProjection * mView * mModel * vPosition;

  texCoord = vTexCoord;

}