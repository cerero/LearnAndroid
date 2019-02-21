#include <jni.h>
#include <string>

extern "C" {
#include "ffmpeg_player.h"
#include "my_log.h"
#include "avio_dir_cmd.h"
#include "avio_reading.h"
#include "decode_video.h"

int demuxing_and_decode_main (int argc, char **argv);
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


void decode(JNIEnv* env, jobject jobj, jstring jstr_input, jstring jstr_video_output, jstring jstr_audio_output) {
    const char *c_input = env->GetStringUTFChars(jstr_input, NULL);
    const char *c_video_output = env->GetStringUTFChars(jstr_video_output, NULL);
    const char *c_audio_output = env->GetStringUTFChars(jstr_audio_output, NULL);

    LOGD("FFMPEG_PLAYER", "native-lib:demuxing_and_decode_main(input: %s\nvideo_output: %s\naudio_output:%s)", c_input, c_video_output, c_audio_output);

    char* argv[] = {"demuxing_and_decode_main", "-refcount", const_cast<char *>(c_input), const_cast<char *>(c_video_output), const_cast<char *>(c_audio_output)};
    demuxing_and_decode_main(5, argv);

    env->ReleaseStringUTFChars(jstr_input, c_input);
    env->ReleaseStringUTFChars(jstr_video_output, c_video_output);
    env->ReleaseStringUTFChars(jstr_audio_output, c_audio_output);
}

//jint add(JNIEnv* env, jobject jobj, jint pa, jint pb) {
//    return pa + pb;
//}
//
//JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* pVm, void *reserved) {
//    JNIEnv* env;
//
//    if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6)) {
//        return -1;
//    }
//
//    JNINativeMethod nm[1];
//    nm[0].name = "add";
//    nm[0].signature = "(II)I";
//    nm[0].fnPtr = (void *)add;
//
//    jclass cls = env->FindClass("com/example/testffmpeg/GiftTestActivity");
//    env->RegisterNatives(cls, nm, 1);
//
//    init_my_log();
//    return JNI_VERSION_1_6;
//}
