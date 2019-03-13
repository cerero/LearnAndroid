#include <jni.h>
#include <string>
#include "h264_soft_decoder.h"

JNIEXPORT jint JNI_OnLoad(JavaVM* pVm, void* reserved){
    JNIEnv* env;
    if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
//    h264softdecoder::OnLoad(env, reserved, "com/kugou/fanxing/allinone/watch/gift/core/view/mp4/media/producer/MediaContentProducer/GiftMp4Player");
    h264softdecoder::OnLoad(env, reserved, "com/kugou/media/H264SoftDecoder");
    return JNI_VERSION_1_6;
}