#include <jni.h>
#include <unordered_map>
#include <string>
#include <mutex>

std::unordered_map<std::string, jobject> dataStore;
std::mutex mtx;

extern "C" {

JNIEXPORT void JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_saveData(JNIEnv* env, jobject obj, jstring key, jobject value) {
    const char* keyStr = env->GetStringUTFChars(key, nullptr);
    std::string keyCppStr(keyStr);
    env->ReleaseStringUTFChars(key, keyStr);

    std::lock_guard<std::mutex> lock(mtx);
    if (value != nullptr) {
        dataStore[keyCppStr] = env->NewGlobalRef(value);
    } else {
        dataStore[keyCppStr] = nullptr;
    }
}

JNIEXPORT jobject JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_getData(JNIEnv* env, jobject obj, jstring key, jboolean isDeleteAfterGet) {
    const char* keyStr = env->GetStringUTFChars(key, nullptr);
    std::string keyCppStr(keyStr);
    env->ReleaseStringUTFChars(key, keyStr);

    std::lock_guard<std::mutex> lock(mtx);
    auto it = dataStore.find(keyCppStr);
    if (it != dataStore.end()) {
        jobject value = it->second;
        if (isDeleteAfterGet == JNI_TRUE) {
            dataStore.erase(it);
        }
        return value;
    }
    return nullptr;
}

JNIEXPORT jboolean JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_hasData(JNIEnv* env, jobject obj, jstring key) {
    const char* keyStr = env->GetStringUTFChars(key, nullptr);
    std::string keyCppStr(keyStr);
    env->ReleaseStringUTFChars(key, keyStr);

    std::lock_guard<std::mutex> lock(mtx);
    return dataStore.find(keyCppStr) != dataStore.end();
}

}
