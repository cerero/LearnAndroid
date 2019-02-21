package cookbook.chapter11.wolf3dport.audio;

import android.media.SoundPool;

public class GameSound {
    private int soundId;
    private SoundPool soundPool;

    public GameSound(SoundPool pSoundPool, int pSoundId) {
        soundId = pSoundId;
        soundPool = pSoundPool;
    }
    public void play(float volume) {
        soundPool.play(soundId, volume, volume, 0, 0, 1);
    }
    public void unload() {
        soundPool.unload(soundId);
    }
}
