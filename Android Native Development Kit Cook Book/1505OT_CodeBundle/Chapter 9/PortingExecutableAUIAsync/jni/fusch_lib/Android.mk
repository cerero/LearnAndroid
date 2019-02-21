LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libseamcarv
LOCAL_SRC_FILES :=\
	sc_core.c  \
	sc_carve.c  \
	sc_color.c  \
	sc_shift.c \
	sc_mgmnt.c \
	seamcarv.c
	
LOCAL_CFLAGS := -std=c99
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)

include $(BUILD_STATIC_LIBRARY)
