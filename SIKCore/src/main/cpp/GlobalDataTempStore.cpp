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
    if (value == nullptr) {
        // 不允许存储空值，直接返回 false
        return JNI_FALSE;
    }

    const char *keyStr = env->GetStringUTFChars(key, nullptr);
    std::string keyCppStr(keyStr);
    env->ReleaseStringUTFChars(key, keyStr);

    std::lock_guard<std::mutex> lock(mtx);

    // 检查是否超过最大引用数
    if (dataStoreList.size() >= MAX_GLOBAL_REFS) {
        return JNI_FALSE;  // 达到上限，返回 false
    }

    // 如果键已存在，删除旧的引用
    if (dataStoreMap.find(keyCppStr) != dataStoreMap.end()) {
        auto it = dataStoreMap[keyCppStr];
        env->DeleteGlobalRef(it->second);
        dataStoreList.erase(it);
    }

    // 添加新的引用
    jobject globalRef = env->NewGlobalRef(value);
    dataStoreList.emplace_front(keyCppStr, globalRef);
    dataStoreMap[keyCppStr] = dataStoreList.begin();

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

        // 创建一个新的 Local 引用，确保返回的对象在 JNI 方法结束前有效
        jobject returnValue = env->NewLocalRef(value);

        if (returnValue == nullptr) {
            // 如果无法创建 LocalRef，返回错误信息或进行重试
            return nullptr;
        }

        if (isDeleteAfterGet == JNI_TRUE) {
            // 删除全局引用并移除数据
            env->DeleteGlobalRef(value);
            dataStoreList.erase(it->second);
            dataStoreMap.erase(it);
        }

        // 返回 Local 引用
        return returnValue;
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
