#include "gmssl/sm3.h"
#include "ConvertUtils.h"
#include <jni.h>
#include <string>
#include <algorithm>

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_message_1digest_SM3MessageDigest_digest(JNIEnv *env, jobject thiz,
                                                                jbyteArray data_bytes) {
    SM3_CTX sm3_ctx;
    uint8_t dgst[32];
    sm3_init(&sm3_ctx);
    uint8_t *uint8Ptr = ConvertUtils::jByteArrayToUInt8t(env, data_bytes);
    sm3_update(&sm3_ctx, uint8Ptr, 3);
    sm3_finish(&sm3_ctx, dgst);
    jbyteArray result = env->NewByteArray(sizeof(dgst));
    env->SetByteArrayRegion(result, 0, sizeof(dgst), reinterpret_cast<jbyte *>(dgst));
    return result;
}