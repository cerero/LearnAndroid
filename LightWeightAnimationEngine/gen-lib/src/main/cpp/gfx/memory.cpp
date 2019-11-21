#include "gfx.h"

MEMORY *mopen( char *filename, unsigned char relative_path )
{
	#ifdef __IPHONE_4_0

		FILE *f;
		
		char fname[ MAX_PATH ] = {""};
		
		if( relative_path )
		{
			get_file_path( getenv( "FILESYSTEM" ), fname );
			
			strcat( fname, filename );
		}
		else strcpy( fname, filename );

		f = fopen( fname, "rb" );
		
		if( !f ) return NULL;
		
		
		MEMORY *memory = ( MEMORY * ) calloc( 1, sizeof( MEMORY ) );
		
		strcpy( memory->filename, fname );
		
		
		fseek( f, 0, SEEK_END );
		memory->size = ftell( f );
		fseek( f, 0, SEEK_SET );
		
		
		memory->buffer = ( unsigned char * ) calloc( 1, memory->size + 1 );
		fread( memory->buffer, memory->size, 1, f );
		memory->buffer[ memory->size ] = 0;
		
		
		fclose( f );
		
		return memory;
	
	
	#else
	
		char fpath[ MAX_PATH ] = {""},
			 fname[ MAX_PATH ] = {""};

		unzFile		    uf;
		unz_file_info   fi;
		unz_file_pos    fp;

		strcpy( fpath, getenv( "FILESYSTEM" ) );

		uf = unzOpen( fpath );
		
		if( !uf ) return NULL;

		if( relative_path ) sprintf( fname, "assets/%s", filename );
		else strcpy( fname, filename );
		
		unzGoToFirstFile( uf );

		MEMORY *memory = ( MEMORY * ) calloc( 1, sizeof( MEMORY ) );

		unzGetFilePos( uf, &fp );
		
		if( unzLocateFile( uf, fname, 1 ) == UNZ_OK )
		{
			unzGetCurrentFileInfo(  uf,
								   &fi,
									memory->filename,
									MAX_PATH,
									NULL, 0,
									NULL, 0 );
		
			if( unzOpenCurrentFilePassword( uf, NULL ) == UNZ_OK )
			{
				memory->position = 0;
				memory->size	 = fi.uncompressed_size;
				memory->buffer   = ( unsigned char * ) realloc( memory->buffer, fi.uncompressed_size + 1 );
				memory->buffer[ fi.uncompressed_size ] = 0;

				while( unzReadCurrentFile( uf, memory->buffer, fi.uncompressed_size ) > 0 ){}

				unzCloseCurrentFile( uf );

				unzClose( uf );
					
				return memory;
			}
		}
		
		unzClose( uf );

		return NULL;
		
	#endif
}


MEMORY *mclose( MEMORY *memory )
{
	if( memory->buffer ) free( memory->buffer );
	
	free( memory );
	return NULL;
}


unsigned int mread( MEMORY *memory, void *dst, unsigned int size )
{
	if( ( memory->position + size ) > memory->size )
	{ size = memory->size - memory->position; }

	memcpy( dst, &memory->buffer[ memory->position ], size );
	
	memory->position += size;

	return size;
}


void minsert( MEMORY *memory, char *str, unsigned int position )
{
	unsigned int s1 = strlen( str ),
				 s2 = memory->size + s1 + 1;

	char *buffer = ( char * )memory->buffer,
		 *tmp	 = ( char * )calloc( 1, s2 );
	
	if( position )
	{ strncpy( &tmp[ 0 ], &buffer[ 0 ], position ); }

	strcat( &tmp[ position ], str );
	
	strcat( &tmp[ position + s1 ], &buffer[ position ] );

	memory->size = s2;
	
	free( memory->buffer );
	memory->buffer = ( unsigned char * )tmp;	
}
