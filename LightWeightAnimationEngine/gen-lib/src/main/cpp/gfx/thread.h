#ifndef THREAD_H
#define THREAD_H


enum
{
	THREAD_PRIORITY_VERY_LOW  = 15,
	THREAD_PRIORITY_LOW       = 23,
	THREAD_PRIORITY_NORMAL    = 31,
	THREAD_PRIORITY_HIGH 	  = 39,
	THREAD_PRIORITY_VERY_HIGH = 47
};


typedef void( THREADCALLBACK( void * ) );


typedef struct
{
	unsigned char	state;

	int				priority;

	unsigned int	timeout;
		
	pthread_t		thread;
	
	unsigned int	thread_hdl;
	
	THREADCALLBACK	*threadcallback;
	
	void			*userdata;

} THREAD;


THREAD *THREAD_create( THREADCALLBACK *threadcallback, void	*userdata, int priority, unsigned int timeout );

THREAD *THREAD_free( THREAD *thread );

void THREAD_set_callback( THREAD *thread, THREADCALLBACK *threadcallback );

void THREAD_play( THREAD *thread );

void THREAD_pause( THREAD *thread );

void THREAD_stop( THREAD *thread );

#endif
