package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.ClassDTO

object DictionariesController {
    private val classesDictionary = ShortNameClassesDictionary()
    private val fqnClassesDictionary = FQNClassesDictionary()
    private val externalClassDTO = ClassDTO("com.dxworks.kolekt.ExternalClassDTO")
    private val notFoundYetDTO = ClassDTO("com.dxworks.kolekt.NotFoundYetDTO")

    fun addClassDTO(classDTO: ClassDTO) {
        classesDictionary.addClassDTO(classDTO)
        fqnClassesDictionary.addClassDTO(classDTO)
    }

    fun findClassesWithSimilarNames(className: String): ArrayList<ClassDTO>? {
        return classesDictionary.findClassDTO(className)
    }

    fun findClassAfterFQN(classFQN: String, shouldReturnExternal: Boolean): ClassDTO {
        return fqnClassesDictionary.findClassDTO(classFQN)
            ?: return if (shouldReturnExternal) externalClassDTO else notFoundYetDTO
    }

    fun getFQNClassesDictionary(): MutableMap<String, ClassDTO> {
        return fqnClassesDictionary.getDict()
    }

    fun EXTERNAL_CLASS(): ClassDTO {
        return externalClassDTO
    }
}