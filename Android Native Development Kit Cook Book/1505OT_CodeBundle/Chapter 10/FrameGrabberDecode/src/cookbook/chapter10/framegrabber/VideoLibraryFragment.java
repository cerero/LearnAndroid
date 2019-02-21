package cookbook.chapter10.framegrabber;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class VideoLibraryFragment extends SherlockListFragment {
	private static final String TAG = VideoLibraryFragment.class.getName();
	private SimpleCursorAdapter mAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
    		Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(
                R.layout.video_library_fragment, container, false);
		return root;
	}
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //populate the list data
		try {
			Cursor cursor = createCursor();
			Log.i(TAG, "No. of videos: " + cursor.getCount());
            mAdapter = new SimpleCursorAdapter(
                this.getActivity(),
                R.layout.video_library_row,
                cursor,
                new String[] {
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media._ID
                    },
                new int[] {
                    R.id.row_size,
                    R.id.row_duration,
                    R.id.row_title,
                    R.id.row_icon
            });
            setListAdapter(mAdapter);
            getListView().setItemsCanFocus(true);
        } catch (SecurityException e) {
            Log.e(TAG, e.toString());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.toString());
        }
		
		getListView().setFastScrollEnabled(true);

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            	if (R.id.row_size == view.getId()) {
            		//format size
            		String size = cursor.getString(columnIndex);
            		if (null == size || 0 == size.compareTo("")) {
            			((TextView)view).setText("Uknown");
            		} else {
	            		double sizeDouble = Double.parseDouble(size);
	            		if (sizeDouble > 1024*1024*1024) {
	            			((TextView)view).setText(String.format("%.2fGB", sizeDouble/1024/1024/1024));
	            		} else if (sizeDouble > 1024*1024) {
	            			((TextView)view).setText(String.format("%.2fMB", sizeDouble/1024/1024));
	            		} else if (sizeDouble > 1024) {
	            			((TextView)view).setText(String.format("%.2fKB", sizeDouble/1024));
	            		} else {
	            			((TextView)view).setText(String.format("%.2fB", sizeDouble));
	            		}
            		}
            		return true;
            	} else if (R.id.row_duration == view.getId()) {
            		//duration
            		String duration = cursor.getString(columnIndex);
            		if (null == duration || 0 == duration.compareTo("")) {
            			((TextView)view).setText("Uknown");
            		} else {
	            		int durationInt = Integer.parseInt(duration)/1000;
	            		((TextView)view).setText(String.format("%02d:%02d:%02d", durationInt/3600,
	            				(durationInt%3600)/60, (durationInt%3600)%60));
            		}
            		return true;
            	}
            	if (R.id.row_icon == view.getId()) {
            		int id = cursor.getInt(columnIndex);
//    				ContentResolver crThumb = VideoLibraryFragment.this.getActivity().getContentResolver();
//    				BitmapFactory.Options options=new BitmapFactory.Options();
//    				options.inSampleSize = 1;
//    				Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
//    				Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
    				Bitmap curThumb = BitmapFactory.decodeResource(getResources(), R.drawable.video_thumb_pl);
    				((ImageView)view).setImageBitmap(curThumb);
                    return true;
                } 
                return false;
            }
        });
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = mAdapter.getCursor();
        int dataIndex = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        String filePath = c.getString(dataIndex);
        Log.i(TAG, "video path " + filePath);
		Intent intent = new Intent();
        intent.putExtra(VideoEditActivity.INTENT_PATH_DATA, filePath);
        intent.setClass(getActivity(), cookbook.chapter10.framegrabber.VideoEditActivity.class);
        startActivity(intent);
	}
	
	private Cursor createCursor() {
        Cursor c = new MergeCursor(new Cursor[] {getExternalVideoCursor(),getInternalVideoCursor()});
        this.getActivity().startManagingCursor(c);
        return c;
    }
	
	private Cursor getInternalVideoCursor() {
        return this.getActivity().managedQuery(
                MediaStore.Video.Media.INTERNAL_CONTENT_URI,
                INTERNAL_COLUMNS,
                null,
                null,
                MediaStore.Video.Media.DEFAULT_SORT_ORDER);
    }

    private Cursor getExternalVideoCursor() {
        return this.getActivity().managedQuery(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                EXTERNAL_COLUMNS,
                null,
                null,
                MediaStore.Video.Media.DEFAULT_SORT_ORDER);
    }
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	private static final String[] INTERNAL_COLUMNS = new String[] {
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DURATION,
        "\"" + MediaStore.Video.Media.INTERNAL_CONTENT_URI + "\""
    };

    private static final String[] EXTERNAL_COLUMNS = new String[] {
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DURATION,
        "\"" + MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "\""
    };
}
