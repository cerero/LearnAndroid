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
	-DANDROID \
    -fomit-frame-pointer \
    -fno-strict-aliasing \
    -finline-limit=64"
export LDFLAGS="-lz"
export CC="$NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/arm-linux-androideabi-gcc --sysroot=$SYSROOT"
./configure \
    --host=arm-linux-androideabi \
    --prefix=$(pwd) \
    --exec-prefix=$(pwd) \
	--enable-shared=false \
	--enable-static=true
make clean
make 
make install

