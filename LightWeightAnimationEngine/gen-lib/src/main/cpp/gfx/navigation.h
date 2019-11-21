#ifndef NAVIGATION_H
#define NAVIGATION_H

#define NAVIGATION_MAX_NODE			1024

#define NAVIGATION_MAX_POLY			512

#define NAVIGATION_MAX_PATH_POLY	256


typedef struct
{
	float cell_size;

	float cell_height;

	float agent_height;

	float agent_radius;
	
	float agent_max_climb;

	float agent_max_slope;

	float region_min_size;

	float region_merge_size;

	float edge_max_len;

	float edge_max_error;

	float vert_per_poly;

	float detail_sample_dst;

	float detail_sample_max_error;

} NAVIGATIONCONFIGURATION;


typedef struct
{
	vec3			start_location;
	
	vec3			end_location;

	dtQueryFilter	path_filter;
	
	dtPolyRef		start_reference;

	dtPolyRef		end_reference;
	
	unsigned int	poly_count;
	
	dtPolyRef		poly_array[ NAVIGATION_MAX_POLY ];

} NAVIGATIONPATH;


typedef struct
{
	unsigned int	path_point_count;

	unsigned char	path_flags_array[ NAVIGATION_MAX_PATH_POLY ];
	
	vec3			path_point_array[ NAVIGATION_MAX_PATH_POLY ];
	
	dtPolyRef		path_poly_array[ NAVIGATION_MAX_PATH_POLY ];

} NAVIGATIONPATHDATA;


typedef struct
{
	char					name[ MAX_CHAR ];
	
	NAVIGATIONCONFIGURATION navigationconfiguration;

	vec3					tolerance;

	unsigned char			*triangle_flags;

	dtNavMesh				*dtnavmesh;
	
	PROGRAM					*program;	

} NAVIGATION;


NAVIGATION *NAVIGATION_init( char *name );

NAVIGATION *NAVIGATION_free( NAVIGATION *navigation );

void NAVIGATION_set_default_configuration( NAVIGATION *navigation );

unsigned char NAVIGATION_build( NAVIGATION *navigation, OBJ *obj, unsigned int mesh_index );

unsigned char NAVIGATION_get_path( NAVIGATION *navigation, NAVIGATIONPATH *navigationpath, NAVIGATIONPATHDATA *navigationpathdata );

void NAVIGATION_draw( NAVIGATION *navigation );

#endif
