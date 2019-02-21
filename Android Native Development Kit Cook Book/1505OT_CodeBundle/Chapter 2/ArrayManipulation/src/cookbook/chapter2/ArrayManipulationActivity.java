package cookbook.chapter2;

import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;

public class ArrayManipulationActivity extends Activity {
	private TextView tv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_array_manipulation);        
        tv = (TextView) findViewById(R.id.display_res);
        
//        callIllegalDirectAccessDemo();
//        callGetArrayLengthDemo();
//        callNewObjectArrayDemo();
//        callNewIntArrayDemo();
//        callGetSetObjectArrayDemo();
//        callGetReleaseIntArrayDemo();
//        callGetSetIntArrayRegionDemo();
        callGetReleasePrimitiveArrayCriticalDemo();
    }
    
    private void callIllegalDirectAccessDemo() {
    	int arr[] = new int[10];
        for (int i = 0; i < arr.length; ++i) {
        	arr[i] = i;
        }
        IllegalDirectAccessDemo(arr);
    }
    
    private void callGetArrayLengthDemo() {
    	 double darr[] = new double[10];
         Dummy dummyArr[] = new Dummy[20];
         int[][] twoDIArr = new int[30][40];
         GetArrayLengthDemo(darr, dummyArr, twoDIArr);
    }
    
    private void callNewObjectArrayDemo() {
    	Dummy dummy = new Dummy(5);
        Dummy[] dummies = NewObjectArrayDemo(dummy);
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < dummies.length; ++i) {
        	strBuf.append(dummies[i].getDummyValue()).append("\t");
        }
        tv.setText(strBuf.toString());
    }
    
    private void callNewIntArrayDemo() {
    	int[] results = NewIntArrayDemo();
    	StringBuffer strBuf = new StringBuffer();
    	for (int i = 0; i < results.length; ++i) {
    		strBuf.append(results[i]).append("\t");
    	}
    	tv.setText(strBuf.toString());
    }
    
    private void callGetSetObjectArrayDemo() {
    	Dummy[] dummies = new Dummy[3];
    	for (int i = 0; i < dummies.length; ++i) {
    		dummies[i] = new Dummy(i);
    	}
    	Dummy dummy = GetSetObjectArrayDemo(dummies, new Dummy(100));
    	StringBuffer strBuf = new StringBuffer();
    	for (int i = 0; i < dummies.length; ++i) {
    		strBuf.append(dummies[i].getDummyValue()).append("\n");
    	}
    	strBuf.append("returned dummy: ").append(dummy.getDummyValue()).append("\n");
    	tv.setText(strBuf.toString());
    }
    
    private void callGetReleaseIntArrayDemo() {
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append("Before native method call: ").append("\n");
    	int[] intArr = new int[5];
    	for (int i = 0; i < intArr.length; ++i) {
    		intArr[i] = i;
    		strBuf.append(intArr[i]).append("\t");
    	}
    	GetReleaseIntArrayDemo(intArr);
    	strBuf.append("\nAfter native method call: ").append("\n");
    	for (int i = 0; i < intArr.length; ++i) {
    		strBuf.append(intArr[i]).append("\t");
    	}
    	tv.setText(strBuf.toString());
    }
    
    private void callGetSetIntArrayRegionDemo() {
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append("Before native method call: ").append("\n");
    	int[] intArr = new int[5];
    	for (int i = 0; i < 5; ++i) {
    		intArr[i] = i;
    		strBuf.append(intArr[i]).append("\t");
    	}
    	GetSetIntArrayRegionDemo(intArr);
    	strBuf.append("\nAfter native method call: ").append("\n");
    	for (int i = 0; i < intArr.length; ++i) {
    		strBuf.append(intArr[i]).append("\t");
    	}
    	tv.setText(strBuf.toString());
    }
    
    private void callGetReleasePrimitiveArrayCriticalDemo() {
    	StringBuffer strBuf = new StringBuffer();
    	strBuf.append("Before native method call: ").append("\n");
    	int[] intArr = new int[5];
    	for (int i = 0; i < intArr.length; ++i) {
    		intArr[i] = i;
    		strBuf.append(intArr[i]).append("\t");
    	}
    	GetReleaseIntArrayDemo(intArr);
    	strBuf.append("\nAfter native method call: ").append("\n");
    	for (int i = 0; i < intArr.length; ++i) {
    		strBuf.append(intArr[i]).append("\t");
    	}
    	tv.setText(strBuf.toString());
    }
    
    static {
        System.loadLibrary("ArrayManipulation");
    }
    
    private native void IllegalDirectAccessDemo(int[] p);
    private native void GetArrayLengthDemo(double[] p1, Dummy[] p2, int[][] p3);
    private native Dummy[] NewObjectArrayDemo(Dummy p);
    private native int[] NewIntArrayDemo();
    private native Dummy GetSetObjectArrayDemo(Dummy[] p1, Dummy p2);
    private native void GetReleaseIntArrayDemo(int[] p1);
    private native void GetSetIntArrayRegionDemo(int[] p1);
    private native void GetReleasePrimitiveArrayCriticalDemo(int[] p1);
}
