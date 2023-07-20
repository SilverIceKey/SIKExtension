#include <jni.h>
#include <vector>
#include "gmssl/sm4.h"
#include "../ConvertUtils.h"
#include "PaddingUtils.h"
#include "gmssl/mem.h"
#include "gmssl/error.h"


extern "C"
void sm4_ecb_encrypt(const SM4_KEY *key,
                     const uint8_t *in, size_t nblocks, uint8_t *out);

extern "C"
void sm4_ecb_decrypt(const SM4_KEY *key,
                     const uint8_t *in, size_t nblocks, uint8_t *out);


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_enrypt_SM4Encrypt_encrypt(JNIEnv *env, jobject thiz, jbyteArray key,
                                                  jstring mode,
                                                  jstring padding, jbyteArray iv,
                                                  jbyteArray data_bytes) {
    const char *modeChars = env->GetStringUTFChars(mode, nullptr);
    const char *paddingChars = env->GetStringUTFChars(padding, nullptr);
    size_t dataEncryptLength = env->GetArrayLength(data_bytes);
    uint8_t *dataEncrypt = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);
    std::vector<uint8_t> dataVector(dataEncrypt, dataEncrypt + dataEncryptLength);
    if (strcmp(modeChars, "ECB") == 0) {
        if (strcmp(paddingChars, "PKCS5Padding") == 0) {
            // 使用 PKCS5Padding
            PKCS5Padding(dataVector, SM4_BLOCK_SIZE);
        }
    }
    size_t resultDataLength = dataVector.size();
    uint8_t resultData[resultDataLength];
    uint8_t *sm4_key_data = ConvertUtils::jByteArrayToUInt8t(env, key);
    SM4_KEY sm4_key;
    sm4_set_encrypt_key(&sm4_key, sm4_key_data);
    if (strcmp(modeChars, "ECB") == 0) {
        sm4_ecb_encrypt(&sm4_key, dataVector.data(), dataEncryptLength / SM4_BLOCK_SIZE,
                        resultData);
    } else if (strcmp(modeChars, "CBC") == 0) {
        uint8_t *ivData = ConvertUtils::jByteArrayToUInt8t(env, iv);
        sm4_cbc_padding_encrypt(&sm4_key, ivData, dataVector.data(), dataEncryptLength,
                                resultData,
                                &resultDataLength);
    } else if (strcmp(modeChars, "CTR") == 0) {
        uint8_t ctr[16];
        sm4_ctr_encrypt(&sm4_key, ctr, dataVector.data(), dataEncryptLength, resultData);
    }
    jbyteArray result = env->NewByteArray(static_cast<jsize>(resultDataLength));
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(resultDataLength),
                            reinterpret_cast<jbyte *>(resultData));
    return result;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_enrypt_SM4Encrypt_decrypt(JNIEnv *env, jobject thiz, jbyteArray key,
                                                  jstring mode,
                                                  jstring padding, jbyteArray iv,
                                                  jbyteArray data_bytes) {
    const char *modeChars = env->GetStringUTFChars(mode, nullptr);
    const char *paddingChars = env->GetStringUTFChars(padding, nullptr);
    size_t dataDecryptLength = env->GetArrayLength(data_bytes);
    uint8_t *dataDecrypt = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);

    size_t outDataLength = dataDecryptLength;
    uint8_t outData[outDataLength];
    uint8_t *sm4_key_data = ConvertUtils::jByteArrayToUInt8t(env, key);
    SM4_KEY sm4_key;
    sm4_set_decrypt_key(&sm4_key, sm4_key_data);
    if (strcmp(modeChars, "ECB") == 0) {
        sm4_ecb_decrypt(&sm4_key, dataDecrypt, dataDecryptLength / SM4_BLOCK_SIZE, outData);
    } else if (strcmp(modeChars, "CBC") == 0) {
        size_t resultLength = 3;
        uint8_t *resultData;
        uint8_t *ivData = ConvertUtils::jByteArrayToUInt8t(env, iv);
        sm4_cbc_padding_decrypt(&sm4_key, ivData, dataDecrypt, dataDecryptLength,
                                resultData,
                                &resultLength);
        return ConvertUtils::uint8_to_jbyteArray(env,resultData,resultLength);
    } else if (strcmp(modeChars, "CTR") == 0) {
        uint8_t ctr[16];
        sm4_ctr_decrypt(&sm4_key, ctr, dataDecrypt, dataDecryptLength, outData);
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

extern "C"
void sm4_ecb_encrypt(const SM4_KEY *key,
                     const uint8_t *in, size_t nblocks, uint8_t *out) {
    while (nblocks--) {
        sm4_encrypt(key, in, out);
        in += 16;
        out += 16;
    }
}

extern "C"
void sm4_ecb_decrypt(const SM4_KEY *key,
                     const uint8_t *in, size_t nblocks, uint8_t *out) {
    while (nblocks--) {
        sm4_encrypt(key, in, out);
        in += 16;
        out += 16;
    }
}
