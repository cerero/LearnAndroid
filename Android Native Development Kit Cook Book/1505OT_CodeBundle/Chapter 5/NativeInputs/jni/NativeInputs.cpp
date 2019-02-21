#include <jni.h>
#include <android_native_app_glue.h>
#include <android/input.h>

#include "mylog.h"

typedef struct USERDATA {
	int xMove;
	int prevWst;
} USERDATA;

void drawSomething(struct android_app* app) {
	ANativeWindow_Buffer lWindowBuffer;
	//step 1. set window buffer format and size
	ANativeWindow* lWindow = app->window;
	ANativeWindow_setBuffersGeometry(lWindow, 0, 0, WINDOW_FORMAT_RGBA_8888);
	//step 2.lock the window's next drawing surface
	if (ANativeWindow_lock(lWindow, &lWindowBuffer, NULL) < 0) {
		LOGE(1, "cannot lock window");
		return;
	}
//	LOGI(2, "height %d, width %d, stride %d", lWindowBuffer.height, lWindowBuffer.width, lWindowBuffer.stride);
	//step 3. clear the buffer
	memset(lWindowBuffer.bits, 0, lWindowBuffer.stride*lWindowBuffer.height*sizeof(uint32_t));
	//step 4. draw something
	int sqh = 150, sqw = 100;
	USERDATA *userData = (USERDATA*)app->userData;
	if (-1 == userData->prevWst) {
		//initialize
		userData->prevWst = lWindowBuffer.stride/2 - sqw/2;
	}
	int wst = userData->prevWst + userData->xMove;
	//update
	userData->prevWst = wst;
	if (wst < 0) {
		wst = 0;
	} else if (wst > lWindowBuffer.stride - sqw) {
		wst = lWindowBuffer.stride - sqw;
	}
	int wed = wst + sqw;
	int hst = lWindowBuffer.height/2 - sqh/2;
	int hed = hst + sqh;
//	LOGI(2, "drawing square width: %d:%d, height: %d:%d", wst, wed, hst, hed);
	for (int i = hst; i < hed; ++i) {
		for (int j = wst; j < wed; ++j) {
			((char*)(lWindowBuffer.bits))[(i*lWindowBuffer.stride + j)*sizeof(uint32_t)] = (char)255;			//R
			((char*)(lWindowBuffer.bits))[(i*lWindowBuffer.stride + j)*sizeof(uint32_t) + 1] = (char)0;		//G
			((char*)(lWindowBuffer.bits))[(i*lWindowBuffer.stride + j)*sizeof(uint32_t) + 2] = (char)0;		//B
			((char*)(lWindowBuffer.bits))[(i*lWindowBuffer.stride + j)*sizeof(uint32_t) + 3] = (char)255;		//A
		}
	}
	//step 5. unlock the window's drawing surface and post the new buffer to display
	ANativeWindow_unlockAndPost(lWindow);
	if (app->redrawNeeded) {
		app->redrawNeeded = 0;
	}
}

void handle_activity_lifecycle_events(struct android_app* app, int32_t cmd) {
//	LOGI(2, "%d: redraw %d", cmd, app->redrawNeeded);
	switch (cmd) {
	case APP_CMD_INIT_WINDOW:
//		LOGI(2, "window initialized");
		drawSomething(app);
		break;
	}
}

char* get_key_event_str(int action, int flags, int keycode) {
	char str[1000];
	switch (action) {
	case AKEY_EVENT_ACTION_DOWN:
		strcpy(str, "key down:");
		break;
	case AKEY_EVENT_ACTION_UP:
		strcpy(str, "key up:");
		break;
	case AKEY_EVENT_ACTION_MULTIPLE:
		strcpy(str, "multiple duplicate key events:");
		break;
	}
	if (flags & AKEY_EVENT_FLAG_WOKE_HERE) {
		strcat(str, "device woke:");
	}
	if (flags & AKEY_EVENT_FLAG_SOFT_KEYBOARD) {
		strcat(str, "software keyboard key event:");
	}
	if (flags & AKEY_EVENT_FLAG_KEEP_TOUCH_MODE) {
		strcat(str, "keep touch mode:");
	}
	if (flags & AKEY_EVENT_FLAG_FROM_SYSTEM) {
		strcat(str, "event from trusted part of system:");
	}
	if (flags & AKEY_EVENT_FLAG_EDITOR_ACTION) {
		strcat(str, "editor action:");
	}
	if (flags & AKEY_EVENT_FLAG_CANCELED) {
		strcat(str, "key event cancelled:");
	}
	if (flags & AKEY_EVENT_FLAG_VIRTUAL_HARD_KEY) {
		strcat(str, "hard key event:");
	}
	if (flags & AKEY_EVENT_FLAG_LONG_PRESS) {
		strcat(str, "long press key event:");
	}
	if (flags & AKEY_EVENT_FLAG_CANCELED_LONG_PRESS) {
		strcat(str, "long press cancelled:");
	}
	if (flags & AKEY_EVENT_FLAG_TRACKING) {
		strcat(str, "event key code is still being tracked:");
	}
	switch (keycode) {
	case AKEYCODE_MENU:
		strcat(str, "menu key pressed:");
		break;
	case AKEYCODE_HOME:
		strcat(str, "home key pressed:");
		break;
	case AKEYCODE_BACK:
		strcat(str, "back key pressed:");
		break;
	case AKEYCODE_SEARCH:
		strcat(str, "search key pressed:");
		break;
	}
	return str;
}

int mPreviousX = -1;
int32_t handle_input_events(struct android_app* app, AInputEvent* event) {
	int etype = AInputEvent_getType(event);
	switch (etype) {
	case AINPUT_EVENT_TYPE_KEY:
		int32_t eaction, eflags, ekeycode, escancode;
		eaction = AKeyEvent_getAction(event);
		eflags = AKeyEvent_getFlags(event);
		ekeycode = AKeyEvent_getKeyCode(event);
//		LOGI(2, "%s", get_key_event_str(eaction, eflags, ekeycode));
		break;
	case AINPUT_EVENT_TYPE_MOTION:
		int32_t action, posX, pointer_index;
		action = AMotionEvent_getAction(event);
		pointer_index = (action&AMOTION_EVENT_ACTION_POINTER_INDEX_MASK) >> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;
		posX = AMotionEvent_getX(event, pointer_index);
		//write a command to the activity lifecycle event queue (pipe)
		if (action == AMOTION_EVENT_ACTION_MOVE) {
			int xMove = posX - mPreviousX;
			USERDATA* userData = (USERDATA*)app->userData;
			userData->xMove = xMove;
			app->redrawNeeded = 1;
		}
		mPreviousX = posX;
//		LOGI(2, "action: %d, posX: %d, mPreviousX: %d, posX: %d", action, posX, mPreviousX, posX);
		break;
	default:
		LOGI(2, "other input event");
		break;
	}
}

void android_main(struct android_app* app) {
	// Make sure glue isn't stripped.
	app_dummy();
	USERDATA userData;
	userData.xMove = 0;
	userData.prevWst = -1;
	app->userData = &userData;
	app->onAppCmd = handle_activity_lifecycle_events;
	app->onInputEvent = handle_input_events;
	while (1) {
		int ident, events;
		struct android_poll_source* source;
		if ((ident=ALooper_pollOnce(app->redrawNeeded?0:-1, NULL, &events, (void**)&source)) >= 0) {
			if (NULL!=source) {
				source->process(app, source);
			}
			if (app->redrawNeeded) {
				drawSomething(app);
			}
		}
	}
}
