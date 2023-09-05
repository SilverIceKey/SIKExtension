//
// Created by zhouw on 2023/6/26.
//

#include "PaddingUtils.h"

std::vector<uint8_t> PKCS5Padding(std::vector<uint8_t>& data, size_t block_size) {
    size_t length = block_size - (data.size() % block_size);
    data.insert(data.end(), length, (uint8_t)length);
    return data;
}

std::vector<uint8_t> PKCS5Unpadding(std::vector<uint8_t>& data) {
    if (data.empty()) {
        // Handle error: data is empty
        return data;
    }

    size_t length = data.back();
    if (length == 0 || length > data.size()) {
        // Handle error: invalid padding length
        return data;
    }

    // Check that all padding bytes have the correct value
    for (size_t i = 1; i <= length; ++i) {
        if (data[data.size() - i] != length) {
            // Handle error: invalid padding bytes
            return data;
        }
    }

    data.erase(data.end() - length, data.end());
    return data;
}
