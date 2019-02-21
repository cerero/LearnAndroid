package cookbook.chapter11.wolf3dport.audio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class MyAudioManager {
	private static final String TAG = "MyAudioManager";
	private SoundPool mSoundPool; 
	private AssetManager assetMgr;
	private HashMap<String, GameSound> preloadSounds = new HashMap<String, GameSound>();
	private GameMusic mMusic = null;
	private Context mContext;
	public MyAudioManager(Context pContext) {
		mContext = pContext;
		assetMgr = mContext.getAssets();
		mSoundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
		//preload the sounds
		preloadSounds();
	}
	
	private void preloadSounds() {
		for (int i = 0; i < Audiowl6.SOUNDS.length; ++i) {
			String name = Audiowl6.SOUNDS[i];
			if (0 == name.compareTo("")) {
				continue;
			}
			if (!preloadSounds.containsKey(name)) {
				AssetFileDescriptor assetDescriptor;
				try {
					assetDescriptor = assetMgr.openFd(name);
					int soundId = mSoundPool.load(assetDescriptor, 0);
					GameSound newSound = new GameSound(mSoundPool, soundId);
					preloadSounds.put(name, newSound);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void unloadSounds() {
		for (Map.Entry<String, GameSound> entry : preloadSounds.entrySet()) {
			GameSound sound = entry.getValue();
			sound.unload();
		}
	}
	
	public void stopMusic() {
		if (null != mMusic) {
			mMusic.destroy();
		}
	}
	
	public void resumeMusic() {
		if (null != mMusic) {
			mMusic.play();
		}
	}
	
	public void pauseMusic() {
		if (null != mMusic) {
			mMusic.pause();
		}
	}
	
	public synchronized void playSound(int soundIdx) {
		Log.i(TAG, "playSound " + soundIdx);
		if (0 == Audiowl6.SOUNDS[soundIdx].compareTo("")) {
			//no sound files 
			Log.i(TAG, "no sound files");
			return;
		} else {
			if (preloadSounds.containsKey(Audiowl6.SOUNDS[soundIdx])) {
				preloadSounds.get(Audiowl6.SOUNDS[soundIdx]).play(50);
			} else {
				AssetFileDescriptor assetDescriptor;
				try {
					assetDescriptor = assetMgr.openFd(Audiowl6.SOUNDS[soundIdx]);
					int soundId = mSoundPool.load(assetDescriptor, 0);
					GameSound newSound = new GameSound(mSoundPool, soundId);
					preloadSounds.put(Audiowl6.SOUNDS[soundIdx], newSound);
					newSound.play(50);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void playMusic(int musicIdx) {
		Log.i(TAG, "playMusic " + musicIdx);
		if (-1 == Audiowl6.MUSICS[musicIdx]) {
			//no sound files 
			Log.i(TAG, "no music files");
			return;
		} else {
			AssetFileDescriptor assetDescriptor;
			if (null != mMusic) {
				mMusic.destroy();
			}
			mMusic = new GameMusic(mContext, Audiowl6.MUSICS[musicIdx]);
			mMusic.setVolume(50);
			mMusic.setLooping(true);
			mMusic.play();
		}
	}
}
