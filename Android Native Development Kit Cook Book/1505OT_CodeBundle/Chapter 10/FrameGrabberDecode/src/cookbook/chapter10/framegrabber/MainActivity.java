package cookbook.chapter10.framegrabber;

import java.io.File;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements
	ActionBar.TabListener,
	ViewPager.OnPageChangeListener {

	private FileBrowserFragment mFileBrowserFragment;
	private VideoLibraryFragment mVideoLibraryFragment;
	
    private ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		if (mViewPager != null) {
			mViewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
            mViewPager.setOnPageChangeListener(this);
            mViewPager.setPageMarginDrawable(R.drawable.grey_border_inset_lr);
            mViewPager.setPageMargin(getResources()
                    .getDimensionPixelSize(R.dimen.page_margin_width));
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.addTab(actionBar.newTab()
                    .setText("Library")
                    .setTabListener(this));
            actionBar.addTab(actionBar.newTab()
                    .setText("Browser")
                    .setTabListener(this));
		}
		//create a directory to save snapped frames
		String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
        	Toast.makeText(getApplicationContext(), "external storage read only", Toast.LENGTH_LONG).show();
            return;
        }
        if (status.equals(Environment.MEDIA_SHARED)) {
        	Toast.makeText(getApplicationContext(), "external storage shared", Toast.LENGTH_LONG).show();
            return;
        }
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "external storage not mounted", Toast.LENGTH_LONG).show();
            return;
        }
		File extDir = Environment.getExternalStorageDirectory();
		File frameGrabDir = new File(extDir.getAbsolutePath() + "/framegrabber/");
		frameGrabDir.mkdirs();
	}
	
	private class MainPagerAdapter extends FragmentPagerAdapter {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                	mVideoLibraryFragment = new VideoLibraryFragment();
                    return mVideoLibraryFragment;
                case 1:
                	mFileBrowserFragment = new FileBrowserFragment();
                    return mFileBrowserFragment;
            }
            return null;
        }
        @Override
        public int getCount() {
        	return 2;
        }
    }

	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
	}

	@Override
	public void onPageSelected(int arg0) {
		getSupportActionBar().setSelectedNavigationItem(arg0);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}
}
