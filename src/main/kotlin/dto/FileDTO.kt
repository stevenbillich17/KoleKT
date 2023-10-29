package dto

data class FileDTO(val filePath: String) {
    internal var filePackage: String? = null
}