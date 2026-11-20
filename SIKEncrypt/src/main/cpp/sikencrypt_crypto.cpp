#include <jni.h>

#include <openssl/bio.h>
#include <openssl/crypto.h>
#include <openssl/evp.h>
#include <openssl/pem.h>
#include <openssl/rsa.h>
#include <openssl/x509.h>

#include <algorithm>
#include <cstdint>
#include <cstring>
#include <memory>
#include <mutex>
#include <string>
#include <unordered_map>
#include <vector>

namespace {

using Bytes = std::vector<unsigned char>;

struct Session {
    EVP_CIPHER_CTX* cipher = nullptr;
    EVP_PKEY_CTX* pkey = nullptr;
    EVP_PKEY* pkeyKey = nullptr;
    bool rsa = false;
    bool encrypt = false;
    bool finalized = false;
    bool closed = false;
    std::string mode;
    std::string padding;
    std::string algorithm;
    Bytes rsaInput;
    std::mutex mutex;
};

std::mutex g_keysMutex;
std::unordered_map<std::string, Bytes> g_keys;
std::mutex g_sessionsMutex;
std::unordered_map<jlong, std::shared_ptr<Session>> g_sessions;
jlong g_nextHandle = 1;

std::string ToString(JNIEnv* env, jstring value) {
    if (value == nullptr) {
        return {};
    }
    const char* chars = env->GetStringUTFChars(value, nullptr);
    if (chars == nullptr) {
        return {};
    }
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}

Bytes ToBytes(JNIEnv* env, jbyteArray array) {
    if (array == nullptr) {
        return {};
    }
    const jsize length = env->GetArrayLength(array);
    Bytes result(static_cast<size_t>(length));
    if (length > 0) {
        env->GetByteArrayRegion(
            array,
            0,
            length,
            reinterpret_cast<jbyte*>(result.data())
        );
    }
    return result;
}

jbyteArray ToJByteArray(JNIEnv* env, const Bytes& data) {
    jbyteArray result = env->NewByteArray(static_cast<jsize>(data.size()));
    if (result != nullptr && !data.empty()) {
        env->SetByteArrayRegion(
            result,
            0,
            static_cast<jsize>(data.size()),
            reinterpret_cast<const jbyte*>(data.data())
        );
    }
    return result;
}

const EVP_CIPHER* SymmetricCipher(
    const std::string& algorithm,
    const std::string& mode,
    size_t keySize
) {
    if (algorithm == "AES") {
        if (keySize == 16) {
            if (mode == "ECB") return EVP_aes_128_ecb();
            if (mode == "CBC") return EVP_aes_128_cbc();
            if (mode == "CTR") return EVP_aes_128_ctr();
            if (mode == "GCM") return EVP_aes_128_gcm();
        } else if (keySize == 24) {
            if (mode == "ECB") return EVP_aes_192_ecb();
            if (mode == "CBC") return EVP_aes_192_cbc();
            if (mode == "CTR") return EVP_aes_192_ctr();
            if (mode == "GCM") return EVP_aes_192_gcm();
        } else if (keySize == 32) {
            if (mode == "ECB") return EVP_aes_256_ecb();
            if (mode == "CBC") return EVP_aes_256_cbc();
            if (mode == "CTR") return EVP_aes_256_ctr();
            if (mode == "GCM") return EVP_aes_256_gcm();
        }
    }

    if (algorithm == "DES") {
        if (mode == "ECB") return EVP_des_ecb();
        if (mode == "CBC") return EVP_des_cbc();
    }

    if (algorithm == "DESede") {
        if (keySize == 16) {
            if (mode == "ECB") return EVP_des_ede_ecb();
            if (mode == "CBC") return EVP_des_ede_cbc();
        } else if (keySize == 24) {
            if (mode == "ECB") return EVP_des_ede3_ecb();
            if (mode == "CBC") return EVP_des_ede3_cbc();
        }
    }

    if (algorithm == "SM4") {
        if (mode == "ECB") return EVP_sm4_ecb();
        if (mode == "CBC") return EVP_sm4_cbc();
        if (mode == "CTR") return EVP_sm4_ctr();
    }

    return nullptr;
}

bool IsRsa(const std::string& algorithm) {
    return algorithm == "RSA";
}

EVP_PKEY* LoadPkey(const Bytes& key, bool encrypt) {
    if (key.empty()) {
        return nullptr;
    }
    if (encrypt) {
        const unsigned char* ptr = key.data();
        return d2i_PUBKEY(nullptr, &ptr, static_cast<long>(key.size()));
    }

    const unsigned char* ptr = key.data();
    EVP_PKEY* privateKey = d2i_AutoPrivateKey(
        nullptr,
        &ptr,
        static_cast<long>(key.size())
    );
    if (privateKey != nullptr) {
        return privateKey;
    }

    ptr = key.data();
    PKCS8_PRIV_KEY_INFO* pkcs8 = d2i_PKCS8_PRIV_KEY_INFO(
        nullptr,
        &ptr,
        static_cast<long>(key.size())
    );
    if (pkcs8 == nullptr) {
        return nullptr;
    }
    privateKey = EVP_PKCS82PKEY(pkcs8);
    PKCS8_PRIV_KEY_INFO_free(pkcs8);
    return privateKey;
}

bool ConfigureRsa(
    EVP_PKEY_CTX* context,
    const std::string& padding,
    bool encrypt
) {
    if (context == nullptr) {
        return false;
    }
    if ((encrypt ? EVP_PKEY_encrypt_init(context) : EVP_PKEY_decrypt_init(context)) <= 0) {
        return false;
    }
    if (padding == "NoPadding") {
        return EVP_PKEY_CTX_set_rsa_padding(context, RSA_NO_PADDING) > 0;
    }
    if (padding == "OAEPWithSHA-256AndMGF1Padding") {
        if (EVP_PKEY_CTX_set_rsa_padding(context, RSA_PKCS1_OAEP_PADDING) <= 0) {
            return false;
        }
        if (EVP_PKEY_CTX_set_rsa_oaep_md(context, EVP_sha256()) <= 0) {
            return false;
        }
        return EVP_PKEY_CTX_set_rsa_mgf1_md(context, EVP_sha256()) > 0;
    }
    return EVP_PKEY_CTX_set_rsa_padding(context, RSA_PKCS1_PADDING) > 0;
}

bool InitSession(
    Session* session,
    const Bytes& key,
    const std::string& algorithm,
    const std::string& mode,
    const std::string& padding,
    Bytes iv,
    bool ivFromKey,
    bool encrypt
) {
    session->algorithm = algorithm;
    session->mode = mode;
    session->padding = padding;
    session->encrypt = encrypt;

    if (IsRsa(algorithm)) {
        session->rsa = true;
        session->pkeyKey = LoadPkey(key, encrypt);
        if (session->pkeyKey == nullptr) {
            return false;
        }
        session->pkey = EVP_PKEY_CTX_new(session->pkeyKey, nullptr);
        return ConfigureRsa(session->pkey, padding, encrypt);
    }

    const EVP_CIPHER* cipher = SymmetricCipher(algorithm, mode, key.size());
    if (cipher == nullptr) {
        return false;
    }

    if (ivFromKey && iv.empty() && mode != "ECB") {
        const int ivLength = EVP_CIPHER_iv_length(cipher);
        if (ivLength <= 0 || key.size() < static_cast<size_t>(ivLength)) {
            return false;
        }
        iv.assign(key.begin(), key.begin() + ivLength);
    }

    session->cipher = EVP_CIPHER_CTX_new();
    if (session->cipher == nullptr) {
        return false;
    }

    const bool noPadding = padding == "NoPadding";
    if (mode == "GCM") {
        if (iv.empty()) {
            return false;
        }
        if (EVP_CipherInit_ex(session->cipher, cipher, nullptr, nullptr, nullptr, encrypt ? 1 : 0) <= 0) {
            return false;
        }
        if (EVP_CIPHER_CTX_ctrl(session->cipher, EVP_CTRL_GCM_SET_IVLEN, static_cast<int>(iv.size()), nullptr) <= 0) {
            return false;
        }
        if (EVP_CipherInit_ex(session->cipher, nullptr, nullptr, key.data(), iv.data(), encrypt ? 1 : 0) <= 0) {
            return false;
        }
    } else {
        const unsigned char* ivData = iv.empty() ? nullptr : iv.data();
        if (EVP_CipherInit_ex(session->cipher, cipher, nullptr, key.data(), ivData, encrypt ? 1 : 0) <= 0) {
            return false;
        }
    }
    EVP_CIPHER_CTX_set_padding(session->cipher, noPadding ? 0 : 1);
    return true;
}

bool CipherUpdate(Session* session, const Bytes& input, Bytes* output) {
    output->clear();
    if (session->closed || session->finalized) {
        return false;
    }
    if (session->rsa) {
        session->rsaInput.insert(session->rsaInput.end(), input.begin(), input.end());
        return true;
    }
    if (session->mode == "GCM" && !session->encrypt) {
        session->rsaInput.insert(session->rsaInput.end(), input.begin(), input.end());
        return true;
    }
    Bytes buffer(
        input.size() +
        static_cast<size_t>(EVP_CIPHER_CTX_block_size(session->cipher)) +
        16
    );
    int outLength = 0;
    if (EVP_CipherUpdate(
            session->cipher,
            buffer.data(),
            &outLength,
            input.empty() ? nullptr : input.data(),
            static_cast<int>(input.size())
        ) <= 0) {
        return false;
    }
    buffer.resize(static_cast<size_t>(outLength));
    *output = std::move(buffer);
    return true;
}

bool CipherFinal(Session* session, Bytes* result) {
    result->clear();
    if (session->closed) {
        return false;
    }
    if (session->finalized) {
        return true;
    }

    if (session->rsa) {
        size_t outLength = 0;
        const bool ok = session->encrypt
            ? EVP_PKEY_encrypt(
                session->pkey,
                nullptr,
                &outLength,
                session->rsaInput.data(),
                session->rsaInput.size()
            ) > 0
            : EVP_PKEY_decrypt(
                session->pkey,
                nullptr,
                &outLength,
                session->rsaInput.data(),
                session->rsaInput.size()
            ) > 0;
        if (!ok) {
            return false;
        }
        Bytes output(outLength);
        const int cipherResult = session->encrypt
            ? EVP_PKEY_encrypt(
                session->pkey,
                output.data(),
                &outLength,
                session->rsaInput.data(),
                session->rsaInput.size()
            )
            : EVP_PKEY_decrypt(
                session->pkey,
                output.data(),
                &outLength,
                session->rsaInput.data(),
                session->rsaInput.size()
            );
        if (cipherResult <= 0) {
            return false;
        }
        output.resize(outLength);
        *result = std::move(output);
        session->finalized = true;
        return true;
    }

    Bytes output(static_cast<size_t>(EVP_CIPHER_CTX_block_size(session->cipher)) + 16);
    int outLength = 0;
    bool ok;
    if (session->mode == "GCM" && !session->encrypt) {
        if (session->rsaInput.size() < 16) {
            return false;
        }
        const size_t tagLength = 16;
        const size_t bodyLength = session->rsaInput.size() - tagLength;
        output.resize(bodyLength + static_cast<size_t>(EVP_CIPHER_CTX_block_size(session->cipher)) + tagLength);
        if (EVP_CipherUpdate(
                session->cipher,
                output.data(),
                &outLength,
                session->rsaInput.data(),
                static_cast<int>(bodyLength)
            ) <= 0) {
            return false;
        }
        if (EVP_CIPHER_CTX_ctrl(
                session->cipher,
                EVP_CTRL_GCM_SET_TAG,
                static_cast<int>(tagLength),
                session->rsaInput.data() + bodyLength
            ) <= 0) {
            return false;
        }
        int finalLength = 0;
        ok = EVP_DecryptFinal_ex(
            session->cipher,
            output.data() + outLength,
            &finalLength
        ) > 0;
        if (ok) {
            outLength += finalLength;
        }
    } else if (session->mode == "GCM" && session->encrypt) {
        ok = EVP_EncryptFinal_ex(session->cipher, output.data(), &outLength) > 0;
        if (ok) {
            output.resize(static_cast<size_t>(outLength) + 16);
            int tagLength = 0;
            ok = EVP_CIPHER_CTX_ctrl(
                session->cipher,
                EVP_CTRL_GCM_GET_TAG,
                16,
                output.data() + outLength
            ) > 0;
            if (ok) {
                tagLength = 16;
                outLength += tagLength;
            }
        }
    } else {
        ok = EVP_CipherFinal_ex(session->cipher, output.data(), &outLength) > 0;
    }
    if (!ok) {
        return false;
    }
    output.resize(static_cast<size_t>(outLength));
    *result = std::move(output);
    session->finalized = true;
    return true;
}

void FreeSession(Session* session) {
    if (session->closed) {
        return;
    }
    if (session->cipher != nullptr) {
        EVP_CIPHER_CTX_free(session->cipher);
        session->cipher = nullptr;
    }
    if (session->pkey != nullptr) {
        EVP_PKEY_CTX_free(session->pkey);
        session->pkey = nullptr;
    }
    if (session->pkeyKey != nullptr) {
        EVP_PKEY_free(session->pkeyKey);
        session->pkeyKey = nullptr;
    }
    if (!session->rsaInput.empty()) {
        OPENSSL_cleanse(session->rsaInput.data(), session->rsaInput.size());
        session->rsaInput.clear();
    }
    session->closed = true;
}

void ThrowIllegalStateException(JNIEnv* env, const char* message) {
    jclass exceptionClass = env->FindClass("java/lang/IllegalStateException");
    if (exceptionClass != nullptr) {
        env->ThrowNew(exceptionClass, message);
    }
}

}  // namespace

extern "C" JNIEXPORT void JNICALL
Java_com_sik_sikencrypt_NativeCipher_registerKeyForTestNative(
    JNIEnv* env,
    jclass,
    jstring keyId,
    jbyteArray key
) {
    const std::string id = ToString(env, keyId);
    if (id.empty()) {
        return;
    }
    Bytes bytes = ToBytes(env, key);
    std::lock_guard<std::mutex> lock(g_keysMutex);
    auto& stored = g_keys[id];
    if (!stored.empty()) {
        OPENSSL_cleanse(stored.data(), stored.size());
    }
    stored = std::move(bytes);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_sik_sikencrypt_NativeCipher_openSession(
    JNIEnv* env,
    jclass,
    jstring keyId,
    jstring algorithm,
    jstring mode,
    jstring padding,
    jbyteArray iv,
    jboolean ivFromKey,
    jboolean encrypt
) {
    const std::string id = ToString(env, keyId);
    Bytes key;
    {
        std::lock_guard<std::mutex> lock(g_keysMutex);
        const auto found = g_keys.find(id);
        if (found == g_keys.end()) {
            return 0;
        }
        key = found->second;
    }

    auto session = std::make_shared<Session>();
    const bool ok = InitSession(
        session.get(),
        key,
        ToString(env, algorithm),
        ToString(env, mode),
        ToString(env, padding),
        ToBytes(env, iv),
        ivFromKey == JNI_TRUE,
        encrypt == JNI_TRUE
    );
    if (!key.empty()) {
        OPENSSL_cleanse(key.data(), key.size());
    }
    if (!ok) {
        FreeSession(session.get());
        return 0;
    }

    std::lock_guard<std::mutex> lock(g_sessionsMutex);
    const jlong handle = g_nextHandle++;
    g_sessions[handle] = std::move(session);
    return handle;
}

extern "C" __attribute__((visibility("default"))) int
sikencrypt_register_key(
    const char* keyId,
    const unsigned char* key,
    size_t keySize
) {
    if (keyId == nullptr || *keyId == '\0' || key == nullptr || keySize == 0) {
        return 0;
    }
    std::lock_guard<std::mutex> lock(g_keysMutex);
    auto& stored = g_keys[std::string(keyId)];
    if (!stored.empty()) {
        OPENSSL_cleanse(stored.data(), stored.size());
    }
    stored.assign(key, key + keySize);
    return 1;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_NativeCipher_update(
    JNIEnv* env,
    jclass,
    jlong handle,
    jbyteArray data
) {
    std::shared_ptr<Session> session;
    {
        std::lock_guard<std::mutex> lock(g_sessionsMutex);
        const auto found = g_sessions.find(handle);
        if (found == g_sessions.end()) {
            ThrowIllegalStateException(env, "Native cipher session not found");
            return nullptr;
        }
        session = found->second;
    }
    const Bytes input = ToBytes(env, data);
    Bytes output;
    {
        std::lock_guard<std::mutex> lock(session->mutex);
        if (session->closed) {
            ThrowIllegalStateException(env, "Native cipher session already closed");
            return nullptr;
        }
        if (!CipherUpdate(session.get(), input, &output)) {
            ThrowIllegalStateException(env, "Native cipher update failed");
            return nullptr;
        }
    }
    return ToJByteArray(env, output);
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_sik_sikencrypt_NativeCipher_doFinal(
    JNIEnv* env,
    jclass,
    jlong handle
) {
    std::shared_ptr<Session> session;
    {
        std::lock_guard<std::mutex> lock(g_sessionsMutex);
        const auto found = g_sessions.find(handle);
        if (found == g_sessions.end()) {
            ThrowIllegalStateException(env, "Native cipher session not found");
            return nullptr;
        }
        session = found->second;
    }
    Bytes output;
    {
        std::lock_guard<std::mutex> lock(session->mutex);
        if (session->closed) {
            ThrowIllegalStateException(env, "Native cipher session already closed");
            return nullptr;
        }
        if (!CipherFinal(session.get(), &output)) {
            ThrowIllegalStateException(env, "Native cipher final failed");
            return nullptr;
        }
    }
    return ToJByteArray(env, output);
}

extern "C" JNIEXPORT void JNICALL
Java_com_sik_sikencrypt_NativeCipher_close(
    JNIEnv*,
    jclass,
    jlong handle
) {
    std::shared_ptr<Session> session;
    {
        std::lock_guard<std::mutex> lock(g_sessionsMutex);
        const auto found = g_sessions.find(handle);
        if (found == g_sessions.end()) {
            return;
        }
        session = found->second;
        g_sessions.erase(found);
    }
    std::lock_guard<std::mutex> lock(session->mutex);
    FreeSession(session.get());
}
