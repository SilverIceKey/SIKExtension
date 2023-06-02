#include <jni.h>
#include <cstring>
#include <string>
#include "LibEncrypt/aes.h"
#include "LibEncrypt/md5.h"
#include <android/log.h>

//static const uint8_t AES_KEY[] = "96b6e222eb4a497ebb1ef91462cf5d0a";
//static const string PWD_MD5_KEY = "96b6e222eb4a497ebb1ef91462cf5d0a";
uint8_t* jstringTostring(JNIEnv* env, jstring jstr);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sk_skextension_utils_encrypt_EncryptUtil_AESEncode(JNIEnv *env, jobject thiz, jstring key,
                                                            jstring content) {
    uint8_t *keyData = jstringTostring(env,key);
    const char *str = env->GetStringUTFChars(content,JNI_FALSE);
    char *result = AES_ECB_PKCS5_Encrypt(str, keyData);//AES ECB PKCS7Padding加密
//    char *result = AES_CBC_PKCS7_Encrypt(str, AES_KEY, AES_IV);//AES CBC PKCS7Padding加密
    return env->NewStringUTF(result);
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sk_skextension_utils_encrypt_EncryptUtil_AESDecode(JNIEnv *env, jobject thiz, jstring key,
                                                            jstring content) {
    uint8_t *keyData = jstringTostring(env,key);
    const char *str = env->GetStringUTFChars(content, 0);
    char *result = AES_ECB_PKCS5_Decrypt(str, keyData);//AES ECB PKCS7Padding解密
//    char *result = AES_CBC_PKCS7_Decrypt(str, AES_KEY, AES_IV);//AES CBC PKCS7Padding解密
    env->ReleaseStringUTFChars(content, str);
    jsize len = (jsize) strlen(result);
    jbyteArray jbArr = env->NewByteArray(len);
    env->SetByteArrayRegion(jbArr, 0, len, (jbyte *) result);
    return jbArr;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sk_skextension_utils_encrypt_EncryptUtil_MD5Encode(JNIEnv *env, jobject thiz,
                                                            jstring content) {
    const char *str = env->GetStringUTFChars(content, 0);
    string result = MD5(string(str)).toStr();
    env->ReleaseStringUTFChars(content, str);
    return env->NewStringUTF((result).data());//最后再加三个#
}
//jstring to uint8_t*
uint8_t* jstringTostring(JNIEnv* env, jstring jstr)
{
    uint8_t* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (uint8_t*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}