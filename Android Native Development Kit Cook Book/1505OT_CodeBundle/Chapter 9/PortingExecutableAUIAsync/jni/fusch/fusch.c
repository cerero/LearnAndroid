/*
fusch.c
Diese Quelltextdatei ist Bestandteil des FUGENSCHNITZER-Anwendungsprogramms.

Das FUGENSCHNITZER-Anwendungsprogramm ist ein Seam-Carving-Programm, das 
auf der FUGENSCHNITZER-Programmbibliothek basiert. 

FUGENSCHNITZER -- Seam Carving fuer jedermann.
http://fugenschnitzer.sourceforge.net
Copyright (C) 2008/9 David Eckardt

Dieses Programm ist freie Software. Sie koennen es unter den Bedingungen
der GNU General Public License, wie von der Free Software
Foundation veroeffentlicht, weitergeben und/oder modifizieren, entweder
gemaess Version 3 der Lizenz oder (nach Ihrer Option) jeder spaeteren
Version.
Die Veroeffentlichung dieses Programms erfolgt in der Hoffnung, dass es
Ihnen von Nutzen sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne
die implizite Garantie der MARKTREIFE oder der VERWENDBARKEIT FUER EINEN
BESTIMMTEN ZWECK. Details finden Sie in der GNU General Public License.
Sie sollten ein Exemplar der GNU General Public License zusammen mit
diesem Programm erhalten haben. Falls nicht, siehe
http://www.gnu.org/licenses/gpl-3.0.html
http://www.gnu.de/documents/gpl-3.0.de.html
.

This source code file is a part of the FUGENSCHNITZER Application Program.

The FUGENSCHNITZER Application Program Library is a Seam Carving program 
basing on the FUGENSCHNITZER program library. 

FUGENSCHNITZER -- Seam Carving for everyone.
http://fugenschnitzer.sourceforge.net
Copyright (C) 2008/9 David Eckardt

This program is free software: you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.
This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
Public License for more details.
You should have received a copy of the GNU General Public License along
with this program. If not, see
http://www.gnu.org/licenses/gpl-3.0.html
.
*/

#include <stdbool.h>
#include <ctype.h>
#include <locale.h>
#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <pthread.h>
#include <png.h>

#include "seamcarv.h"

#ifndef DE
	#include "fusch.h"
#else
	#ifdef DE850
		#include "fusch_de850.h"
	#else
		#include "fusch_de.h"
	#endif
#endif	

#define SIG_BYTES 8

struct hms_s {unsigned int h:5, m:6, s:6;};

void ftime(unsigned long int sec, struct hms_s *hms) {
	hms->s = sec % 60;
	sec /= 60;
	hms->m = sec % 60;
	sec /= 60;
	hms->h = sec;
}

#if defined WIN32 || defined _WIN32 || defined __CYGWIN__
	#include <time.h>

	void sleep_sync(const clock_t t) {
		_sleep(t - (clock() % t));
	}
#else
	#include <sys/time.h>
	#include <unistd.h>

	unsigned long int usec(void) {
		struct timeval t;
		gettimeofday(&t, (struct timezone*)NULL);
		unsigned long int us = t.tv_sec;
		us *= 1000000u;
		us += t.tv_usec;
		return us;
	}
	
	void sleep_sync(unsigned long int t) {
		t *= 1000;
		unsigned long int u = usec();
		usleep(t - (u % t));
	}
#endif

#ifdef ANDROID_BUILD
#include <jni.h>
#include "mylog.h"

JavaVM *cachedJvm;
jobject cachedMainActObj;
#endif

void* seam_progress(void *arg) {
	const int n = labs(*(int*)arg);
	const float fn = n;
#ifdef ANDROID_BUILD
	char progBuf[500];
	JNIEnv *env;
	jmethodID updateProgMID;
	(*cachedJvm)->AttachCurrentThread(cachedJvm, &env, NULL);
	jstring progStr;
	jclass mainActivityClass = (*env)->GetObjectClass(env, cachedMainActObj);
	updateProgMID = (*env)->GetMethodID(env, mainActivityClass, "updateProgress", "(Ljava/lang/String;I)V");
	if (NULL == updateProgMID) {
		LOGE(1, "error finding method updateProgress at seam_progress");
		(*cachedJvm)->DetachCurrentThread(cachedJvm);
		pthread_exit((void*)NULL);
	}
#endif
	
//	printf("Fugensuche: ges. erm.   %% Fu/s    Dauer  verstr.   verbl.\n            %4d ", n);
#ifdef ANDROID_BUILD
	sprintf(progBuf, "%s\n", MSG[I_COMPUTING_TABLE]);
	progStr = (*env)->NewStringUTF(env, progBuf);
	(*env)->CallVoidMethod(env, cachedMainActObj, updateProgMID, progStr, 0);
#else
	puts(MSG[I_COMPUTING_TABLE]);
	printf("            %4d ", n);
#endif
	int i = 0,
		s = 0;
	bool goon = true;
	do {
		sleep_sync(500);
		s = sc_seam_progress();
		goon = (s < n);

		const int ela = i >> 1;
		const float rate = ela? (float)(s) / (float)(ela): 1;
		const int lrate = lrintf(rate),
					 pc = (float)(s * 100) / fn;

		if ((i++ & 1) && goon) {
			printf("%4d %3d %4d\b\b\b\b\b\b\b\b\b\b\b\b\b\b ", s, pc, lrate);
		} else {
			const int ttl = lrintf(n / rate);
			struct hms_s tela, trem, tttl;
			ftime(ela, &tela);
			ftime(ttl - ela, &trem);
			ftime(ttl, &tttl);
#ifdef ANDROID_BUILD
			sprintf(progBuf, "%4d %4d %3d %4d "
				"%02d:%02d:%02d %02d:%02d:%02d %02d:%02d:%02d",
				n, s, pc, lrate,
				tttl.h, tttl.m, tttl.s, tela.h, tela.m, tela.s, trem.h, trem.m, trem.s
			);
			progStr = (*env)->NewStringUTF(env, progBuf);
			(*env)->CallVoidMethod(env, cachedMainActObj, updateProgMID, progStr, 1);
#else
			printf(
				"%4d %3d %4d "
				"%02d:%02d:%02d %02d:%02d:%02d %02d:%02d:%02d"
				"\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b",
				s, pc, lrate,
				tttl.h, tttl.m, tttl.s, tela.h, tela.m, tela.s, trem.h, trem.m, trem.s
			);
#endif
		}
	} while (goon);
#ifdef ANDROID_BUILD
	(*cachedJvm)->DetachCurrentThread(cachedJvm);
#endif
	pthread_exit((void*)NULL);
}

struct sync_s {int *p; pthread_mutex_t *m;};

void* seam_paral_accu(void *arg) {
	struct sync_s *sync = (struct sync_s*)arg;
	int p = 0;

	pthread_mutex_lock(sync->m);

	do if (p >= *sync->p)
		pthread_mutex_lock(sync->m);
	while (!sc_seam_paral_accu(p++));
	
	pthread_exit((void*)NULL);
}

struct dtid_s {int p, t;};

void* seam_paral_diff_part(void *arg) {
	const struct dtid_s *id = (struct dtid_s*)arg;

	sc_seam_paral_diff(id->p, id->t);

	pthread_exit((void*)NULL);
}

void seam_paral(long int diff) {
	diff = labs(diff);

	const int T = 5;

	pthread_t accuth, diffth[T];

	pthread_mutex_t m = PTHREAD_MUTEX_INITIALIZER;

	int p = 0;
	struct sync_s sync = {&p, &m};
	struct dtid_s dtid[T];
	
	sc_seam_paral_init();

	for (long int d = 0; d < diff; d++) {
		p = 0;	

		pthread_create(&accuth, (pthread_attr_t*)NULL, seam_paral_accu, &sync);
		const int P = sc_seam_paral_start(0x40000, T);

		while (p < P) {
			for (int t = 0; t < T; t++) {
				dtid[t].p = p;
				dtid[t].t = t;
				pthread_create(&diffth[t], (pthread_attr_t*)NULL, seam_paral_diff_part, &dtid[t]);
			}
			for (int t = 0; t < T; t++)
				pthread_join(diffth[t], (void*)NULL);
	
			p++;
			pthread_mutex_unlock(&m);
		}

		pthread_join(accuth, (void*)NULL);

		sc_seam_paral_finish();

	}
	sc_seam_paral_close();
	pthread_mutex_destroy(&m);
}

void* carve_progress(void *arg) {
	const long int max = *(long int*)arg;
	long int pro = 0;
	bool goon;
#ifdef ANDROID_BUILD
	char progBuf[500];
	JNIEnv *env;
	jmethodID updateProgMID;
	(*cachedJvm)->AttachCurrentThread(cachedJvm, &env, NULL);
	jstring progStr;
	jclass mainActivityClass = (*env)->GetObjectClass(env, cachedMainActObj);
	updateProgMID = (*env)->GetMethodID(env, mainActivityClass, "updateProgress", "(Ljava/lang/String;I)V");
	if (NULL == updateProgMID) {
		LOGE(1, "error finding method updateProgress at seam_progress");
		(*cachedJvm)->DetachCurrentThread(cachedJvm);
		pthread_exit((void*)NULL);
	}
#endif

#ifdef ANDROID_BUILD
	sprintf(progBuf, "%6d ", max);
	progStr = (*env)->NewStringUTF(env, progBuf);
	(*env)->CallVoidMethod(env, cachedMainActObj, updateProgMID, progStr, 0);
#else
	printf("%6d ", max);
#endif
	
	do  {
		sleep_sync(250);
		pro = sc_carve_paral_progress();
		goon = (pro >= 0);
		if (!goon)
			pro = max;
#ifdef ANDROID_BUILD
		sprintf(progBuf, "%6d %6d %3d%%", max, pro, lrintf((float)(pro * 100) / max));
		progStr = (*env)->NewStringUTF(env, progBuf);
		(*env)->CallVoidMethod(env, cachedMainActObj, updateProgMID, progStr, 1);
#else
		printf("%6d %3d%%\b\b\b\b\b\b\b\b\b\b\b", pro, lrintf((float)(pro * 100) / max));
#endif
	} while (goon);

#ifdef ANDROID_BUILD
	(*cachedJvm)->DetachCurrentThread(cachedJvm);
#endif
	pthread_exit((void*)NULL);	
}

void* carve_part(void *arg) {
	sc_carve_paral(*(int*)arg);

	pthread_exit((void*)NULL);
}

long int carve_paral(
	long int nom,
	long int *width, long int *height, long int *pwidth, long int *pheight
) {
	const int T  = 4;
	pthread_t carveth[T], carveproth;
	int ctid[T];
	
	long int max = sc_carve_paral_init(nom, T);

	pthread_create(&carveproth, (pthread_attr_t*)NULL, carve_progress, &max);
	
	for (int t = 0; t < T; t++) {
		ctid[t] = t;
		pthread_create(&carveth[t], (pthread_attr_t*)NULL, carve_part, &ctid[t]);
	}
	
	for (int t = 0; t < T; t++)
		pthread_join(carveth[t], (void*)NULL);

	nom = sc_carve_paral_finish(width, height, pwidth, pheight);
	pthread_join(carveproth, (void*)NULL);
	return nom;
}

void try_png_read(
	const bool failed,
	png_struct **png_ptr, png_info **info_ptr, png_info **end_info,
	FILE *f, const char *message
) {
	if (failed) {
		fputs(message? message: MSG[E_GENERIC], stderr);
		fputc('\n', stderr);
		png_destroy_read_struct(png_ptr, info_ptr, end_info);
		fclose(f);
		exit(EXIT_FAILURE);
	}
}

void try_png_write(
	const bool failed,
	png_struct **png_ptr, png_info **info_ptr,
	FILE *f, const char *message
) {
	if (failed) {
		fputs(message? message: MSG[E_GENERIC], stderr);
		fputc('\n', stderr);
		png_destroy_write_struct(png_ptr, info_ptr);
		fclose(f);
		exit(EXIT_FAILURE);
	}
}

//	h+### h-### v+### v-### %+### %-###

#ifdef ANDROID_BUILD

//int naMain(JNIEnv* env, jobject pObj, jstring pCmdStr);
int naMain(JNIEnv* env, jobject pObj, jstring pCmdStr, jobject pMainActObj);

jint JNI_OnLoad(JavaVM* pVm, void* reserved) {
	JNIEnv* env;
	if ((*pVm)->GetEnv(pVm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		 return -1;
	}
	cachedJvm = pVm;
	JNINativeMethod nm[1];
	nm[0].name = "naMain";
//	nm[0].signature = "(Ljava/lang/String;)I";
	nm[0].signature = "(Ljava/lang/String;Lcookbook/chapter9/portingexecutableaui/MainActivity;)I";
	nm[0].fnPtr = (void*)naMain;
	jclass cls = (*env)->FindClass(env, "cookbook/chapter9/portingexecutableaui/MainActivity");
	// Register methods with env->RegisterNatives.
	(*env)->RegisterNatives(env, cls, nm, 1);
	return JNI_VERSION_1_6;
}
  int naMain(JNIEnv* env, jobject pObj, jstring pCmdStr, jobject pMainActObj) {
	char progBuf[500];
	jmethodID updateProgMID, toStringMID;
	jstring progStr;
	jclass mainActivityClass = (*env)->GetObjectClass(env, pMainActObj);
	cachedMainActObj = (*env)->NewGlobalRef(env, pMainActObj);

//	toStringMID = (*env)->GetMethodID(env, mainActivityClass, "toString", "()Ljava/lang/String;");
//	jstring name = (*env)->CallObjectMethod(env, pMainActObj, toStringMID);
//	const jbyte* nameStr = (*env)->GetStringUTFChars(env, name, NULL);
//	LOGI(2, "toString: %s", nameStr);
//	(*env)->ReleaseStringUTFChars(env, name, nameStr);

	updateProgMID = (*env)->GetMethodID(env, mainActivityClass, "updateProgress", "(Ljava/lang/String;I)V");
	if (NULL == updateProgMID) {
		LOGE(1, "error finding method updateProgress");
		return EXIT_FAILURE;
	}
//	char* test = "test";
//	jstring jtest = (*env)->NewStringUTF(env, test);
//	(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, jtest);

	int argc = 0;
	char** argv = (char**) malloc (sizeof(char*)*4);
	//the first input argument should be the program name itself
	*argv = "fusch";
	char** targv = argv + 1;
	argc++;
	jboolean isCopy;
    char *cmdstr = (*env)->GetStringUTFChars(env, pCmdStr, &isCopy);
    if (NULL == cmdstr) {
    	LOGI(2, "get string failed");
    	return EXIT_FAILURE;
    }
    char* pch;
    pch = strtok(cmdstr, " ");
    while (NULL != pch) {
    	*targv = pch;
    	argc++;
    	targv++;
    	pch = strtok(NULL, " ");
    }
    LOGI(1, "No. of arguments: %d", argc);
    LOGI(1, "%s %s %s %s", argv[0], argv[1], argv[2], argv[3]);
#else
  int main(int argc, char *argv[]) {
#endif
	setlocale(LC_ALL, "");
	setvbuf(stdout, (char*)NULL, _IONBF, 0);

	if(argc != 4) {
		fprintf(stderr, MSG[I_HELP1], argv[0]);
		fputs(MSG[I_HELP2], stderr);
		return EXIT_FAILURE;
	}

	char *inname  = argv[1],
		 *outname = argv[2],
		 *valarg   = argv[3];

	/* Quelldatei oeffnen und auf PNG-Signatur ueberpruefen **********************/

#ifdef ANDROID_BUILD
//	progStr = (*env)->NewStringUTF(env, MSG[I_OPEN]);
//	(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 0);
#else
	puts(MSG[I_OPEN]);
#endif

	FILE *f;

	f = fopen(inname, "rb");
	if (f == NULL) {
		fputs(inname, stderr);
		fputs(MSG[E_OPEN], stderr);
		fputc('\n', stderr);
		return EXIT_FAILURE;
	}

	unsigned char sig[SIG_BYTES];
	fread(sig, 1, SIG_BYTES, f);
	if (png_sig_cmp(sig, 0, SIG_BYTES)) {
		fputs(inname, stderr);
		fputs(MSG[E_CORRUPTED], stderr);
		fputc('\n', stderr);
		fclose(f);
		return EXIT_FAILURE;
	}

	/* PNG-Lesevorgang initialisieren *****************************************/

	png_struct *png_ptr;
	png_info *info_ptr, *end_info;

	png_ptr = png_create_read_struct(
		PNG_LIBPNG_VER_STRING,
		(png_voidp)NULL, (png_error_ptr)NULL, (png_error_ptr)NULL
	);
	if (png_ptr == NULL) {
		fputs(MSG[E_GENERIC], stderr);
		fputc('\n', stderr);
		fclose(f);
		return EXIT_FAILURE;
	}
	
	info_ptr = png_create_info_struct(png_ptr);
	end_info = png_create_info_struct(png_ptr);

	try_png_read(
		(info_ptr == NULL) || (end_info == NULL),
		&png_ptr, &info_ptr, &end_info, f, (char*)NULL
	);

	try_png_read(
		setjmp(png_jmpbuf(png_ptr)),
		&png_ptr, &info_ptr, &end_info, f, MSG[E_READ]
	);

   	png_init_io(png_ptr, f);
	png_set_sig_bytes(png_ptr, SIG_BYTES);

	/* Bildinfo lesen: Bilddimensionen und ggf. Farbpalette.
	 * Palette ggf. konvertieren. *********************************************/

	long int width, height, pwidth, pheight;
//	png_uint_32 width, height, pwidth, pheight;
	comp_t *image, **row, *rwp;

	png_read_info(png_ptr, info_ptr);

	width = info_ptr->width;
	height = info_ptr->height;
	
	const long int original_width = info_ptr->width;

	const int bit_depth = info_ptr->bit_depth,
			 color_type = info_ptr->color_type;
	const bool image_is_pal = (info_ptr->color_type == PNG_COLOR_TYPE_PALETTE);
	const bool image_is_gray = (
		(color_type == PNG_COLOR_TYPE_GRAY) ||
		(color_type == PNG_COLOR_TYPE_GRAY_ALPHA)
	);
	const bool alpha = (
		(color_type == PNG_COLOR_TYPE_GRAY_ALPHA) ||
		(color_type == PNG_COLOR_TYPE_RGB_ALPHA)
	);

	int num_palette = 0;
	png_color *png_pal_got = NULL;

	if (image_is_pal)

		try_png_read(
			!png_get_PLTE(png_ptr, info_ptr, &png_pal_got, &num_palette),
			&png_ptr, &info_ptr, &end_info, f, MSG[E_READPAL]
		);

	png_color   png_pal[num_palette];	
	comp_t     comp_pal[num_palette];	

	if (image_is_pal)
		for (int i = 0; i < num_palette; i++) {
			png_pal[i] = png_pal_got[i];
			comp_t c;
			c = png_pal[i].red;
			c <<= CHAR_BIT;
			c |= png_pal[i].green;
			c <<= CHAR_BIT;
			c |= png_pal[i].blue;
			comp_pal[i] = c;
		}

	png_color_16 *img_bkgd;
	png_color_16 background;
	if (png_get_bKGD(png_ptr, info_ptr, &img_bkgd))
		background = *img_bkgd;

	/* Parameter fuer Groessenaenderung auswerten:
	 * diff: (Seitenlaenge nachher) - (Seitenlaenge vorher),
	 * vert: true <=> Aenderung der Bildhoehe (sonst -breite). ******************/

	long int diff;
	bool vert;

	bool aspp = false,
		 asp2 = false,
		 enlg = false,
		 sign = true;
	
	switch (tolower(*valarg++)) {
		case 'h': vert = false; break;
		case 'v': vert = true;  break;
		case '%': aspp = true;  break;
		case '@': asp2 = true;  break;
		default :
			try_png_read(true, &png_ptr, &info_ptr, &end_info, f, MSG[E_DIM]);
	}

	switch (*valarg) {
		case '+': enlg = true;  break;
		case '-': enlg = false; break;
		default:  sign = false; break;
	}

	diff = atol(valarg);
	bool valargok = !!diff;

	if (aspp || asp2) {
		try_png_read(!sign, &png_ptr, &info_ptr, &end_info, f, MSG[E_SIGN]);

		const float fheight = (float)height,
					fwidth  = (float)width,
					casp =  fheight / fwidth;
		float nasp;
		if (asp2) {
			const char *aspsw = strtok(valarg, ":"),
					   *aspsh = strtok((char*)NULL, ":");
			valargok = ((aspsw != NULL) && (aspsh != NULL));
			const float aspw = valargok? atol(aspsw): 0,
						asph = valargok? atol(aspsh): 0;
						nasp = valargok? fabs(asph / aspw): 0;
		} else
			nasp = ((float)labs(diff) / 100.0f);

		vert = ((nasp < casp) ^ enlg);
		
		diff = valargok? labs(vert?
			(height - (fwidth  * nasp)):
			(width  - (fheight / nasp))
		): 0;
		if (!enlg)
			diff = -diff;
	}

	if (!diff) {
		png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
		fclose(f);
		if (valargok) {
#ifdef ANDROID_BUILD
			progStr = (*env)->NewStringUTF(env, MSG[I_NOTHINGTODO]);
			(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 0);
#else
			puts(MSG[I_NOTHINGTODO]);
#endif
//			const char copycmd[] = "copy /b /y";
			const char copycmd[] = "cp";

			char copycmdln[
				strlen(copycmd) + strlen(argv[1]) + strlen(argv[2]) + 7
			];
			sprintf(
				copycmdln, "%s \"%s\" \"%s\"",
				copycmd, argv[1], argv[2]
			);
			return system(copycmdln);
		}
		try_png_read(!valargok, &png_ptr, &info_ptr, &end_info, f, MSG[E_PAR]);
	}

	if (!(aspp || asp2 || sign))
		diff -= vert? height: width;
	
		try_png_read(
			labs(diff) > (vert? height: width),
			&png_ptr, &info_ptr, &end_info, f, MSG[E_SIZE]
		);

	/* Bild- sowie Zeilenzeigerspeicher anfordern und Zeiger setzen. **********/

	image = malloc(width * height * sizeof(comp_t));
	try_png_read(image == NULL, &png_ptr, &info_ptr, &end_info, f, (char*)NULL);
	
	row = malloc(height * sizeof(comp_t*));
	try_png_read(row == NULL, &png_ptr, &info_ptr, &end_info, f, (char*)NULL);
	
	rwp = image;
	for (int i = 0; i < height; i++) {
		row[i] = rwp;
		rwp += width;
	}
	
	/* Bild laden.
	 * Falls Alphakanal vorhanden, Alpha invertieren: hoher Wert => hohe Deckung.
	 * Falls Nicht-Palettenbild ohne Alphakanal (24 bpp) oder Graubild
	 * (8 oder 16 bpp), mit "png_set_filler" die Bilddaten auf 32 bzw. 16 bpp
	 * ausweiten. 32 Bit ist die *  comp_t-Breite. ****************************/
#ifdef ANDROID_BUILD
//	progStr = (*env)->NewStringUTF(env, MSG[I_LOAD]);
//	(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 0);
#else
	puts(MSG[I_LOAD]);
#endif

	if (alpha)
		png_set_invert_alpha(png_ptr);
	else if (!image_is_pal)
		png_set_filler(png_ptr, 0, PNG_FILLER_AFTER);

	png_read_image(png_ptr, (void*)row);

	/* Falls 8 oder 16 bpp, Bilddaten auf "Pixel = comp_t-Element" ausweiten. */

//	trw: Temporaere Zeile.
	void *trw = malloc(image_is_gray? (width * 2): width);

	try_png_read(trw == NULL, &png_ptr, &info_ptr, &end_info, f, (char*)NULL);

//	Zunaechst Bildzeile nach Temporaerzeile kopieren, dann Elemente einzeln zurueck.
	if (image_is_pal) {
		for (int i = 0; i < height; i++) {
			uint8_t *trwp = trw;
			rwp = row[i];
			memcpy(trw, row[i], width * sizeof *trwp);
			for (int j = 0; j < width; j++)
				*rwp++ = *trwp++;
		}
	}

	if (image_is_gray) {
		for (int i = 0; i < height; i++) {
			uint16_t *trwp = trw;
			rwp = row[i];
			memcpy(trw, row[i], width * sizeof *trwp);
			for (int j = 0; j < width; j++)
				*rwp++ = *trwp++;
		}
	}

	/* Lesevorgang beenden und Quelldatei schliessen. ************************/
	
	png_destroy_read_struct(&png_ptr, &info_ptr, &end_info);
	fclose(f);

#ifdef ANDROID_BUILD
	sprintf(progBuf, "%s: %d*%d > %d*%d: %s %+d",
			MSG[I_MEASURES],
			width, height,
			vert? width: width + diff,
			vert? height + diff: height,
			vert? MSG[I_VERT]: MSG[I_HORI], diff
		);
	progStr = (*env)->NewStringUTF(env, progBuf);
	(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 0);
#else
	printf(
		"%s: %d*%d > %d*%d: %s %+d\n",
		MSG[I_MEASURES],
		width, height,
		vert? width: width + diff,
		vert? height + diff: height,
		vert? MSG[I_VERT]: MSG[I_HORI], diff
	);
#endif

	/* Hier kommt Fugenschnitzer zum Einsatz.
	 * diff: (Seitenlaenge nachher) - (Seitenlaenge vorher),
	 * vert: true <=> Aenderung der Bildhoehe (sonst -breite). ****************/

	sc_init();
	sc_load(image, &width, &height, 0);

	const int prep = sc_prepare(
		vert, diff,									// Bild ggf. erweitern
		false,		
		alpha? MARK_ALPHA: MARK_KEEP,				// Ggf. Alpha-Markierung
		(mark_t*)NULL,								// keine Markierung sonst
		&width, &height, &pwidth, &pheight
	);
	
	if (prep < 0) {
		fputs(MSG[E_GENERIC], stderr);
		fputc('\n', stderr);
		free(image);
		free(row);
		return EXIT_FAILURE;
	}
	
	if (prep & 2) {
#ifdef ANDROID_BUILD
		progStr = (*env)->NewStringUTF(env, MSG[E_ALPHA]);
		(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 0);
#else
		puts(MSG[E_ALPHA]);
#endif
	}

//	Bildspeicher erweitern, falls das Bild vergroessert wurde:

	if (prep & 1) {
		image = realloc(image, (size_t)width * (size_t)height * sizeof(comp_t));
		if (image == NULL) {
			fputs(MSG[E_GENERIC], stderr);
			fputc('\n', stderr);
			return EXIT_FAILURE;		
		}
		
		if (vert) {
			row = realloc(row, (size_t)height * sizeof(comp_t*));
			if (row == NULL) {
				fputs(MSG[E_GENERIC], stderr);
				fputc('\n', stderr);
				free(image);
				return EXIT_FAILURE;		
			}
		}		
	}

	pthread_t seam_th;
	pthread_create(&seam_th, NULL, seam_progress, (void*)(&diff));
	
//	sc_seam(diff);
	seam_paral(diff);
	
	pthread_join(seam_th, NULL);

#ifdef ANDROID_BUILD
	progStr = (*env)->NewStringUTF(env, MSG[I_RESIZING]);
	(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 0);
#else
	putchar('\n');
	printf(MSG[I_RESIZING]);
#endif
	
//	sc_carve(diff, &width, &height, &pwidth, &pheight);

	carve_paral(diff, &width, &height, &pwidth, &pheight);
	sc_fix(
		false,									// Nicht wiederherstellen
		&width, &height, &pwidth, &pheight
	);

	sc_eject(image);

	sc_close();

	/* Das war's mit Fugenschnitzer. -- Zeilenzeiger neu setzen. **************/

	rwp = image;
	for (int i = 0; i < height; i++) {
		row[i] = rwp;
		rwp += width;
	}

    /* Zieldatei oeffnen und Schreibvorgang initialisieren. ********************/
#ifdef ANDROID_BUILD
	progStr = (*env)->NewStringUTF(env, MSG[I_SAVE]);
	(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 0);
#else
	putchar('\n');
	printf(MSG[I_SAVE]);
#endif

	f = fopen(outname, "wb");
	if (f == NULL) {
		fputs(outname, stderr);
		fputs(MSG[E_SAVE], stderr);
		fputc('\n', stderr);
		return EXIT_FAILURE;
	}

	png_ptr = png_create_write_struct(
		PNG_LIBPNG_VER_STRING,
		(png_voidp)NULL, (png_error_ptr)NULL, (png_error_ptr)NULL
	);
	if (png_ptr == NULL) {
		fputs(MSG[E_GENERIC], stderr);
		fputc('\n', stderr);
		fclose(f);
		return EXIT_FAILURE;
	}
	
	info_ptr = png_create_info_struct(png_ptr);
	try_png_write(info_ptr == NULL, &png_ptr, &info_ptr, f, (char*)NULL);

	try_png_write(
		setjmp(png_jmpbuf(png_ptr)),
		&png_ptr, &info_ptr, f, MSG[E_WRITE]
	);

   	png_init_io(png_ptr, f);

	/* Bildparameter setzen. **************************************************/

	png_set_IHDR(
		png_ptr, info_ptr, width, height, bit_depth, color_type,
		PNG_INTERLACE_NONE,
		PNG_COMPRESSION_TYPE_DEFAULT, PNG_FILTER_TYPE_DEFAULT
	);

	if (image_is_pal)
		png_set_PLTE(png_ptr, info_ptr, png_pal, num_palette);

	if (alpha)
		png_set_bKGD(png_ptr, info_ptr, &background);

	png_write_info(png_ptr, info_ptr);

	/* Falls 8 oder 16 bpp, Bilddaten wieder zusammenschieben. ****************/

	trw = realloc(trw, image_is_gray? (width * 2): width);
	try_png_write(trw == NULL, &png_ptr, &info_ptr, f, (char*)NULL);

	if (image_is_pal) {
		for (int i = 0; i < height; i++) {
			uint8_t *trwp = trw;
			rwp = row[i];
			for (int j = 0; j < width; j++)
				*trwp++ = *rwp++;
			memcpy(row[i], trw, width * sizeof *trwp);
		}
	}

	if (image_is_gray) {
		for (int i = 0; i < height; i++) {
			uint16_t *trwp = trw;
			rwp = row[i];
			for (int j = 0; j < width; j++)
				*trwp++ = *rwp++;
			memcpy(row[i], trw, width * sizeof *trwp);
		}
	}

	/* Bild speichern. Wieder Alpha invertieren (hoher Wert => hohe Transparenz)
	*  sowie mit "png_set_filler" 32/16 bpp => 24/8 bpp setzen. ***************/

	if (alpha)
		png_set_invert_alpha(png_ptr);
	else
		png_set_filler(png_ptr, 0, PNG_FILLER_AFTER);

	png_write_image(png_ptr, (void*)row);
	png_write_end(png_ptr, (png_info*)NULL);

#ifdef ANDROID_BUILD
	progStr = (*env)->NewStringUTF(env, MSG[I_FINISHED]);
	(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 2);
#else
	puts(MSG[I_FINISHED]);
#endif

	/* Schreibvorgang beenden, Datei schliessen, Speicher freigeben, fertig. ***/

	png_destroy_write_struct(&png_ptr, &info_ptr);
	fclose(f);

	free(trw);
	free(image);
	free(row);
#ifdef ANDROID_BUILD
	progStr = (*env)->NewStringUTF(env, MSG[I_COMPLETED]);
	(*env)->CallVoidMethod(env, pMainActObj, updateProgMID, progStr, 0);
#else
	puts(MSG[I_COMPLETED]);
#endif

#ifdef ANDROID_BUILD
    (*env)->ReleaseStringUTFChars(env, pCmdStr, cmdstr);
	(*env)->DeleteGlobalRef(env, cachedMainActObj);
	cachedMainActObj = NULL;
#endif

	return EXIT_SUCCESS;
}
