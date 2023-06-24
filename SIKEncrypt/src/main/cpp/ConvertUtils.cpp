//
// Created by zhouw on 2023/6/24.
//

#include "ConvertUtils.h"


uint8_t *ConvertUtils::jByteArrayToUInt8t(JNIEnv *env, jbyteArray dataArray){
    jbyte *byteArrayData = env->GetByteArrayElements(dataArray, NULL);
    uint8_t *result = reinterpret_cast<uint8_t *>(byteArrayData);
    env->ReleaseByteArrayElements(dataArray, byteArrayData, 0);
    return result;
}
