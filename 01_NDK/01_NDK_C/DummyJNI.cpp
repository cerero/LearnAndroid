#include <iostream>

struct JNIEnv_;
typedef JNIEnv_ JNIEnv;

typedef struct JNINativeInterface_ {
  const char * (*NewStringUTF)(JNIEnv *env, const char *);
} JNINativeInterface_;

const char * NewStringUTF(JNIEnv *env, const char *str) {
  return str;
}

struct JNIEnv_ {
  JNINativeInterface_ *jniIntterface;

  const char * NewStringUTF(const char *str) {
    return jniIntterface->NewStringUTF(this, str);
  }
};

int main() {
  JNINativeInterface_ jniIntterface;
  jniIntterface.NewStringUTF = NewStringUTF;

  JNIEnv jniEnv;
  jniEnv.jniIntterface = &jniIntterface;

  JNIEnv *env = &jniEnv;
  const char *str = env->NewStringUTF("Dummy JNI");
  printf("%s\n", str);
  return 0;
}
