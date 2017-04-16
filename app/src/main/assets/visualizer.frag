#version 300 es

precision lowp float;

in vec4 pass_color;

out vec4 out_color;

void main() {
    out_color = pass_color;
}