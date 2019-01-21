#ifndef TESTFFMPEG_MY_LOG_H
#define TESTFFMPEG_MY_LOG_H

#ifdef ANDROID
#include <android/log.h>
#ifndef LOG_TAG
#define LOG_TAG "FFMPEG"
#endif //LOG_TAG
#define  LOGD(TAG, format, ...)  __android_log_print(ANDROID_LOG_INFO, TAG, format, __VA_ARGS__)
#define  LOGE(TAG, format, ...)  __android_log_print(ANDROID_LOG_ERROR, TAG, format, __VA_ARGS__)
#else
#include <stdio.h>
#define LOGE(TAG, format, ...)  fprintf(stdout, TAG ": " format "\n", ##__VA_ARGS__)
#define LOGI(TAG, format, ...)  fprintf(stderr, TAG ": " format "\n", ##__VA_ARGS__)
#endif  //ANDROID

void init_my_log();

#endif //TESTFFMPEG_MY_LOG_H
