package com.kugou.media;

/**
 * callback interface
 */
public interface IFrameCallback {
	void onStart();
	/**
	 * called when preparing finshed
	 */
	void onPrepared(Boolean canHardWareDecode);
	/**
	 * called when playing finished
	 */
    void onFinished();
    void onFinishing();
    /**
     * called every frame before time adjusting
     * return true if you don't want to use internal time adjustment
     */
    boolean onFrameAvailable(long presentationTimeUs);
}