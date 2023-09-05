//
// Created by zhouw on 2023/6/24.
//

#ifndef SKEXTENSIONSAMPLE_CONVERTUTILS_H
#define SKEXTENSIONSAMPLE_CONVERTUTILS_H

#include <jni.h>
#include <string>


bool jByteArrayToUInt8(JNIEnv *env, jbyteArray array, uint8_t keyValue[16]);
unsigned char* jByteArrayToUnsignedChar(JNIEnv *env, jbyteArray array);
class ConvertUtils {
/**
 * jbyteArray转uint8_t
 * @param dataArray
 * @return
 */
public:
    static uint8_t* jByteArrayToUInt8t(JNIEnv *env, jbyteArray dataArray);
    static uint8_t* jStringToUInt8T(JNIEnv *env, jstring dataString);
    static jstring* uInt8TToJString(JNIEnv *env,uint8_t data);
    static jbyteArray uint8_to_jbyteArray(JNIEnv *env, uint8_t* buf, int len);
};

#endif //SKEXTENSIONSAMPLE_CONVERTUTILS_H
