#!/bin/bash
NDK=~/Desktop/android/android-ndk-r8b
SYSROOT=$NDK/platforms/android-8/arch-arm/
export CFLAGS="-fpic \
    -ffunction-sections \
    -funwind-tables \
    -D__ARM_ARCH_5__ -D__ARM_ARCH_5T__ \
    -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__ \
	-Wno-psabi \
	-march=armv5te \
    -mtune=xscale \
    -msoft-float \
	-mthumb \
    -Os \
    -fomit-frame-pointer \
    -fno-strict-aliasing \
    -finline-limit=64 \
	-std=c99 \
	-DANDROID"
export CC="$NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/arm-linux-androideabi-gcc --sysroot=$SYSROOT"
AR="$NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/arm-linux-androideabi-ar"
SRC_FILES="\
	sc_core.c  \
	sc_carve.c  \
	sc_color.c  \
	sc_shift.c \
	sc_mgmnt.c \
	seamcarv.c"
$CC $SRC_FILES $CFLAGS -c
$AR cr libseamcarv.a *.o 

