#include <jni.h>
#include <string>

extern "C" {
#include "ffmpeg_player.h"
#include "my_log.h"
int avio_dir_cmd_main(int argc, char *argv[]);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_testffmpeg_MainActivity_init(JNIEnv* env, jclass jcls) {
    init_my_log();
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_testffmpeg_MainActivity_stringFromJNI(JNIEnv* env, jobject jobj) {
    std::string hello = "Hello FFMPEG";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_testffmpeg_MainActivity_testListDir(JNIEnv* env, jobject jobj, jstring jstr_path) {
    const char *c_path = env->GetStringUTFChars(jstr_path, NULL);

    LOGD("FFMPEG", "test input path=%s", c_path);
    std::string opt = "list";
    char *argv[3] = {"test", "list", const_cast<char *>(c_path)};
    avio_dir_cmd_main(3, argv);

    env->ReleaseStringUTFChars(jstr_path, c_path);
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_testffmpeg_MainActivity_decode(JNIEnv* env, jobject jobj, jstring jstr_input, jstring jstr_output) {
    const char *c_input = env->GetStringUTFChars(jstr_input, NULL);
    const char *c_output = env->GetStringUTFChars(jstr_output, NULL);

    LOGD("FFMPEG_PLAYER", "native-lib:decode(%s, %s)", c_input, c_output);
    int ret = player_decode(c_input, c_output);

    env->ReleaseStringUTFChars(jstr_input, c_input);
    env->ReleaseStringUTFChars(jstr_output, c_output);
}
