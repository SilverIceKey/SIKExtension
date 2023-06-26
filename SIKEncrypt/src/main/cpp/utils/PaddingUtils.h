//
// Created by zhouw on 2023/6/26.
//

#ifndef SKEXTENSIONSAMPLE_PADDINGUTILS_H
#define SKEXTENSIONSAMPLE_PADDINGUTILS_H

#include <vector>

std::vector<uint8_t> PKCS5Padding(std::vector<uint8_t>& data, size_t block_size);
std::vector<uint8_t> PKCS5Unpadding(std::vector<uint8_t>& data);


#endif //SKEXTENSIONSAMPLE_PADDINGUTILS_H
