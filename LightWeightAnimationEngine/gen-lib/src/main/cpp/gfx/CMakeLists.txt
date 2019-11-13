cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../distribution)

aux_source_directory(. SRC_LIST)
aux_source_directory(./bullet bullet_SRC_LIST)
aux_source_directory(./detour detour_SRC_LIST)
aux_source_directory(./GLES2 GLES2_SRC_LIST)
aux_source_directory(./nvtristrip nvtristrip_SRC_LIST)
aux_source_directory(./png png_SRC_LIST)
aux_source_directory(./recast recast_SRC_LIST)
aux_source_directory(./ttf ttf_SRC_LIST)
aux_source_directory(./vorbis vorbis_SRC_LIST)
aux_source_directory(./zlib zlib_SRC_LIST)
list(APPEND SRC_LIST
        ${bullet_SRC_LIST} ${detour_SRC_LIST} ${GLES2_SRC_LIST}
        ${nvtristrip_SRC_LIST} ${png_SRC_LIST} ${recast_SRC_LIST}
        ${ttf_SRC_LIST} ${vorbis_SRC_LIST} ${zlib_SRC_LIST})

#file(GLOB_RECURSE HEADER_LIST "*.h")

#target_include_directories(gfx PRIVATE
#        ./
#        ${distribution_DIR})

include_directories(. ${distribution_DIR})

add_library(openal-lib SHARED IMPORTED)
set_target_properties(openal-lib PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/openal/lib/${ANDROID_ABI}/libopenal.so)

add_library(gfx SHARED ${SRC_LIST})
set_target_properties(gfx
        PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY
        "${distribution_DIR}/gfx/lib/${ANDROID_ABI}")

find_library(log-lib log)
find_library(GLESv2-lib GLESv2)
find_library(EGL-lib EGL)
find_library(z-lib z)
find_library(m-lib m)
find_library(dl-lib dl)

target_link_libraries(gfx
        ${log-lib} ${GLESv2-lib} ${EGL-lib}
        ${z-lib} ${m-lib} ${dl-lib}
        openal-lib )

file(COPY . DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./bullet DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./detour DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./GLES2 DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./nvtristrip DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./png DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./recast DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./ttf DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./vorbis DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
file(COPY ./zlib DESTINATION ${distribution_DIR}/gfx FILES_MATCHING PATTERN "*.h")
