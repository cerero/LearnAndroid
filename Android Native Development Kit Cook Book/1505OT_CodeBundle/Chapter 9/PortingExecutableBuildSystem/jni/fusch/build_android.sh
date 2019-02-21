#!/bin/bash
NDK=~/Desktop/android/android-ndk-r8b
SYSROOT=$NDK/platforms/android-8/arch-arm
CUR_D=$(pwd)
#echo $CUR_D
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
	-DANDROID \
	-I$CUR_D/../fusch_lib \
	-I$CUR_D/../libpng-1.2.50/include"
export LDFLAGS="-Wl,--no-undefined -Wl,-z,noexecstack -Wl,-z,relro -Wl,-z,now -lz -lc -lm -lpng -lseamcarv -L$CUR_D/../fusch_lib -L$CUR_D/../libpng-1.2.50/lib"
export CC="$NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/arm-linux-androideabi-gcc --sysroot=$SYSROOT"
SRC_FILES="fusch.c"
$CC $SRC_FILES $CFLAGS $LDFLAGS -o fusch
