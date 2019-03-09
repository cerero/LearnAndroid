#pragma once

#define MY_LOG_LEVEL_VERBOSE 1
#define MY_LOG_LEVEL_DEBUG 2
#define MY_LOG_LEVEL_INFO 3
#define MY_LOG_LEVEL_WARNING 4
#define MY_LOG_LEVEL_ERROR 5
#define MY_LOG_LEVEL_FATAL 6
#define MY_LOG_LEVEL_SILENT 7

#ifndef MY_LOG_TAG
#   define MY_LOG_TAG __FILE__
#endif

#ifndef MY_LOG_LEVEL
#   define MY_LOG_LEVEL MY_LOG_LEVEL_VERBOSE
#endif

#define MY_LOG_NOOP (void) 0


#ifdef ANDROID
#include <android/log.h>

#define MY_LOG_PRINT(level,fmt,...) \
        __android_log_print(level, MY_LOG_TAG, "(%s:%u) %s: ", fmt, \
                __FILE__, __LINE__, __PRETTY_FUNCTION__, ##__VA_ARGS__)

#if MY_LOG_LEVEL_VERBOSE >= MY_LOG_LEVEL
#   define MY_LOG_VERBOSE(fmt,...) \
        MY_LOG_PRINT(ANDROID_LOG_VERBOSE, fmt, ##__VA_ARGS__)
#else
#   define MY_LOG_VERBOSE(...)  MY_LOG_NOOP
#endif

#if MY_LOG_LEVEL_DEBUG >= MY_LOG_LEVEL
#   define MY_LOG_DEBUG(fmt,...) \
        MY_LOG_PRINT(ANDROID_LOG_DEBUG, fmt, ##__VA_ARGS__)
#else
#   define MY_LOG_DEBUG(...) MY_LOG_NOOP
#endif

#if MY_LOG_LEVEL_INFO >= MY_LOG_LEVEL
#   define MY_LOG_INFO(fmt,...) \
        MY_LOG_PRINT(ANDROID_LOG_INFO, fmt, ##__VA_ARGS__)
#else
#   define MY_LOG_INFO(...) MY_LOG_NOOP
#endif

#if MY_LOG_LEVEL_WARNING >= MY_LOG_LEVEL
#   define MY_LOG_WARNING(fmt,...) \
        MY_LOG_PRINT(ANDROID_LOG_WARN, fmt, ##__VA_ARGS__)
#else
#   define MY_LOG_WARNING(...) MY_LOG_NOOP
#endif

#if MY_LOG_LEVEL_ERROR >= MY_LOG_LEVEL
#   define MY_LOG_ERROR(fmt,...) \
        MY_LOG_PRINT(ANDROID_LOG_ERROR, fmt, ##__VA_ARGS__)
#else
#   define MY_LOG_ERROR(...) MY_LOG_NOOP
#endif

#if MY_LOG_LEVEL_FATAL >= MY_LOG_LEVEL
#   define MY_LOG_FATAL(fmt,...) \
        MY_LOG_PRINT(ANDROID_LOG_FATAL, fmt, ##__VA_ARGS__)
#else
#   define MY_LOG_FATAL(...) MY_LOG_NOOP
#endif

#if MY_LOG_LEVEL_SILENT >= MY_LOG_LEVEL
#   define MY_LOG_SILENT(fmt,...) \
        MY_LOG_PRINT(ANDROID_LOG_SILENT, fmt, ##__VA_ARGS__)
#else
#   define MY_LOG_SILENT(...) MY_LOG_NOOP
#endif

#if MY_LOG_LEVEL_FATAL >= MY_LOG_LEVEL
#   define MY_LOG_ASSERT(expression,fmt,...) \
        if (!(expression)) \
        { \
            __android_log_assert(#expression, MY_LOG_TAG, fmt, ##__VA_ARGS__);\
        }
#else
#   define MY_LOG_ASSERT(...) MY_LOG_NOOP
#endif


#define  LOGD(TAG, format, ...)  __android_log_print(ANDROID_LOG_INFO, TAG, format, __VA_ARGS__)
#define  LOGE(TAG, format, ...)  __android_log_print(ANDROID_LOG_ERROR, TAG, format, __VA_ARGS__)

#else //pc
#include <stdio.h>
#define LOGE(TAG, format, ...)  fprintf(stdout, TAG ": " format "\n", ##__VA_ARGS__)
#define LOGI(TAG, format, ...)  fprintf(stderr, TAG ": " format "\n", ##__VA_ARGS__)

#define MY_LOG_VERBOSE(...) MY_LOG_NOOP
#define MY_LOG_DEBUG(...) MY_LOG_NOOP
#define MY_LOG_INFO(...) MY_LOG_NOOP
#define MY_LOG_WARNING(...) MY_LOG_NOOP
#define MY_LOG_ERROR(...) MY_LOG_NOOP
#define MY_LOG_FATAL(...) MY_LOG_NOOP
#define MY_LOG_SILENT(...) MY_LOG_NOOP
#define MY_LOG_ASSERT(...) MY_LOG_NOOP

#endif  //ANDROID

void init_my_log();

