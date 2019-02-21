package cookbook.chapter10.framegrabber;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;

public class FileBrowserFragment extends SherlockListFragment {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
    		Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(
                R.layout.file_browser_fragment, container, false);
		return root;
	}
	@Override
	public void onPause() {
		super.onPause();
	}
}
