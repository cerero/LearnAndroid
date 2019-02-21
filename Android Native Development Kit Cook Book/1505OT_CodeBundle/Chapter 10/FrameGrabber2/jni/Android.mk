LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := framegrabber
LOCAL_SRC_FILES := framegrabber.c
#LOCAL_CFLAGS := -DANDROID_BUILD
LOCAL_LDLIBS := -llog -ljnigraphics -lz 
LOCAL_STATIC_LIBRARIES := libavformat_static libavcodec_static libswscale_static libavutil_static
#LOCAL_ARM_NEON := true

include $(BUILD_SHARED_LIBRARY)
#$(call import-module,ffmpeg-1.0.1/android/armv5te)
#$(call import-module,ffmpeg-1.0.1/android/armv6)
#$(call import-module,ffmpeg-1.0.1/android/armv6vfp)
#$(call import-module,ffmpeg-1.0.1/android/armv7)
#$(call import-module,ffmpeg-1.0.1/android/armv7-neon)
$(call import-module,ffmpeg-1.0.1/android/armv7-neon-2)


