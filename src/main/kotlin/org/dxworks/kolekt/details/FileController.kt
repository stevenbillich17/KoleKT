package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.FileDTO

object FileController {
    private val files = mutableMapOf<String, FileDTO>()
    private val classesDictionary = FQNClassesDictionary()

    fun addFileDTO(fileDTO: FileDTO) {
        files[fileDTO.filePackage + fileDTO.fileName] = fileDTO
        fileDTO.classes.forEach { classesDictionary.addClassDTO(it) }
    }

    fun getFilesWithSamePackageName(packageName: String): Map<String, FileDTO> {
        return files.filter { it.value.filePackage == packageName }
    }

    fun findClassInFiles(classFQN: String): ClassDTO? {
        return classesDictionary.findClassDTO(classFQN)
    }
}
