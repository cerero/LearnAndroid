#include <jni.h>
#include <stdint.h>
#include "h264_soft_decoder.h"

#include "my_log.h"
extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
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

        external_dir = NULL;
        total_decode_frame = 0;
        frame_ready = 0;
        codec = avcodec_find_decoder(AV_CODEC_ID_H264);//CODEC_ID_H264
        codec_ctx = avcodec_alloc_context3(codec);

        codec_ctx->pix_fmt = AV_PIX_FMT_YUV420P;
        codec_ctx->flags2 |= CODEC_FLAG2_CHUNKS;

        src_frame = av_frame_alloc();
        dst_frame = av_frame_alloc();

        avcodec_open2(codec_ctx, codec, NULL);
//        LOGD(TAG, "DecoderContext construct 0x%x", this);
    }

    AVPixelFormat color_format;
    AVCodec *codec;
    AVCodecContext *codec_ctx;
    AVFrame *src_frame;
    AVFrame *dst_frame;
    SwsContext *convert_ctx;
    int frame_ready;
    int64_t pkt_pts;
    int32_t total_decode_frame;

    uint64_t current_write_size;
    int new_y_width;
    int new_u_width;
    int new_v_width;

    char *external_dir;
    ~DecoderContext() {
        if (src_frame) {
//            av_frame_unref(src_frame);
            av_frame_free(&src_frame);
            src_frame = NULL;
        }

        if (dst_frame) {
            av_frame_free(&dst_frame);
            dst_frame = NULL;
        }

        if (codec_ctx) {
            avcodec_free_context(&codec_ctx);
            codec_ctx = NULL;
        }

        if (external_dir)
            delete external_dir;

//        LOGD(TAG, "DecoderContext release 0x%x", this);
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

//void nativeInit(JNIEnv* env, jobject thiz, jint color_format);
void nativeInit(JNIEnv* env, jobject thiz, jint color_format, jstring external_path);
void nativeDestroy(JNIEnv* env, jobject thiz);
jint consumeNalUnitsFromDirectBuffer(JNIEnv* env, jobject thiz, jobject nal_units, jint num_bytes, jlong pkt_pts);
jboolean isFrameReady(JNIEnv* env, jobject thiz);
jint getWidth(JNIEnv* env, jobject thiz);
jint getHeight(JNIEnv* env, jobject thiz);
jint getOutputByteSize(JNIEnv* env, jobject thiz);
jlong getLastPTS(JNIEnv* env, jobject thiz);
jlong decodeFrameToDirectBuffer(JNIEnv* env, jobject thiz, jobject out_ybuffer, jobject out_ubuffer, jobject out_vbuffer);
//void getWriteInfoAfterDecode(JNIEnv* env, jobject thiz, jlongArray outInfo);

int _CopyColorComponents( unsigned char* dst, unsigned char* src, int linesize, int width, int height);

void h264softdecoder::OnLoad(JNIEnv* env, void* reserved, const char* register_class_path) {
    av_register_all();

    JNINativeMethod nm[9];

    nm[0].name = "nativeInit";
    nm[0].signature = "(ILjava/lang/String;)V";
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
    nm[7].signature = "(Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)J";
    nm[7].fnPtr = (void *)decodeFrameToDirectBuffer;

    nm[8].name = "getLastPTS";
    nm[8].signature = "()J";
    nm[8].fnPtr = (void *)getLastPTS;

//    nm[9].name = "getWriteSizeAfterDecode";
//    nm[9].signature = "([J)V";
//    nm[9].fnPtr = (void *)getWriteInfoAfterDecode;

    jclass cls = env->FindClass(register_class_path);

    env->RegisterNatives(cls, nm, 9);
}

static void save_raw_yuv(uint8_t *yBuf, uint8_t *uBuf, uint8_t *vBuf,
                     int ySize, int uSize, int vSize,
                     char *filename)
{
//    MY_LOG_DEBUG("ybuf addr:%x, ySize:%d", yBuf, ySize);
//    MY_LOG_DEBUG("uBuf addr:%x, uSize:%d", uBuf, uSize);
//    MY_LOG_DEBUG("vBuf addr:%x, vSize:%d", vBuf, vSize);

    FILE *f;
    int i;
    f = fopen(filename,"wb");
    if (f) {
        fwrite(yBuf, 1, ySize, f);
        fwrite(uBuf, 1, uSize, f);
        fwrite(vBuf, 1, vSize, f);
        fclose(f);
    } else {
//        MY_LOG_DEBUG("file %s can not write!!!", filename);
    }
}


void nativeInit(JNIEnv* env, jobject thiz, jint color_format, jstring external_dir) {
    DecoderContext *ctx = new DecoderContext(color_format);

//    const char* c_external_dir = env->GetStringUTFChars(external_dir, NULL);
//    ctx->external_dir = (char *)malloc(sizeof(char) * (strlen(c_external_dir) + 1));
//    strcpy(ctx->external_dir, c_external_dir);
//    env->ReleaseStringUTFChars(external_dir, c_external_dir);

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
        ctx->total_decode_frame ++;
    }
//    LOGD(TAG, "consumeNalUnitsFromDirectBuffer got_picture:%d, size cosumed:%d, total decode:%d", got_picture, ret, ctx->total_decode_frame);
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

//void getWriteInfoAfterDecode(JNIEnv* env, jobject thiz, jlongArray outInfo) {
//    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);
//    jlong* out_info = (jlong*) env->GetLongArrayElements(outInfo, NULL);
//
//    out_info[0] = ctx->current_write_size;
//    out_info[1] = ctx->new_y_width;
//    out_info[2] = ctx->new_u_width;
//    out_info[3] = ctx->new_v_width;
//
//    env->ReleaseLongArrayElements(outInfo, out_info, 0);
//}

jlong decodeFrameToDirectBuffer(JNIEnv* env, jobject thiz, jobject out_ybuffer, jobject out_ubuffer, jobject out_vbuffer) {
    DecoderContext *ctx = (DecoderContext *)DecoderContext::get_ctx(env, thiz);

    if (!ctx->frame_ready)
        return -1;

    jbyte *out_ybuf = (jbyte *)env->GetDirectBufferAddress(out_ybuffer);
    jbyte *out_ubuf = (jbyte *)env->GetDirectBufferAddress(out_ubuffer);
    jbyte *out_vbuf = (jbyte *)env->GetDirectBufferAddress(out_vbuffer);

    if (out_ybuf == NULL || out_ubuf == NULL || out_vbuf == NULL) {
//        LOGE(TAG, "Error getting direct buffer address", 1);
        return -1;
    }

    long out_ybuf_len = env->GetDirectBufferCapacity(out_ybuffer);
    long out_ubuf_len = env->GetDirectBufferCapacity(out_ubuffer);
    long out_vbuf_len = env->GetDirectBufferCapacity(out_vbuffer);
    long total_out_len = out_ybuf_len + out_ubuf_len + out_vbuf_len;
//    int pic_buf_size = av_image_get_buffer_size(ctx->color_format, ctx->codec_ctx->width, ctx->codec_ctx->height, 0);
    int pic_buf_size = avpicture_get_size(ctx->color_format, ctx->codec_ctx->width, ctx->codec_ctx->height);
    if (total_out_len < pic_buf_size) {
//        LOGE(TAG, "Input buffer size:%ld too small, couldn't decode to direct buffer, need size:%d", total_out_len, pic_buf_size);
        return -1;
    }

    /**
     * ffmpeg解码得到的AVFrame里面有data数组和linesize数组，
     * data[0]是Y平面数据，其大小是linesize[0]，
     * data[1]是U,大小linesize[1]，
     * data[2]是V平面数据大小linesize[2]，
     * **/
    _CopyColorComponents((unsigned char*)out_ybuf, ctx->src_frame->data[0], ctx->src_frame->linesize[0], ctx->src_frame->width, ctx->src_frame->height);
    _CopyColorComponents((unsigned char*)out_ubuf, ctx->src_frame->data[1], ctx->src_frame->linesize[1], ctx->src_frame->width / 2, ctx->src_frame->height / 2);
    _CopyColorComponents((unsigned char*)out_vbuf, ctx->src_frame->data[2], ctx->src_frame->linesize[2], ctx->src_frame->width / 2, ctx->src_frame->height / 2);
        /*
        //写入Y数据
        memcpy(out_buf, ctx->src_frame->data[0], ctx->codec_ctx->height * ctx->src_frame->linesize[0]);
        out_buf += ctx->codec_ctx->height * ctx->src_frame->linesize[0];

        //写入U数据
        memcpy(out_buf, ctx->src_frame->data[1], ctx->codec_ctx->height / 2 * ctx->src_frame->linesize[1]);
        out_buf += ctx->codec_ctx->height / 2 * ctx->src_frame->linesize[1];

        //写入V数据
        memcpy(out_buf, ctx->src_frame->data[2], ctx->codec_ctx->height / 2 * ctx->src_frame->linesize[2]);
        out_buf += ctx->codec_ctx->height / 2 * ctx->src_frame->linesize[2];
        */
//        LOGD(TAG, "write yuv to out_buf 0x%x", out_buf);
//        char path[2048] = {0};
//        sprintf(path, "%s/%d.yuv420p", ctx->external_dir, ctx->total_decode_frame);
//        LOGD(TAG, "write to %s", path);
//
//        save_raw_yuv((uint8_t *)out_ybuf,
//                     (uint8_t *)out_ubuf,
//                     (uint8_t *)out_vbuf,
//                     out_ybuf_len, out_ubuf_len, out_vbuf_len,
//                     path);

//        LOGD(TAG, "写入yuv完毕 out_buf_end=%x, out_buf=%x", out_buf_end, out_buf);

    av_frame_unref(ctx->src_frame);
    ctx->frame_ready = 0;

//    if (ctx->src_frame->pkt_pts == AV_NOPTS_VALUE) {
//        D("No PTS was passed from avcodec_decode!");
//    }

    return ctx->src_frame->pkt_pts;
}

int _CopyColorComponents( unsigned char* dst, unsigned char* src, int linesize, int width, int height)
{
    if( width > linesize )
    {
        width = linesize;
    }
    int i = 0;
    for( i = 0; i < height; i++ )
    {
        memcpy( dst, src, width );

        dst += width;
        src += linesize;
    }
    return 0;
}
