#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <GLES/gl.h>
#include <math.h>

#include "dm04/DiceG1.h"
#include "dm04/Cube.h"
#include "dm02/mylog.h"
using namespace dm04;

void naLoadTexture(JNIEnv* env, jclass clazz, jobject pBitmap, int pWidth, int pHeight, int pId);
void naInitGL1x_dm04(JNIEnv* env, jclass clazz);
void naDrawGraphics_dm04(JNIEnv* env, jclass clazz, float pAngleX, float pAngleY);
void naSurfaceChanged_dm04(JNIEnv* env, jclass clazz, int width, int height);

void DiceG1::OnLoad(JNIEnv* env, void* reserved)
{
	JNINativeMethod nm[4];
	nm[0].name = "naInitGL1x";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)naInitGL1x_dm04;
	nm[1].name = "naDrawGraphics";
	nm[1].signature = "(FF)V";
	nm[1].fnPtr = (void*)naDrawGraphics_dm04;
	nm[2].name = "naSurfaceChanged";
	nm[2].signature = "(II)V";
	nm[2].fnPtr = (void*)naSurfaceChanged_dm04;
	nm[3].name = "naLoadTexture";
	nm[3].signature = "(Landroid/graphics/Bitmap;III)V";
	nm[3].fnPtr = (void*)naLoadTexture;

    jclass cls = env->FindClass("com/example/videodecoderandrender/demo/chap04/dm04/MyRenderer");
    env->RegisterNatives(cls, nm, 4);
}

GLfloat vertices_dm04[108] = {
		-1.0,-1.0,1.0,  1.0,1.0,1.0,   1.0,-1.0,1.0,    //465
		-1.0,-1.0,1.0,  -1.0,1.0,1.0,  1.0,1.0,1.0,     //476
		1.0,-1.0,-1.0,   -1.0,1.0,-1.0,   -1.0,-1.0,-1.0,//130
		1.0,-1.0,-1.0,   1.0,1.0,-1.0,    -1.0,1.0,-1.0, //123
		1.0,-1.0,1.0,   1.0,1.0,-1.0,   1.0,-1.0,-1.0,   //521
		1.0,-1.0,1.0,   1.0,1.0,1.0,    1.0,1.0,-1.0,    //562
		-1.0,-1.0,-1.0,  -1.0,1.0,1.0,  -1.0,-1.0,1.0,   //074
		-1.0,-1.0,-1.0,  -1.0,1.0,-1.0, -1.0,1.0,1.0,    //037
		-1.0,-1.0,-1.0,   1.0,-1.0,1.0,   1.0,-1.0,-1.0, //051
		-1.0,-1.0,-1.0,  -1.0,-1.0,1.0,   1.0,-1.0,1.0,  //045
		-1.0,1.0,1.0,    1.0,1.0,-1.0,  1.0,1.0,1.0,    //726
		-1.0,1.0,1.0,   -1.0,1.0,-1.0,  1.0,1.0,-1.0  //732
};

GLfloat texCoords_dm04[12] = {
		0.0f, 0.0f,    1.0f, 1.0f,    1.0f, 0.0f,
		0.0f, 0.0f,    0.0f, 1.0f,    1.0f, 1.0f
					};

GLbyte indices_dm04[6] = {
	0, 1, 2, 3, 4, 5
};

GLuint texIds[6];

Cube mCube_dm04(vertices_dm04, texCoords_dm04, indices_dm04);

void naLoadTexture(JNIEnv* env, jclass clazz, jobject pBitmap, int pWidth, int pHeight, int pId) {
	int lRet;
	AndroidBitmapInfo lInfo;
	void* l_Bitmap;
	GLint format;
	GLenum type;
	if ((lRet = AndroidBitmap_getInfo(env, pBitmap, &lInfo)) < 0) {
		LOGE(1, "AndroidBitmap_getInfo failed! error = %d", lRet);
		return;
	}
	if (lInfo.format == ANDROID_BITMAP_FORMAT_RGB_565) {
		LOGE(1, "Bitmap format is not RGB_555!");
		format = GL_RGB;
		type = GL_UNSIGNED_SHORT_5_6_5;
	} else if (lInfo.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE(1, "Bitmap format is RGBA_8888!");
		format = GL_RGBA;
		type = GL_UNSIGNED_BYTE;
	} else {
		LOGE(1, "Bitmap format is not support!");
		return;
	}
	if ((lRet = AndroidBitmap_lockPixels(env, pBitmap, &l_Bitmap)) < 0) {
		LOGE(1, "AndroidBitmap_lockPixels() failed! error = %d", lRet);
		return;
	}
	LOGI(1, "bitmap locked: %d: %d: %d", pId, pWidth, pHeight);
	glGenTextures(1, &texIds[pId]);
	glBindTexture(GL_TEXTURE_2D, texIds[pId]);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	// Loads image data into OpenGL.
	glTexImage2D(GL_TEXTURE_2D, 0, format, pWidth, pHeight, 0,
			format, type, l_Bitmap);
	AndroidBitmap_unlockPixels(env, pBitmap);
}

void naInitGL1x_dm04(JNIEnv* env, jclass clazz) {
	glDisable(GL_DITHER);	//disable dither to improve performance with reduced quality
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);	//set clear value for color buffer as black
	glEnable(GL_CULL_FACE);		//enabled for better performance
	glClearDepthf(1.0f);	//set clear value [0, 1] for depth buffer as farthest
	glEnable(GL_DEPTH_TEST);	//do depth comparison and update depth buffer
	glDepthFunc(GL_LEQUAL);		//type of depth test
	glShadeModel(GL_SMOOTH);   // Enable smooth shading of color

	mCube_dm04.setTexCoords(texIds);
	glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
	glEnable(GL_TEXTURE_2D);
}

void naSurfaceChanged_dm04(JNIEnv* env, jclass clazz, int width, int height) {
	glViewport(0, 0, width, height);
    float ratio = (float) width / height;
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrthof(-ratio, ratio, -1, 1, -10, 10);
}

void naDrawGraphics_dm04(JNIEnv* env, jclass clazz, float pAngleX, float pAngleY) {
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    glRotatef(pAngleX, 0, 1, 0);	//rotate around y-axis
    glRotatef(pAngleY, 1, 0, 0);	//rotate around x-axis
	glScalef(0.3f, 0.3f, 0.3f);      // Scale down
	mCube_dm04.draw();
}



