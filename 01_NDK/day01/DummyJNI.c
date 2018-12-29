#include <stdio.h>

typedef struct JNINativeInterface_ *JNIEnv;

typedef struct JNINativeInterface_ {
  const char * (*NewStringUTF)(JNIEnv *env, const char *);
} JNINativeInterface_;

const char * NewStringUTF(JNIEnv *env, const char *str) {
  return str;
}
int main() {
  JNINativeInterface_ jniIntterface;
  jniIntterface.NewStringUTF = NewStringUTF;
  JNIEnv jniEnv = &jniIntterface;
  JNIEnv *env = &jniEnv;
  const char *str = (*env)->NewStringUTF(env, "Dummy JNI");
  printf("%s\n", str);
  return 0;
}
