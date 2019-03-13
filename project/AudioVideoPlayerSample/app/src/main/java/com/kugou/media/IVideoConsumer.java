package com.kugou.media;

import android.view.Surface;

import java.nio.ByteBuffer;

public interface IVideoConsumer {
    /**
     * @param mode 1 - 使用硬解渲染模式  2 - 使用软解渲染模式(onYUVData会被调用)
     * **/
    void choseRenderMode(int mode);

    /**软解的yuv数据回调**/
    void onYUVData(ByteBuffer yuvData, int frameWidth, int frameHeight);

    void onTextureInfo(int imgWidth, int imgHeight);
    /**没数据可以渲染了**/
    void end();
    void start();
    /**销毁**/
    void release();
    /**产生一个用于硬解输出的surface**/
    Surface generateHardWareOutputSurface();
}
