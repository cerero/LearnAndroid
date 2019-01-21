#!/bin/bash
#export STAND_ALONE=$(pwd)/../android-toolchain/arm
#export CPU=armeabi-v7a
#export PREFIX=$(pwd)/libffmpeg/android-21/$CPU

export ANDROID_PLATFORM=android-19
export STAND_ALONE=$(pwd)/../android-toolchain/${ANDROID_PLATFORM}/arm
export SYSROOT=$STAND_ALONE/sysroot
export CPU=armeabi
export PREFIX=$(pwd)/libffmpeg/${ANDROID_PLATFORM}/$CPU
export ADDI_CFLAGS="-marm"

rm -rf libffmpeg/${ANDROID_PLATFORM}
mkdir libffmpeg/${ANDROID_PLATFORM}

cd ./ffmpeg-4.1

./configure \
--target-os=linux \
--prefix=$PREFIX \
--arch=$CPU \
--enable-shared \
--disable-static \
--disable-yasm \
--disable-symver \
--enable-gpl \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-avdevice \
--disable-doc \
--disable-symver \
--cross-prefix=$STAND_ALONE/bin/arm-linux-androideabi- \
--strip=$STAND_ALONE/bin/arm-linux-androideabi-strip \
--enable-cross-compile \
--sysroot=$SYSROOT \
--extra-cflags="-Os -fpic $ADDI_CFLAGS" \
--extra-ldflags="$ADDI_LDFLAGS"
$ADDITIONAL_CONFIGURE_FLAG

make clean
make
make install
