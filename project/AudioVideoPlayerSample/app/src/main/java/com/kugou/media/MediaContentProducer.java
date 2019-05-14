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
import android.view.Surface;

import com.kugou.util.CodecSupportCheck;
import com.kugou.util.LogWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaContentProducer {
    private static final boolean DEBUG = true;
    private static final String TAG = "MediaContentProducer";
	private static final String TAG_PLAYER_TASK = "playertask";
	private static final String TAG_VIDEO_TASK = "videotask";
	private static final String TAG_AUDIO_TASK = "audiotask";

	private IFrameCallback mCallback;
	private boolean mCanHardDecodeH264;

	private IVideoConsumer mVideoConsumer;
	private IAudioConsumer mAudioConsumer;
	private IErrorReceiver mErrorReceiver;

	public MediaContentProducer(IVideoConsumer videoConsumer, IAudioConsumer audioConsumer, IFrameCallback frameCallback, IErrorReceiver errorReceiver) {
		this.mVideoConsumer = videoConsumer;
		this.mAudioConsumer = audioConsumer;
		this.mCallback = frameCallback;
		this.mErrorReceiver = errorReceiver;

		Thread playerTask = new Thread(mMoviePlayerTask, TAG_PLAYER_TASK);
		playerTask.setUncaughtExceptionHandler(h);
		playerTask.start();

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
    	LogWrapper.LOGD(TAG, "request prepare(" + src_movie + ")");
    	synchronized (mSync) {
			if (mState == STATE_PREPARED)
				return;
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
    	LogWrapper.LOGD(TAG, "request play()");
    	synchronized (mSync) {
    		if (mState == STATE_PLAYING)
    			return;
    		mRequest = REQ_START;
    		mSync.notifyAll();
    	}
	}

    /**
     * request to seek to specifc timed frame<br>
     * if the frame is not a key frame, frame image will be broken
     * @param newTime seek to new time[usec]
     */
    public final void seek(final long newTime) {
    	LogWrapper.LOGD(TAG, "request seek(" + newTime + ")");
    	synchronized (mSync) {
    		mRequest = REQ_SEEK;
    		mRequestTime = newTime;
    		mSync.notifyAll();
    	}
    }

    public final void finishing() {
    	LogWrapper.LOGV(TAG, "request finishing()");
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
    	LogWrapper.LOGV(TAG, "request stop()");
    	synchronized (mSync) {
    		if (mState == STATE_STOP)
    			return;

			mRequest = REQ_STOP;
			mSync.notifyAll();
//			try {
//				mSync.wait(50);
//			} catch (final InterruptedException e) {
//				// ignore
//			}

    	}
    }

    /**
     * request pause playing
     */
    public final void pause() {
    	LogWrapper.LOGV(TAG, "request pause()");
    	synchronized (mSync) {
    		mRequest = REQ_PAUSE;
    		mSync.notifyAll();
    	}
    }

    /**
     * request resume from pausing
     */
    public final void resume() {
    	LogWrapper.LOGV(TAG, "request resume()");
    	synchronized (mSync) {
    		mRequest = REQ_RESUME;
    		mSync.notifyAll();
    	}
    }

    /**
     * release releated resources
     */
    public final void release() {
    	LogWrapper.LOGV(TAG, "request release()");
//    	stop();
    	synchronized (mSync) {
    		mRequest = REQ_QUIT;
    		mSync.notifyAll();
    	}
    }

//================================================================================
    private static final int TIMEOUT_USEC = 10000;	// 10msec

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
	private static final int REQ_FINISHING = 4;
    private static final int REQ_STOP = 5;
    private static final int REQ_PAUSE = 6;
    private static final int REQ_RESUME = 7;
    private static final int REQ_QUIT = 8;
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

	private H264SoftDecoder mH264SoftDecoder;
	private ByteBuffer mVideoSoftDecodeInputBuffer;
	private ByteBuffer mYDecodeOutBuffer;
	private ByteBuffer mUDecodeOutBuffer;
	private ByteBuffer mVDecodeOutBuffer;

	private MediaCodec mVideoMediaCodec;
	private MediaCodec.BufferInfo mVideoBufferInfo;
	private ByteBuffer[] mVideoInputBuffers;
	private ByteBuffer[] mVideoOutputBuffers;
	private long mVideoStartTime;

	private long previousVideoPresentationTimeUs = -1;
	private volatile int mVideoTrackIndex = -1;
	private volatile boolean mVideoInputDone = true;
	private volatile boolean mVideoOutputDone = true;
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

	private long previousAudioPresentationTimeUs = -1;
	private volatile int mAudioTrackIndex = - 1;
	private volatile boolean mAudioInputDone = true;
	private volatile boolean mAudioOutputDone = true;
	private int mAudioChannels;
	private int mAudioSampleRate;
	private int mAudioInputBufSize;
	private boolean mHasAudio;
	private byte[] mAudioOutTempBuf;
	private AudioTrack mAudioTrack;

//--------------------------------------------------------------------------------
	private Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
		public void uncaughtException(Thread th, Throwable ex) {
			LogWrapper.LOGE(TAG, String.format("Thread(%s) uncaught exception:%s", th.getName(), ex.toString()));
			ex.printStackTrace();
//			if (th.getName() == TAG_VIDEO_TASK) {
//				mErrorReceiver.onError(IMP4Player.EventCallBack.ERROR_VIDEO_DECODE_ERROR, ex.toString());
//			} else if (th.getName() == TAG_AUDIO_TASK) {
//
//			}  else if (th.getName() == TAG_PLAYER_TASK) {
//				mErrorReceiver.onError(IMP4Player.EventCallBack.ERROR_VIDEO_DECODE_ERROR, ex.toString());
//			}
		}
	};

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
						//继续执行
						LogWrapper.LOGE(TAG, "MoviePlayerTask:", e);
					} catch (final Throwable e) {
						LogWrapper.LOGE(TAG, "MoviePlayerTask:", e);
						mErrorReceiver.onError(IMP4Player.EventCallBack.ERROR_VIDEO_CODEC_ERROR, e.toString());
						handleStop();
					}
				} // end while (local_isRunning)
			} finally {
				LogWrapper.LOGV(TAG, "player task finished, local_isRunning=" + local_isRunning);
				handleStop();
				mVideoConsumer.release();
				mCallback = null;
				mVideoConsumer = null;
				mAudioConsumer = null;
				mErrorReceiver = null;
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
			LogWrapper.LOGV(TAG, "VideoTask:start");
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
					LogWrapper.LOGE(TAG, "VideoTask:", e);
					synchronized (mVideoTask) {//发生错误，停止解码
						mVideoInputDone = mVideoOutputDone = true;
						mErrorReceiver.onError(IMP4Player.EventCallBack.ERROR_VIDEO_DECODE_ERROR, e.toString());
						mVideoTask.notifyAll();
					}
				}
			} // end of for
			LogWrapper.LOGV(TAG, "VideoTask:finished");
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
			LogWrapper.LOGV(TAG, "AudioTask:start");
			for (; mIsRunning && !mAudioInputDone && !mAudioOutputDone ;) {
				try {
			        if (!mAudioInputDone) {
			        	handleInputAudio();
			        }
					if (!mAudioOutputDone) {
						handleOutputAudio(mCallback);
					}
				} catch (final Exception e) {
					LogWrapper.LOGE(TAG, "VideoTask:", e);
					break;
				}
			} // end of for
			LogWrapper.LOGV(TAG, "AudioTask:finished");
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
	 */
	private final boolean runStopStateByPlayerTask(final int req) throws InterruptedException {
		boolean local_isRunning = true;
		switch (req) {
            case REQ_PREPARE:
                handlePrepare(mSourcePath);
                break;
            case REQ_START:
            case REQ_PAUSE:
            case REQ_RESUME:
			case REQ_FINISHING:
				LogWrapper.LOGW(TAG, "runStopStateByPlayerTask invalid req:" + req);
//                throw new IllegalStateException("invalid state:" + mState);
                break;
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
				LogWrapper.LOGW(TAG, "runPreparedStateByPlayerTask invalid req:" + req);
				break;
//				throw new IllegalStateException("invalid state:" + mState);
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
//				mRequestTime = 1;
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
				LogWrapper.LOGW(TAG, "runFinishingStateByPlayerTask invalid req:" + req);
//				throw new IllegalStateException("invalid state:" + mState);
				break;
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
				LogWrapper.LOGW(TAG, "runPlayingStateByPlayerTask invalid req:" + req);
//				throw new IllegalStateException("invalid state:" + mState);
				break;
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
				LogWrapper.LOGW(TAG, "runPausedStateByPlayerTask invalid req:" + req);
//                throw new IllegalStateException("invalid state:" + mState);
                break;
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
	private final void handlePrepare(final String source_file) {
		LogWrapper.LOGV(TAG, "handlePrepare(" + source_file + ")");

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

		mCanHardDecodeH264 = CodecSupportCheck.isSupportH264(mVideoWidth, mVideoHeight);
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
		if (mVideoMediaExtractor != null) {
			mVideoMediaExtractor.release();
		}
		mVideoMediaExtractor = new MediaExtractor();
		try {
			mVideoMediaExtractor.setDataSource(sourceFile);
			trackIndex = selectTrack(mVideoMediaExtractor, "video/");
			if (trackIndex >= 0) {
				mVideoMediaExtractor.selectTrack(trackIndex);
		        final MediaFormat format = mVideoMediaExtractor.getTrackFormat(trackIndex);
	        	mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
	        	mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
	        	mVideoConsumer.onTextureInfo(mVideoWidth, mVideoHeight);
//	        	mDuration = format.getLong(MediaFormat.KEY_DURATION);
//				mBitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);
//				mFrameRate = format.getFloat(MediaFormat.KEY_FRAME_RATE);
//				LogWrapper.LOGV(TAG, String.format("format:size(%d,%d),duration=%d,bps=%d,framerate=%f", mVideoWidth, mVideoHeight, mDuration, mBitrate, mFrameRate));
			}
		} catch (final IOException e) {
			LogWrapper.LOGW(TAG, e);
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

		if (mAudioTrack != null) {
			if (mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
				mAudioTrack.stop();
			}
			mAudioTrack.release();
		}

		if (mAudioMediaExtractor != null) {
			mAudioMediaExtractor.release();
		}

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
		        LogWrapper.LOGV(TAG, String.format("getMinBufferSize=%d,max_input_size=%d,mAudioInputBufSize=%d",min_buf_size, max_input_size, mAudioInputBufSize));
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
		        	LogWrapper.LOGE(TAG, "failed to start audio track playing", e);
		    		mAudioTrack.release();
		        	mAudioTrack = null;
		        }
			}
		} catch (final IOException e) {
			LogWrapper.LOGW(TAG, e);
		}
		return trackIndex;
	}

//	private int ind = 0;
	/**prepare 或 finishing状态下调用
	 * 创建解码器/启动视频与音频线程
	 * **/
	private final void handleStart() {
    	LogWrapper.LOGD(TAG, "handleStart()");
		synchronized (mSync) {
			mState = STATE_PLAYING;
			if (mCallback != null)
				mCallback.onStart();
		}
        if (mRequestTime > 0) {
        	handleSeek(mRequestTime);
        }

        previousVideoPresentationTimeUs = previousAudioPresentationTimeUs = -1;
		mVideoStartTime = mAudioStartTime = -1;

		if (mVideoMediaCodec != null || mH264SoftDecoder != null) {
			//解码器能重用，但是extrator最好是新新建，否则会出现花屏/解码异常情况
			mVideoTrackIndex = selectVideoTrackByPlayerTask(mSourcePath);
		}
		if (mAudioMediaCodec != null) {
			mAudioTrackIndex = selectAudioTrackByPlayerTask(mSourcePath);
		}
        //-----初始化硬解码器或软解码器-----
		mVideoInputDone = mVideoOutputDone = true;
		Thread videoThread = null, audioThread = null;
		if (mVideoTrackIndex >= 0) {
		    if (mCanHardDecodeH264) {
		    	if (mVideoMediaCodec == null) {
					MediaCodec codec = internalStartVideo(mVideoMediaExtractor, mVideoTrackIndex);
					if (codec != null) {
//						if (ind % 2 == 0) {
//							codec = null;
//						}
//						ind++;
						mVideoMediaCodec = codec;
						mVideoBufferInfo = new MediaCodec.BufferInfo();
						mVideoInputBuffers = codec.getInputBuffers();
						mVideoOutputBuffers = codec.getOutputBuffers();
					} else {
						LogWrapper.LOGW(TAG, "create video hw decoder fail, change to soft decoder");
						//硬解码器创建失败,降级走软解
						mCanHardDecodeH264 = false;
						mVideoConsumer.choseRenderMode(2);
						mH264SoftDecoder = internalStartVideoWithSoftDecode(mVideoMediaExtractor, mVideoTrackIndex);
					}
				} else {
					LogWrapper.LOGD(TAG, "resuse video hw decoder");
				}
            } else {
		    	if (mH264SoftDecoder == null) {
					mH264SoftDecoder = internalStartVideoWithSoftDecode(mVideoMediaExtractor, mVideoTrackIndex);
				} else {
					LogWrapper.LOGD(TAG, "resuse video soft decoder");
				}
            }

			mVideoInputDone = mVideoOutputDone = false;
			videoThread = new Thread(mVideoTask, TAG_VIDEO_TASK);
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
				} else {
					//TODO 创建音频解码器失败 抛出异常
				}
			} else {
				LogWrapper.LOGD(TAG, "resuse audio decoder");
			}
			mAudioInputDone = mAudioOutputDone = false;
	        audioThread = new Thread(mAudioTask, TAG_AUDIO_TASK);
		}

		if (videoThread != null) {
			videoThread.setUncaughtExceptionHandler(h);
			videoThread.start();//开启视频解码线程
		}

		if (audioThread != null) {
			audioThread.setUncaughtExceptionHandler(h);
			audioThread.start();//开启音频解码线程
		}
	}

	protected H264SoftDecoder internalStartVideoWithSoftDecode(final MediaExtractor media_extractor, final int trackIndex) {
		 LogWrapper.LOGV(TAG, "internalStartVideoWithSoftDecode()");
		H264SoftDecoder softDecoder = null;
		if (trackIndex >= 0) {
			final MediaFormat format = media_extractor.getTrackFormat(trackIndex);
			final String mime = format.getString(MediaFormat.KEY_MIME);

			ByteBuffer spsByteBuffer = format.getByteBuffer("csd-0");
			ByteBuffer ppsByteBuffer = format.getByteBuffer("csd-1");

//			int spsByteLen = spsByteBuffer.limit();
//			int ppsByteLen = ppsByteBuffer.limit();
			int cacheByteSize = mVideoWidth * mVideoHeight / 2;
			if (mVideoSoftDecodeInputBuffer == null || mVideoSoftDecodeInputBuffer.capacity() < cacheByteSize) {
				LogWrapper.LOGV(TAG, "allocate cache:" + cacheByteSize + " byte");
				mVideoSoftDecodeInputBuffer = ByteBuffer.allocateDirect(cacheByteSize);
				mVideoSoftDecodeInputBuffer.order(spsByteBuffer.order());
			}

			mVideoSoftDecodeInputBuffer.position(0);
			mVideoSoftDecodeInputBuffer.put(spsByteBuffer);
			mVideoSoftDecodeInputBuffer.put(ppsByteBuffer);
			mVideoSoftDecodeInputBuffer.flip();

			softDecoder = new H264SoftDecoder();

			Boolean initRet = softDecoder.initColorFormat(H264SoftDecoder.COLOR_FORMAT_YUV420);
			if (initRet) {
				softDecoder.consumeNalUnitsFromDirectBuffer(mVideoSoftDecodeInputBuffer, mVideoSoftDecodeInputBuffer.limit(), 0);
				LogWrapper.LOGV(TAG, "internalStartVideoWithSoftDecode:codec started");
			} else {
				softDecoder = null;
				LogWrapper.LOGV(TAG, "internalStartVideoWithSoftDecode:codec fail");
				throw new RuntimeException("internalStartVideoWithSoftDecode:codec fail");
			}

		}
		return softDecoder;
	}
	/**
	 * @param media_extractor
	 * @param trackIndex
	 * @return
	 */
	protected MediaCodec internalStartVideo(final MediaExtractor media_extractor, final int trackIndex) {
		LogWrapper.LOGV(TAG, "internalStartVideo()");
		MediaCodec codec = null;
		if (trackIndex >= 0) {
	        final MediaFormat format = media_extractor.getTrackFormat(trackIndex);
	        final String mime = format.getString(MediaFormat.KEY_MIME);
			try {
				codec = MediaCodec.createDecoderByType(mime);
				codec.configure(format, mOutputSurface, null, 0);
		        codec.start();
			} catch (final Exception e) {
				LogWrapper.LOGW(TAG, e);
				codec = null;
			}
	    	 LogWrapper.LOGV(TAG, "internalStartVideo:codec started");
		}
		return codec;
	}

	/**
	 * @param media_extractor
	 * @param trackIndex
	 * @return
	 */
	protected MediaCodec internalStartAudio(final MediaExtractor media_extractor, final int trackIndex) {
		LogWrapper.LOGV(TAG, "internalStartAudio()");
		MediaCodec codec = null;
		if (trackIndex >= 0) {
	        final MediaFormat format = media_extractor.getTrackFormat(trackIndex);
	        final String mime = format.getString(MediaFormat.KEY_MIME);
			try {
				codec = MediaCodec.createDecoderByType(mime);
				codec.configure(format, null, null, 0);
		        codec.start();
		        LogWrapper.LOGV(TAG, "internalStartAudio:codec started");
		    	//
		        final ByteBuffer[] buffers = codec.getOutputBuffers();
		        int sz = buffers[0].capacity();
		        if (sz <= 0)
		        	sz = mAudioInputBufSize;
		        LogWrapper.LOGV(TAG, "AudioOutputBufSize:" + sz);
		        mAudioOutTempBuf = new byte[sz];
			} catch (final IOException e) {
				LogWrapper.LOGW(TAG, e);
				codec = null;
			}
		}
		return codec;
	}

	private final void handleSeek(final long newTime) {
        LogWrapper.LOGD(TAG, "handleSeek("+newTime+")");
		if (newTime < 0) return;

		if (mVideoTrackIndex >= 0) {
			mVideoMediaExtractor.seekTo(newTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
	        mVideoMediaExtractor.advance();
		}
		if (mAudioTrackIndex >= 0) {
			mAudioMediaExtractor.seekTo(newTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
	        mAudioMediaExtractor.advance();
		}
        mRequestTime = -1;
	}

	private final void handleReplaySeek() {
		LogWrapper.LOGD(TAG, "handleReplaySeek mFirstAudioPTS:" + mFirstAudioPTS + ", mFirstVideoPTS=" + mFirstVideoPTS);
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
//		LogWrapper.LOGD(TAG, "handleLoopInPlayingState");

		synchronized (mSync) {
			try {
				mSync.wait();
			} catch (final InterruptedException e) {
			}
		}
        if (mVideoInputDone && mVideoOutputDone && mAudioInputDone && mAudioOutputDone) {
            LogWrapper.LOGD(TAG, "Reached EOS, looping check");
			handleFinishing();
        }
	}

    protected boolean internal_process_input_with_soft_decode(final H264SoftDecoder softDecoder, final MediaExtractor extractor, final long presentationTimeUs) {
//		 LogWrapper.LOGV(TAG, "internal_process_input_with_soft_decode:presentationTimeUs=" + presentationTimeUs);
        boolean result = true;
        boolean frame_ready = false;
        if (mIsRunning) {
            final int sample_size = extractor.readSampleData(mVideoSoftDecodeInputBuffer, 0);
			mVideoSoftDecodeInputBuffer.position(0);
			if (sample_size > 0) {
//				LogWrapper.LOGD(TAG, "extrator readSampleData nalu type: " + (mVideoSoftDecodeInputBuffer.get(4) & 0x1f) + ",sample_size: " + sample_size + ", pts: " + presentationTimeUs);
				softDecoder.consumeNalUnitsFromDirectBuffer(mVideoSoftDecodeInputBuffer, sample_size, presentationTimeUs);
//				frame_ready = softDecoder.isFrameReady();
				if (mFirstVideoPTS < 0) {
					mFirstVideoPTS = presentationTimeUs;
					LogWrapper.LOGD(TAG, "mFirstVideoPTS:" + mFirstVideoPTS);
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
//		 LogWrapper.LOGV(TAG, "internalProcessInput:presentationTimeUs=" + presentationTimeUs);
		boolean result = true;
		while (mIsRunning) {
            final int inputBufIndex = codec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER)
            	break;
            if (inputBufIndex >= 0) {
                final int size = extractor.readSampleData(inputBuffers[inputBufIndex], 0);
                if (size > 0) {
//                	if (!isAudio) {
//                		LogWrapper.LOGD(TAG, "internal_process_input video sample data dequeue size:" + size + ", pts:" + presentationTimeUs);
//					}
                	codec.queueInputBuffer(inputBufIndex, 0, size, presentationTimeUs, 0);
                	if (!isAudio) {
						if (mFirstVideoPTS < 0) {
							mFirstVideoPTS = presentationTimeUs;
							LogWrapper.LOGD(TAG, "mFirstVideoPTS:" + mFirstVideoPTS);
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
//			LogWrapper.LOGD(TAG, "mFirstVideoPTS:" + mFirstVideoPTS);
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
        	LogWrapper.LOGI(TAG, "video track input reached EOS");
        	if (mCanHardDecodeH264) {//通知硬解码起结束接收数据
                while (mIsRunning) {
                    final int inputBufIndex = mVideoMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0) {
                        mVideoMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        LogWrapper.LOGV(TAG, "sent input EOS:" + mVideoMediaCodec);
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
//    	 LogWrapper.LOGV(TAG, "handleDrainVideo:");
		boolean doRender = false;
        if (mCanHardDecodeH264) {
            while (mIsRunning && !mVideoOutputDone) { //硬解后的图像，输出到surface，并按照pts进行帧同步显示
                final int decoderStatus = mVideoMediaCodec.dequeueOutputBuffer(mVideoBufferInfo, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    return;
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    mVideoOutputBuffers = mVideoMediaCodec.getOutputBuffers();
                    LogWrapper.LOGD(TAG, "INFO_OUTPUT_BUFFERS_CHANGED:");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    final MediaFormat newFormat = mVideoMediaCodec.getOutputFormat();
                    LogWrapper.LOGD(TAG, "video decoder output format changed: " + newFormat);
                } else if (decoderStatus < 0) {
                    throw new RuntimeException(
                            "unexpected result from video decoder.dequeueOutputBuffer: " + decoderStatus);
                } else { // decoderStatus >= 0
                    doRender = false;
                    if (mVideoBufferInfo.size > 0) {
                        doRender = !internalWriteVideo(mVideoOutputBuffers[decoderStatus], null, null, 0, mVideoBufferInfo.size, mVideoBufferInfo.presentationTimeUs);

                        if (doRender) {
//                            if (frameCallback == null || !frameCallback.onFrameAvailable(mVideoBufferInfo.presentationTimeUs))
                                mVideoStartTime = adjustPresentationTime(mVideoSync, mVideoStartTime, mVideoBufferInfo.presentationTimeUs);
                        }
                    }
                    mVideoMediaCodec.releaseOutputBuffer(decoderStatus, doRender);
                    if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        LogWrapper.LOGD(TAG, "video:output EOS");
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
					if (mYDecodeOutBuffer == null) {
						LogWrapper.LOGD(TAG, "allocate SoftDecodeOutBuffer size:" + output_size + "byte");
//						mYDecodeOutBuffer = ByteBuffer.allocateDirect(output_size/2);
//						mUDecodeOutBuffer = ByteBuffer.allocateDirect(output_size/4);
//						mVDecodeOutBuffer = ByteBuffer.allocateDirect(output_size/4);
						int ySize = mVideoWidth * mVideoHeight;
						int uvSize = ySize / 4;
						mYDecodeOutBuffer = ByteBuffer.allocateDirect(ySize);
						mUDecodeOutBuffer = ByteBuffer.allocateDirect(uvSize);
						mVDecodeOutBuffer = ByteBuffer.allocateDirect(uvSize);
					}

					if (mH264SoftDecoder.isFrameReady()) {
						mH264SoftDecoder.decodeFrameToDirectBuffer(mYDecodeOutBuffer, mUDecodeOutBuffer, mVDecodeOutBuffer);

						mYDecodeOutBuffer.position(0);
						mUDecodeOutBuffer.position(0);
						mVDecodeOutBuffer.position(0);
//						long decode_info[] = {0, 0, 0, 0};
//						mH264SoftDecoder.getWriteInfoAfterDecode(decode_info);
//						LogWrapper.LOGD(TAG, "decode_info:" + decode_info.toString());
						doRender = !internalWriteVideo(mYDecodeOutBuffer, mUDecodeOutBuffer, mVDecodeOutBuffer, 0, output_size, pts);
					}
				}

//				if (doRender && !frameCallback.onFrameAvailable(pts))
				if (doRender)
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
	protected boolean internalWriteVideo(final ByteBuffer ybuffer, final ByteBuffer ubuffer, final ByteBuffer vbuffer, final int offset, final int size, final long presentationTimeUs) {
//		 LogWrapper.LOGV(TAG, "internalWriteVideo");
		if (!mCanHardDecodeH264) {
			mVideoConsumer.onYUVData(ybuffer, ubuffer, vbuffer, mVideoWidth, mVideoHeight);
		}
		return false;
	}

	private final void handleInputAudio() {
		final long presentationTimeUs = mAudioMediaExtractor.getSampleTime();
		if (mFirstAudioPTS < 0) {
			mFirstAudioPTS = presentationTimeUs;
			LogWrapper.LOGD(TAG, "mFirstAudioPTS:" + mFirstAudioPTS);
		}
/*		if (presentationTimeUs < previousAudioPresentationTimeUs) {
    		presentationTimeUs += previousAudioPresentationTimeUs - presentationTimeUs; //  + EPS;
    	}
    	previousAudioPresentationTimeUs = presentationTimeUs; */
        final boolean b = internal_process_input(mAudioMediaCodec, mAudioMediaExtractor, mAudioInputBuffers,
        		presentationTimeUs, true);
        if (!b) {
        	LogWrapper.LOGI(TAG, "audio track input reached EOS");
    		while (mIsRunning) {
                final int inputBufIndex = mAudioMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                	mAudioMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                		MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                	LogWrapper.LOGV(TAG, "sent input EOS:" + mAudioMediaCodec);
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
//		 LogWrapper.LOGV(TAG, "handleDrainAudio:");
		while (mIsRunning && !mAudioOutputDone) {
			final int decoderStatus = mAudioMediaCodec.dequeueOutputBuffer(mAudioBufferInfo, TIMEOUT_USEC);
			if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
				return;
			} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				mAudioOutputBuffers = mAudioMediaCodec.getOutputBuffers();
				LogWrapper.LOGD(TAG, "INFO_OUTPUT_BUFFERS_CHANGED:");
			} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				final MediaFormat newFormat = mAudioMediaCodec.getOutputFormat();
				LogWrapper.LOGD(TAG, "audio decoder output format changed: " + newFormat);
			} else if (decoderStatus < 0) {
				throw new RuntimeException(
					"unexpected result from audio decoder.dequeueOutputBuffer: " + decoderStatus);
			} else { // decoderStatus >= 0
				if (mAudioBufferInfo.size > 0) {
					internalWriteAudio(mAudioOutputBuffers[decoderStatus],
						0, mAudioBufferInfo.size, mAudioBufferInfo.presentationTimeUs);
//					if (!frameCallback.onFrameAvailable(mAudioBufferInfo.presentationTimeUs)) {
						mAudioStartTime = adjustPresentationTime(mAudioSync, mAudioStartTime, mAudioBufferInfo.presentationTimeUs);
//					}
				}
				mAudioMediaCodec.releaseOutputBuffer(decoderStatus, false);
				if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					LogWrapper.LOGD(TAG, "audio:output EOS");
					synchronized (mAudioTask) {
						mAudioOutputDone = true;
						mAudioTask.notifyAll();
					}
				}
				return;
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
//		LogWrapper.LOGD(TAG, "internalWriteAudio");
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
	 * playing状态检测到播放完毕会调用
	 * playing状态处理finshing请求
	 * prepare状态处理finshing请求
	 *
	 * finishing处理与stop类似，区别是只停止解码与渲染，不会销毁. 方便后续复用解码器，重新开始播放
	 * */
	private final void handleFinishing() {
		LogWrapper.LOGV(TAG, "handleFinishing()");

		synchronized (mVideoTask) {
			if (mVideoTrackIndex >= 0) {
				mVideoOutputDone = true;
				while (!mVideoInputDone) {
					try {
						mVideoTask.wait(); //等待视频线程停止input
					} catch (final InterruptedException e) {
						break;
					}
				}
			}

//			if (mCanHardDecodeH264 && mVideoBufferInfo != null && (mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				if (mVideoMediaCodec != null) {
					LogWrapper.LOGD(TAG, "mVideoMediaCodec.flush()");
					mVideoMediaCodec.flush();
				}
//			}

			mVideoOutputDone = mVideoInputDone = true;
		}

		synchronized (mAudioTask) {
			if (mAudioTrackIndex >= 0) {
				mAudioOutputDone = true;
				while (!mAudioInputDone) {
					try {
						mAudioTask.wait(); //等待音频线程停止input
					} catch (final InterruptedException e) {
						break;
					}
				}
			}

//			if (mAudioBufferInfo != null && (mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				if (mAudioMediaCodec != null) {
					LogWrapper.LOGD(TAG, "mAudioMediaCodec.flush()");
					mAudioMediaCodec.flush();
				}
//			}
			mAudioOutputDone = mAudioInputDone = true;
		}

		synchronized (mSync) {
			mState = STATE_FINISHING;
		}

		if (mCallback != null)
			mCallback.onFinishing();
	}

	/**prepare状态下处理stop请求
	 * playing状态下处理stop请求
	 * pause状态下处理stop请求
	 *finishing状态下处理stop请求
	 * 退出player线程后执行
	 * **/
	private final void handleStop() {
		LogWrapper.LOGV(TAG, "handleStop()");
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

    	try {
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

			if (mH264SoftDecoder != null) {
				mH264SoftDecoder.nativeDestroy();
				mH264SoftDecoder = null;
			}
		} catch (Exception e) {
			LogWrapper.LOGW(TAG, "release resource exception:" + e);
		}


        mVideoSoftDecodeInputBuffer = null;
		mYDecodeOutBuffer = mUDecodeOutBuffer = mVDecodeOutBuffer = null;

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
		 LogWrapper.LOGV(TAG, "internalStopVideo:");
	}

	protected void internalStopAudio() {
		 LogWrapper.LOGV(TAG, "internalStopAudio:");
    	if (mAudioTrack != null) {
			try {
				if (mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
					mAudioTrack.stop();
				}
				mAudioTrack.release();
			} catch (Exception e) {
				 LogWrapper.LOGV(TAG, "internalStopAudio exception:" + e);
			} finally {
				mAudioTrack = null;
			}
    	}
		mAudioOutTempBuf = null;
	}

	private final void handlePause() {
    	 LogWrapper.LOGV(TAG, "handlePause:");
    	// FIXME unimplemented yet
	}

	private final void handleResume() {
    	 LogWrapper.LOGV(TAG, "handleResume:");
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
            	LogWrapper.LOGD(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }
        return -1;
    }

    /**音视频子线程检测能否进入stop状态**/
    private void checkCanStopBySubTask() {

	}
}
