#ifdef ANDROID
#include <android/log.h>
#ifndef LOG_TAG
#define LOG_TAG   "FFMPEG"
#endif
#define  XLOGD(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  XLOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
#include <stdio.h>
#define XLOGE(format, ...)  fprintf(stdout, LOG_TAG ": " format "\n", ##__VA_ARGS__)
#define XLOGI(format, ...)  fprintf(stderr, LOG_TAG ": " format "\n", ##__VA_ARGS__)
#endif  //ANDROID

#include <jni.h>
#include <string>
#include "android/log.h"


extern "C" {
#include "libavutil/log.h"
int avio_dir_cmd_main(int argc, char *argv[]);
}

static void log_callback_null(void *ptr, int level, const char *fmt, va_list vl)
{
    static int print_prefix = 1;
    static int count;
    static char prev[1024];
    char line[1024];
    static int is_atty;

    av_log_format_line(ptr, level, fmt, vl, line, sizeof(line), &print_prefix);

    strcpy(prev, line);
    //sanitize((uint8_t *)line);

    if (level <= AV_LOG_WARNING)
    {
        XLOGE("%s", line);
    }
    else
    {
        XLOGD("%s", line);
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_testffmpeg_MainActivity_stringFromJNI(JNIEnv* env, jobject jobj) {
    std::string hello = "Hello FFMPEG";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_testffmpeg_MainActivity_testListDir(JNIEnv* env, jobject jobj, jstring jstr_path) {
    const char *c_path = env->GetStringUTFChars(jstr_path, NULL);
    av_log_set_callback(log_callback_null);
    XLOGD("input path=%s", c_path);
    std::string opt = "list";
    char *argv[3] = {"test", "list", const_cast<char *>(c_path)};
    avio_dir_cmd_main(3, argv);
}
