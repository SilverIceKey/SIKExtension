#include <jni.h>
#include <unordered_map>
#include <list>
#include <string>
#include <mutex>

const size_t MAX_GLOBAL_REFS = 50000;

std::unordered_map<std::string, std::list<std::pair<std::string, jobject>>::iterator> dataStoreMap;
std::list<std::pair<std::string, jobject>> dataStoreList;
std::mutex mtx;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_saveData(JNIEnv *env, jobject obj, jstring key,
                                                       jobject value) {
    const char *keyStr = env->GetStringUTFChars(key, nullptr);
    std::string keyCppStr(keyStr);
    env->ReleaseStringUTFChars(key, keyStr);

    std::lock_guard<std::mutex> lock(mtx);

    // 检查是否超过最大引用数
    if (dataStoreList.size() >= MAX_GLOBAL_REFS) {
        // 达到上限，返回false
        return JNI_FALSE;
    }

    // 如果键已存在，先删除旧的引用
    if (dataStoreMap.find(keyCppStr) != dataStoreMap.end()) {
        auto it = dataStoreMap[keyCppStr];
        env->DeleteGlobalRef(it->second);
        dataStoreList.erase(it);
    }

    // 添加新的引用
    jobject globalRef = env->NewGlobalRef(value);
    dataStoreList.emplace_front(keyCppStr, globalRef);
    dataStoreMap[keyCppStr] = dataStoreList.begin();

    // 返回true表示保存成功
    return JNI_TRUE;
}

JNIEXPORT jobject JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_getData(JNIEnv *env, jobject obj, jstring key,
                                                      jboolean isDeleteAfterGet) {
    const char *keyStr = env->GetStringUTFChars(key, nullptr);
    std::string keyCppStr(keyStr);
    env->ReleaseStringUTFChars(key, keyStr);

    std::lock_guard<std::mutex> lock(mtx);
    auto it = dataStoreMap.find(keyCppStr);
    if (it != dataStoreMap.end()) {
        jobject value = it->second->second;
        if (isDeleteAfterGet == JNI_TRUE) {
            env->DeleteGlobalRef(value);
            dataStoreList.erase(it->second);
            dataStoreMap.erase(it);
        }
        return value;
    }
    return nullptr;
}

JNIEXPORT jboolean JNICALL
Java_com_sik_sikcore_data_GlobalDataTempStore_hasData(JNIEnv *env, jobject obj, jstring key) {
    const char *keyStr = env->GetStringUTFChars(key, nullptr);
    std::string keyCppStr(keyStr);
    env->ReleaseStringUTFChars(key, keyStr);

    std::lock_guard<std::mutex> lock(mtx);
    return dataStoreMap.find(keyCppStr) != dataStoreMap.end();
}
}
