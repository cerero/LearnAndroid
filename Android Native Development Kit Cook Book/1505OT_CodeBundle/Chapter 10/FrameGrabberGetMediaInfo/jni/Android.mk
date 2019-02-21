LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := framegrabber
LOCAL_SRC_FILES := framegrabber.c
#LOCAL_CFLAGS := -DANDROID_BUILD
LOCAL_LDLIBS := -llog -lz
LOCAL_STATIC_LIBRARIES := libavformat_static libavcodec_static libavutil_static 
#LOCAL_SHARED_LIBRARIES := libavformat_shared libavcodec_shared libavutil_shared 

include $(BUILD_SHARED_LIBRARY)
$(call import-module,ffmpeg-1.0.1/android/arm)


