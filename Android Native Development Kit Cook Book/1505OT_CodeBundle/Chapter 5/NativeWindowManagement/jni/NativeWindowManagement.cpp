#include <jni.h>
#include <android_native_app_glue.h>

#include "mylog.h"

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
	LOGI(2, "height %d, width %d, stride %d", lWindowBuffer.height, lWindowBuffer.width, lWindowBuffer.stride);
	//step 3. clear the buffer
	memset(lWindowBuffer.bits, 0, lWindowBuffer.stride*lWindowBuffer.height*sizeof(uint32_t));
	//step 4. draw something
	int sqh = 150, sqw = 100;
	int wst = lWindowBuffer.stride/2 - sqw/2;
	int wed = wst + sqw;
	int hst = lWindowBuffer.height/2 - sqh/2;
	int hed = hst + sqh;
	LOGI(2, "drawing square width: %d:%d, height: %d:%d", wst, wed, hst, hed);
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
}

void handle_activity_lifecycle_events(struct android_app* app, int32_t cmd) {
	LOGI(2, "%d: dummy data %d", cmd, *((int*)(app->userData)));
	switch (cmd) {
	case APP_CMD_INIT_WINDOW:
		LOGI(2, "window initialized");
		drawSomething(app);
		break;
	}
}

void android_main(struct android_app* app) {
	// Make sure glue isn't stripped.
	app_dummy();
	int dummyData = 111;
	app->userData = &dummyData;
	app->onAppCmd = handle_activity_lifecycle_events;
	while (1) {
		int ident, events;
		struct android_poll_source* source;
//		if ((ident=ALooper_pollAll(-1, NULL, &events, (void**)&source)) >= 0) {
		if ((ident=ALooper_pollOnce(-1, NULL, &events, (void**)&source)) >= 0) {
			source->process(app, source);
		}
	}
}
