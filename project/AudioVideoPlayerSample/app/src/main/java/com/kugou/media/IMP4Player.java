package com.kugou.media;

import android.app.Activity;
import android.support.annotation.NonNull;

public interface IMP4Player {

    /**触发播放动画逻辑,播放完毕会触发 STATE_FINISHING 状态回调
     * @param localMp4ResPath 本地资源路径
     * @param loops 播放的循环次数
     * **/
    public void start(String localMp4ResPath, int loops, EventCallBack callBack);

    /**追加loop次数(用于礼物连接的次数最加)
     * **/
    public void addLoops(int loops);

    /**强制停止当前在播的效果，会收到 STATE_FINISHING 状态回调
     * **/
    public void stop();

    /**调用方确认状态改变***/
    public void confirmStatus(int status);

    /**设置可视化**/
    public void setVisible(Boolean val);

    /**当前activity生命周期的onStop回调，请执行这个方法
     * 如果当前有在播的效果，会触发 STATE_FINISHING 状态回调
     * **/
    public void onActivityStop();

    /**当前activity生命周期的onResume回调，请执行这个方法**/
    public void onActivityResume();

    /**销毁player实例**/
    public void release();

    public interface EventCallBack {
        /**本地mp4资源不存在**/
        public static final int ERROR_RES_NOT_EXIT = 0x000001;
        /**vertext shader或fragment shader编译失败**/
        public static final int ERROR_SHADER_FAIL = 0x00002;
        /**opengl相关错误**/
        public static final int ERROR_GL_ERROR = 0x00003;
        /**视频编码相关错误**/
        public static final int ERROR_VIDEO_CODEC_ERROR = 0x00004;
        /**视频解码相关错误**/
        public static final int ERROR_VIDEO_DECODE_ERROR = 0x00005;
        /**视频渲染错误**/
        public static final int ERROR_VIDEO_RENDER = 0x00006;
        /**操作错误**/
        public static final int ERROR_WRONG_ACTION = 0x00007;

        public void onErrorOccur(int errorId, String desc);

        public static final int STATE_NONE = 0;

        /**调用startGift()后的状态变更(该状态下还可以继续addloop)**/
        public static final int STATE_START = 1;

        /**播放完loop次后状态变更(该状态下还可以继续addloop)**/
        public static final int STATE_FINISHING = 2;

        /**调用方确认finishing状态后，跟着player流转到finished**/
        public static final int STATE_FINISHED = 3;

        public void onStatusChange(int status);
    }
}
