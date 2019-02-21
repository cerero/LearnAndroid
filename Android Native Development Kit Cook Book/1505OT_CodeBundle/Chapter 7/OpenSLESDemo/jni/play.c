#include "common.h"
#include "mylog.h"

int noMoreData = 0;

// this callback handler is called every time a buffer finishes playing
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    // for streaming playback, replace this test by logic to find and fill the next buffer
    if (!noMoreData) {
        SLresult result;
		int numOfRecords = fread(recorderBuffer, sizeof(short), RECORDER_FRAMES, recordF);
		if (RECORDER_FRAMES != numOfRecords) {
			if (numOfRecords <= 0) {
				noMoreData = 1;
				LOGI(2, "no data for playing");
				(*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
				fclose(recordF);
				LOGI(2, "player finished playing");
				return;
			}
			LOGE(2, "no more data");
			noMoreData = 1;
		} else {
			LOGI(2, "read %d", numOfRecords);
		}
		result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, recorderBuffer,  RECORDER_FRAMES * sizeof(short));
        // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
        // which for this code example would indicate a programming error
        assert(SL_RESULT_SUCCESS == result);
    } else {
    	(*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
    	fclose(recordF);
    	LOGI(2, "player finished playing");
    }
}

// create buffer queue audio player
void createBufferQueueAudioPlayer() {
    SLresult result;
    // configure audio source
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 1};
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_16,
        SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
        SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};
    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};
    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc, &audioSnk,
            3, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    // realize the player
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    // get the play interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);
    // get the buffer queue interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
            &bqPlayerBufferQueue);
    if (SL_RESULT_SUCCESS!=result) {
    	LOGE(1, "cannot get buffer queue interface");
    }
    assert(SL_RESULT_SUCCESS == result);
    // register callback on the buffer queue
    result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, NULL);
    assert(SL_RESULT_SUCCESS == result);
    // get the effect send interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_EFFECTSEND,
            &bqPlayerEffectSend);
    assert(SL_RESULT_SUCCESS == result);
    // get the volume interface
    result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME, &bqPlayerVolume);
    assert(SL_RESULT_SUCCESS == result);
    LOGI(2, "player created");
}

jboolean startPlaying() {
	SLresult result;
	//open the file to read data
	recordF = fopen("/sdcard/test.pcm", "rb");
	if (NULL == recordF) {
		LOGE(1, "cannot open pcm file to read");
		return JNI_FALSE;
	}
	noMoreData = 0;
	//fill the two buffers for playing first
	int numOfRecords = fread(recorderBuffer, sizeof(short), RECORDER_FRAMES, recordF);
	if (RECORDER_FRAMES != numOfRecords) {
		if (numOfRecords <= 0) {
			LOGI(2, "no data for playing");
			return JNI_TRUE;
		}
		noMoreData = 1;
		LOGE(2, "no more data");
	} else {
		LOGI(2, "read %d", numOfRecords)
	}
	result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, recorderBuffer, RECORDER_FRAMES * sizeof(short));
	if (SL_RESULT_SUCCESS != result) {
		return JNI_FALSE;
	}
	// set the player's state to playing
	result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
	assert(SL_RESULT_SUCCESS == result);
	LOGI(2, "player started playing");
	return JNI_TRUE;
}




