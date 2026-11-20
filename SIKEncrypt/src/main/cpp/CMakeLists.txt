cmake_minimum_required(VERSION 3.22.1)

project(sikencrypt_crypto)

find_package(openssl CONFIG REQUIRED)

add_library(
    sikencrypt_crypto
    SHARED
    sikencrypt_crypto.cpp
)

target_link_libraries(
    sikencrypt_crypto
    openssl::libcrypto.a
    log
)

target_compile_options(
    sikencrypt_crypto
    PRIVATE
    -fvisibility=hidden
    -ffunction-sections
    -fdata-sections
)

target_link_options(
    sikencrypt_crypto
    PRIVATE
    -Wl,--gc-sections
    -Wl,--exclude-libs,ALL
)
