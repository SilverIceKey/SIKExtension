#include "gmssl/sm3.h"
#include <jni.h>
#include <string>
#include <algorithm>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_message_1digest_SM3MessageDigest_digest(JNIEnv *env, jobject thiz,
                                                                jbyteArray data_bytes) {
    SM3_CTX sm3_ctx;
    uint8_t buf[4096];
    size_t len;
    uint8_t dgst[32];
    int i;
    jsize arrayLength = env->GetArrayLength(data_bytes);
    jbyte *byteArrayData = env->GetByteArrayElements(data_bytes, NULL);
    sm3_init(&sm3_ctx);
    size_t offset = 0; // 记录偏移量

    uint8_t *uint8Ptr = reinterpret_cast<uint8_t *>(byteArrayData);
    sm3_update(&sm3_ctx,uint8Ptr,3);
    sm3_finish(&sm3_ctx, dgst);
    env->ReleaseByteArrayElements(data_bytes, byteArrayData, 0);
    jbyteArray result = env->NewByteArray(sizeof(dgst));
    env->SetByteArrayRegion(result, 0, sizeof(dgst), reinterpret_cast<jbyte *>(dgst));
    return result;
}