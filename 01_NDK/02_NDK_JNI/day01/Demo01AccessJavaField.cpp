#include <cstring>
#include "Demo01AccessJavaField.h"

//访问属性
//修改属性key
JNIEXPORT void JNICALL Java_Demo01AccessJavaField_changeJavaField(JNIEnv *jEnv, jobject jobj) {
  //获取jclass
  jclass jcls = jEnv->GetObjectClass(jobj);
  //jfieldID
  //属性名称 属性签名
  jfieldID fid = jEnv->GetFieldID(jcls, "key", "Ljava/lang/String;");

  //更改属性key的值
  jstring jstr = (jstring)jEnv->GetObjectField(jobj, fid);

  //jstring-> c字符串
  //isCopy
  jboolean jbool = 1;
  const char *c_str = jEnv->GetStringUTFChars(jstr, &jbool);
  char out_str[100] = "sugar And ";
  strcat(out_str, c_str);

  jstring joutstr = jEnv->NewStringUTF(out_str);
  //c 字符串 -> jstring
  //修改key
  jEnv->SetObjectField(jobj, fid, joutstr);

  //释放NewStringUTF创建的字符串资源
  // jEnv->ReleaseStringUTFChars(joutstr, out_str);
}

//修改静态属性
JNIEXPORT void JNICALL Java_Demo01AccessJavaField_changeJavaStaticField(JNIEnv *jEnv, jobject jobj) {
  //获取jclass
  jclass jcls = jEnv->GetObjectClass(jobj);
  //jfieldID
  //属性名称 属性签名
  jfieldID fid = jEnv->GetStaticFieldID(jcls, "count", "I");

  //更改属性key的值
  jint count = (jint)jEnv->GetStaticIntField(jcls, fid);
  count++;
  //修改 count属性
  jEnv->SetStaticIntField(jcls, fid, count);
}

//访问java方法 int getAge(int base)
JNIEXPORT void JNICALL Java_Demo01AccessJavaField_callJavaMethod(JNIEnv *jEnv, jobject jobj) {
  jclass jcls = jEnv->GetObjectClass(jobj);
  jmethodID mid = jEnv->GetMethodID(jcls, "getAge", "(I)I");
  jint age = jEnv->CallIntMethod(jobj, mid, 20);
  printf("age: %d\n", age);
}


//访问java静态方法
JNIEXPORT void JNICALL Java_Demo01AccessJavaField_callJavaStaticMethod(JNIEnv *jEnv, jobject jobj) {
  jclass jcls = jEnv->GetObjectClass(jobj);
  jmethodID mid = jEnv->GetStaticMethodID(jcls, "getUUID", "()Ljava/lang/String;");
  jstring jstr = (jstring)jEnv->CallStaticObjectMethod(jcls, mid);

  jboolean jbool = 0;
  const char *c_str = jEnv->GetStringUTFChars(jstr, &jbool);

  printf("string frame java: %s\n", c_str);
}
