#include <jni.h>
#include "mylog.h"

void outputLogs() {
	LOGU(9, "unknown log message");
	LOGD(8, "default log message");
	LOGV(7, "verbose log message");
	LOGDE(6, "debug log message");
	LOGI(5, "information log message");
	LOGW(4, "warning log message");
	LOGE(3, "error log message");
	LOGF(2, "fatal error log message");
	LOGS(1, "silent log message");
}

extern "C" {
	JNIEXPORT void JNICALL Java_cookbook_chapter3_NDKLoggingDemoActivity_LoggingDemo(JNIEnv* env, jobject o){
		outputLogs();
	}
}
