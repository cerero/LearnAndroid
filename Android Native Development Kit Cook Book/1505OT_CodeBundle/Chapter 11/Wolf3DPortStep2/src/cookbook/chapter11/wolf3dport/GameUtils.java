package cookbook.chapter11.wolf3dport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.util.Log;

public class GameUtils {
	private static final String TAG = "GameUtils";
	private static final String GAME_DATA_ZIP_FILE = "wolfsw.zip";
	public static void unzip(InputStream is, File dest) {
    	ZipInputStream zipStream = new ZipInputStream(is);
    	ZipEntry zipEntry;
    	try {
	    	while (null != (zipEntry = zipStream.getNextEntry())) {
	    		String path = dest.getAbsolutePath() + File.separator + zipEntry.getName();  		
	    		//copy files to dest
	    		FileOutputStream fout = new FileOutputStream(path);
	    		byte[] bytes = new byte[1024];		
	            for (int c = zipStream.read(bytes); c != -1; c = zipStream.read(bytes)) {
	            	fout.write(bytes,0, c);
	            }
	            zipStream.closeEntry();
	            fout.close();    		
	    	}
    	} catch (IOException e) {
    		e.printStackTrace();
    		Log.e(TAG, e.getMessage());
    	}
    }
	
	public static void installGameDataFiles(Context pContext, File pDir) {
		try {
			unzip(pContext.getAssets().open(GAME_DATA_ZIP_FILE), pDir);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}
}
