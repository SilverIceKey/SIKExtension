#include <jni.h>
#include <vector>
#include "gmssl/sm4.h"
#include "../ConvertUtils.h"
#include "PaddingUtils.h"
#include "gmssl/mem.h"
#include "gmssl/error.h"
#include <android/log.h>



extern "C"
void sm4_ecb_encrypt(const SM4_KEY *key,
                     const uint8_t *in, size_t nblocks, uint8_t *out);

extern "C"
void sm4_ecb_decrypt(const SM4_KEY *key,
                     const uint8_t *in, size_t nblocks, uint8_t *out);

void logJByteArray(JNIEnv *env, jbyteArray array);

void logUnsignedCharArray(const unsigned char* array, size_t length);


extern "C"
void sm4_ecb_encrypt(const SM4_KEY *key,
                     const uint8_t *in, size_t nblocks, uint8_t *out) {
    while (nblocks--) {
        sm4_encrypt(key, in, out);
        in += SM4_BLOCK_SIZE;
        out += SM4_BLOCK_SIZE;
    }
}

extern "C"
void sm4_ecb_decrypt(const SM4_KEY *key,
                     const uint8_t *in, size_t nblocks, uint8_t *out) {
    while (nblocks--) {
        sm4_decrypt(key, in, out);
        in += SM4_BLOCK_SIZE;
        out += SM4_BLOCK_SIZE;
    }
}

void logJByteArray(JNIEnv *env, jbyteArray array) {
    jbyte* byteArrayData = env->GetByteArrayElements(array, nullptr);
    jsize length = env->GetArrayLength(array);

    if (byteArrayData == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "YourTag", "Failed to get byte array elements");
        return;
    }

    char* logMessage = new char[length * 3 + 1]; // 3 characters per byte (2 hex digits and a space)
    for (int i = 0; i < length; i++) {
        sprintf(logMessage + i * 3, "%02X ", static_cast<unsigned char>(byteArrayData[i]));
    }
    logMessage[length * 3] = '\0'; // Null-terminate the string

    __android_log_print(ANDROID_LOG_INFO, "数据", "%s", logMessage);

    delete[] logMessage;
    env->ReleaseByteArrayElements(array, byteArrayData, JNI_ABORT);
}

void logUnsignedCharArray(const unsigned char* array, size_t length) {
    char* logMessage = new char[length * 3 + 1]; // 3 characters per byte (2 hex digits and a space)
    for (size_t i = 0; i < length; i++) {
        sprintf(logMessage + i * 3, "%02X ", array[i]);
    }
    logMessage[length * 3] = '\0'; // Null-terminate the string

    __android_log_print(ANDROID_LOG_INFO, "数据", "%s", logMessage);

    delete[] logMessage;
}


unsigned char* keyValue;

extern "C"
JNIEXPORT void JNICALL
Java_com_sik_sikencrypt_enrypt_SM4Encrypt_initKey(JNIEnv *env, jobject thiz, jbyteArray key) {
    keyValue = jByteArrayToUnsignedChar(env, key);
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_enrypt_SM4Encrypt_encryptECB(JNIEnv *env, jobject thiz,
                                                     jbyteArray data_bytes) {
    uint8_t *messageData = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);
    size_t messageLength = env->GetArrayLength(data_bytes);
    uint8_t encryptResult[messageLength];
//    sm4_encrypt(&sm4EncryptKey, messageData, *encryptResult);
    SM4_KEY sm4Key;
    sm4_set_encrypt_key(&sm4Key,keyValue);
    sm4_ecb_encrypt(&sm4Key, messageData, messageLength / SM4_BLOCK_SIZE, encryptResult);
    jbyteArray result = env->NewByteArray(static_cast<jsize>(messageLength));
    if (result == nullptr) {
        // Handle error (e.g., throw an exception)
        return nullptr;
    }
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(messageLength),
                            reinterpret_cast<const jbyte *>(encryptResult));

    return result;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_enrypt_SM4Encrypt_decryptECB(JNIEnv *env, jobject thiz,
                                                     jbyteArray data_bytes) {
    uint8_t *messageData = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);
    size_t messageLength = env->GetArrayLength(data_bytes);
    uint8_t decryptResult[messageLength];
//    sm4_encrypt(&sm4EncryptKey, messageData, *decryptResult);
    SM4_KEY sm4Key;
    sm4_set_decrypt_key(&sm4Key,keyValue);
    sm4_ecb_decrypt(&sm4Key,messageData,messageLength/SM4_BLOCK_SIZE,decryptResult);
    jbyteArray result = env->NewByteArray(static_cast<jsize>(SM4_BLOCK_SIZE));
    if (result == nullptr) {
        // Handle error (e.g., throw an exception)
        return nullptr;
    }
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(SM4_BLOCK_SIZE),
                            reinterpret_cast<const jbyte *>(decryptResult));

    return result;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_enrypt_SM4Encrypt_encryptCBC(JNIEnv *env, jobject thiz, jbyteArray iv,
                                                     jbyteArray data_bytes) {
    uint8_t* messageData = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);
    uint8_t* ivData = ConvertUtils::jByteArrayToUInt8t(env, iv);
    logJByteArray(env,data_bytes);
    size_t messageLength = env->GetArrayLength(data_bytes);
    size_t encryptResultLength;
    uint8_t encryptResult[messageLength];
    SM4_KEY sm4Key;
    sm4_set_encrypt_key(&sm4Key,keyValue);
    sm4_cbc_padding_encrypt(&sm4Key, ivData, messageData, messageLength, encryptResult,
                            &encryptResultLength);
    logUnsignedCharArray(reinterpret_cast<const unsigned char *>(encryptResult), encryptResultLength);
    jbyteArray result = env->NewByteArray(static_cast<jsize>(encryptResultLength));
    if (result == nullptr) {
        // Handle error (e.g., throw an exception)
        return nullptr;
    }
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(encryptResultLength),
                            reinterpret_cast<const jbyte *>(encryptResult));
    logJByteArray(env,result);
    return result;
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_enrypt_SM4Encrypt_decryptCBC(JNIEnv *env, jobject thiz, jbyteArray iv,
                                                     jbyteArray data_bytes) {
    uint8_t* messageData = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);
    uint8_t* ivData = ConvertUtils::jByteArrayToUInt8t(env, iv);
    logJByteArray(env,data_bytes);
    size_t messageLength = env->GetArrayLength(data_bytes);
    size_t decryptResultLength = messageLength;
    uint8_t decryptResult[messageLength];
    SM4_KEY sm4Key;
    sm4_set_decrypt_key(&sm4Key,keyValue);
    sm4_cbc_padding_decrypt(&sm4Key, ivData, messageData, messageLength, decryptResult,
                            &decryptResultLength);
    logUnsignedCharArray(reinterpret_cast<const unsigned char *>(decryptResult), decryptResultLength);
    jbyteArray result = env->NewByteArray(static_cast<jsize>(decryptResultLength));
    if (result == nullptr) {
        // Handle error (e.g., throw an exception)
        return nullptr;
    }
    env->SetByteArrayRegion(result, 0, static_cast<jsize>(decryptResultLength),
                            reinterpret_cast<const jbyte *>(decryptResult));
    logJByteArray(env,result);
    return result;
}