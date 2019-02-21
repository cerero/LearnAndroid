#include <jni.h>

#include <boost/regex.hpp>
#include <iostream>
#include <sstream>
#include <string>

#include "mylog.h"

void naExtractSubject(JNIEnv* pEnv, jclass clazz, jstring pInputStr);

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[1];
	nm[0].name = "naExtractSubject";
	nm[0].signature = "(Ljava/lang/String;)V";
	nm[0].fnPtr = (void*)naExtractSubject;
	jclass cls = env->FindClass("cookbook/chapter8/portingboost/MainActivity");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 1);
	return JNI_VERSION_1_6;
}

void naExtractSubject(JNIEnv* pEnv, jclass clazz, jstring pInputStr) {
    std::string line;
    boost::regex pat( "^Subject: (Re: |Aw: )*(.*)" );
    const char *str;
    str = pEnv->GetStringUTFChars(pInputStr, NULL);

    std::stringstream stream;	//TODO: ??? valid call
    stream << str;

    while (1) {
        std::getline(stream, line);
        boost::smatch matches;
        LOGI(1, "%s", line.c_str());
        //check out the details of regex_match
        if (boost::regex_match(line, matches, pat)) {
            LOGI(1, "matched: %s", matches[0].str().c_str());
        } else {
        	LOGI(1, "not matched");
        }
        if (stream.eof()) {
        	break;
        }
    }
}
