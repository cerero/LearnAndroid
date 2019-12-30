#ifndef FONT_H
#define FONT_H


typedef struct
{
	char			name[ MAX_CHAR ];
	
	stbtt_bakedchar *character_data;
	
	float			font_size;
	
	int				texture_width;
	
	int				texture_height;
	
	int				first_character;
	
	int				count_character;
	
	PROGRAM			*program;
	
	unsigned int	tid;

} FONT;


FONT *FONT_init( char *name );

FONT *FONT_free( FONT *font );

unsigned char FONT_load( FONT *font, char *filename, unsigned char relative_path, float font_size, unsigned int texture_width, unsigned int texture_height, int first_character, int count_character );

void FONT_print( FONT *font, float x, float y, char *text, vec4 *color );

float FONT_length( FONT *font, char *text );

#endif
