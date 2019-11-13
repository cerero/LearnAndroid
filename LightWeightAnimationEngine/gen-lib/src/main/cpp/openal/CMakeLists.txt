cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

aux_source_directory(. SRC_LIST)
#file(GLOB_RECURSE HEADER_LIST "*.h")

add_library(openal SHARED ${SRC_LIST})
find_library(log-lib log)
target_link_libraries(openal ${log-lib} )

# copy out the lib binary and remove generated files
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../distribution)
set_target_properties(openal
        PROPERTIES
        LIBRARY_OUTPUT_DIRECTORY
        "${distribution_DIR}/openal/lib/${ANDROID_ABI}")

file(COPY . DESTINATION ${distribution_DIR}/openal FILES_MATCHING PATTERN "*.h")
#add_custom_command(TARGET openal POST_BUILD
#                   COMMAND "${CMAKE_COMMAND}" -E
#                   copy "${HEADER_LIST}"
#                   "${distribution_DIR}/openal"
#                   COMMENT "Copying openal to output directory => ${distribution_DIR}")
