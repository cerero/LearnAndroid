#include <jni.h>
#include <errno.h>

#include <EGL/egl.h>
#include <GLES/gl.h>

#include <android/log.h>

#include "Cube.h"
#include "mylog.h"

GLfloat vertices[24] = {
	-1.0f, -1.0f, -1.0f,
	1.0f, -1.0f, -1.0f,
	1.0f,  1.0f, -1.0f,
	-1.0f, 1.0f, -1.0f,
	-1.0f, -1.0f,  1.0f,
	1.0f, -1.0f,  1.0f,
	1.0f,  1.0f,  1.0f,
	-1.0f,  1.0f,  1.0f
};

GLfloat colors[32] = {
	0.0f,  1.0f,  0.0f,  1.0f,
	0.0f,  1.0f,  0.0f,  1.0f,
	1.0f,  0.5f,  0.0f,  1.0f,
	1.0f,  0.5f,  0.0f,  1.0f,
	1.0f,  0.0f,  0.0f,  1.0f,
	1.0f,  0.0f,  0.0f,  1.0f,
	0.0f,  0.0f,  1.0f,  1.0f,
	1.0f,  0.0f,  1.0f,  1.0f
};

GLbyte indices[36] = {
	0, 4, 5, 0, 5, 1,
	1, 5, 6, 1, 6, 2,
	2, 6, 7, 2, 7, 3,
	3, 7, 4, 3, 4, 0,
	4, 7, 6, 4, 6, 5,
	3, 0, 1, 3, 1, 2
};

Cube mCube(vertices, colors, indices);

void naInitGL1x() {
	LOGI(2, "naInitGL1x");
	glDisable(GL_DITHER);	//disable dither to improve performance with reduced quality
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);	//set clear value for color buffer as black
	glEnable(GL_CULL_FACE);		//enabled for better performance
	glClearDepthf(1.0f);	//set clear value [0, 1] for depth buffer as farthest
	glEnable(GL_DEPTH_TEST);	//do depth comparison and update depth buffer
	glDepthFunc(GL_LEQUAL);		//type of depth test
	glShadeModel(GL_SMOOTH);   // Enable smooth shading of color
}

void naSurfaceChanged(int width, int height) {
	LOGI(2, "naSurfaceChanged: %d:%d", width, height);
	glViewport(0, 0, width, height);
    float ratio = (float) width / height;
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrthof(-ratio, ratio, -1, 1, -10, 10);
}

void naDrawGraphics(float pAngleX, float pAngleY) {
	LOGI(2, "naDrawGraphics");
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
//	glTranslatef(0, 0, -3.0f);
	glRotatef(pAngleX, 0, 1, 0);	//rotate around y-axis
	glRotatef(pAngleY, 1, 0, 0);	//rotate around x-axis
	glScalef(0.3f, 0.3f, 0.3f);      // Scale down

	mCube.draw();
}
