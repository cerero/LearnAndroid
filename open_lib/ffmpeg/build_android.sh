#!/bin/bash
export STAND_ALONE=../android-toolchain/arm
export SYSROOT=$STAND_ALONE/sysroot
export CPU=arm
export PREFIX=$(pwd)/libffmpeg/$CPU
export ADDI_CFLAGS="-marm"

rm -rf libffmpeg
mkdir libffmpeg
cd ./ffmpeg-4.1

./configure \
--target-os=linux \
--prefix=$PREFIX \
--arch=arm \
--disable-doc \
--enable-shared \
--disable-static \
--disable-yasm \
--disable-symver \
--enable-gpl \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-doc \
--disable-symver \
--cross-prefix=$STAND_ALONE/bin/arm-linux-androideabi- \
--strip=$STAND_ALONE/bin/arm-linux-androideabi-strip \
--enable-cross-compile \
--sysroot=$SYSROOT \
--extra-cflags="-Os -fpic $ADDI_CFLAGS" \
--extra-ldflags="$ADDI_LDFLAGS"

make clean
make
make install
