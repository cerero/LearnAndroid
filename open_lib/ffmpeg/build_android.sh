#!/bin/bash
export STAND_ALONE=$(pwd)/../android-toolchain/arm
export SYSROOT=$STAND_ALONE/sysroot
export CPU=armeabi-v7a
export PREFIX=$(pwd)/libffmpeg/$CPU
export ADDI_CFLAGS="-marm"

rm -rf libffmpeg
mkdir libffmpeg
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
