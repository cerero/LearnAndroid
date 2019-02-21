package cookbook.chapter2;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class PassingPrimitiveActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passing_primitive);
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("boolean: ").append(passBooleanReturnBoolean(false)).append(System.getProperty("line.separator"))
        	.append("byte: ").append(passByteReturnByte((byte)10)).append(System.getProperty("line.separator"))
        	.append("char: ").append(passCharReturnChar('#')).append(System.getProperty("line.separator"))
        	.append("short: ").append(passShortReturnShort((short)100)).append(System.getProperty("line.separator"))
        	.append("int: ").append(passIntReturnInt(1000)).append(System.getProperty("line.separator"))
        	.append("long: ").append(passLongReturnLong(4294967396L)).append(System.getProperty("line.separator"))
        	.append("float: ").append(passFloatReturnFloat(1.11f)).append(System.getProperty("line.separator"))
        	.append("double: ").append(passDoubleReturnDouble(11.11)).append(System.getProperty("line.separator"));
        TextView tv = (TextView) findViewById(R.id.display_res);
        tv.setText(strBuilder.toString());
    }
    private native boolean passBooleanReturnBoolean(boolean p);
    private native byte passByteReturnByte(byte p);
    private native char passCharReturnChar(char p);
    private native short passShortReturnShort(short p);
    private native int passIntReturnInt(int p);
    private native long passLongReturnLong(long p);
    private native float passFloatReturnFloat(float p);
    private native double passDoubleReturnDouble(double p);
    
    static {
        System.loadLibrary("PassingPrimitive");
    }
}
