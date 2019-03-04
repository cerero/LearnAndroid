#include <jni.h>
#include <string>
#include "h264_soft_decoder.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include "my_log.h"
}

#define COLOR_FORMAT_YUV420 0
#define COLOR_FORMAT_RGB565LE 1
#define COLOR_FORMAT_BGR32 2
//#define COLOR_FORMAT_RGBA32 3

class DecoderContext {
public:
    DecoderContext(jint color_format) {
        switch (color_format) {
            case COLOR_FORMAT_YUV420:
                color_format = PIX_FMT_YUV420P;
                break;
            case COLOR_FORMAT_RGB565LE:
                color_format = PIX_FMT_RGB565LE;
                break;
            case COLOR_FORMAT_BGR32:
                color_format = PIX_FMT_BGR32;
                break;
//            case COLOR_FORMAT_RGBA32:
//                color_format = PIX_FMT_RGBA;
//                break;
        }

        codec = avcodec_find_decoder(CODEC_ID_H264);
        codec_ctx = avcodec_alloc_context3(codec);

        codec_ctx->pix_fmt = PIX_FMT_YUV420P;
        codec_ctx->flags2 |= CODEC_FLAG2_CHUNKS;

        src_frame = av_frame_alloc();
        dst_frame = av_frame_alloc();

        avcodec_open2(codec_ctx, codec, NULL);
    }

    AVPixelFormat color_format;
    AVCodec *codec;
    AVCodecContext *codec_ctx;
    AVFrame *src_frame;
    AVFrame *dst_frame;
    SwsContext *convert_ctx;
    int frame_ready;

    ~DecoderContext() {
        if (codec_ctx)
            avcodec_close(codec_ctx);

        if (codec_ctx)
            av_free(codec_ctx);

        if (src_frame)
            av_free(src_frame);

        if (dst_frame)
            av_free(dst_frame);
    }

    static void set_ctx(JNIEnv *env, jobject thiz, DecoderContext *ctx) {
        jclass cls = env->GetObjectClass(thiz);
        jfieldID fid = env->GetFieldID(cls, "cdata", "I");
        env->SetIntField(thiz, fid, (jint)ctx);
    }

    static DecoderContext* get_ctx(JNIEnv *env, jobject thiz) {
        jclass cls = env->GetObjectClass(thiz);
        jfieldID fid = env->GetFieldID(cls, "cdata", "I");
        return (DecoderContext *)env->GetIntField(thiz, fid);
    }

};

void nativeInit(JNIEnv* env, jobject thiz, jint color_format);
void nativeDestroy(JNIEnv* env, jobject thiz);
jint consumeNalUnitsFromDirectBuffer(JNIEnv* env, jobject thiz, jobject nal_units, jint num_bytes, jlong pkt_pts);
jboolean isFrameReady(JNIEnv* env, jobject thiz);
jint getWidth(JNIEnv* env, jobject thiz);
jint getHeight(JNIEnv* env, jobject thiz);
jint getOutputByteSize(JNIEnv* env, jobject thiz);
jlong decodeFrameToDirectBuffer(JNIEnv* env, jobject thiz, jobject out_buffer);

void h264softdecoder::OnLoad(JNIEnv* env, void* reserved) {
    av_register_all();

    JNINativeMethod nm[8];
    nm[0].name = "nativeInit";
    nm[0].signature = "(I)V";
    nm[0].fnPtr = (void *)nativeInit;

    nm[1].name = "nativeDestroy";
    nm[1].signature = "()V";
    nm[1].fnPtr = (void *)nativeDestroy;

    nm[2].name = "consumeNalUnitsFromDirectBuffer";
    nm[2].signature = "(Ljava/nio/ByteBuffer;IJ)V";
    nm[2].fnPtr = (void *)consumeNalUnitsFromDirectBuffer;

    nm[3].name = "isFrameReady";
    nm[3].signature = "()Z";
    nm[3].fnPtr = (void *)isFrameReady;

    nm[4].name = "getWidth";
    nm[4].signature = "()I";
    nm[4].fnPtr = (void *)getWidth;

    nm[5].name = "getHeight";
    nm[5].signature = "()I";
    nm[5].fnPtr = (void *)getHeight;

    nm[6].name = "getOutputByteSize";
    nm[6].signature = "()I";
    nm[6].fnPtr = (void *)getOutputByteSize;

    nm[7].name = "decodeFrameToDirectBuffer";
    nm[7].signature = "(Ljava/nio/ByteBuffer;)J";
    nm[7].fnPtr = (void *)decodeFrameToDirectBuffer;
    jclass cls = env->FindClass("com/kugou/media/H264SoftDecoder");

    env->RegisterNatives(cls, nm, 8);
}

//JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
//
//}


void nativeInit(JNIEnv* env, jobject thiz, jint color_format) {
    DecoderContext *ctx = new DecoderContext(color_format);
//    D("Creating native H264 decoder context");
    DecoderContext::set_ctx(env, thiz, ctx);
}

void nativeDestroy(JNIEnv* env, jobject thiz) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);
//    D("Destroying native H264 decoder context");
    delete ctx;
}

jint consumeNalUnitsFromDirectBuffer(JNIEnv* env, jobject thiz, jobject nal_units, jint num_bytes, jlong pkt_pts) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);

    void *buf = NULL;
    if (nal_units == NULL) {
//        D("Received null buffer, sending empty packet to decoder");
    } else {
        buf = env->GetDirectBufferAddress(nal_units);
        if (buf == NULL) {
//            D("Error getting direct buffer address");
            return -1;
        }
    }

    AVPacket packet = {
            .data = (uint8_t*)buf,
            .size = num_bytes,
            .pts = pkt_pts
    };

    int frameFinished = 0;
    int res = avcodec_decode_video2(ctx->codec_ctx, ctx->src_frame, &frameFinished, &packet);
    LOGD("NativeSoftDecode", "consumeNalUnitsFromDirectBuffer frameFinished:%d", frameFinished);
    if (frameFinished)
        ctx->frame_ready = 1;

    return res;
}

jboolean isFrameReady(JNIEnv* env, jobject thiz) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);
    return ctx->frame_ready ? JNI_TRUE : JNI_FALSE;
}

jint getWidth(JNIEnv* env, jobject thiz) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);
    return ctx->codec_ctx->width;
}

jint getHeight(JNIEnv* env, jobject thiz) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);
    return ctx->codec_ctx->height;
}

jint getOutputByteSize(JNIEnv* env, jobject thiz) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);
    return avpicture_get_size(ctx->color_format, ctx->codec_ctx->width, ctx->codec_ctx->height);
}

jlong decodeFrameToDirectBuffer(JNIEnv* env, jobject thiz, jobject out_buffer) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);

    if (!ctx->frame_ready)
        return -1;

    void *out_buf = env->GetDirectBufferAddress(out_buffer);
    if (out_buf == NULL) {
//        D("Error getting direct buffer address");
        return -1;
    }

    long out_buf_len = env->GetDirectBufferCapacity(out_buffer);

    int pic_buf_size = avpicture_get_size(ctx->color_format, ctx->codec_ctx->width, ctx->codec_ctx->height);

    if (out_buf_len < pic_buf_size) {
//        D("Input buffer too small");
        return -1;
    }

    if (ctx->color_format == COLOR_FORMAT_YUV420) {
        memcpy(ctx->src_frame->data, out_buffer, pic_buf_size);
    } else {
        if (ctx->convert_ctx == NULL) {
            ctx->convert_ctx = sws_getContext(ctx->codec_ctx->width, ctx->codec_ctx->height, ctx->codec_ctx->pix_fmt,
                                              ctx->codec_ctx->width, ctx->codec_ctx->height, ctx->color_format, SWS_FAST_BILINEAR, NULL, NULL, NULL);
        }

        avpicture_fill((AVPicture*)ctx->dst_frame, (uint8_t*)out_buf, ctx->color_format, ctx->codec_ctx->width,
                       ctx->codec_ctx->height);

        sws_scale(ctx->convert_ctx, (const uint8_t**)ctx->src_frame->data, ctx->src_frame->linesize, 0, ctx->codec_ctx->height,
                  ctx->dst_frame->data, ctx->dst_frame->linesize);
    }

    ctx->frame_ready = 0;

    if (ctx->src_frame->pkt_pts == AV_NOPTS_VALUE) {
//        D("No PTS was passed from avcodec_decode!");
    }

    return ctx->src_frame->pkt_pts;
}
