package com.kugou.media;

import java.nio.ByteBuffer;

public interface IYUVDataReceiver {
    void onYUVData(ByteBuffer yuvData, int frameWidth, int frameHeight, int outputSize);
}
