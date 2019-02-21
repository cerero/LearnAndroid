#include <jni.h>
#include <zlib.h>
#include <string.h>

#include "mylog.h"

int writeToFile();
void readFromFile(int pBytesToRead);

void naGzFileDemo(JNIEnv* pEnv, jclass clazz) {
	int byteWritten = writeToFile();
	readFromFile(byteWritten);
}

int writeToFile() {
	gzFile file;
	file = gzopen("/sdcard/test.gz", "w6");
	if (NULL == file) {
		LOGE(1, "cannot open file to write");
		return 0;
	}
	const char* dataStr = "hello, Android NDK!";
	int bytesWritten = gzwrite(file, dataStr, strlen(dataStr));
	gzclose(file);
	return bytesWritten;
}

void readFromFile(int pBytesToRead) {
	gzFile file;
	file = gzopen("/sdcard/test.gz", "r6");
	if (NULL == file) {
		LOGE(1, "cannot open file to read");
		return;
	}
	char readStr[100];
	int bytesRead = gzread(file, readStr, pBytesToRead);
	gzclose(file);
	LOGI(1, "%d: %s", bytesRead, readStr);
}
