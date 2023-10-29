package DTO

data class FileDTO(val filePath: String,val fileName: String) {
    internal var filePackage: String? = null
}