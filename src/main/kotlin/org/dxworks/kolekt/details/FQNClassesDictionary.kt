package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.ClassDTO

class FQNClassesDictionary {
    private val fileSavedNameToClassesFqn = mutableMapOf<String, MutableSet<String>>()
    private val classesSavedToFile = mutableMapOf<String, String>()
    private val dict = mutableMapOf<String, ClassDTO?>()

    fun addClassDTO(classDTO: ClassDTO, fileSavedName: String) {
        val classesFqn = fileSavedNameToClassesFqn.getOrPut(fileSavedName) { mutableSetOf() }
        classesFqn.add(classDTO.getFQN())
        dict[classDTO.getFQN()] = classDTO
        classesSavedToFile[classDTO.getFQN()] = fileSavedName
    }

    fun findClassDTO(classFQN: String): ClassDTO? {
        return dict[classFQN]
    }

    fun findClassFile(classFQN: String): String? {
        return classesSavedToFile[classFQN]
    }

//    fun findClassesInsidePackage(packageName: String): List<ClassDTO> {
//        return dict.filter { it.key.startsWith(packageName) }.values.toList()
//    }

    fun removeClassesForFile(fileSavedName: String) {
        val classesFqn = fileSavedNameToClassesFqn[fileSavedName] ?: throw IllegalArgumentException("File not found")
        classesFqn.forEach { dict[it] = null }
    }

}