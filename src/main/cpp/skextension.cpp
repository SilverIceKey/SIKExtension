#include <jni.h>
#include <cstring>
#include <string>
// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("skextension");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("skextension")
//      }
//    }
#include "extlibrary/aes.h"
#include "extlibrary/md5.h"
//#include <android/log.h>

//static const uint8_t AES_KEY[] = "96b6e222eb4a497ebb1ef91462cf5d0a";
//static const string PWD_MD5_KEY = "96b6e222eb4a497ebb1ef91462cf5d0a";

extern "C"
JNIEXPORT jstring JNICALL
Java_com_sk_skextension_utils_encrypt_EncryptUtil_AESEncode(JNIEnv *env, jobject thiz, jstring key,
                                                            jbyteArray content) {
    const char *cKey = env->GetStringUTFChars(key, NULL);
    std::string Keystr = std::string(cKey);
    uint8_t keyData[sizeof(*key)];
    std::copy(Keystr.begin(),Keystr.end(),keyData);
    char *str = NULL;
    jsize alen = env->GetArrayLength(content);
    jbyte *ba = env->GetByteArrayElements(content, JNI_FALSE);
    str = (char *) malloc(alen + 1);
    memcpy(str, ba, alen);
    str[alen] = '\0';
    env->ReleaseByteArrayElements(content, ba, 0);

    char *result = AES_ECB_PKCS7_Encrypt(str, keyData);//AES ECB PKCS7Padding加密
//    char *result = AES_CBC_PKCS7_Encrypt(str, AES_KEY, AES_IV);//AES CBC PKCS7Padding加密
    return env->NewStringUTF(result);
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sk_skextension_utils_encrypt_EncryptUtil_AESDecode(JNIEnv *env, jobject thiz, jstring key,
                                                            jstring content) {
    const char *cKey = env->GetStringUTFChars(key, NULL);
    std::string Keystr = std::string(cKey);
    uint8_t keyData[sizeof(*key)];
    std::copy(Keystr.begin(),Keystr.end(),keyData);
    const char *str = env->GetStringUTFChars(content, 0);
    char *result = AES_ECB_PKCS7_Decrypt(str, keyData);//AES ECB PKCS7Padding解密
//    char *result = AES_CBC_PKCS7_Decrypt(str, AES_KEY, AES_IV);//AES CBC PKCS7Padding解密
    env->ReleaseStringUTFChars(content, str);

    jsize len = (jsize) strlen(result);
    jbyteArray jbArr = env->NewByteArray(len);
    env->SetByteArrayRegion(jbArr, 0, len, (jbyte *) result);
    return jbArr;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sk_skextension_utils_encrypt_EncryptUtil_MD5Encode(JNIEnv *env, jobject thiz,jstring key,
                                                            jstring content) {
    const char *cKey = env->GetStringUTFChars(key, NULL);
    std::string Keystr = std::string(cKey);
    const char *str = env->GetStringUTFChars(content, 0);
    string result = MD5(MD5(Keystr + string(str)).toStr()).toStr();//加盐后进行两次md5
    env->ReleaseStringUTFChars(content, str);
    return env->NewStringUTF(("###" + result).data());//最后再加三个#
}