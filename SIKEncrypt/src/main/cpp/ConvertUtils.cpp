//
// Created by zhouw on 2023/6/24.
//

#include "ConvertUtils.h"

bool jByteArrayToUInt8(JNIEnv *env, jbyteArray array, uint8_t keyValue[16]) {
    jsize length = env->GetArrayLength(array);
    if (length != 16) {
        // Handle error if the size is not what you expected
        return false;
    }

    jbyte* byteArrayData = env->GetByteArrayElements(array, nullptr);
    if (byteArrayData == nullptr) {
        // Handle error
        return false;
    }

    std::copy(byteArrayData, byteArrayData + length, keyValue);

    env->ReleaseByteArrayElements(array, byteArrayData, JNI_ABORT);

    return true;
}
unsigned char* jByteArrayToUnsignedChar(JNIEnv *env, jbyteArray array) {
    jsize length = env->GetArrayLength(array);
    jbyte* byteArrayData = env->GetByteArrayElements(array, nullptr);
    if (byteArrayData == nullptr) {
        // Handle error
        return nullptr;
    }

    unsigned char* result = new unsigned char[length];
    std::copy(byteArrayData, byteArrayData + length, result);

    env->ReleaseByteArrayElements(array, byteArrayData, JNI_ABORT);

    return result;
}


uint8_t* ConvertUtils::jByteArrayToUInt8t(JNIEnv *env, jbyteArray array) {
    jsize length = env->GetArrayLength(array);
    jbyte* byteArrayData = env->GetByteArrayElements(array, nullptr);
    if (byteArrayData == nullptr) {
        // Handle error
        return nullptr;
    }

    uint8_t* result = new uint8_t[length];
    std::copy(byteArrayData, byteArrayData + length, result);
    env->ReleaseByteArrayElements(array, byteArrayData, JNI_ABORT);
    return result;
}


uint8_t *ConvertUtils::jStringToUInt8T(JNIEnv *env, jstring dataString) {
    // 将 jstring 转换为 const char*
    const char *str = env->GetStringUTFChars(dataString, nullptr);

    // 做一些事情，例如将字符串转换为 uint8_t 数组
    // 注意这个示例中的转换可能并不适合所有用途，特别是如果你的字符串包含非 ASCII 字符时
    // 对于这种情况，你可能需要使用更复杂的转换方法
    uint8_t *data = new uint8_t[strlen(str)];
    for (int i = 0; i < strlen(str); i++) {
        data[i] = static_cast<uint8_t>(str[i]);
    }
    // 清理
    env->ReleaseStringUTFChars(dataString, str);
    return data;
}

jstring *ConvertUtils::uInt8TToJString(JNIEnv *env, uint8_t data) {
    // 将 uint8_t 数组转换回 jstring，这只是一个示例，实际上你可能会根据需要处理 uint8_t 数组
    jstring output = env->NewStringUTF(reinterpret_cast<const char *>(data));
    return reinterpret_cast<jstring *>(output);
}

jbyteArray ConvertUtils::uint8_to_jbyteArray(JNIEnv *env, uint8_t* buf, int len) {
    jbyteArray array = env->NewByteArray(len);
    env->SetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte*>(buf));
    return array;
}

