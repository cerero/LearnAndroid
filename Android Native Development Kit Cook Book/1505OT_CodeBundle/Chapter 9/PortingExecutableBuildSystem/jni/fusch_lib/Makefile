# Makefile fuer die FUGENSCHNITZER-Programmbibliothek.
# Dieses Makefile erzeugt die Programmbibliothek
# - seamcarv.dll      (Windows) oder
# - libseamcarv.dylib (OSX) oder
# - libseamcarv.so    (Linux).
#
# Makefile for the FUGENSCHNITZER Program Library.
# This makefile builds the program library file
# - seamcarv.dll      (Windows) oder
# - libseamcarv.dylib (OSX) oder
# - libseamcarv.so    (Linux).
#
# IMPORTANT NOTE: Before executing make, you need to uncomment the
# section for your platform below. For example, to build libseamcarv.dylib
# for OSX, uncomment the lines between "# *** OSX" and "# *** Linux".
#
# CC      -- command line for compiler
# RM      -- remove command (Windows: del, Unix-like: rm)
# WINDRES -- command line for resources compiler (Windows)
#
# CFLAGS  -- GCC command line parameters for compiling:
#            -c -std=c99 are essential;
#            -DBUILDING_LIB is essential on Windows and elses ignored;
#            -fvisibility=hidden is recommended on Non-Windows;
#            -fno-common is essential on OSX and may only be set there;
#            -Ox may be appended individually.
#            You may also need to set -fpic or -fPIC on some platforms. See
#            the GCC manual for details.
# LDFLAGS -- command line parameters for linking; shared library
#            parameter is essential: -shared (Windows/Linux) or -dynamiclib (OSX)
#            If you experience linker errors complaining that some math functions
#            like lrintf cannot be resolved, try adding -lm to explicitely link
#            against the math library.
#
# LIB  -- file name of the program library to build; this is the linker output
# OBJ  -- compiler output files; the library is built by linking these
# O3   -- source code files to be compiled using maximum optimization -O3
# Os   -- source code files to be compiled without using maximum optimization
# Ores -- resource file for version information (Windows)
#
# lib     -- Make target for building the library without clean/cleanup
# obj     -- Make target for compiling the source code without clean
# clean   -- delete compiler and linker output
# cleanup -- delete compiler output

CC             = gcc

# *** Windows
# LIBSUFFIX      = .dll
# LIBPREFIX      =
# ores           = scdllres.o

# RM             = del
# WINDRES        = windres

# CFLAGS         = -c -std=c99 -DBUILDING_LIB -pedantic -Wextra -Os -mmmx -msse
# LDFLAGS        = -shared -s

# *** OSX

# LIBSUFFIX      = .dylib
# LIBPREFIX      = lib
# ores           =

# RM             = rm
# WINDRES        =

# CFLAGS         = -c -std=c99 -pedantic -Wextra -Os -fvisibility=hidden -fno-common -arch i386 -arch ppc -msse
# LDFLAGS        = -dynamiclib -arch i386 -arch ppc -install_name libseamcarv.dylib -Wl,-x

# *** Linux

# LIBSUFFIX      = .so
# LIBPREFIX      = lib
# ores           =

# RM             = rm
# WINDRES        =

# CFLAGS         = -c -std=c99 -pedantic -Wextra -Os -fvisibility=hidden -mmmx -msse
# LDFLAGS        = -shared -s

# ***

LIBBASENAME    = seamcarv

O3             = sc_core.c sc_carve.c sc_color.c sc_shift.c
Os             = sc_mgmnt.c seamcarv.c
Ores           = scdllres.rc

OBJ            = o3 os ores
ALLOBJ         = *.o

LIB            = $(LIBPREFIX)$(LIBBASENAME)$(LIBSUFFIX)

.PHONY: all obj o3 os ores clean cleanup

all: clean lib cleanup

lib: $(LIB)

$(LIB): $(OBJ)
	@echo Linking...
	$(CC) $(ALLOBJ) -o $@ $(LDFLAGS)
	@echo $(LIB) was successfully built.

o3: $(O3)
	$(CC) $^ $(CFLAGS) -O3
os: $(Os)
	$(CC) $^ $(CFLAGS)

ores: $(ores)
$(ores): $(Ores)
	$(WINDRES) $< $@

obj: $(OBJ)

cleanup:
	-$(RM) $(ALLOBJ)

clean:
	-$(RM) $(ALLOBJ) $(LIB)
