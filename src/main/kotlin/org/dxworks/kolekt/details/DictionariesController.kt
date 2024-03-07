package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.ClassDTO

object DictionariesController {
    private val classesDictionary = ShortNameClassesDictionary()
    private val fqnClassesDictionary = FQNClassesDictionary()

    fun addClassDTO(classDTO: ClassDTO) {
        classesDictionary.addClassDTO(classDTO)
        fqnClassesDictionary.addClassDTO(classDTO)
    }

    fun findClassesWithSimilarNames(className: String): ArrayList<ClassDTO>? {
        return classesDictionary.findClassDTO(className)
    }

    fun findClassAfterFQN(classFQN: String): ClassDTO? {
        return fqnClassesDictionary.findClassDTO(classFQN)
    }

    fun getFQNClassesDictionary(): MutableMap<String, ClassDTO> {
        return fqnClassesDictionary.getDict()
    }
}