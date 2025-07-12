package com.example.file

import java.util.*

data class FileUpload (
    val id: UUID,
    val fileName: String,
    val fileBytes: ByteArray,
    val tags: Set<String>,
    var status: FileProcessingStatus = FileProcessingStatus.NOT_PROCESSED
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileUpload

        if (id != other.id) return false
        if (fileName != other.fileName) return false
        if (!fileBytes.contentEquals(other.fileBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + fileBytes.contentHashCode()
        return result
    }
}

enum class FileProcessingStatus {
    NOT_PROCESSED, PROCESSING, PROCESSED
}