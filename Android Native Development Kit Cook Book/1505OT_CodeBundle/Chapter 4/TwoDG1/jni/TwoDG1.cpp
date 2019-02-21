#include <jni.h>
#include <android/log.h>
#include <GLES/gl.h>

#include <math.h>

#include "Square.h"
#include "Triangle.h"

void naInitGL1x(JNIEnv* env, jclass clazz);
void naDrawGraphics(JNIEnv* env, jclass clazz);
void naSurfaceChanged(JNIEnv* env, jclass clazz, int width, int height);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[3];
	nm[0].name = "naInitGL1x";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)naInitGL1x;
	nm[1].name = "naDrawGraphics";
	nm[1].signature = "()V";
	nm[1].fnPtr = (void*)naDrawGraphics;
	nm[2].name = "naSurfaceChanged";
	nm[2].signature = "(II)V";
	nm[2].fnPtr = (void*)naSurfaceChanged;
	jclass cls = env->FindClass("cookbook/chapter4/gl1x/MyRenderer");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 3);
	return JNI_VERSION_1_6;
}

void naInitGL1x(JNIEnv* env, jclass clazz) {
	glDisable(GL_DITHER);	//disable dither to improve performance with reduced quality
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);	//set clear value for color buffer as black
	glShadeModel(GL_SMOOTH);   // Enable smooth shading of color
//	glShadeModel(GL_FLAT);
}

void naSurfaceChanged(JNIEnv* env, jclass clazz, int width, int height) {
	/*
	 * Set the viewport. This doesn't have to be d1.0f each time
	 * draw() is called. Typically this is called when the view
	 * is resized.
	 */
	glViewport(0, 0, width, height);
//	glViewport(0, 0, width/2, height/5);

	/*
	 * Set our projection matrix. This doesn't have to be d1.0f
	 * each time we draw, but usualy a new projection needs to be set
	 * when the viewport is resized.
	 */
	float ratio = (float) width / (float)height;
	//set projection
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
//	glFrustumf(-ratio, ratio, -1, 1, 0, 1);
	glOrthof(-ratio, ratio, -1, 1, 0, 1);	//set the clipping volume
//	glOrthof(-1, 1, -1, 1, 0, 1);	//set the clipping volume
}

GLfloat trangleVertices[9] = {
	0.0f,  1.0f, 0.0f, // 0. top
	-1.0f, -1.0f, 0.0f, // 1. left-bottom
	1.0f, -1.0f, 0.0f  // 2. right-bottom
};
Triangle mTriangle(trangleVertices);

GLfloat vertices[12] = {
	-1.0f, -1.0f,  0.0f,  // 0. left-bottom
	1.0f, -1.0f,  0.0f,  // 1. right-bottom
	-1.0f,  1.0f,  0.0f,  // 2. left-top
	1.0f,  1.0f,  0.0f   // 3. right-top
};
GLfloat colors[16] = {
	1.0f, 0.0f, 0.0f, 1.0f, // Red
	0.0f, 1.0f, 0.0f, 1.0f, // Green
	0.0f, 0.0f, 1.0f, 1.0f, // Blue
	1.0f, 1.0f, 1.0f, 1.0f	// white
};
GLbyte indices[6] = {
	0, 1, 2,
	1, 2, 3
};
Square mSquare(vertices, colors, indices);

GLfloat matrix[16];

void naDrawGraphics(JNIEnv* env, jclass clazz) {
	glClear(GL_COLOR_BUFFER_BIT);
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
	glTranslatef(0.3f, 0.0f, 0.0f);  	//move to the right
	glScalef(0.2f, 0.2f, 0.2f);      	// Scale down
	mTriangle.draw();

	glLoadIdentity();
	glTranslatef(-0.3f, 0.0f, 0.0f);  	//move to the left
	glScalef(0.2f, 0.2f, 0.2f);      // Scale down
	glRotatef(45.0, 0.0, 0.0, 1.0);	//rotate
	mSquare.draw();
}



