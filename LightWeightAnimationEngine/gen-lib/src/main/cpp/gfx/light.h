#ifndef LIGHT_H
#define LIGHT_H

enum
{
	LIGHT_DIRECTIONAL			 = 0,
	LIGHT_POINT					 = 1,
	LIGHT_POINT_WITH_ATTENUATION = 2,
	LIGHT_POINT_SPHERE			 = 3,
	LIGHT_SPOT					 = 4
};


typedef struct
{
	char	name[ MAX_CHAR ];

	vec4	color;

	vec3	direction;
	
	vec4	position;

	float	linear_attenuation;
	
	float	quadratic_attenuation;
	
	float	distance;
	
	float	spot_fov;

	float	spot_cos_cutoff;

	float	spot_blend;
	
	vec3	spot_direction;	

	unsigned char type;
	
} LIGHT;


LIGHT *LIGHT_create_directional( char *name, vec4 *color, float rotx, float roty, float rotz );

LIGHT *LIGHT_create_point( char *name, vec4 *color, vec3 *position );

LIGHT *LIGHT_create_point_with_attenuation( char *name, vec4 *color, vec3 *position, float distance, float linear_attenuation, float quadratic_attenuation );

LIGHT *LIGHT_create_point_sphere( char *name, vec4 *color, vec3 *position, float distance );

LIGHT *LIGHT_create_spot( char *name, vec4 *color, vec3 *position, float rotx, float roty, float rotz, float fov, float spot_blend );

void LIGHT_get_direction_in_object_space( LIGHT *light, mat4 *m, vec3 *direction );

void LIGHT_get_direction_in_eye_space( LIGHT *light, mat4 *m, vec3 *direction );

void LIGHT_get_position_in_eye_space( LIGHT *light, mat4 *m, vec4 *position );

LIGHT *LIGHT_free( LIGHT *light );

#endif
