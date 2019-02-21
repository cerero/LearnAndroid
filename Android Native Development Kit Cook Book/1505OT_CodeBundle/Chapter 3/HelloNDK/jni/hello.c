#include <string.h>
#include <jni.h>

jstring Java_cookbook_chapter3_HelloNDKActivity_naGetHelloNDKStr(JNIEnv* pEnv, jobject pObj)
{
    return (*pEnv)->NewStringUTF(pEnv, "Hello NDK!");
}
