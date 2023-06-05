cmake_minimum_required(VERSION 3.10.2)

add_library( # Specifies the name of the library.
        LibEncrypt

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        aes.c
        aes.h
        base64.c
        base64.h
        md5.cpp
        md5.h)