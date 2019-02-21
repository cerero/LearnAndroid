#include "wl_def.h"
#include "mylog.h"

#include "jni_wolf3dport.h"

byte *gfxbuf = NULL;

typedef struct Color
{
	int r;
	int g;
	int b;
} Color;
// color palette
Color pal[256];

/*
==========================
=
= Quit
=
==========================
*/

void Quit(const char *error)
{
	memptr screen = NULL;
/*
	if (!error || !*error) {
		CA_CacheGrChunk(ORDERSCREEN);
		screen = grsegs[ORDERSCREEN];
		WriteConfig();
	} else if (error) {
		CA_CacheGrChunk(ERRORSCREEN);
		screen = grsegs[ERRORSCREEN];
	}
*/
	ShutdownId();

	if (screen) {
		//DisplayTextSplash(screen);
	}

	if (error && *error) {
		LOGE(1, "Quit: %s", error);
		fprintf(stderr, "Quit: %s\n", error);
		exit(EXIT_FAILURE);
 	}
	exit(EXIT_SUCCESS);
}

void VL_WaitVBL(int vbls)
{
	unsigned long last = get_TimeCount() + vbls;
	while (last > get_TimeCount()) ;
}

extern void* gBitmap;
void VW_UpdateScreen()
{
	int i;
	int numOfPixels = vwidth*vheight;
	for(i = 0; i < numOfPixels; ++i) {
		byte colIdx = gfxbuf[i];
		((uint32_t*)gBitmap)[i] = (0xFF << 24)
			| (pal[colIdx].r << 16)
			| (pal[colIdx].g << 8)
			| (pal[colIdx].b);
	}
	//update the screen through JNI
//	LOGI(3, "calling jni_updateGameBitmap");
	jni_updateGameBitmap();
}

/*
=======================
=
= VL_Startup
=
=======================
*/

void VL_Startup()
{
	LOGI(2, "%d:%d", vwidth, vheight);
	if (NULL == gfxbuf) {
		gfxbuf = malloc(vwidth*vheight);
	}
}

/*
=======================
=
= VL_Shutdown
=
=======================
*/

void VL_Shutdown()
{
	if (gfxbuf != NULL) {
		free(gfxbuf);
		gfxbuf = NULL;
	}
}

//===========================================================================

/*
=================
=
= VL_SetPalette
=
=================
*/

void VL_SetPalette(const byte *palette)
{
	int i;
	VL_WaitVBL(1);
	for (i = 0; i < 256; i++)
	{
		pal[i].r = palette[i*3+0] << 2;
		pal[i].g = palette[i*3+1] << 2;
		pal[i].b = palette[i*3+2] << 2;
	}
}

/*
=================
=
= VL_GetPalette
=
=================
*/

void VL_GetPalette(byte *palette)
{
	int i;
	for (i=0;i<256;i++)
	{
		palette[i*3+0] = pal[i].r >> 2;
		palette[i*3+1] = pal[i].g >> 2;
		palette[i*3+2] = pal[i].b >> 2;
	}
}

void INL_Update()
{
}

void IN_GetMouseDelta(int *dx, int *dy)
{
}

byte IN_MouseButtons()
{
	return 0;
}

/*
===================
=
= IN_JoyButtons
=
===================
*/

byte IN_JoyButtons()
{
	return 0;
}

///////////////////////////////////////////////////////////////////////////
//
//	IN_GetJoyAbs() - Reads the absolute position of the specified joystick
//
///////////////////////////////////////////////////////////////////////////
void IN_GetJoyAbs(word joy,word *xp,word *yp)
{
	*xp = 0;
	*yp = 0;
}

///////////////////////////////////////////////////////////////////////////
//
//	INL_GetJoyDelta() - Returns the relative movement of the specified
//		joystick (from +/-127)
//
///////////////////////////////////////////////////////////////////////////
void INL_GetJoyDelta(word joy,int *dx,int *dy)
{
	*dx = 0;
	*dy = 0;
}

///////////////////////////////////////////////////////////////////////////
//
//	INL_GetJoyButtons() - Returns the button status of the specified
//		joystick
//
///////////////////////////////////////////////////////////////////////////
word INL_GetJoyButtons(word joy)
{
	return 0;
}

///////////////////////////////////////////////////////////////////////////
//
//      IN_SetupJoy() - Sets up thresholding values and calls INL_SetJoyScale()
//              to set up scaling values
//
///////////////////////////////////////////////////////////////////////////
void IN_SetupJoy(word joy,word minx,word maxx,word miny,word maxy)
{
}
