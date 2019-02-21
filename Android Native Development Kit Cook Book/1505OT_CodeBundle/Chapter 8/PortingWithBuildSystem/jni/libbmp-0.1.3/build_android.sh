#!/bin/bash
NDK=~/Desktop/android/android-ndk-r8b
SYSROOT=$NDK/platforms/android-8/arch-arm/
export CFLAGS="-mthumb"
export LDFLAGS="-Wl,--fix-cortex-a8"
export CC="$NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86/bin/arm-linux-androideabi-gcc --sysroot=$SYSROOT"
./configure \
    --host=arm-linux-androideabi \
    --prefix=$(pwd) \
    --exec-prefix=$(pwd)
    #--libdir=$(pwd) \
    #--includedir=$(pwd)/include \
    #--oldincludedir=$(pwd)/include \
make clean
make 
make install
#$PREBUILT/bin/arm-linux-androideabi-ld -rpath-link=$PLATFORM/usr/lib -L$PLATFORM/usr/lib  -soname libffmpeg.so -shared -nostdlib  -z,noexecstack -Bsymbolic --whole-archive --no-undefined -o $PREFIX/libffmpeg.so ../x264/libx264.a libavcodec/libavcodec.a libavformat/libavformat.a libavutil/libavutil.a libswscale/libswscale.a libavfilter/libavfilter.a libswresample/libswresample.a -lc -lm -lz -ldl -llog  --warn-once  --dynamic-linker=/system/bin/linker $PREBUILT/lib/gcc/arm-linux-androideabi/4.4.3/libgcc.a
