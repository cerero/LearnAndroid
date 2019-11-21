#ifndef MATRIX_H
#define MATRIX_H

void vec3_multiply_mat3( vec3 *dst, vec3 *v, mat3 *m );

void vec3_multiply_mat4( vec3 *dst, vec3 *v, mat4 *m );

void vec4_multiply_mat4( vec4 *dst, vec4 *v, mat4 *m );

void mat3_identity( mat3 *m );

void mat3_copy_mat4( mat3 *dst, mat4 *m );

void mat4_identity( mat4 *m );

void mat4_copy_mat4( mat4 *dst, mat4 *m );

void mat4_translate( mat4 *dst, mat4 *m, vec3 *v );

void mat4_rotate_fast( mat4 *m, vec4 *v );

void mat4_rotate( mat4 *dst, mat4 *m, vec4 *v );

void mat4_scale( mat4 *dst, mat4 *m, vec3 *v );

unsigned char mat4_invert( mat4 *m );

unsigned char mat4_invert_full( mat4 *m );

void mat4_transpose( mat4 *m );

void mat4_ortho( mat4 *dst, float left, float right, float bottom, float top, float clip_start, float clip_end );

void mat4_copy_mat3( mat4 *dst, mat3 *m );

void mat4_multiply_mat3( mat4 *dst, mat4 *m0, mat3 *m1 );

void mat4_multiply_mat4( mat4 *dst, mat4 *m0, mat4 *m1 );

#endif
