//封装格式
#include "libavformat/avformat.h"
//解码
#include "libavcodec/avcodec.h"


int player_decode(const char *inputStr, const char *outStr) {
    av_register_all();
    return 0;
}