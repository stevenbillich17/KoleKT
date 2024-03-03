package org.dxworks.kolekt.dtos

data class FileDTO(val filePath: String,val fileName: String) {
    internal var filePackage: String? = null
    internal val classes: MutableList<ClassDTO> = mutableListOf()
    internal val imports: MutableList<String> = mutableListOf()
    internal val functions: MutableList<MethodDTO> = mutableListOf()
    fun addClasses(classesDTOs: List<ClassDTO>) {
        classes.addAll(classesDTOs)
    }

    fun addImport(import: String) {
        this.imports.add(import)
    }

    fun addFunctions(functionsDTOs: List<MethodDTO>) {
        functions.addAll(functionsDTOs)
    }

    /**
     * Returns the full import path for a given short name
     * or the short name if it's not found
     */
    fun getImport(shortName: String): String {
        for (import in imports) {
            if (import.endsWith(shortName)) {
                return import
            }
        }
        if (filePackage != null) {
            return "$filePackage.$shortName"
        }
        return shortName
    }

    override fun toString(): String {
        return "FileDTO(\n" +
                " filePath='$filePath',\n" +
                " fileName='$fileName',\n" +
                " filePackage='$filePackage',\n" +
                " imports=$imports, \n" +
                " classes=$classes,\n" +
                " functions=$functions\n" + ")"
    }
}