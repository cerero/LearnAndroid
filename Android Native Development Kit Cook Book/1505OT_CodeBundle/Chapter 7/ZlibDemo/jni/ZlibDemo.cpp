#include <jni.h>
#include <android/log.h>
#include <zlib.h>

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "mylog.h"

void naCompressAndDecompress(JNIEnv* pEnv, jclass clazz);
void naGzFileDemo(JNIEnv* pEnv, jclass clazz);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[2];
	nm[0].name = "naCompressAndDecompress";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)naCompressAndDecompress;
	nm[1].name = "naGzFileDemo";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*)naGzFileDemo;
	jclass cls = env->FindClass("cookbook/chapter7/zlibdemo/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 2);
	return JNI_VERSION_1_6;
}

#define S_BUF_SIZE 10000000
unsigned char dataBuf[S_BUF_SIZE], decompressedBuf[S_BUF_SIZE];
unsigned char* compressedBuf;

unsigned long getOriginalDataLen() {
	srand(time(NULL));
	return rand()%5000000 + 5000000;
}

void generateOriginalDataRandom(unsigned long size) {
	int i;
	srand(time(NULL));
	for (i = 0; i < size; ++i) {
		dataBuf[i] = (unsigned char)rand()%255;
	}
}

void generateOriginalData(unsigned long size) {
	int i;
	srand(time(NULL));
	unsigned char value;
	for (i = 0; i < size; ++i) {
		if (0 == size%50) {
			value = (unsigned char)rand()%255;
		}
		dataBuf[i] = value;
	}
}

void compressUtil(unsigned long originalDataLen) {
	//get compress buffer bound
	int rv;
	int compressBufBound = compressBound(originalDataLen);
	compressedBuf = (unsigned char*) malloc(sizeof(unsigned char)*compressBufBound);
	unsigned long compressedDataLen = compressBufBound;
	//compress
	rv = compress2(compressedBuf, &compressedDataLen, dataBuf, originalDataLen, 6);
	if (Z_OK != rv) {
		LOGE(1, "compression error");
		free(compressedBuf);
		return;
	}
	LOGI(1, "upper bound:%d; input: %d; compressed: %d",
			compressBufBound, originalDataLen, compressedDataLen);
	//decompress and verify result
	unsigned long decompressedDataLen = S_BUF_SIZE;
	rv = uncompress(decompressedBuf, &decompressedDataLen, compressedBuf, compressedDataLen);
	if (Z_OK != rv) {
		LOGE(1, "decompression error");
		free(compressedBuf);
		return;
	}
	LOGI(1, "decompressed: %d", decompressedDataLen);
	if (0 == memcmp(dataBuf, decompressedBuf, originalDataLen)) {
		LOGI(1, "decompressed data same as original data");
	} else {
		LOGI(1, "decompressed data different from original data");
	}
	//free resource
	free(compressedBuf);
}

void naCompressAndDecompress(JNIEnv* pEnv, jclass clazz) {
	unsigned long originalDataLen = getOriginalDataLen();
	LOGI(1, "---------data with repeated bytes---------")
	generateOriginalData(originalDataLen);
	compressUtil(originalDataLen);
	LOGI(1, "---------data with random bytes---------")
	generateOriginalDataRandom(originalDataLen);
	compressUtil(originalDataLen);
}
