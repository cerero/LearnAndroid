package com.kugou.media;
/*
 * play audio and video from MPEG4 file using MediaCodec.
 * 原理:
 *  开启一个总控线程，负责接收外部的prepare/play/stop/resume/release/seek指令，通过锁，对音频与视频解码线程进行控制
 * 	在独立的线程里，video通过MediaCodec进行硬解，解码到surface，然后按照帧pts进行休眠，达到音画同步效果
 * 	在独立的线程里，audio通过MediaCodec进行硬解，硬解后的pcm添加到AudioTrack进行播放,然后按照帧pts进行休眠，达到音画同步效果
*/

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.kugou.util.HardwareSupportCheck;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaContentProducer {
    private static final boolean DEBUG = true;
    private static final String TAG_STATIC = "MediaContentProducer:";
    private final String TAG = TAG_STATIC;// + getClass().getSimpleName();

	private IFrameCallback mCallback;
	private boolean mCanHardDecodeH264;

	private IVideoConsumer mVideoConsumer;
	private IAudioConsumer mAudioConsumer;

	public MediaContentProducer(IVideoConsumer videoConsumer, IAudioConsumer audioConsumer, IFrameCallback frameCallback) {
		this.mVideoConsumer = videoConsumer;
		this.mAudioConsumer = audioConsumer;
		this.mCallback = frameCallback;

		new Thread(mMoviePlayerTask, TAG).start();

		//下面只所以进行线程同步，是为了让playertask正常初始化，设置好状态
		synchronized (mSync) {
			while (!mIsRunning) {
				try {
					mSync.wait();
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		}
	}

    public final int getWidth() {
        return mVideoWidth;
    }

    public final int getHeight() {
        return mVideoHeight;
    }

    public final int getBitRate() {
    	return mBitrate;
    }

    public final float getFramerate() {
    	return mFrameRate;
    }

    /**
     * get duration time as micro seconds
     * @return
     */
    public final long getDurationUs() {
    	return mDuration;
    }

    /**
     * get audio sampling rate[Hz]
     * @return
     */
    public final int getSampleRate() {
    	return mAudioSampleRate;
    }

    public final boolean hasAudio() {
    	return mHasAudio;
    }

    /**
     * 请求设置mp4资源文件路径
     * @param src_movie
     */
    public final void prepare(final String src_movie) {
    	if (DEBUG) Log.v(TAG, "prepare for file: " + src_movie);
    	synchronized (mSync) {
    		mSourcePath = src_movie;
    		mRequest = REQ_PREPARE;
    		mSync.notifyAll();
    	}
    }

	/**
     * request to start playing movie
     * this method can be called after prepare
     */
    public final void play() {
    	if (DEBUG) Log.v(TAG, "play:");
    	synchronized (mSync) {
    		if (mState == STATE_PLAYING)
    			return;
    		mRequest = REQ_START;
//			if (DEBUG) Log.v(TAG, "set request to REQ_START");
    		mSync.notifyAll();
    	}
	}

    /**
     * request to seek to specifc timed frame<br>
     * if the frame is not a key frame, frame image will be broken
     * @param newTime seek to new time[usec]
     */
    public final void seek(final long newTime) {
    	if (DEBUG) Log.v(TAG, "seek to " + newTime);
    	synchronized (mSync) {
    		mRequest = REQ_SEEK;
    		mRequestTime = newTime;
    		mSync.notifyAll();
    	}
    }

    public final void finishing() {
		if (DEBUG) Log.v(TAG, "finishing:");
		synchronized (mSync) {
			if (mState == STATE_PLAYING
					|| mState == STATE_PREPARED
					|| mState == STATE_PAUSED) {
				mRequest = REQ_FINISHING;
				mSync.notifyAll();
//				try {
//					mSync.wait(50);
//				} catch (final InterruptedException e) {
//					// ignore
//				}
			}
		}
	}
    /**
     * request stop playing
     */
    public final void stop() {
    	if (DEBUG) Log.v(TAG, "stop:");
    	synchronized (mSync) {
    		if (mState == STATE_STOP)
    			return;

			mRequest = REQ_STOP;
//			if (DEBUG) Log.v(TAG, "set request to REQ_STOP");
			mSync.notifyAll();
//			try {
//				mSync.wait(50);
//			} catch (final InterruptedException e) {
//				// ignore
//			}

    	}
    }

    /**
     * request pause playing<br>
     * this function is un-implemented yet
     */
    public final void pause() {
    	if (DEBUG) Log.v(TAG, "pause:");
    	synchronized (mSync) {
    		mRequest = REQ_PAUSE;
    		mSync.notifyAll();
    	}
    }

    /**
     * request resume from pausing<br>
     * this function is un-implemented yet
     */
    public final void resume() {
    	if (DEBUG) Log.v(TAG, "resume:");
    	synchronized (mSync) {
    		mRequest = REQ_RESUME;
    		mSync.notifyAll();
    	}
    }

    /**
     * release releated resources
     */
    public final void release() {
    	if (DEBUG) Log.v(TAG, "release:");
//    	stop();
    	synchronized (mSync) {
    		mRequest = REQ_QUIT;
    		mSync.notifyAll();
    	}
    }

//================================================================================
    private static final int TIMEOUT_USEC = 10000;	// 10msec

    /*
     * STATE_CLOSED => [preapre] => STATE_PREPARED [start]
     * 	=> STATE_PLAYING => [seek] => STATE_PLAYING
     * 		=> [pause] => STATE_PAUSED => [resume] => STATE_PLAYING
     * 		=> [stop] => STATE_CLOSED
     */
    private static final int STATE_STOP = 0;
    private static final int STATE_PREPARED = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSED = 3;
	private static final int STATE_FINISHING = 4;

    // request code
    private static final int REQ_NON = 0;
    private static final int REQ_PREPARE = 1;
    private static final int REQ_START = 2;
    private static final int REQ_SEEK = 3;
    private static final int REQ_STOP = 4;
    private static final int REQ_PAUSE = 5;
    private static final int REQ_RESUME = 6;
    private static final int REQ_QUIT = 7;
	private static final int REQ_FINISHING = 8;
//	private static final long EPS = (long)(1 / 240.0f * 1000000);	// 1/240 seconds[micro seconds]

	private final Object mSync = new Object();
	private volatile boolean mIsRunning;
	private int mState;
	private String mSourcePath;
	private long mDuration;
	private int mRequest;
	private long mRequestTime = -1;
	private long mFirstVideoPTS = -1;
	private long mFirstAudioPTS = -1;
    // for video playback
	private final Object mVideoSync = new Object();
	private Surface mOutputSurface;
	protected MediaExtractor mVideoMediaExtractor;

	private IYUVDataReceiver mYUVReceiver;
	private H264SoftDecoder mH264SoftDecoder;
	private ByteBuffer mVideoSoftDecodeInputBuffer;
    private ByteBuffer mVideoSoftDecodeOutBuffer;

	private MediaCodec mVideoMediaCodec;
	private MediaCodec.BufferInfo mVideoBufferInfo;
	private ByteBuffer[] mVideoInputBuffers;
	private ByteBuffer[] mVideoOutputBuffers;
	private long mVideoStartTime;
	@SuppressWarnings("unused")
	private long previousVideoPresentationTimeUs = -1;
	private volatile int mVideoTrackIndex;
	private volatile boolean mVideoInputDone;
	private volatile boolean mVideoOutputDone;
	private int mVideoWidth, mVideoHeight;
	private int mBitrate;
	private float mFrameRate;
	// for audio playback
	private final Object mAudioSync = new Object();
	protected MediaExtractor mAudioMediaExtractor;
	private MediaCodec mAudioMediaCodec;
	private MediaCodec.BufferInfo mAudioBufferInfo;
	private ByteBuffer[] mAudioInputBuffers;
	private ByteBuffer[] mAudioOutputBuffers;
	private long mAudioStartTime;
	@SuppressWarnings("unused")
	private long previousAudioPresentationTimeUs = -1;
	private volatile int mAudioTrackIndex;
	private volatile boolean mAudioInputDone;
	private volatile boolean mAudioOutputDone;
	private int mAudioChannels;
	private int mAudioSampleRate;
	private int mAudioInputBufSize;
	private boolean mHasAudio;
	private byte[] mAudioOutTempBuf;
	private AudioTrack mAudioTrack;

//--------------------------------------------------------------------------------
	/**
	 * playback control task
	 */
	private final Runnable mMoviePlayerTask = new Runnable() {
		@Override
		public final void run() {
			boolean local_isRunning = false;
			int local_req;
			try {
		    	synchronized (mSync) {
					local_isRunning = mIsRunning = true;
					mState = STATE_STOP;
					mRequest = REQ_NON;
					mRequestTime = -1;
		    		mSync.notifyAll(); //通知构造函数的调用者线程，构造函数执行初始化的动作ok了
		    	}

		    	while (local_isRunning) {
					try {
						synchronized (mSync) {
							local_isRunning = mIsRunning;
							local_req = mRequest;
							mRequest = REQ_NON;
						}
						if (local_isRunning) {
							switch (mState) {
                                case STATE_STOP:
                                    local_isRunning = runStopStateByPlayerTask(local_req);
                                    break;
                                case STATE_PREPARED:
                                    local_isRunning = runPreparedStateByPlayerTask(local_req);
                                    break;
                                case STATE_PLAYING:
                                    local_isRunning = runPlayingStateByPlayerTask(local_req);
                                    break;
                                case STATE_PAUSED:
                                    local_isRunning = runPausedStateByPlayerTask(local_req);
                                    break;
								case STATE_FINISHING:
									local_isRunning = runFinishingStateByPlayerTask(local_req);
									break;
							}
						}
					} catch (final InterruptedException e) {
						break;
					} catch (final Exception e) {
						Log.e(TAG, "MoviePlayerTask:", e);
						break;
					}
				} // end while (local_isRunning)
			} finally {
				if (DEBUG) Log.v(TAG, "player task finished:local_isRunning=" + local_isRunning);
				handleStop();
			}
		}
	};

//--------------------------------------------------------------------------------
	/**
	 * video playback task
	 */
	private final Runnable mVideoTask = new Runnable() {
		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "VideoTask:start");
			mVideoConsumer.start();
			for (; mIsRunning && !mVideoInputDone && !mVideoOutputDone ;) {
				try {
			        if (!mVideoInputDone) {
			        	handleInputVideo();
			        }
			        if (!mVideoOutputDone) {
						handleOutputVideo(mCallback);
			        }
				} catch (final Exception e) {
					Log.e(TAG, "VideoTask:", e);
					break;
				}
			} // end of for
			if (DEBUG) Log.v(TAG, "VideoTask:finished");
			synchronized (mVideoTask) {
				mVideoInputDone = mVideoOutputDone = true;
				mVideoConsumer.end();
				mVideoTask.notifyAll();
			}

			synchronized (mSync) {
				mSync.notifyAll();//playertask线程可能处于looping waiting中，通知激活
			}
		}
	};

//--------------------------------------------------------------------------------
	/**
	 * audio playback task
	 */
	private final Runnable mAudioTask = new Runnable() {
		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "AudioTask:start");
			for (; mIsRunning && !mAudioInputDone && !mAudioOutputDone ;) {
				try {
			        if (!mAudioInputDone) {
			        	handleInputAudio();
			        }
					if (!mAudioOutputDone) {
						handleOutputAudio(mCallback);
					}
				} catch (final Exception e) {
					Log.e(TAG, "VideoTask:", e);
					break;
				}
			} // end of for
			if (DEBUG) Log.v(TAG, "AudioTask:finished");
			synchronized (mAudioTask) {
				mAudioInputDone = mAudioOutputDone = true;
				mAudioTask.notifyAll();
			}

			synchronized (mSync) {
				mSync.notifyAll();//playertask线程可能处于looping waiting中，通知激活
			}
		}
	};

//--------------------------------------------------------------------------------
	/**
     * STATE_STOP 下只能处理 REQ_PREPARE / REQ_QUIT 请求
	 * @param req
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private final boolean runStopStateByPlayerTask(final int req) throws InterruptedException, IOException {
		boolean local_isRunning = true;
		switch (req) {
            case REQ_PREPARE:
                handlePrepareByPlayerTask(mSourcePath);
                break;
            case REQ_START:
            case REQ_PAUSE:
            case REQ_RESUME:
			case REQ_FINISHING:
                throw new IllegalStateException("invalid state:" + mState);
            case REQ_QUIT:
                local_isRunning = false;
                break;
    //		case REQ_SEEK:
    //		case REQ_STOP:
            default:
                synchronized (mSync) {
                    mSync.wait();
                }
                break;
		}
		synchronized (mSync) {
			local_isRunning &= mIsRunning;
		}
		return local_isRunning;
	}

	/**
     * STATE_PREPARED状态下只能处理 REQ_START / REQ_STOP / REQ_QUIT 等请求
	 * @param req
	 * @return
	 * @throws InterruptedException
	 */
	private final boolean runPreparedStateByPlayerTask(final int req) throws InterruptedException {
		boolean local_isRunning = true;
		switch (req) {
            case REQ_START:
                handleStart();
                break;
            case REQ_PAUSE:
            case REQ_RESUME:
				throw new IllegalStateException("invalid state:" + mState);
			case REQ_FINISHING:
				handleFinishing();
				break;
            case REQ_STOP:
                handleStop();
                break;
            case REQ_QUIT:
                local_isRunning = false;
                break;
    //		case REQ_PREPARE:
    //		case REQ_SEEK:
            default:
                synchronized (mSync) {
                    mSync.wait();
                }
                break;
		} // end of switch (req)
		synchronized (mSync) {
			local_isRunning &= mIsRunning;
		}
		return local_isRunning;
	}

	private final boolean runFinishingStateByPlayerTask(final int req)  throws InterruptedException {
		boolean local_isRunning = true;
		switch (req) {
			case REQ_START:
				mRequestTime = 1;
				handleStart();
				break;
			case REQ_STOP:
				handleStop();
				break;
			case REQ_QUIT:
				local_isRunning = false;
				break;
			case REQ_PREPARE:
			case REQ_SEEK:
			case REQ_RESUME:
			case REQ_FINISHING:
			case REQ_PAUSE:
				throw new IllegalStateException("invalid state:" + mState);
			default:
				synchronized (mSync) {
					mSync.wait();
				}
				break;
		}

		synchronized (mSync) {
			local_isRunning &= mIsRunning;
		}
		return local_isRunning;
	}

	/**
     * STATE_PLAYING状态下只能处理 REQ_SEEK / REQ_STOP / REQ_PAUSE / REQ_QUIT 等请求
	 * @param req
	 * @return
	 */
	private final boolean runPlayingStateByPlayerTask(final int req) {
		boolean local_isRunning = true;
		switch (req) {
			case REQ_PREPARE:
			case REQ_START:
			case REQ_RESUME:
				throw new IllegalStateException("invalid state:" + mState);
			case REQ_FINISHING:
				handleFinishing();
				break;
			case REQ_SEEK:
				handleSeek(mRequestTime);
				break;
			case REQ_STOP:
				handleStop();
				break;
			case REQ_PAUSE:
				handlePause();
				break;
			case REQ_QUIT:
				local_isRunning = false;
				break;
			default:
				handleLoopInPlayingState(mCallback);
				break;
		} // end of switch (req)
		synchronized (mSync) {
			local_isRunning &= mIsRunning;
		}
		return local_isRunning;
	}

	/**
     * STATE_PAUSED状态下只能处理 REQ_SEEK / REQ_STOP / REQ_RESUME / REQ_QUIT 等请求
	 * @param req
	 * @return
	 * @throws InterruptedException
	 */
	private final boolean runPausedStateByPlayerTask(final int req) throws InterruptedException {
		boolean local_isRunning = true;
		switch (req) {
            case REQ_PREPARE:
            case REQ_START:
                throw new IllegalStateException("invalid state:" + mState);
			case REQ_FINISHING:
				handleFinishing();
				break;
            case REQ_SEEK:
                handleSeek(mRequestTime);
                break;
            case REQ_STOP:
                handleStop();
                break;
            case REQ_RESUME:
                handleResume();
                break;
            case REQ_QUIT:
                local_isRunning = false;
                break;
    //		case REQ_PAUSE:
            default:
                synchronized (mSync) {
                    mSync.wait();
                }
                break;
		} // end of switch (req)
		synchronized (mSync) {
			local_isRunning &= mIsRunning;
		}
		return local_isRunning;
	}

//--------------------------------------------------------------------------------
//
//--------------------------------------------------------------------------------
	/**
     * 当前是STATE_STOP状态下，才会进入Prepare请求
     * 执行成功改更改为 STATE_PREPARED 状态
	 * @param source_file
	 * @throws IOException
	 */
	private final void handlePrepareByPlayerTask(final String source_file) throws IOException {
		if (DEBUG) Log.v(TAG, "handlePrepareByPlayerTask:" + source_file);
        synchronized (mSync) {
			if (mState != STATE_STOP) {//确保是STOP状态下进行的Prepare请求
				throw new RuntimeException("invalid state:" + mState);
			}
		}
        final File src = new File(source_file);
        if (TextUtils.isEmpty(source_file) || !src.canRead()) {
            throw new FileNotFoundException("Unable to read " + source_file);
        }

        mVideoTrackIndex = mAudioTrackIndex = -1;
		mVideoWidth = mVideoHeight = mBitrate = 0;
		mDuration = 0;
		mFrameRate = 0;

		// preparation for video playback
		mVideoTrackIndex = selectVideoTrackByPlayerTask(source_file);
		// preparation for audio playback
		mAudioTrackIndex = selectAudioTrackByPlayerTask(source_file);
		mHasAudio = mAudioTrackIndex >= 0;

		if ((mVideoTrackIndex < 0) && (mAudioTrackIndex < 0)) {
			throw new RuntimeException("No video and audio track found in " + source_file);
		}

		mCanHardDecodeH264 = HardwareSupportCheck.isSupportH264(mVideoWidth, mVideoHeight);
//		mCanHardDecodeH264 = false;
		mVideoConsumer.choseRenderMode(mCanHardDecodeH264 ? 1 : 2);

		if (mCanHardDecodeH264) {
			mOutputSurface = mVideoConsumer.generateHardWareOutputSurface();
			if (mOutputSurface == null) {
				throw new RuntimeException("generate HardWareOutputSurface fail!!");
			}
		}

		synchronized (mSync) {//更新为 STATE_PREPARED
			mState = STATE_PREPARED;
		}

//		if (DEBUG) {Log.e(TAG, "isHardWareDecode: " + mCanHardDecodeH264);

		if (mCallback != null)
			mCallback.onPrepared(mCanHardDecodeH264);
	}

	/**
     * 通过MediaExtractor寻找视频轨道
	 * @param sourceFile
	 * @return first video track index, -1 if not found
	 */
	protected int selectVideoTrackByPlayerTask(final String sourceFile) {
		int trackIndex = -1;
		mVideoMediaExtractor = new MediaExtractor();
		try {
			mVideoMediaExtractor.setDataSource(sourceFile);
			trackIndex = selectTrack(mVideoMediaExtractor, "video/");
			if (trackIndex >= 0) {
				mVideoMediaExtractor.selectTrack(trackIndex);
		        final MediaFormat format = mVideoMediaExtractor.getTrackFormat(trackIndex);
	        	mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
	        	mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
//	        	mDuration = format.getLong(MediaFormat.KEY_DURATION);
//				mBitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);
//				mFrameRate = format.getFloat(MediaFormat.KEY_FRAME_RATE);

				if (DEBUG)
					Log.v(TAG, String.format("format:size(%d,%d),duration=%d,bps=%d,framerate=%f", mVideoWidth, mVideoHeight, mDuration, mBitrate, mFrameRate));
			}
		} catch (final IOException e) {
			Log.w(TAG, e);
		}
		return trackIndex;
	}

	/**
     * 通过MediaExtractor寻找音频轨道,通过创建AudioTrack，用于写入PCM音频数据进行播放
	 * @param sourceFile
	 * @return first audio track index, -1 if not found
	 */
	protected int selectAudioTrackByPlayerTask(final String sourceFile) {
		int trackIndex = -1;
		mAudioMediaExtractor = new MediaExtractor();
		try {
			mAudioMediaExtractor.setDataSource(sourceFile);
			trackIndex = selectTrack(mAudioMediaExtractor, "audio/");
			if (trackIndex >= 0) {
				mAudioMediaExtractor.selectTrack(trackIndex);
		        final MediaFormat format = mAudioMediaExtractor.getTrackFormat(trackIndex);
		        mAudioChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
		        mAudioSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
		        final int min_buf_size = AudioTrack.getMinBufferSize(mAudioSampleRate,
		        	(mAudioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
		        	AudioFormat.ENCODING_PCM_16BIT);
		        final int max_input_size = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
		        mAudioInputBufSize =  min_buf_size > 0 ? min_buf_size * 4 : max_input_size;
		        if (mAudioInputBufSize > max_input_size) mAudioInputBufSize = max_input_size;
		        final int frameSizeInBytes = mAudioChannels * 2;
		        mAudioInputBufSize = (mAudioInputBufSize / frameSizeInBytes) * frameSizeInBytes;
		        if (DEBUG) Log.v(TAG, String.format("getMinBufferSize=%d,max_input_size=%d,mAudioInputBufSize=%d",min_buf_size, max_input_size, mAudioInputBufSize));
		        //
		        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
		        	mAudioSampleRate,
		        	(mAudioChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO),
		        	AudioFormat.ENCODING_PCM_16BIT,
		        	mAudioInputBufSize,
		        	AudioTrack.MODE_STREAM);
		        try {
		        	mAudioTrack.play();
		        } catch (final Exception e) {
		        	Log.e(TAG, "failed to start audio track playing", e);
		    		mAudioTrack.release();
		        	mAudioTrack = null;
		        }
			}
		} catch (final IOException e) {
			Log.w(TAG, e);
		}
		return trackIndex;
	}

	private final void handleStart() {
    	if (DEBUG) Log.v(TAG, "handleStart:");
		synchronized (mSync) {
			if (mState != STATE_PREPARED && mState != STATE_FINISHING)
				throw new RuntimeException("invalid state:" + mState);
			mState = STATE_PLAYING;
//			if (DEBUG) Log.v(TAG, "set mState to STATE_PLAYING");
			if (mCallback != null)
				mCallback.onStart();
		}
        if (mRequestTime > 0) {
			if (DEBUG) Log.v(TAG, "seektime:" + mRequestTime);
        	handleSeek(mRequestTime);
        }
//		handleReplaySeek();
        previousVideoPresentationTimeUs = previousAudioPresentationTimeUs = -1;

		mVideoStartTime = mAudioStartTime = -1;
        //-----初始化硬解码器或软解码器-----
		mVideoInputDone = mVideoOutputDone = true;
		Thread videoThread = null, audioThread = null;
		if (mVideoTrackIndex >= 0) {
		    if (mCanHardDecodeH264) {
		    	if (mVideoMediaCodec == null) {
					final MediaCodec codec = internalStartVideo(mVideoMediaExtractor, mVideoTrackIndex);
					if (codec != null) {
						mVideoMediaCodec = codec;
						mVideoBufferInfo = new MediaCodec.BufferInfo();
						mVideoInputBuffers = codec.getInputBuffers();
						mVideoOutputBuffers = codec.getOutputBuffers();
					} else {
						//硬解码器创建失败,降级走软解
						mCanHardDecodeH264 = false;
						mH264SoftDecoder = internalStartVideoWithSoftDecode(mVideoMediaExtractor, mVideoTrackIndex);
					}
				}
            } else {
		    	if (mH264SoftDecoder == null) {
					mH264SoftDecoder = internalStartVideoWithSoftDecode(mVideoMediaExtractor, mVideoTrackIndex);
				}
            }

			mVideoInputDone = mVideoOutputDone = false;
			videoThread = new Thread(mVideoTask, "VideoTask");
		}
		//-------------  end

		mAudioInputDone = mAudioOutputDone = true;
		if (mAudioTrackIndex >= 0) {
			if (mAudioMediaCodec == null) {
				final MediaCodec codec = internalStartAudio(mAudioMediaExtractor, mAudioTrackIndex);
				if (codec != null) {
					mAudioMediaCodec = codec;
					mAudioBufferInfo = new MediaCodec.BufferInfo();
					mAudioInputBuffers = codec.getInputBuffers();
					mAudioOutputBuffers = codec.getOutputBuffers();
				}
			}
			mAudioInputDone = mAudioOutputDone = false;
	        audioThread = new Thread(mAudioTask, "AudioTask");
		}

//		if (DEBUG) Log.d(TAG, "videoThread and audioThread start");

		if (videoThread != null) videoThread.start();//开启视频解码线程
		if (audioThread != null) audioThread.start();//开启音频解码线程
	}

	protected H264SoftDecoder internalStartVideoWithSoftDecode(final MediaExtractor media_extractor, final int trackIndex) {
		if (DEBUG) Log.v(TAG, "internalStartVideoWithSoftDecode: format width=" + mVideoWidth + ",height=" + mVideoHeight);
		H264SoftDecoder softDecoder = null;
		if (trackIndex >= 0) {
			final MediaFormat format = media_extractor.getTrackFormat(trackIndex);
			final String mime = format.getString(MediaFormat.KEY_MIME);

			ByteBuffer spsByteBuffer = format.getByteBuffer("csd-0");
			ByteBuffer ppsByteBuffer = format.getByteBuffer("csd-1");

			int spsByteLen = spsByteBuffer.limit();
			int ppsByteLen = ppsByteBuffer.limit();

			if (mVideoSoftDecodeInputBuffer == null) {
				mVideoSoftDecodeInputBuffer = ByteBuffer.allocateDirect((int)(mVideoWidth * mVideoHeight * 1.5));
				mVideoSoftDecodeInputBuffer.order(spsByteBuffer.order());
			}

			mVideoSoftDecodeInputBuffer.position(0);
			mVideoSoftDecodeInputBuffer.put(spsByteBuffer);
			mVideoSoftDecodeInputBuffer.put(ppsByteBuffer);
			mVideoSoftDecodeInputBuffer.flip();
			softDecoder = new H264SoftDecoder(H264SoftDecoder.COLOR_FORMAT_YUV420);
			softDecoder.consumeNalUnitsFromDirectBuffer(mVideoSoftDecodeInputBuffer, mVideoSoftDecodeInputBuffer.limit(), 0);

//			if (!softDecoder.isFrameReady()) {
//				throw new IllegalStateException(TAG + ":解析sps/pps异常");
//			}

//			softDecoder.consumeNalUnitsFromDirectBuffer(mVideoSoftDecodeInputBuffer, ppsByteLen, 0);
//			if (!softDecoder.isFrameReady()) {
//				throw new IllegalStateException(TAG + ":解析pps异常");
//			}
		}
		return softDecoder;
	}
	/**
	 * @param media_extractor
	 * @param trackIndex
	 * @return
	 */
	protected MediaCodec internalStartVideo(final MediaExtractor media_extractor, final int trackIndex) {
		if (DEBUG) Log.v(TAG, "internalStartVideo:");
		MediaCodec codec = null;
		if (trackIndex >= 0) {
	        final MediaFormat format = media_extractor.getTrackFormat(trackIndex);
	        final String mime = format.getString(MediaFormat.KEY_MIME);
			try {
				codec = MediaCodec.createDecoderByType(mime);
				codec.configure(format, mOutputSurface, null, 0);
		        codec.start();
			} catch (final IOException e) {
				Log.w(TAG, e);
				codec = null;
			}
	    	if (DEBUG) Log.v(TAG, "internalStartVideo:codec started");
		}
		return codec;
	}

	/**
	 * @param media_extractor
	 * @param trackIndex
	 * @return
	 */
	protected MediaCodec internalStartAudio(final MediaExtractor media_extractor, final int trackIndex) {
		if (DEBUG) Log.v(TAG, "internalStartAudio:");
		MediaCodec codec = null;
		if (trackIndex >= 0) {
	        final MediaFormat format = media_extractor.getTrackFormat(trackIndex);
	        final String mime = format.getString(MediaFormat.KEY_MIME);
			try {
				codec = MediaCodec.createDecoderByType(mime);
				codec.configure(format, null, null, 0);
		        codec.start();
		    	if (DEBUG) Log.v(TAG, "internalStartAudio:codec started");
		    	//
		        final ByteBuffer[] buffers = codec.getOutputBuffers();
		        int sz = buffers[0].capacity();
		        if (sz <= 0)
		        	sz = mAudioInputBufSize;
		        if (DEBUG) Log.v(TAG, "AudioOutputBufSize:" + sz);
		        mAudioOutTempBuf = new byte[sz];
			} catch (final IOException e) {
				Log.w(TAG, e);
				codec = null;
			}
		}
		return codec;
	}

	private final void handleSeek(final long newTime) {
        if (DEBUG) Log.d(TAG, "handleSeek");
		if (newTime < 0) return;

		if (mVideoTrackIndex >= 0) {
			mVideoMediaExtractor.seekTo(newTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
	        Boolean hasSample = mVideoMediaExtractor.advance();
//	        if (mVideoSoftDecodeInputBuffer != null) {
//				while (hasSample) {
//					final int sample_size = mVideoMediaExtractor.readSampleData(mVideoSoftDecodeInputBuffer, 0);
//					final long presentationTimeUs = mVideoMediaExtractor.getSampleTime();
//					mVideoSoftDecodeInputBuffer.position(0);
//					if (sample_size > 0) {
//						int naluetype = mVideoSoftDecodeInputBuffer.get(4) & 0x1f;
//						if (naluetype == 1 || naluetype == 5 || naluetype == 6) {
//							if (DEBUG) Log.d(TAG, "find the first keyframe naluetype: " + naluetype + ",pts:" + presentationTimeUs);
//							break;
//						}
//					}
//					hasSample = mVideoMediaExtractor.advance();
//				}
//			}
		}
		if (mAudioTrackIndex >= 0) {
			mAudioMediaExtractor.seekTo(newTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
	        mAudioMediaExtractor.advance();
		}
        mRequestTime = -1;
	}

	private final void handleReplaySeek() {
		if (DEBUG) Log.d(TAG, "handleReplaySeek mFirstAudioPTS:" + mFirstAudioPTS + ", mFirstVideoPTS=" + mFirstVideoPTS);
		if (mFirstAudioPTS < 0 || mFirstVideoPTS < 0) return;

		if (mVideoTrackIndex >= 0) {
			mVideoMediaExtractor.seekTo(mFirstVideoPTS, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
			mVideoMediaExtractor.advance();
		}
		if (mAudioTrackIndex >= 0) {
			mAudioMediaExtractor.seekTo(mFirstAudioPTS, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
			mAudioMediaExtractor.advance();
		}
//		mFirstAudioPTS = mFirstVideoPTS = -1;
	}

	private final void handleLoopInPlayingState(final IFrameCallback frameCallback) {
//		if (DEBUG) Log.d(TAG, "handleLoopInPlayingState");

		synchronized (mSync) {
			try {
				mSync.wait();
			} catch (final InterruptedException e) {
			}
		}
        if (mVideoInputDone && mVideoOutputDone && mAudioInputDone && mAudioOutputDone) {
            if (DEBUG) Log.d(TAG, "Reached EOS, looping check");
//        	handleStop();
			handleFinishing();
        }
	}

    protected boolean internal_process_input_with_soft_decode(final H264SoftDecoder softDecoder, final MediaExtractor extractor, final long presentationTimeUs) {
//		if (DEBUG) Log.v(TAG, "internal_process_input_with_soft_decode:presentationTimeUs=" + presentationTimeUs);
        boolean result = true;
        boolean frame_ready = false;
        if (mIsRunning) {
            final int sample_size = extractor.readSampleData(mVideoSoftDecodeInputBuffer, 0);
			mVideoSoftDecodeInputBuffer.position(0);
			if (sample_size > 0) {
//				if(DEBUG) Log.d(TAG, "extrator readSampleData nalu type: " + (mVideoSoftDecodeInputBuffer.get(4) & 0x1f) + ",sample_size: " + sample_size + ", pts: " + presentationTimeUs);
				softDecoder.consumeNalUnitsFromDirectBuffer(mVideoSoftDecodeInputBuffer, sample_size, presentationTimeUs);
				frame_ready = softDecoder.isFrameReady();
//				if (frame_ready) {
//					if(DEBUG) Log.d(TAG, String.format("soft_decode width=%1$d height=%2$d", softDecoder.getWidth(), softDecoder.getHeight()));
//				}

				if (mFirstVideoPTS < 0) {
					mFirstVideoPTS = presentationTimeUs;
					if (DEBUG) Log.d(TAG, "mFirstVideoPTS:" + mFirstVideoPTS);
				}
			}
            result = extractor.advance();	// return false if no data is available
        }
        return result;
    }
	/**
     * 取出MediaCodec的inputbuffer
     * 调用MediaExtractor的readSampleData
     * 写入inputbuffer
	 * @param codec
	 * @param extractor
	 * @param inputBuffers
	 * @param presentationTimeUs
	 * @param isAudio
	 */
	protected boolean internal_process_input(final MediaCodec codec, final MediaExtractor extractor, final ByteBuffer[] inputBuffers, final long presentationTimeUs, final boolean isAudio) {
//		if (DEBUG) Log.v(TAG, "internalProcessInput:presentationTimeUs=" + presentationTimeUs);
		boolean result = true;
		while (mIsRunning) {
            final int inputBufIndex = codec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER)
            	break;
            if (inputBufIndex >= 0) {
                final int size = extractor.readSampleData(inputBuffers[inputBufIndex], 0);
                if (size > 0) {
//                	if (!isAudio) {
//                		if (DEBUG) Log.d(TAG, "internal_process_input video sample data dequeue size:" + size + ", pts:" + presentationTimeUs);
//					}
                	codec.queueInputBuffer(inputBufIndex, 0, size, presentationTimeUs, 0);
                	if (!isAudio) {
						if (mFirstVideoPTS < 0) {
							mFirstVideoPTS = presentationTimeUs;
							if (DEBUG) Log.d(TAG, "mFirstVideoPTS:" + mFirstVideoPTS);
						}
					}
                }
            	result = extractor.advance();	// return false if no data is available
                break;
            }
		}
		return result;
	}

	private final void handleInputVideo() {
    	final long presentationTimeUs = mVideoMediaExtractor.getSampleTime();
//    	if (mFirstVideoPTS < 0) {
//			mFirstVideoPTS = presentationTimeUs;
//			if (DEBUG) Log.d(TAG, "mFirstVideoPTS:" + mFirstVideoPTS);
//		}

/*		if (presentationTimeUs < previousVideoPresentationTimeUs) {
    		presentationTimeUs += previousVideoPresentationTimeUs - presentationTimeUs; // + EPS;
    	}
    	previousVideoPresentationTimeUs = presentationTimeUs; */
        final boolean b;
        if (mCanHardDecodeH264) {
            b = internal_process_input(mVideoMediaCodec, mVideoMediaExtractor, mVideoInputBuffers,
                    presentationTimeUs, false);
        } else {
            b = internal_process_input_with_soft_decode(mH264SoftDecoder, mVideoMediaExtractor, presentationTimeUs);
        }

        if (!b) {//no data is available in extrator
        	if (DEBUG) Log.i(TAG, "video track input reached EOS");
        	if (mCanHardDecodeH264) {//通知硬解码起结束接收数据
                while (mIsRunning) {
                    final int inputBufIndex = mVideoMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0) {
                        mVideoMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        if (DEBUG) Log.v(TAG, "sent input EOS:" + mVideoMediaCodec);
                        break;
                    }
                }
            } else {//软解结束逻辑
				// 暂时没有需要处理的结束逻辑
            }

    		synchronized (mVideoTask) {
    			mVideoInputDone = true;
    			mVideoTask.notifyAll();
    		}
        }
	}
	/**
	 * @param frameCallback
	 */
	private final void handleOutputVideo(final IFrameCallback frameCallback) {
//    	if (DEBUG) Log.v(TAG, "handleDrainVideo:");
		boolean doRender = false;
        if (mCanHardDecodeH264) {
            while (mIsRunning && !mVideoOutputDone) { //硬解后的图像，输出到surface，并按照pts进行帧同步显示
                final int decoderStatus = mVideoMediaCodec.dequeueOutputBuffer(mVideoBufferInfo, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    return;
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    mVideoOutputBuffers = mVideoMediaCodec.getOutputBuffers();
                    if (DEBUG) Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED:");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    final MediaFormat newFormat = mVideoMediaCodec.getOutputFormat();
                    if (DEBUG) Log.d(TAG, "video decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    throw new RuntimeException(
                            "unexpected result from video decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    doRender = false;
                    if (mVideoBufferInfo.size > 0) {
                        doRender = !internalWriteVideo(mVideoOutputBuffers[decoderStatus], 0, mVideoBufferInfo.size, mVideoBufferInfo.presentationTimeUs);

                        if (doRender) {
                            if (frameCallback == null || !frameCallback.onFrameAvailable(mVideoBufferInfo.presentationTimeUs))
                                mVideoStartTime = adjustPresentationTime(mVideoSync, mVideoStartTime, mVideoBufferInfo.presentationTimeUs);
                        }
                    }
                    mVideoMediaCodec.releaseOutputBuffer(decoderStatus, doRender);
                    if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (DEBUG) Log.d(TAG, "video:output EOS");
                        synchronized (mVideoTask) {
                            mVideoOutputDone = true;
                            mVideoTask.notifyAll();
                        }
                    }
                }
            }
        } else {
			if (mIsRunning && !mVideoOutputDone) {
				int output_size = mH264SoftDecoder.getOutputByteSize();
				long pts = mH264SoftDecoder.getLastPTS();
				doRender = false;
				if (output_size > 0) {
					//取出软解后的yuv
					if (mVideoSoftDecodeOutBuffer == null) {
						mVideoSoftDecodeOutBuffer = ByteBuffer.allocateDirect(output_size);
					}

					if (mH264SoftDecoder.isFrameReady()) {
						mH264SoftDecoder.decodeFrameToDirectBuffer(mVideoSoftDecodeOutBuffer);
						doRender = !internalWriteVideo(mVideoSoftDecodeOutBuffer, 0, output_size, pts);
					}
				}

				if (doRender && !frameCallback.onFrameAvailable(pts))
					mVideoStartTime = adjustPresentationTime(mVideoSync, mVideoStartTime, pts);

				synchronized (mVideoTask) {
					if (mVideoInputDone) {
						mVideoOutputDone = true;
						mVideoTask.notifyAll();
					}
				}

			}
        }

	}

	/**
	 * @param buffer
	 * @param offset
	 * @param size
	 * @param presentationTimeUs
	 * @return if return false, automatically adjust frame rate
	 */
	protected boolean internalWriteVideo(final ByteBuffer buffer, final int offset, final int size, final long presentationTimeUs) {
//		if (DEBUG) Log.v(TAG, "internalWriteVideo");
		if (!mCanHardDecodeH264) {
			mVideoConsumer.onYUVData(buffer, mVideoWidth, mVideoHeight);
		}
		return false;
	}

	private final void handleInputAudio() {
		final long presentationTimeUs = mAudioMediaExtractor.getSampleTime();
		if (mFirstAudioPTS < 0) {
			mFirstAudioPTS = presentationTimeUs;
			if (DEBUG) Log.d(TAG, "mFirstAudioPTS:" + mFirstAudioPTS);
		}
/*		if (presentationTimeUs < previousAudioPresentationTimeUs) {
    		presentationTimeUs += previousAudioPresentationTimeUs - presentationTimeUs; //  + EPS;
    	}
    	previousAudioPresentationTimeUs = presentationTimeUs; */
        final boolean b = internal_process_input(mAudioMediaCodec, mAudioMediaExtractor, mAudioInputBuffers,
        		presentationTimeUs, true);
        if (!b) {
        	if (DEBUG) Log.i(TAG, "audio track input reached EOS");
    		while (mIsRunning) {
                final int inputBufIndex = mAudioMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                	mAudioMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                		MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                	if (DEBUG) Log.v(TAG, "sent input EOS:" + mAudioMediaCodec);
                	break;
                }
        	}
    		synchronized (mAudioTask) {
    			mAudioInputDone = true;
    			mAudioTask.notifyAll();
    		}
       }
	}

	private final void handleOutputAudio(final IFrameCallback frameCallback) {
//		if (DEBUG) Log.v(TAG, "handleDrainAudio:");
		while (mIsRunning && !mAudioOutputDone) {
			final int decoderStatus = mAudioMediaCodec.dequeueOutputBuffer(mAudioBufferInfo, TIMEOUT_USEC);
			if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
				return;
			} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				mAudioOutputBuffers = mAudioMediaCodec.getOutputBuffers();
				if (DEBUG) Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED:");
			} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				final MediaFormat newFormat = mAudioMediaCodec.getOutputFormat();
				if (DEBUG) Log.d(TAG, "audio decoder output format changed: " + newFormat);
			} else if (decoderStatus < 0) {
				throw new RuntimeException(
					"unexpected result from audio decoder.dequeueOutputBuffer: " + decoderStatus);
			} else { // decoderStatus >= 0
				if (mAudioBufferInfo.size > 0) {
					internalWriteAudio(mAudioOutputBuffers[decoderStatus],
						0, mAudioBufferInfo.size, mAudioBufferInfo.presentationTimeUs);
					if (!frameCallback.onFrameAvailable(mAudioBufferInfo.presentationTimeUs))
						mAudioStartTime = adjustPresentationTime(mAudioSync, mAudioStartTime, mAudioBufferInfo.presentationTimeUs);
				}
				mAudioMediaCodec.releaseOutputBuffer(decoderStatus, false);
				if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (DEBUG) Log.d(TAG, "audio:output EOS");
					synchronized (mAudioTask) {
						mAudioOutputDone = true;
						mAudioTask.notifyAll();
					}
				}
			}
		}
	}

	/**
	 * @param buffer
	 * @param offset
	 * @param size
	 * @param presentationTimeUs
	 * @return ignored
	 */
	protected boolean internalWriteAudio(final ByteBuffer buffer, final int offset, final int size, final long presentationTimeUs) {
//		if (DEBUG) Log.d(TAG, "internalWriteAudio");
        if (mAudioOutTempBuf.length < size) {
        	mAudioOutTempBuf = new byte[size];
        }
        buffer.position(offset);
        buffer.get(mAudioOutTempBuf, 0, size);
        buffer.clear();
        if (mAudioTrack != null)
        	mAudioTrack.write(mAudioOutTempBuf, 0, size);
        return true;
	}

	/**
	 * adjusting frame rate
	 * @param sync
	 * @param startTime
	 * @param presentationTimeUs
	 * @return startTime
	 */
	protected long adjustPresentationTime(final Object sync, final long startTime, final long presentationTimeUs) {
		if (startTime > 0) {
			for (long t = presentationTimeUs - (System.nanoTime() / 1000 - startTime);
					t > 0; t = presentationTimeUs - (System.nanoTime() / 1000 - startTime)) {
				synchronized (sync) {
					try {
						sync.wait(t / 1000, (int)((t % 1000) * 1000));
					} catch (final InterruptedException e) {
						// ignore
					}
					if ((mState == REQ_STOP) || (mState == REQ_QUIT))
						break;
				}
			}
			return startTime;
		} else {
			return System.nanoTime() / 1000;
		}
	}

	/***
	 * finishing处理与stop类似，区别是只停止解码与渲染，不会销毁. 方便后续复用解码器，重新开始播放
	 * */
	private final void handleFinishing() {
		synchronized (mSync) {
			if (mState == STATE_STOP)
				return;
		}

		if (DEBUG) Log.v(TAG, "handleFinishing:");
		synchronized (mVideoTask) {
			if (mVideoTrackIndex >= 0) {
				mVideoOutputDone = true;
				while (!mVideoInputDone) {
					try {
						mVideoTask.wait();
					} catch (final InterruptedException e) {
						break;
					}
				}
			}

			if (mCanHardDecodeH264 && mVideoBufferInfo != null && (mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				if (mVideoMediaCodec != null) {
					if (DEBUG) Log.d(TAG, "video codec flush()");
					mVideoMediaCodec.flush();
				}
			}

			mVideoOutputDone = mVideoInputDone = true;
		}

		synchronized (mAudioTask) {
			if (mAudioTrackIndex >= 0) {
				mAudioOutputDone = true;
				while (!mAudioInputDone) {
					try {
						mAudioTask.wait();
					} catch (final InterruptedException e) {
						break;
					}
				}
			}

			if (mAudioBufferInfo != null && (mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				if (mAudioMediaCodec != null) {
					if (DEBUG) Log.d(TAG, "audio codec flush()");
					mAudioMediaCodec.flush();
				}
			}
			mAudioOutputDone = mAudioInputDone = true;
		}

		synchronized (mSync) {
			mState = STATE_FINISHING;
		}

		if (mCallback != null)
			mCallback.onFinishing();
	}

	private final void handleStop() {
		synchronized (mSync) {
			if (mState == STATE_STOP)
				return;
		}

    	if (DEBUG) Log.v(TAG, "handleStop:");
    	synchronized (mVideoTask) {
    		if (mVideoTrackIndex >= 0) {
        		mVideoOutputDone = true;
        		for ( ; !mVideoInputDone ;)
        		try {
        			mVideoTask.wait();
				} catch (final InterruptedException e) {
					break;
				}
        		internalStopVideo();
        		mVideoTrackIndex = -1;
    		}
			mVideoOutputDone = mVideoInputDone = true;
    	}
    	synchronized (mAudioTask) {
    		if (mAudioTrackIndex >= 0) {
        		mAudioOutputDone = true;
        		for ( ; !mAudioInputDone ;)
        		try {
        			mAudioTask.wait();
				} catch (final InterruptedException e) {
					break;
				}
        		internalStopAudio();
        		mAudioTrackIndex = -1;
    		}
			mAudioOutputDone = mAudioInputDone = true;
    	}
    	if (mVideoMediaCodec != null) {
    		mVideoMediaCodec.stop();
    		mVideoMediaCodec.release();
    		mVideoMediaCodec = null;
    	}
    	if (mAudioMediaCodec != null) {
    		mAudioMediaCodec.stop();
    		mAudioMediaCodec.release();
    		mAudioMediaCodec = null;
    	}
		if (mVideoMediaExtractor != null) {
			mVideoMediaExtractor.release();
			mVideoMediaExtractor = null;
		}
		if (mAudioMediaExtractor != null) {
			mAudioMediaExtractor.release();
			mAudioMediaExtractor = null;
		}

        mH264SoftDecoder = null;
        mVideoSoftDecodeInputBuffer = null;
        mVideoSoftDecodeOutBuffer = null;

        mVideoBufferInfo = mAudioBufferInfo = null;
        mVideoInputBuffers = mVideoOutputBuffers = null;
        mAudioInputBuffers = mAudioOutputBuffers = null;
		mFirstVideoPTS = mFirstAudioPTS = -1;
		mVideoStartTime = mAudioStartTime = -1;
		synchronized (mSync) {
			mState = STATE_STOP;
		}
		if (mCallback != null)
			mCallback.onFinished();
	}

	protected void internalStopVideo() {
		if (DEBUG) Log.v(TAG, "internalStopVideo:");
	}

	protected void internalStopAudio() {
		if (DEBUG) Log.v(TAG, "internalStopAudio:");
    	if (mAudioTrack != null) {
    		if (mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED)
    			mAudioTrack.stop();
    		mAudioTrack.release();
    		mAudioTrack = null;
    	}
		mAudioOutTempBuf = null;
	}

	private final void handlePause() {
    	if (DEBUG) Log.v(TAG, "handlePause:");
    	// FIXME unimplemented yet
	}

	private final void handleResume() {
    	if (DEBUG) Log.v(TAG, "handleResume:");
    	// FIXME unimplemented yet
	}

    /**
     * search first track index matched specific MIME
     * @param extractor
     * @param mimeType "video/" or "audio/"
     * @return track index, -1 if not found
     */
    protected static final int selectTrack(final MediaExtractor extractor, final String mimeType) {
        final int numTracks = extractor.getTrackCount();
        MediaFormat format;
        String mime;
        for (int i = 0; i < numTracks; i++) {
            format = extractor.getTrackFormat(i);
            mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(mimeType)) {
                if (DEBUG) {
                    Log.d(TAG_STATIC, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }
        return -1;
    }

    /**音视频子线程检测能否进入stop状态**/
    private void checkCanStopBySubTask() {

	}
}
