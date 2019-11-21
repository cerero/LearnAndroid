#include "gfx.h"


void *THREAD_run( void *ptr )
{
	THREAD *thread = ( THREAD * )ptr;

	struct sched_param param;

	pthread_setschedparam( thread->thread, SCHED_RR, &param );

	param.sched_priority = thread->priority;

	while( thread->state )
	{
		if( thread->state == PLAY && thread->threadcallback )
		{ thread->threadcallback( thread ); }
		
		usleep( thread->timeout * 1000 );
	}
	
	pthread_exit( NULL );
	
	return NULL;
}


THREAD *THREAD_create( THREADCALLBACK *threadcallback,
					   void			  *userdata,
					   int			   priority,
					   unsigned int	   timeout )
{
	THREAD *thread = ( THREAD * ) calloc( 1, sizeof( THREAD ) );

	thread->threadcallback = threadcallback;
	
	thread->priority = priority;
	thread->userdata = userdata;
	thread->timeout  = timeout;

	THREAD_pause( thread );

	thread->thread_hdl = pthread_create( &thread->thread,
										 NULL,
										 THREAD_run,
										 ( void * )thread );
	return thread;
}


THREAD *THREAD_free( THREAD *thread )
{
	THREAD_stop( thread );
	
	pthread_join( thread->thread, NULL );
	
	free( thread );
	return NULL;
}


void THREAD_set_callback( THREAD *thread, THREADCALLBACK *threadcallback )
{ thread->threadcallback = threadcallback; }


void THREAD_play( THREAD *thread )
{ thread->state = PLAY; }


void THREAD_pause( THREAD *thread )
{
	thread->state = PAUSE;
	
	usleep( thread->timeout * 1000 );
}


void THREAD_stop( THREAD *thread )
{
	thread->state = STOP;

	usleep( thread->timeout * 1000 );
}
