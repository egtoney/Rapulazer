#version 300 es

uniform mat4 projection_matrix;
uniform mat4 view_matrix;
uniform mat4 model_matrix;

// Per object
layout (location = 0) in vec3 in_position;

// Per instance
layout (location = 1) in vec3 in_color;
layout (location = 2) in vec3 in_offset; // dx, dy, scale

out vec4 pass_color;

void main() {
    vec4 new_position = vec4(in_position, 1);
    new_position.xyz *= in_offset.z;
    new_position.xy += in_offset.xy;
    gl_Position = projection_matrix * view_matrix * model_matrix * new_position;

    vec3 computed_color = in_color;
//    if( in_position.z < 0 ) {
//        computed_color = computed_color * 0.5;
//    }
    pass_color = vec4(computed_color, 1);
}