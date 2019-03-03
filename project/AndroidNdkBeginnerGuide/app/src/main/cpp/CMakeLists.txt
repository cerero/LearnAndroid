cmake_minimum_required(VERSION 3.4.1)

include_directories(include dm04BasicJNI dm05LiveCamera)

add_library( # Sets the name of the library.
        com_packtpub_store_Store

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        dm04BasicJNI/com_packtpub_store_Store.cpp
        dm04BasicJNI/Store.cpp)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

add_library( # Sets the name of the library.
        livecamera

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        dm05LiveCamera/CameraDecoder.c)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library( # Sets the name of the path variable.
        jnigraphics-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        jnigraphics)


target_link_libraries(com_packtpub_store_Store
        ${log-lib})

target_link_libraries(native-lib
        ${log-lib})

target_link_libraries(livecamera
        ${log-lib}
        ${jnigraphics-lib})
