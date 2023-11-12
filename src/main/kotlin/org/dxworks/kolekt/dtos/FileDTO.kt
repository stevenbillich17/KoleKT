package org.dxworks.kolekt.dtos

data class FileDTO(val filePath: String,val fileName: String) {
    internal var filePackage: String? = null
    internal val classes: MutableList<ClassDTO> = mutableListOf()
    internal val imports: MutableList<String> = mutableListOf()
    fun addClasses(classesDTOs: List<ClassDTO>) {
        classes.addAll(classesDTOs)
    }

    fun addImport(import: String) {
        this.imports.add(import)
    }

    override fun toString(): String {
        return "FileDTO(\n" +
                " filePath='$filePath',\n" +
                " fileName='$fileName',\n" +
                " filePackage='$filePackage',\n" +
                " imports=$imports, \n" +
                " classes=$classes\n)"
    }
}