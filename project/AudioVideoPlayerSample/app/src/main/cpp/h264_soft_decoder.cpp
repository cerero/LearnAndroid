#include <jni.h>
#include <string>
#include <cstdint>
#include "h264_soft_decoder.h"

extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include "my_log.h"
}

#define COLOR_FORMAT_YUV420 0
#define COLOR_FORMAT_RGB565LE 1
#define COLOR_FORMAT_BGR32 2

static char TAG[] = "NativeSoftDecode";

class DecoderContext {
public:
    DecoderContext(jint color_format) {
        switch (color_format) {
            case COLOR_FORMAT_YUV420:
                this->color_format = AV_PIX_FMT_YUV420P;
                break;
            case COLOR_FORMAT_RGB565LE:
                this->color_format = AV_PIX_FMT_RGB565LE;
                break;
            case COLOR_FORMAT_BGR32:
                this->color_format = AV_PIX_FMT_RGB32;
                break;
        }

        frame_ready = 0;
        codec = avcodec_find_decoder(AV_CODEC_ID_H264);//CODEC_ID_H264
        codec_ctx = avcodec_alloc_context3(codec);

        codec_ctx->pix_fmt = AV_PIX_FMT_YUV420P;
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
    int64_t pkt_pts;

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
jlong getLastPTS(JNIEnv* env, jobject thiz);
jlong decodeFrameToDirectBuffer(JNIEnv* env, jobject thiz, jobject out_buffer);

void h264softdecoder::OnLoad(JNIEnv* env, void* reserved) {
    av_register_all();

    JNINativeMethod nm[9];

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

    nm[8].name = "getLastPTS";
    nm[8].signature = "()J";
    nm[8].fnPtr = (void *)getLastPTS;

    jclass cls = env->FindClass("com/kugou/media/H264SoftDecoder");

    env->RegisterNatives(cls, nm, 9);
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
    ctx->pkt_pts = pkt_pts;

    int got_picture = 0;
    ctx->frame_ready = 0;
    int ret = avcodec_decode_video2(ctx->codec_ctx, ctx->src_frame, &got_picture, &packet);
    if (ret > 0 && got_picture > 0) {
        ctx->frame_ready = 1;
    }

    LOGD(TAG, "consumeNalUnitsFromDirectBuffer got_picture:%d, size cosumed:%d, frame ready:%d", got_picture, ret, ctx->frame_ready);
    return ret;
}

jboolean isFrameReady(JNIEnv* env, jobject thiz) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);
    return ctx->frame_ready ? JNI_TRUE : JNI_FALSE;
}

jlong getLastPTS(JNIEnv* env, jobject thiz) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);
    return ctx->pkt_pts;
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
//    return av_image_get_buffer_size(ctx->color_format, ctx->codec_ctx->width, ctx->codec_ctx->height, 0);
    return avpicture_get_size(ctx->color_format, ctx->codec_ctx->width, ctx->codec_ctx->height);
}

jlong decodeFrameToDirectBuffer(JNIEnv* env, jobject thiz, jobject out_buffer) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);

    if (!ctx->frame_ready)
        return -1;

    jbyte *out_buf = (jbyte *)env->GetDirectBufferAddress(out_buffer);

    if (out_buf == NULL) {
//        D("Error getting direct buffer address");
        return -1;
    }

    long out_buf_len = env->GetDirectBufferCapacity(out_buffer);

//    int pic_buf_size = av_image_get_buffer_size(ctx->color_format, ctx->codec_ctx->width, ctx->codec_ctx->height, 0);
    int pic_buf_size = avpicture_get_size(ctx->color_format, ctx->codec_ctx->width, ctx->codec_ctx->height);
//    LOGD(TAG, "decodeFrameToDirectBuffer-> avpicture_get_size=%d, out_buf_len=%ld", pic_buf_size, out_buf_len);
    if (out_buf_len < pic_buf_size) {
        LOGD(TAG, "Input buffer size:%ld too small, couldn't decode to direct buffer, need size:%d", out_buf_len, pic_buf_size);
        return -1;
    }

    jbyte *out_buf_end = out_buf + out_buf_len;
    if (ctx->color_format == AV_PIX_FMT_YUV420P) {
//        memcpy(ctx->src_frame->data, out_buffer, pic_buf_size);

        /**
         * ffmpeg解码得到的AVFrame里面有data数组和linesize数组，
         * data[0]是Y平面数据，其大小是linesize[0]，
         * data[1]是U,大小linesize[1]，
         * data[2]是V平面数据大小linesize[2]，
         * **/
        int i;
        //写入Y数据
        for (i = 0; i < ctx->codec_ctx->height; i++) {
            memcpy(out_buf, ctx->src_frame->data[0] + i * ctx->src_frame->linesize[0], ctx->src_frame->linesize[0]);
            out_buf += ctx->src_frame->linesize[0];
        }
        //写入U数据
        for (i = 0; i < ctx->codec_ctx->height / 2; i++) {
            memcpy(out_buf, ctx->src_frame->data[1] + i * ctx->src_frame->linesize[1], ctx->src_frame->linesize[1]);
            out_buf += ctx->src_frame->linesize[1];
        }
        //写入V数据
        for (i = 0; i < ctx->codec_ctx->height / 2; i++) {
            memcpy(out_buf, ctx->src_frame->data[2] + i * ctx->src_frame->linesize[2], ctx->src_frame->linesize[2]);
            out_buf += ctx->src_frame->linesize[2];
        }
//        LOGD(TAG, "写入yuv完毕 out_buf_end=%x, out_buf=%x", out_buf_end, out_buf);
    } else {
        if (ctx->convert_ctx == NULL) {
            ctx->convert_ctx = sws_getContext(ctx->codec_ctx->width, ctx->codec_ctx->height, ctx->codec_ctx->pix_fmt,
                                              ctx->codec_ctx->width, ctx->codec_ctx->height, ctx->color_format, SWS_BILINEAR, NULL, NULL, NULL);//SWS_BILINEAR SWS_FAST_BILINEAR
        }

        avpicture_fill((AVPicture*)ctx->dst_frame, (uint8_t*)out_buf, ctx->color_format,
                  ctx->codec_ctx->width, ctx->codec_ctx->height);

        int out_h = sws_scale(ctx->convert_ctx, (const uint8_t * const*)ctx->src_frame->data,
                  ctx->src_frame->linesize, 0, 0,//ctx->codec_ctx->height,
                  ctx->dst_frame->data, ctx->dst_frame->linesize);

        LOGD(TAG, "width:%d, height:%d, out_h:%d, src pix_fmt:%s ,dst pix_fmt:%s, pic_buf_size:%d", ctx->codec_ctx->width, ctx->codec_ctx->height, out_h, av_get_pix_fmt_name(ctx->codec_ctx->pix_fmt), av_get_pix_fmt_name(ctx->color_format), pic_buf_size);
        LOGD(TAG, "ctx->dst_frame->linesize[0]: %d, x%d = %d", ctx->dst_frame->linesize[0], ctx->codec_ctx->height, ctx->codec_ctx->height * ctx->dst_frame->linesize[0]);
    }

    ctx->frame_ready = 0;

    if (ctx->src_frame->pkt_pts == AV_NOPTS_VALUE) {
//        D("No PTS was passed from avcodec_decode!");
    }

    return ctx->src_frame->pkt_pts;
}
