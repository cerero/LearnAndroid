# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

include_directories(include)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp

        demo/dm01/Square.cpp
        demo/dm01/Triangle.cpp
        demo/dm01/TwoDG1.cpp

        demo/dm02/Cube.cpp
        demo/dm02/CubeG2.cpp
        demo/dm02/matrix.cpp

        demo/dm03/Cube.cpp
        demo/dm03/EGLDemo.cpp
        demo/dm03/Renderer.cpp
        demo/dm03/OldRenderMethods.cpp
        demo/dm04/Cube.cpp
        demo/dm04/DiceG1.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library(GLESv1_CM-lib GLESv1_CM)
find_library(GLESv2-lib GLESv2)
find_library(EGL-lib EGL)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib}
        ${GLESv1_CM-lib}
        ${GLESv2-lib}
        ${EGL-lib}
        ${android-lib}
        ${jnigraphics-lib})