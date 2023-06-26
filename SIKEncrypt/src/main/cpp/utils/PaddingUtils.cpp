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
    size_t length = data.back();
    data.erase(data.end() - length, data.end());
    return data;
}