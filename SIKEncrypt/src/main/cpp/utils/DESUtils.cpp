#include <jni.h>
#include <vector>
#include "gmssl/des.h"
#include "../ConvertUtils.h"
//
// Created by zhouw on 2023/6/26.
//

DES_KEY encrypt_des_key;

extern "C"
JNIEXPORT void JNICALL
Java_com_sik_sikencrypt_enrypt_DESEncrypt_initDES(JNIEnv *env, jobject thiz, jbyteArray key) {
    uint8_t *tempKey = ConvertUtils::jByteArrayToUInt8t(env, key);
    des_set_encrypt_key(&encrypt_des_key, tempKey);
}