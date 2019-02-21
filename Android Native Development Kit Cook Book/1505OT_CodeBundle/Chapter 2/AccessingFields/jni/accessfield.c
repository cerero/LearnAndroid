#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include "accessfield.h"

JNIEXPORT void JNICALL Java_cookbook_chapter2_AccessingFieldsActivity_AccessStaticFieldDemo(JNIEnv *pEnv, jobject pObj) {
	//	jfieldID GetStaticFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	//	<NativeType> GetStatic<Type>Field(JNIEnv *env,jclass clazz, jfieldID fieldID);
	//	void SetStatic<Type>Field(JNIEnv *env, jclass clazz, jfieldID fieldID,<NativeType> value);
	jfieldID sintFID, sstrFID, sintArrFID, sdummyFID;
	jclass cls = (*pEnv)->GetObjectClass(pEnv, pObj);

	sintFID = (*pEnv)->GetStaticFieldID(pEnv, cls, "sintF", "I");
	//get the integer field
	jint sint = (*pEnv)->GetStaticIntField(pEnv, cls, sintFID);
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticFieldDemo", "sintF: %d", sint);
	//set the integer field to value 123
	(*pEnv)->SetStaticIntField(pEnv, cls, sintFID, 123);

	sstrFID = (*pEnv)->GetStaticFieldID(pEnv, cls, "sstrF", "Ljava/lang/String;");
	//get the string field and read the content
	jobject sstr = (*pEnv)->GetStaticObjectField(pEnv, cls, sstrFID);
	const char* str = (*pEnv)->GetStringUTFChars(pEnv, sstr, NULL);
	if (str == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "AccessStaticFieldDemo", "failed GetStringUTFChars for sstr");
	}
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticFieldDemo", "sstrF: %s", str);
	(*pEnv)->ReleaseStringUTFChars(pEnv, sstr, str);
	//set the string field to a new string
	jstring newStr = (*pEnv)->NewStringUTF(pEnv, "hello from native");
	(*pEnv)->SetStaticObjectField(pEnv, cls, sstrFID, newStr);

	sintArrFID = (*pEnv)->GetStaticFieldID(pEnv, cls, "sintArrF", "[I");
	//get the int array field and read the value of each element
	jobject sintarr = (*pEnv)->GetStaticObjectField(pEnv, cls, sintArrFID);
	jint *intArr = (*pEnv)->GetIntArrayElements(pEnv, sintarr, NULL);
	int i;
	jsize len = (*pEnv)->GetArrayLength(pEnv, sintarr);
	for (i = 0; i < len; ++i) {
		__android_log_print(ANDROID_LOG_INFO, "AccessStaticFieldDemo", "sintArrF %d: %d", i, intArr[i]);
	}
	(*pEnv)->ReleaseIntArrayElements(pEnv, sintarr, intArr, 0);
	//set the int array field to a newly created array
	jintArray newIntArr = (*pEnv)->NewIntArray(pEnv, 5);
	jint *newIntArrNative = (*pEnv)->GetIntArrayElements(pEnv, newIntArr, NULL);
	for (i = 0; i < 5; ++i) {
		newIntArrNative[i] = i;
	}
	(*pEnv)->ReleaseIntArrayElements(pEnv, newIntArr, newIntArrNative, 0);
	(*pEnv)->SetStaticObjectField(pEnv, cls, sintArrFID, newIntArr);

	sdummyFID = (*pEnv)->GetStaticFieldID(pEnv, cls, "sdummyF", "Lcookbook/chapter2/Dummy;");
	//get the Dummy object field
	jobject sdummy = (*pEnv)->GetStaticObjectField(pEnv, cls, sdummyFID);
	jclass dummyCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Dummy");
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticFieldDemo", "sdummy is instance of dummyCls: %d", (*pEnv)->IsInstanceOf(pEnv, sdummy, dummyCls));
	jfieldID dummyValueFID = (*pEnv)->GetFieldID(pEnv, dummyCls, "value", "I");
	jint dummyValue = (*pEnv)->GetIntField(pEnv, sdummy, dummyValueFID);
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticFieldDemo", "sdummyF value: %d", dummyValue);
	//set the Dummy object field to a newly created one
	jmethodID dummyConstructor = (*pEnv)->GetMethodID(pEnv, dummyCls, "<init>","(I)V");
	jobject newDummy = (*pEnv)->NewObject(pEnv, dummyCls, dummyConstructor, 100);
	(*pEnv)->SetStaticObjectField(pEnv, cls, sdummyFID, newDummy);
}

JNIEXPORT void JNICALL Java_cookbook_chapter2_AccessingFieldsActivity_AccessInstanceFieldDemo(JNIEnv *pEnv, jobject pObj) {
	//	jfieldID GetFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	//	<NativeType> Get<Type>Field(JNIEnv *env,jobject obj, jfieldID fieldID);
	//	void Set<Type>Field(JNIEnv *env, jobject obj, jfieldID fieldID, <NativeType> value);
	jfieldID intFID, strFID, intArrFID, dummyFID;
	jclass cls = (*pEnv)->GetObjectClass(pEnv, pObj);

	intFID = (*pEnv)->GetFieldID(pEnv, cls, "intF", "I");
	//get the integer field
	jint aint = (*pEnv)->GetIntField(pEnv, pObj, intFID);
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceFieldDemo", "intF: %d", aint);
	//set the integer field to value 123
	(*pEnv)->SetIntField(pEnv, pObj, intFID, 123);

	strFID = (*pEnv)->GetFieldID(pEnv, cls, "strF", "Ljava/lang/String;");
	//get the string field and read the content
	jobject astr = (*pEnv)->GetObjectField(pEnv, pObj, strFID);
	const char* str = (*pEnv)->GetStringUTFChars(pEnv, astr, NULL);
	if (str == NULL) {
		__android_log_print(ANDROID_LOG_INFO, "AccessInstanceFieldDemo", "failed GetStringUTFChars for sstr");
	}
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceFieldDemo", "strF: %s", str);
	(*pEnv)->ReleaseStringUTFChars(pEnv, astr, str);
	//set the string field to a new string
	jstring newStr = (*pEnv)->NewStringUTF(pEnv, "hello from native 2");
	(*pEnv)->SetObjectField(pEnv, pObj, strFID, newStr);

	intArrFID = (*pEnv)->GetFieldID(pEnv, cls, "intArrF", "[I");
	//get the int array field and read the value of each element
	jobject intarr = (*pEnv)->GetObjectField(pEnv, pObj, intArrFID);
	jint *intArr = (*pEnv)->GetIntArrayElements(pEnv, intarr, NULL);
	int i;
	jsize len = (*pEnv)->GetArrayLength(pEnv, intarr);
	for (i = 0; i < len; ++i) {
		__android_log_print(ANDROID_LOG_INFO, "AccessInstanceFieldDemo", "intArrF %d: %d", i, intArr[i]);
	}
	(*pEnv)->ReleaseIntArrayElements(pEnv, intarr, intArr, 0);
	//set the int array field to a newly created array
	jintArray newIntArr = (*pEnv)->NewIntArray(pEnv, 3);
	jint *newIntArrNative = (*pEnv)->GetIntArrayElements(pEnv, newIntArr, NULL);
	for (i = 0; i < 3; ++i) {
		newIntArrNative[i] = i*10;
	}
	(*pEnv)->ReleaseIntArrayElements(pEnv, newIntArr, newIntArrNative, 0);
	(*pEnv)->SetObjectField(pEnv, pObj, intArrFID, newIntArr);

	dummyFID = (*pEnv)->GetFieldID(pEnv, cls, "dummyF", "Lcookbook/chapter2/Dummy;");
	//get the Dummy object field
	jobject adummy = (*pEnv)->GetObjectField(pEnv, pObj, dummyFID);
	jclass dummyCls = (*pEnv)->FindClass(pEnv, "cookbook/chapter2/Dummy");
	__android_log_print(ANDROID_LOG_INFO, "AccessInstanceFieldDemo", "dummyF is instance of dummyCls: %d", (*pEnv)->IsInstanceOf(pEnv, adummy, dummyCls));
	jfieldID dummyValueFID = (*pEnv)->GetFieldID(pEnv, dummyCls, "value", "I");
	jint dummyValue = (*pEnv)->GetIntField(pEnv, adummy, dummyValueFID);
	__android_log_print(ANDROID_LOG_INFO, "AccessStaticFieldDemo", "dummyF value: %d", dummyValue);
	//set the Dummy object field to a newly created one
	jmethodID dummyConstructor = (*pEnv)->GetMethodID(pEnv, dummyCls, "<init>","(I)V");
	jobject newDummy = (*pEnv)->NewObject(pEnv, dummyCls, dummyConstructor, 200);
	(*pEnv)->SetObjectField(pEnv, pObj, dummyFID, newDummy);
}

JNIEXPORT jobject JNICALL Java_cookbook_chapter2_AccessingFieldsActivity_FieldReflectionDemo(JNIEnv *pEnv, jobject pObj, jobject pField, jobject pDummy) {
//	jfieldID FromReflectedField(JNIEnv *env, jobject field);
//	jobject ToReflectedField(JNIEnv *env, jclass cls, jfieldID fieldID, jboolean isStatic);
	jclass dummyCls = (*pEnv)->GetObjectClass(pEnv, pDummy);
	jfieldID fid = (*pEnv)->FromReflectedField(pEnv, pField);
	jint dummyValue = (*pEnv)->GetIntField(pEnv, pDummy, fid);
	__android_log_print(ANDROID_LOG_INFO, "FieldReflectionDemo", "dummy value: %d", dummyValue);

	jfieldID fid2 = (*pEnv)->GetStaticFieldID(pEnv, dummyCls, "value2", "I");
	return (*pEnv)->ToReflectedField(pEnv, dummyCls, fid2, JNI_TRUE);
}

//jfieldID GetFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
//name: the field name in a 0-terminated UTF-8 string.
//sig: the field descriptor in a 0-terminated UTF-8 string.
//Returns a field ID, or NULL if the operation fails.

//GetFieldID performs a symbolic lookup on a given class and returns the field ID of a named instance field.
//Functions of the Get<Type>Field and Set<Type>Field families access instance fields.

//GetStaticFieldID performs a symbolic lookup on a given class or interface and returns the field ID of a named static field.
//Functions of the GetStatic<Type>Field and SetStatic<Type>Field families access static fields.

//FromReflectedField converts instances of java.lang.reflect.Field in the
//Java Core Reflection API into field IDs. FromReflectedField is new in Java 2 SDK release 1.2.

//FromReflectedMethod converts instances of java.lang.reflect.Method or
//instances of java.lang.reflect.Constructor into method IDs. From-ReflectedMethod
//is new in Java 2 SDK release 1.2.

//ToReflectedField and ToReflectedMethod carry out the conversions in the opposite direction.
//Both functions are new in Java 2 SDK release 1.2.


