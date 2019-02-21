#include <jni.h>
#include <android_native_app_glue.h>

#include "mylog.h"

void handle_activity_lifecycle_events(struct android_app* app, int32_t cmd) {
	LOGI(2, "%d: dummy data %d", cmd, *((int*)(app->userData)));
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
