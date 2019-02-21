package cookbook.chapter6.nativethreadsschedule;

import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jni_thread_scope();
//        jni_thread_get_priority_ranges();
//        jni_thread_fifo();
//        jni_thread_set_priority();
//        jni_process_setThreadPriority();
    }
    
    private static native void jni_thread_scope();
    private static native void jni_thread_get_priority_ranges();
    private static native void jni_thread_fifo();
    private static native void jni_thread_set_priority();
    private static native void jni_process_setThreadPriority();
    
    static {
        System.loadLibrary("NativeThreadsSchedule");
    }
}
