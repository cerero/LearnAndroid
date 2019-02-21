package cookbook.chapter2;

import java.nio.charset.Charset;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ManagingReferenceActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managing_reference);
        
        localReference("hello local ref", true);
        localReference("hello local ref", false);
        globalReference("hello global ref", false);
        globalReference("hello global ref 2", true);
        globalReference("hello global ref 3", true);
        referenceAssignmentAndNew("hello ndk");
        
//        weakReference("hello weak ref", false);
//        weakReference("hello weak ref 2", true);
//        weakReference("hello weak ref 3", true);
    }

    private native void referenceAssignmentAndNew(String p);
    private native void localReference(String p, boolean p1);
    private native void globalReference(String p, boolean p1);
    private native void weakReference(String p, boolean p1);
    
    static {
        System.loadLibrary("ReferenceManage");
    }
    
}
