package com.kugou.util;

import android.util.Log;

public class LogWrapper {
    public static int MY_LOG_LEVEL_VERBOSE = 1;
    public static int MY_LOG_LEVEL_DEBUG = 2;
    public static int MY_LOG_LEVEL_INFO = 3;
    public static int MY_LOG_LEVEL_WARNING = 4;
    public static int MY_LOG_LEVEL_ERROR = 5;
    public static int MY_LOG_LEVEL_FATAL = 6;
    public static int MY_LOG_LEVEL_SILENT = 7;

    public static int LOG_LEVEL = MY_LOG_LEVEL_DEBUG;
    public static void LOGV(String TAG, String conteng) {
        if (MY_LOG_LEVEL_VERBOSE >= LOG_LEVEL)
            Log.v(TAG, conteng);
    }
    public static void LOGV(String TAG, String conteng, Throwable t) {
        if (MY_LOG_LEVEL_VERBOSE >= LOG_LEVEL)
            Log.v(TAG, conteng, t);
    }

    public static void LOGD(String TAG, String conteng) {
        if (MY_LOG_LEVEL_DEBUG >= LOG_LEVEL)
            Log.d(TAG, conteng);
    }

    public static void LOGD(String TAG, String conteng, Throwable t) {
        if (MY_LOG_LEVEL_DEBUG >= LOG_LEVEL)
            Log.d(TAG, conteng, t);
    }

    public static void LOGE(String TAG, String conteng) {
        if (MY_LOG_LEVEL_ERROR >= LOG_LEVEL)
            Log.e(TAG, conteng);
    }

    public static void LOGE(String TAG, String conteng, Throwable t) {
        if (MY_LOG_LEVEL_ERROR >= LOG_LEVEL)
            Log.e(TAG, conteng, t);
    }

    public static void LOGI(String TAG, String conteng) {
        if (MY_LOG_LEVEL_INFO >= LOG_LEVEL)
            Log.i(TAG, conteng);
    }

    public static void LOGI(String TAG, String conteng, Throwable t) {
        if (MY_LOG_LEVEL_INFO >= LOG_LEVEL)
            Log.i(TAG, conteng, t);
    }

    public static void LOGW(String TAG, String conteng) {
        if (MY_LOG_LEVEL_WARNING >= LOG_LEVEL)
            Log.w(TAG, conteng);
    }

    public static void LOGW(String TAG, Throwable t) {
        if (MY_LOG_LEVEL_WARNING >= LOG_LEVEL)
            Log.w(TAG, t);
    }

    public static void LOGW(String TAG, String conteng, Throwable t) {
        if (MY_LOG_LEVEL_WARNING >= LOG_LEVEL)
            Log.w(TAG, conteng, t);
    }
}
