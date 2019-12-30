#ifndef AUDIO_H
#define AUDIO_H


typedef struct
{
	ALCdevice		*al_device;

	ALCcontext		*al_context;
	
	ov_callbacks	callbacks;
		
} AUDIO;


extern AUDIO audio;

void AUDIO_start( void );

void AUDIO_stop( void );

void AUDIO_error( void );

void AUDIO_set_listener( vec3 *location, vec3 *direction, vec3 *up );

size_t AUDIO_ogg_read( void *ptr, size_t size, size_t read, void *memory_ptr );

int AUDIO_ogg_seek( void *memory_ptr, ogg_int64_t offset, int stride );

long AUDIO_ogg_tell( void *memory_ptr );

int AUDIO_ogg_close( void *memory_ptr );

#endif
