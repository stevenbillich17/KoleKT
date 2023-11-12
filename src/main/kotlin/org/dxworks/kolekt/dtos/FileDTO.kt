package org.dxworks.kolekt.dtos

data class FileDTO(val filePath: String,val fileName: String) {
    fun addClasses(classesDTOs: List<ClassDTO>) {
        classes.addAll(classesDTOs)
    }

    internal var filePackage: String? = null
    internal val classes: MutableList<ClassDTO> = mutableListOf()

    override fun toString(): String {
        return "FileDTO(\n" +
                " filePath='$filePath',\n" +
                " fileName='$fileName',\n" +
                " filePackage='$filePackage',\n" +
                " classes=$classes\n)"
    }
}