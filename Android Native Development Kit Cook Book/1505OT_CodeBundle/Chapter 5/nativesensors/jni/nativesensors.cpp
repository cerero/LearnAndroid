#include <jni.h>
#include <android_native_app_glue.h>
#include <android/input.h>
#include <android/sensor.h>
#include <time.h>
#include <math.h>

#include "mylog.h"

typedef struct SAVED_USERDATA {
	int xMove;
	int prevWst;
	int moveDir;
} SAVED_USERDATA;

typedef struct USERDATA {
	SAVED_USERDATA drawingData;

	ASensorManager* sensorManager;
	const ASensor* accelerometerSensor;
	const ASensor* gyroscopeSensor;
	ASensorEventQueue* sensorEventQueue;
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
	if (-1 == userData->drawingData.prevWst) {
		//initialize
		userData->drawingData.prevWst = lWindowBuffer.stride/2 - sqw/2;
	}
	int wst = userData->drawingData.prevWst + userData->drawingData.xMove*userData->drawingData.moveDir;
	//update
	userData->drawingData.prevWst = wst;
	if (wst < 0) {
		wst = 0;
		userData->drawingData.moveDir *= -1;	//switch direction
	} else if (wst > lWindowBuffer.stride - sqw) {
		wst = lWindowBuffer.stride - sqw;
		userData->drawingData.moveDir *= -1;	//switch direction
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
	USERDATA* userData;
	switch (cmd) {
	case APP_CMD_INIT_WINDOW:
//		LOGI(2, "window initialized");
		drawSomething(app);
		break;
	case APP_CMD_SAVE_STATE:
		// save current state
		userData = (USERDATA*)(app->userData);
		app->savedState = malloc(sizeof(SAVED_USERDATA));
		*((SAVED_USERDATA*)app->savedState) = userData->drawingData;
		app->savedStateSize = sizeof(SAVED_USERDATA);
		break;
	case APP_CMD_GAINED_FOCUS:
		// When our app gains focus, we start monitoring the accelerometer.
		userData = (USERDATA*)(app->userData);
		if (NULL != userData->accelerometerSensor) {
			ASensorEventQueue_enableSensor(userData->sensorEventQueue,
					userData->accelerometerSensor);
			// We'd like to get 60 events per second (in us).
			ASensorEventQueue_setEventRate(userData->sensorEventQueue,
					userData->accelerometerSensor, (1000L/60)*1000);
		}
		break;
	case APP_CMD_LOST_FOCUS:
		// When our app loses focus, we stop monitoring the accelerometer.
		// This is to avoid consuming battery while not being used.
		USERDATA userData = *(USERDATA*) app->userData;
		if (NULL!=userData.accelerometerSensor) {
			ASensorEventQueue_disableSensor(userData.sensorEventQueue,
				userData.accelerometerSensor);
		}
		break;
	}
}

int64_t get_time() {
	timespec lTimeVal;
	clock_gettime(CLOCK_MONOTONIC, &lTimeVal);
	return (lTimeVal.tv_sec*1000 + (lTimeVal.tv_nsec * 1.0e-6));
}

void android_main(struct android_app* app) {
	// Make sure glue isn't stripped.
	app_dummy();
	USERDATA userData;
	userData.drawingData.xMove = 0;
	userData.drawingData.prevWst = -1;
	userData.drawingData.moveDir = 1;
	app->userData = &userData;
	app->onAppCmd = handle_activity_lifecycle_events;

	// Prepare to monitor accelerometer
	userData.sensorManager = ASensorManager_getInstance();
	ASensorList list;
	int i, cnt;
	cnt = ASensorManager_getSensorList(userData.sensorManager, &list);
	for (i = 0; i < cnt; ++i) {
		const ASensor* sensor = list[i];
		const char* name = ASensor_getName(sensor);
		int type = ASensor_getType(sensor);
		LOGI(2, "name: %s, type: %d", name, type);
	}
	userData.accelerometerSensor = ASensorManager_getDefaultSensor(userData.sensorManager,
			ASENSOR_TYPE_ACCELEROMETER);
//	userData.gyroscopeSensor = ASensorManager_getDefaultSensor(userData.sensorManager,
//			ASENSOR_TYPE_GYROSCOPE);
	userData.sensorEventQueue = ASensorManager_createEventQueue(userData.sensorManager,
			app->looper, LOOPER_ID_USER, NULL, NULL);

	if (app->savedState != NULL) {
		// We are starting with a previous saved state; restore from it.
		userData.drawingData = *(SAVED_USERDATA*)app->savedState;
	}

	int64_t lastTime = get_time();
	int64_t currentEventTime = 0, lastEventTime = 0;
	//if shake count doesn't reach SHAKE_COUNT within the SHAKE_TIMEOUT
	//period, the shake count is reset to 0
	int SHAKE_TIMEOUT = 1000;
	int SHAKE_COUNT_THRESHOLD = 2;
	//a single shake time threshold, used to identify a single shake
	int TIME_THRESHOLD = 100;
	//event time threshold, used to prevent duplicate events
	int TIME_EVENT_THRESHOLD = 100;
	//only when pseudo force exceeds the limit, it's
	//counted as a single shake
	int FORCE_THRESHOLD = 350;
	int shakeCount = 0;
	int lastX = 0, lastY = 0, lastZ = 0;

	while (0==app->destroyRequested) {
		int ident, events;
		struct android_poll_source* source;
		if ((ident=ALooper_pollOnce(-1, NULL, &events, (void**)&source)) >= 0) {
			if (NULL!=source) {
				source->process(app, source);
			}
			if (LOOPER_ID_USER == ident) {
				if (NULL != userData.accelerometerSensor) {
					ASensorEvent event;
					while (ASensorEventQueue_getEvents(userData.sensorEventQueue,
							&event, 1) > 0) {
						if (ASENSOR_TYPE_ACCELEROMETER == event.type) {
							int64_t currentTime = get_time();
							if (currentEventTime == lastEventTime) {
								currentEventTime = currentTime;
							}
							if ((currentTime - lastTime) > SHAKE_TIMEOUT) {
								shakeCount = 0;
							}
							if ((currentTime - lastTime) > TIME_THRESHOLD) {
								long diff = currentTime - lastTime;
								float speedX = (event.acceleration.x - lastX)/diff*10000;
								float speedY = (event.acceleration.y - lastY)/diff*10000;
								float speedZ = (event.acceleration.z - lastZ)/diff*10000;
								float speed = fabs(speedX + speedY + speedZ);
								if (speed > FORCE_THRESHOLD) {
									//a shake event occurred
									if ((++shakeCount >= SHAKE_COUNT_THRESHOLD) && (currentEventTime-lastEventTime > TIME_EVENT_THRESHOLD)) {
										USERDATA* userData = (USERDATA*)app->userData;
										LOGI(2, "speed: %f",  speed/100);
										userData->drawingData.xMove += speed/100;
										drawSomething(app);
										shakeCount = 0;
										lastEventTime = currentEventTime;
									}
								}
								lastTime = currentTime;
								lastX = event.acceleration.x;
								lastY = event.acceleration.y;
								lastZ = event.acceleration.z;
							}
						}
					}
				}
			}
		}
	}
	ASensorManager_destroyEventQueue(userData.sensorManager, userData.sensorEventQueue);
}

