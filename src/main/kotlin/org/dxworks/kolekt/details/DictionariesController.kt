package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.ClassDTO

object DictionariesController {
    private val classesDictionary = ShortNameClassesDictionary()
    private val fqnClassesDictionary = FQNClassesDictionary()
    public val EXTERNAL_CLASS = ClassDTO("com.dxworks.kolekt.ExternalClassDTO")
    public val NOT_FOUND_YET_CLASS= ClassDTO("com.dxworks.kolekt.NotFoundYetDTO")
    public val BASIC_CLASS = ClassDTO("com.dxworks.kolekt.BasicClassDTO")

    fun addClassDTO(classDTO: ClassDTO) {
        classesDictionary.addClassDTO(classDTO)
        fqnClassesDictionary.addClassDTO(classDTO)
    }

    fun findClassesWithSimilarNames(className: String): ArrayList<ClassDTO>? {
        return classesDictionary.findClassDTO(className)
    }

    fun findClassAfterFQN(classFQN: String, shouldReturnExternal: Boolean): ClassDTO {
        return fqnClassesDictionary.findClassDTO(classFQN)
            ?: return if (shouldReturnExternal) EXTERNAL_CLASS else NOT_FOUND_YET_CLASS
    }

    fun getFQNClassesDictionary(): MutableMap<String, ClassDTO> {
        return fqnClassesDictionary.getDict()
    }

}