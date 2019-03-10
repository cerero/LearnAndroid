package com.kugou.media;

import android.app.Activity;
import android.support.annotation.NonNull;

public interface IGiftMp4Player {
    public void startGift(Activity activity, String localMp4ResPath, int loops, @NonNull EventCallBack callBack);
    public void addLoops(int loops);
    public void pauseGift();
    public void resumeGift();
    public void stopGift();
    public void setVisible(Boolean val);

    public void onActivityStop();
    public void onActivityResume();

    public interface EventCallBack {
        public static int ERROR_RES_NOT_EXIT = 0x000001;

        public void onActionFeedBack();
        public void onErrorOccur(int errorId);
        public void onStatusChange();
    }
}
