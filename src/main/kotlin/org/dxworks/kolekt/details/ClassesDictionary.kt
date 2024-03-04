package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.ClassDTO

class ClassesDictionary {
    private val dict = mutableMapOf<String, ArrayList<ClassDTO>>()

    fun addClassDTO(classDTO: ClassDTO) {
        classDTO.className?.let {
            val classesWithSameName = dict.getOrPut(it) { arrayListOf() }
            classesWithSameName.add(classDTO)
        }
    }

    fun findClassDTO(className: String): ArrayList<ClassDTO>? {
        return dict[className]
    }
}