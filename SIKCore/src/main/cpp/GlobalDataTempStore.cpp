#include <jni.h>
#include <unordered_map>
#include <mutex>
#include <string>

std::unordered_map<std::string, std::string> dataStore;
std::mutex storeMutex;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_nativeSaveData(
        JNIEnv *env, jobject /*obj*/, jstring key, jstring jsonValue) {
    const char *keyStr = env->GetStringUTFChars(key, nullptr);
    const char *jsonStr = env->GetStringUTFChars(jsonValue, nullptr);

    std::lock_guard<std::mutex> lock(storeMutex);
    dataStore[keyStr] = jsonStr;

    env->ReleaseStringUTFChars(key, keyStr);
    env->ReleaseStringUTFChars(jsonValue, jsonStr);
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_nativeGetData(
        JNIEnv *env, jobject /*obj*/, jstring key, jboolean isDeleteAfterGet) {
    const char *keyStr = env->GetStringUTFChars(key, nullptr);
    std::lock_guard<std::mutex> lock(storeMutex);

    auto it = dataStore.find(keyStr);
    jstring result = nullptr;

    if (it != dataStore.end()) {
        result = env->NewStringUTF(it->second.c_str());
        if (isDeleteAfterGet == JNI_TRUE) {
            dataStore.erase(it);
        }
    }

    env->ReleaseStringUTFChars(key, keyStr);
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_nativeHasData(
        JNIEnv *env, jobject /*obj*/, jstring key) {
    const char *keyStr = env->GetStringUTFChars(key, nullptr);
    std::lock_guard<std::mutex> lock(storeMutex);
    bool exists = (dataStore.find(keyStr) != dataStore.end());
    env->ReleaseStringUTFChars(key, keyStr);
    return exists ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_nativeClearData(
        JNIEnv *env, jobject /*obj*/, jstring key) {
    const char *keyStr = env->GetStringUTFChars(key, nullptr);
    std::lock_guard<std::mutex> lock(storeMutex);
    auto erased = dataStore.erase(keyStr);
    env->ReleaseStringUTFChars(key, keyStr);
    return erased ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_nativeClearAll(
        JNIEnv *env, jobject /*obj*/) {
    std::lock_guard<std::mutex> lock(storeMutex);
    dataStore.clear();
}

}
