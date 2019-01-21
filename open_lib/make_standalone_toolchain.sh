#! /bin/sh
# export NDK_HOME=/workspace/android-ndk-r10e
#platform=android-21
platform=android-19
shmake=$NDK_HOME/build/tools/make-standalone-toolchain.sh

archs=(
    'arm'
    'arm64'
)

toolchains=(
    'arm-linux-androideabi-4.9'
    'aarch64-linux-android-4.9'
)

echo $NDK_HOME
num=${#archs[@]}
for ((i=0;i<$num;i++))
do
   sh $shmake --arch=${archs[i]} --platform=$platform --install-dir=./android-toolchain/${platform}/${archs[i]} --toolchain=${toolchains[i]} --stl=stlport
done
