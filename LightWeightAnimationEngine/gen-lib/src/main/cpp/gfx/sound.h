#ifndef SOUND_H
#define SOUND_H

#define MAX_BUFFER 4

#define MAX_CHUNK_SIZE 1024 << 3


typedef struct
{
	char			name[ MAX_CHAR ];
	
	OggVorbis_File	*file;

	vorbis_info		*info;
	
	MEMORY			*memory;
	
	unsigned int	bid[ MAX_BUFFER ];

} SOUNDBUFFER;


typedef struct
{
	char			name[ MAX_CHAR ];
	
	unsigned int	sid;

	int				loop;

	SOUNDBUFFER		*soundbuffer;

} SOUND;


SOUNDBUFFER *SOUNDBUFFER_load( char *name, MEMORY *memory );

SOUNDBUFFER *SOUNDBUFFER_load_stream( char *name, MEMORY *memory );

unsigned char SOUNDBUFFER_decompress_chunk( SOUNDBUFFER *soundbuffer, unsigned int buffer_index );

SOUNDBUFFER *SOUNDBUFFER_free( SOUNDBUFFER *soundbuffer );

SOUND *SOUND_add( char *name, SOUNDBUFFER *soundbuffer );

SOUND *SOUND_free( SOUND *sound );

void SOUND_play( SOUND *sound, int loop );

void SOUND_pause( SOUND *sound );

void SOUND_stop( SOUND *sound );

void SOUND_set_speed( SOUND *sound, float speed );

void SOUND_set_volume( SOUND *sound, float volume );

void SOUND_set_location( SOUND *sound, vec3 *location, float reference_distance );

void SOUND_rewind( SOUND *sound );

float SOUND_get_time( SOUND *sound );

int SOUND_get_state( SOUND *sound );

float SOUND_get_volume( SOUND *sound );

void SOUND_update_queue( SOUND *sound );

#endif
