#ifndef MEMORY_H
#define MEMORY_H

#include "types.h"
typedef struct
{
	char			filename[ MAX_PATH ];
	
	unsigned int	size;
	
	unsigned int	position;

	unsigned char	*buffer;

} MEMORY;


MEMORY *mopen( char *filename, unsigned char relative_path );

MEMORY *mclose( MEMORY *memory );

unsigned int mread( MEMORY *memory, void *dst, unsigned int size );

void minsert( MEMORY *memory, char *str, unsigned int position );

#endif
