//
// Created by zhouw on 2023/6/24.
//

#ifndef SKEXTENSIONSAMPLE_CONVERTUTILS_H
#define SKEXTENSIONSAMPLE_CONVERTUTILS_H

#include <jni.h>
#include <string>

class ConvertUtils {
/**
 * jbyteArray转uint8_t
 * @param dataArray
 * @return
 */
public:
    static uint8_t* jByteArrayToUInt8t(JNIEnv *env, jbyteArray dataArray);
};


#endif //SKEXTENSIONSAMPLE_CONVERTUTILS_H
