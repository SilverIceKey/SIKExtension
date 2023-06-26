#include <jni.h>
#include "gmssl/aes.h"
#include "../ConvertUtils.h"
#include "PaddingUtils.h"
#include <android/log.h>

//
// Created by zhouw on 2023/6/26.
//
AES_KEY encrypt_aes_key;
AES_KEY decrypt_aes_key;

void aes_ecb_encrypt(const AES_KEY *key, const uint8_t *in, size_t nblocks, uint8_t *out);

void aes_ecb_decrypt(const AES_KEY *key, const uint8_t *in, size_t nblocks, uint8_t *out);

extern "C"
JNIEXPORT void JNICALL
Java_com_sik_sikencrypt_enrypt_AESEncrypt_initAES(JNIEnv *env, jobject thiz, jbyteArray key) {
    uint8_t *tempKey = ConvertUtils::jByteArrayToUInt8t(env, key);
    size_t keySize = env->GetArrayLength(key);
    aes_set_encrypt_key(&encrypt_aes_key, tempKey, keySize);
    aes_set_decrypt_key(&decrypt_aes_key, tempKey, keySize);
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_enrypt_AESEncrypt_encrypt(JNIEnv *env, jobject thiz, jstring mode,
                                                  jstring padding, jbyteArray iv,
                                                  jbyteArray data_bytes) {
    const char *modeChars = env->GetStringUTFChars(mode, nullptr);
    const char *paddingChars = env->GetStringUTFChars(padding, nullptr);
    size_t dataEncryptLength = env->GetArrayLength(data_bytes);
    uint8_t *dataEncrypt = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);
    std::vector<uint8_t> dataVector(dataEncrypt, dataEncrypt + dataEncryptLength);
    if (strcmp(modeChars, "CBC") != 0 && strcmp(paddingChars, "PKCS5Padding") == 0) {
        if (strcmp(paddingChars, "PKCS5Padding") == 0) {
            // 使用 PKCS5Padding
            PKCS5Padding(dataVector, AES_BLOCK_SIZE);
        }
    }
    size_t resultDataLength = dataVector.size();
    uint8_t resultData[resultDataLength];
    if (strcmp(modeChars, "ECB") == 0) {
        aes_ecb_encrypt(&encrypt_aes_key, dataVector.data(), resultDataLength / 16, resultData);
    } else if (strcmp(modeChars, "CBC") == 0) {
        uint8_t *ivData = ConvertUtils::jByteArrayToUInt8t(env, iv);
        aes_cbc_padding_encrypt(&encrypt_aes_key, ivData, dataVector.data(), resultDataLength,
                                resultData,
                                &resultDataLength);
    } else if (strcmp(modeChars, "CTR") == 0) {
        uint8_t ctr[16];
        aes_ctr_encrypt(&encrypt_aes_key, ctr, dataVector.data(), resultDataLength, resultData);
    }
    jbyteArray result = env->NewByteArray(static_cast<jsize>(resultDataLength));
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(resultDataLength),
                            reinterpret_cast<jbyte *>(resultData));
    return result;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_enrypt_AESEncrypt_decrypt(JNIEnv *env, jobject thiz, jstring mode,
                                                  jstring padding, jbyteArray iv,
                                                  jbyteArray data_bytes) {
    const char *modeChars = env->GetStringUTFChars(mode, nullptr);
    const char *paddingChars = env->GetStringUTFChars(padding, nullptr);
    size_t dataDecryptLength = env->GetArrayLength(data_bytes);
    uint8_t *dataDecrypt = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);

    size_t outDataLength = dataDecryptLength;
    uint8_t outData[outDataLength];
    if (strcmp(modeChars, "ECB") == 0) {
        aes_ecb_decrypt(&decrypt_aes_key, dataDecrypt, dataDecryptLength / 16, outData);
    } else if (strcmp(modeChars, "CBC") == 0) {
        uint8_t *ivData = ConvertUtils::jByteArrayToUInt8t(env, iv);
        aes_cbc_padding_decrypt(&decrypt_aes_key, ivData, dataDecrypt, dataDecryptLength,
                                outData,
                                &outDataLength);
    } else if (strcmp(modeChars, "CTR") == 0) {
        uint8_t ctr[16];
        aes_ctr_decrypt(&decrypt_aes_key, ctr, dataDecrypt, dataDecryptLength, outData);
    }
    // 将 jbyteArray 转换为 std::vector<uint8_t>
    std::vector<uint8_t> dataVector(outData, outData + outDataLength);
    if (strcmp(modeChars, "ECB") == 0) {
        if (strcmp(paddingChars, "PKCS5Padding") == 0) {
            // 使用 PKCS5Unpadding
            PKCS5Unpadding(dataVector);
        }
    }
    size_t resultDataLength = dataVector.size();
    uint8_t *resultData = new uint8_t[resultDataLength];
    std::copy(dataVector.begin(), dataVector.end(), resultData);
    jbyteArray result = env->NewByteArray(static_cast<jsize>(resultDataLength));
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(resultDataLength),
                            reinterpret_cast<jbyte *>(resultData));
    delete[] resultData;
    return result;
}

//aes ecb 加密
void aes_ecb_encrypt(const AES_KEY *key, const uint8_t *in, size_t nblocks, uint8_t *out) {
    while (nblocks--) {
        aes_encrypt(key, in, out);
        in += 16;
        out += 16;
    }
}

//aes ecb 解密
void aes_ecb_decrypt(const AES_KEY *key, const uint8_t *in, size_t nblocks, uint8_t *out) {
    while (nblocks--) {
        aes_decrypt(key, in, out);
        in += 16;
        out += 16;
    }
}