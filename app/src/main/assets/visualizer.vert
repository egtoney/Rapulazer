#version 300 es

uniform mat4 projection_matrix;
uniform mat4 view_matrix;
uniform mat4 model_matrix;

in vec3 in_position;
in vec3 in_color;

out vec4 pass_color;

void main() {
    gl_Position = projection_matrix * view_matrix * model_matrix * vec4(in_position, 1);
    pass_color = vec4(in_color, 1);
}