LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := framegrabber
LOCAL_SRC_FILES := framegrabber.c
#LOCAL_CFLAGS := -DANDROID_BUILD
LOCAL_LDLIBS := -llog -ljnigraphics -lz 
LOCAL_STATIC_LIBRARIES := libavformat_static libavcodec_static libswscale_static libavutil_static

include $(BUILD_SHARED_LIBRARY)
$(call import-module,ffmpeg-1.0.1/android/arm)


