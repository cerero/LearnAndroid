package com.kugou.media;

import java.nio.ByteBuffer;

public class H264SoftDecoder {
    public static final int COLOR_FORMAT_YUV420 = 0;
    public static final int COLOR_FORMAT_RGB565LE = 1;
    public static final int COLOR_FORMAT_BGR32 = 2;

    public H264SoftDecoder(int colorFormat) {
        nativeInit(colorFormat);
    }

    protected void finalize() throws Throwable {
        nativeDestroy();
        super.finalize();
    }

    private int cdata;

    private native void nativeInit(int colorFormat);
    private native void nativeDestroy();

    public native void consumeNalUnitsFromDirectBuffer(ByteBuffer nalUnits, int numBytes, long packetPTS);
    public native boolean isFrameReady();
    public native int getWidth();
    public native int getHeight();
    public native int getOutputByteSize();
    public native long decodeFrameToDirectBuffer(ByteBuffer buffer);
    public native long getLastPTS();
}
