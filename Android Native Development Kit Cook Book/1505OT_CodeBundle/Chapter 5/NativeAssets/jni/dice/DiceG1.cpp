#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>
#include <GLES/gl.h>

#include <math.h>

#include <png.h>
#include <setjmp.h>

#include "Cube.h"
#include "mylog.h"

void naLoadTexture(JNIEnv* env, jclass clazz, jobject pAssetManager);
void naInitGL1x(JNIEnv* env, jclass clazz);
void naDrawGraphics(JNIEnv* env, jclass clazz, float pAngleX, float pAngleY);
void naSurfaceChanged(JNIEnv* env, jclass clazz, int width, int height);

jint JNI_OnLoad(JavaVM* pVm, void* reserved)
{
	JNIEnv* env;
	if (pVm->GetEnv((void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	JNINativeMethod nm[4];
	nm[0].name = "naInitGL1x";
	nm[0].signature = "()V";
	nm[0].fnPtr = (void*)naInitGL1x;
	nm[1].name = "naDrawGraphics";
	nm[1].signature = "(FF)V";
	nm[1].fnPtr = (void*)naDrawGraphics;
	nm[2].name = "naSurfaceChanged";
	nm[2].signature = "(II)V";
	nm[2].fnPtr = (void*)naSurfaceChanged;
	nm[3].name = "naLoadTexture";
	nm[3].signature = "(Landroid/content/res/AssetManager;)V";
	nm[3].fnPtr = (void*)naLoadTexture;
	jclass cls = env->FindClass("cookbook/chapter5/nativeassets/MyRenderer");
	// Register methods with env->RegisterNatives.
	env->RegisterNatives(cls, nm, 4);
	return JNI_VERSION_1_6;
}

GLfloat vertices[108] = {
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

GLfloat texCoords[12] = {
		0.0f, 0.0f,    1.0f, 1.0f,    1.0f, 0.0f,
		0.0f, 0.0f,    0.0f, 1.0f,    1.0f, 1.0f
					};

GLbyte indices[6] = {
	0, 1, 2, 3, 4, 5
};

GLuint texIds[6];

Cube mCube(vertices, texCoords, indices);

void readPng(png_structp pPngPtr, png_bytep pBuf, png_size_t pCount) {
	AAsset* assetF = (AAsset*)png_get_io_ptr(pPngPtr);
	AAsset_read(assetF, pBuf, pCount);
}

void naLoadTexture(JNIEnv* env, jclass clazz, jobject pAssetManager) {
	GLint format;
	GLenum type;
	AAssetManager* assetManager = AAssetManager_fromJava(env, pAssetManager);
	AAssetDir* texDir = AAssetManager_openDir(assetManager, "");
	const char* texFn;
	int pId = 0;
	LOGI(2, "naLoadTexture");
	while (NULL != (texFn = AAssetDir_getNextFileName(texDir))) {
		LOGI(2, "loading asset file: %s", texFn);
		AAsset* assetF = AAssetManager_open(assetManager, texFn, AASSET_MODE_UNKNOWN);
		if (NULL == assetF) {
			LOGE(1, "cannot open asset file");
			goto FEND;
		}
		//read the png header
		png_bytep *rowPointers;
		png_structp pngPtr;
		png_infop infoPtr;
		png_byte header[8];
		png_byte *imageData;
		int rowBytes;
		bool lTransparency;
		if (8 != AAsset_read(assetF, header, 8)) {
			LOGE(1, "error reading png file header");
			goto FEND;
		}
		//test if it's a png file
		if (0 != png_sig_cmp(header, 0, 8)) {
		    LOGE(1, "not png file : %s", texFn);
		    goto FEND;
		}
		//create png struct, set error handling functions to NULL
		pngPtr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);
		if (!pngPtr) {
		    LOGE(1, "Unable to create png struct : %s", texFn);
		    goto FEND;
		}
		//create png info struct
		infoPtr = png_create_info_struct(pngPtr);
		if (!infoPtr) {
		    png_destroy_read_struct(&pngPtr, (png_infopp) NULL, (png_infopp) NULL);
		    LOGE(2, "Unable to create png info : %s", texFn);
		    goto FEND;
		}
		//error handling
		if (setjmp(png_jmpbuf(pngPtr))) {
		    LOGE(2, "Error at setjmp : %s", texFn);
		    png_destroy_read_struct(&pngPtr, &infoPtr, NULL);
		    goto FEND;
		}
		//init png reading by setting a read callback
		png_set_read_fn(pngPtr, assetF, readPng);
		//we have already read the first 8 bytes as png header
		png_set_sig_bytes(pngPtr, 8);
		//read all the info up to the image data
		png_read_info(pngPtr, infoPtr);
		//variables to pass to get info
		int bitDepth, colorType;
		png_uint_32 width, height;
		//get info about png
		png_get_IHDR(pngPtr, infoPtr, &width, &height, &bitDepth, &colorType, NULL, NULL, NULL);
		// Creates a full alpha channel if transparency is encoded as
		// an array of palette entries or a single transparent color.
		lTransparency = false;
		if (png_get_valid(pngPtr, infoPtr, PNG_INFO_tRNS)) {
			png_set_tRNS_to_alpha(pngPtr);
			lTransparency = true;
			goto FEND;
		}
		// Expands PNG with less than 8bits per channel to 8bits.
		if (bitDepth < 8) {
			png_set_packing (pngPtr);
		// Shrinks PNG with 16bits per color channel down to 8bits.
		} else if (bitDepth == 16) {
			png_set_strip_16(pngPtr);
		}
		// Indicates that image needs conversion to RGBA if needed.
		switch (colorType) {
		case PNG_COLOR_TYPE_PALETTE:
			png_set_palette_to_rgb(pngPtr);
			format = lTransparency ? GL_RGBA : GL_RGB;
			break;
		case PNG_COLOR_TYPE_RGB:
			format = lTransparency ? GL_RGBA : GL_RGB;
			break;
		case PNG_COLOR_TYPE_RGBA:
			format = GL_RGBA;
			break;
		case PNG_COLOR_TYPE_GRAY:
			png_set_expand_gray_1_2_4_to_8(pngPtr);
			format = lTransparency ? GL_LUMINANCE_ALPHA:GL_LUMINANCE;
			break;
		case PNG_COLOR_TYPE_GA:
			png_set_expand_gray_1_2_4_to_8(pngPtr);
			format = GL_LUMINANCE_ALPHA;
			break;
		}
		type = GL_UNSIGNED_BYTE;
		//update png info struct.
		png_read_update_info(pngPtr, infoPtr);
		// Row size in bytes.
		rowBytes = png_get_rowbytes(pngPtr, infoPtr);
		// Allocate the image_data as a big block, to be given to opengl
		imageData = new png_byte[rowBytes * height];
		if (!imageData) {
		    //clean up memory and close stuff
		    png_destroy_read_struct(&pngPtr, &infoPtr, NULL);
		    LOGE(2, "Unable to allocate image_data while loading %s ", texFn);
		    goto FEND;
		}
		//row_pointers is for pointing to image_data for reading the png with libpng
		rowPointers = new png_bytep[height];
		if (!rowPointers) {
		    //clean up memory and close stuff
		    png_destroy_read_struct(&pngPtr, &infoPtr, NULL);
		    delete[] imageData;
		    LOGE(1, "Unable to allocate row_pointer while loading %s ", texFn);
		    goto FEND;
		}
		// set the individual row_pointers to point at the correct offsets of image_data
		for (int i = 0; i < height; ++i) {
			rowPointers[height - 1 - i] = imageData + i * rowBytes;
		}
		//read the png into image_data through row_pointers
		png_read_image(pngPtr, rowPointers);

		//OpenGL operations
		glGenTextures(1, &texIds[pId]);
		glBindTexture(GL_TEXTURE_2D, texIds[pId]);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		// Loads image data into OpenGL.
		glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0,
				format, type, imageData);
FEND:
		AAsset_close(assetF);
		pId++;
	}
	AAssetDir_close(texDir);
}

void naInitGL1x(JNIEnv* env, jclass clazz) {
	glDisable(GL_DITHER);	//disable dither to improve performance with reduced quality
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);	//set clear value for color buffer as black
	glEnable(GL_CULL_FACE);		//enabled for better performance
	glClearDepthf(1.0f);	//set clear value [0, 1] for depth buffer as farthest
	glEnable(GL_DEPTH_TEST);	//do depth comparison and update depth buffer
	glDepthFunc(GL_LEQUAL);		//type of depth test
	glShadeModel(GL_SMOOTH);   // Enable smooth shading of color

	mCube.setTexCoords(texIds);
	glTexEnvx(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
	glEnable(GL_TEXTURE_2D);
}

void naSurfaceChanged(JNIEnv* env, jclass clazz, int width, int height) {
	glViewport(0, 0, width, height);
    float ratio = (float) width / height;
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrthof(-ratio, ratio, -1, 1, -10, 10);
}

void naDrawGraphics(JNIEnv* env, jclass clazz, float pAngleX, float pAngleY) {
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
    glRotatef(pAngleX, 0, 1, 0);	//rotate around y-axis
    glRotatef(pAngleY, 1, 0, 0);	//rotate around x-axis
	glScalef(0.3f, 0.3f, 0.3f);      // Scale down
	mCube.draw();
}



