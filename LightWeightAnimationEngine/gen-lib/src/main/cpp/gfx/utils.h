#ifndef UTILS_H
#define UTILS_H

unsigned int get_micro_time( void );

unsigned int get_milli_time( void );

void adjust_file_path( char *filepath );

void get_file_path( char *filepath, char *path );

void get_file_name( char *filepath, char *name );

void get_file_extension( char *filepath, char *ext, unsigned char uppercase );

void generate_color_from_index( unsigned int index, vec4 *color );

void console_print( const char *str, ... );

void build_frustum( vec4 frustum[ 6 ], mat4 *modelview_matrix, mat4 *projection_matrix );

float sphere_distance_in_frustum( vec4 *frustum, vec3  *location, float radius );

unsigned char point_in_frustum( vec4 *frustum, vec3 *location );

unsigned char box_in_frustum( vec4 *frustum, vec3 *location, vec3 *dimension );

unsigned char sphere_intersect_frustum( vec4 *frustum, vec3 *location, float radius );

unsigned char box_intersect_frustum( vec4 *frustum, vec3 *location, vec3 *dimension );

unsigned int get_next_pow2( unsigned int size );

unsigned int get_nearest_pow2( unsigned int size );

void create_direction_vector( vec3 *dst, vec3 *up_axis, float rotx, float roty, float rotz );

#endif
