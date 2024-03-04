package org.dxworks.kolekt.dtos

import org.slf4j.LoggerFactory

data class FileDTO(val filePath: String,val fileName: String) {
    internal var filePackage: String? = null
    internal val classes: MutableList<ClassDTO> = mutableListOf()
    internal val imports: MutableList<String> = mutableListOf()
    internal val functions: MutableList<MethodDTO> = mutableListOf()
    internal val importAliases: MutableMap<String, String> = mutableMapOf()
    private val logger = LoggerFactory.getLogger("FileDTO@$fileName")

    fun addClasses(classesDTOs: List<ClassDTO>) {
        classes.addAll(classesDTOs)
    }

    fun addImport(import: String) {
        this.imports.add(import)
    }

    fun addFunctions(functionsDTOs: List<MethodDTO>) {
        functions.addAll(functionsDTOs)
    }

    fun addImportAlias(alias: String, import: String) {
        importAliases[alias] = import
        logger.trace("Added import alias $alias for $import")
    }

    fun getImportFromAlias(alias: String): String? {
        return importAliases[alias]
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