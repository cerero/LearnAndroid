#ifndef SHADER_H
#define SHADER_H

typedef struct
{
	char			name[ MAX_CHAR ];
	
	unsigned int	type;

	unsigned int	sid;
	
} SHADER;


SHADER *SHADER_init( char *name, unsigned int type );

SHADER *SHADER_free( SHADER *shader );

unsigned char SHADER_compile( SHADER *shader, const char *code, unsigned char debug );

void SHADER_delete_id( SHADER *shader );

#endif
