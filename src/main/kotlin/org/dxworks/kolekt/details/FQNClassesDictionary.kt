package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.ClassDTO

class FQNClassesDictionary {
    private val dict = mutableMapOf<String, ClassDTO>()

    fun addClassDTO(classDTO: ClassDTO) {
        dict[classDTO.getFQN()] = classDTO
    }

    fun findClassDTO(classFQN: String): ClassDTO? {
        return dict[classFQN]
    }

}