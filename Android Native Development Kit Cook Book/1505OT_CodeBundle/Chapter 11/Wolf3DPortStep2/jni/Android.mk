LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE:= libwolf3dport
LOCAL_CFLAGS := -DANDROID
LOCAL_SRC_FILES := objs.c misc.c id_ca.c id_vh.c id_us.c \
wl_act1.c wl_act2.c wl_act3.c wl_agent.c wl_game.c \
wl_inter.c wl_menu.c wl_play.c wl_state.c wl_text.c wl_main.c \
wl_debug.c vi_comm.c sd_comm.c \
wl_draw.c jni_wolf3dport.c vi_android.c sd_null.c
#LOCAL_STATIC_LIBRARIES := 
LOCAL_LDLIBS := -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)

