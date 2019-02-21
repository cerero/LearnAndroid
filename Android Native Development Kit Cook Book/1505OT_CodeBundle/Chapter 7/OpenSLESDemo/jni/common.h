#ifndef COMMON_H_
#define COMMON_H_

#include <jni.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

#include <stdio.h>
#include <assert.h>

//engine interfaces
SLEngineItf engineEngine;
SLObjectItf engineObject;

//recorder interfaces
SLObjectItf recorderObject;
SLRecordItf recorderRecord;
SLAndroidSimpleBufferQueueItf recorderBufferQueue;

// 5 seconds of recorded audio at 16 kHz mono, 16-bit signed little endian
#define RECORDER_FRAMES (16000 * 5)
#define RECORD_TIME 15
static short recorderBuffer[RECORDER_FRAMES];
static FILE *recordF;
static int recordCnt = 0;

// buffer queue player interfaces
SLObjectItf bqPlayerObject;
SLPlayItf bqPlayerPlay;
SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
SLEffectSendItf bqPlayerEffectSend;
SLVolumeItf bqPlayerVolume;

//output mix interfaces
SLObjectItf outputMixObject;
SLEnvironmentalReverbItf outputMixEnvironmentalReverb;

// aux effect on the output mix, used by the buffer queue player
static const SLEnvironmentalReverbSettings reverbSettings =
    SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;


#endif /* COMMON_H_ */
