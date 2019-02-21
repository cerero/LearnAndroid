package cookbook.chapter10.framegrabber;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public abstract class BaseActivity extends SherlockFragmentActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.Theme_Sherlock_Light);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}
