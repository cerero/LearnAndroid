package com.kugou.media;

import android.os.Environment;

import com.kugou.util.LogWrapper;

import java.io.File;
import java.nio.ByteBuffer;

public class H264SoftDecoder {
//    static {
//        System.loadLibrary("fdk-aac");
//        System.loadLibrary("mp3lame");
//        System.loadLibrary("x264");
//        System.loadLibrary("rtmp");
//        System.loadLibrary("ffmpeg");
//
//        System.loadLibrary("mini_yuv_decoder");
//    }

    public static final int COLOR_FORMAT_YUV420 = 0;
    public static final int COLOR_FORMAT_RGB565LE = 1;
    public static final int COLOR_FORMAT_BGR32 = 2;

    private static boolean hasInitLibrary = false;
    private static boolean hasInitSuccess = false;

    public H264SoftDecoder() {
    }

    public boolean initColorFormat(int colorFormat) {
        String external_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "myyuv";
        if (!hasInitLibrary) {
            hasInitLibrary = true;
            hasInitSuccess = true;
            try {
                System.loadLibrary("fdk-aac");
                System.loadLibrary("mp3lame");
                System.loadLibrary("x264");
                System.loadLibrary("rtmp");
                System.loadLibrary("ffmpeg");

                System.loadLibrary("mini_yuv_decoder2");
            } catch (Exception e) {
                LogWrapper.LOGE("H264SoftDecoder", e.toString());
            }
        }

        if (hasInitSuccess) {
            nativeInit(colorFormat, external_dir);
        }
        return hasInitSuccess;
    }

//    protected void finalize() throws Throwable {
//        nativeDestroy();
//        super.finalize();
//    }

    private int cdata;
    private native void nativeInit(int colorFormat, String externalDir);
//    private native void nativeInit(int colorFormat);
    public native void nativeDestroy();

    public native void consumeNalUnitsFromDirectBuffer(ByteBuffer nalUnits, int numBytes, long packetPTS);
    public native boolean isFrameReady();
    public native int getWidth();
    public native int getHeight();
    public native int getOutputByteSize();
    public native long decodeFrameToDirectBuffer(ByteBuffer ybuffer, ByteBuffer ubuffer, ByteBuffer vbuffer);
    public native long getLastPTS();
//    public native void getWriteInfoAfterDecode(long info[]);
}
