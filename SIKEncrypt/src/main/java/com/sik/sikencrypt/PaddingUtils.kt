package com.sik.sikencrypt

object PaddingUtils {

    /**
     * Applies PKCS#5 padding to the given data.
     *
     * @param data       The data to be padded.
     * @param blockSize  The block size, in bytes.
     * @return A new byte array containing the padded data.
     */
    fun applyPKCS5Padding(data: ByteArray, blockSize: Int): ByteArray {
        require(blockSize > 0) { "Block size must be positive" }

        val paddingLength = blockSize - (data.size % blockSize)
        val paddedData = ByteArray(data.size + paddingLength)

        // Copy the original data
        System.arraycopy(data, 0, paddedData, 0, data.size)

        // Append the padding bytes
        for (i in 0 until paddingLength) {
            paddedData[data.size + i] = paddingLength.toByte()
        }

        return paddedData
    }

    /**
     * Removes PKCS#5 padding from the given data.
     *
     * @param data The padded data.
     * @return A new byte array containing the original data.
     * @throws IllegalArgumentException If the padding is invalid.
     */
    fun removePKCS5Padding(data: ByteArray): ByteArray {
        val paddingLength = data.last().toInt()
        if (paddingLength <= 0 || paddingLength > data.size) {
            throw IllegalArgumentException("Invalid PKCS#5 padding length")
        }

        // Check that all padding bytes have the correct value
        for (i in 1..paddingLength) {
            if (data[data.size - i] != paddingLength.toByte()) {
                throw IllegalArgumentException("Invalid PKCS#5 padding bytes")
            }
        }

        return data.copyOfRange(0, data.size - paddingLength)
    }
}
