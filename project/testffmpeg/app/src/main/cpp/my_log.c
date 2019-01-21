#include "my_log.h"
#include "libavutil/log.h"

static void log_callback_null(void *ptr, int level, const char *fmt, va_list vl)
{
    static int print_prefix = 1;
    static int count;
    static char prev[1024];
    char line[1024];
    static int is_atty;

    av_log_format_line(ptr, level, fmt, vl, line, sizeof(line), &print_prefix);

    strcpy(prev, line);

    if (level <= AV_LOG_WARNING)
    {
        LOGE("FFMPEG", "%s", line);
    }
    else
    {
        LOGD("FFMPEG", "%s", line);
    }
}

void init_my_log() {
    av_log_set_callback(log_callback_null);
}
