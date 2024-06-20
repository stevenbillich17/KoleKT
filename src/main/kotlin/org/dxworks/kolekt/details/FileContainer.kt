package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.FileDTO
import org.dxworks.kolekt.serialization.KoleSerializer
import org.slf4j.LoggerFactory
import java.io.File

class FileContainer(private var fileDTO: FileDTO? = null) {
    private var fileName = fileDTO?.fileName
    private var filePackage = fileDTO?.filePackage
    private var fileSavedName = "$filePackage.$fileName"
    private val logger = LoggerFactory.getLogger("FileContainer@$fileSavedName")

    fun getFileDTO(pathOnDisk: String?): FileDTO {
        if (fileDTO == null) {
            loadFromDisk(pathOnDisk)
        }
        return fileDTO!!
    }

    private fun loadFromDisk(pathOnDisk: String?): FileDTO {
        if (pathOnDisk == null) {
            logger.warn("Path on disk is null")
            return FileDTO("", "UNKNOWN")
        }
        val file = File("$pathOnDisk\\${fileSavedName}.json")
        val fileDTOString = file.readText()
        fileDTO = KoleSerializer.deserialize(fileDTOString)
        if (fileDTO == null) {
            throw IllegalStateException("FileDTO could not be deserialized from file $fileSavedName")
        }
        logger.debug("Loaded file $fileSavedName from disk")
        return fileDTO!!
    }

    fun setFileSavedName(fileSavedName: String) {
        this.fileSavedName = fileSavedName
        val parts = fileSavedName.split(".")
        this.filePackage = parts.dropLast(2).joinToString(".")
        this.fileName = parts.takeLast(2).joinToString(".")
        logger.trace("Set fileSavedName - $fileSavedName & filePackage - $filePackage & fileName - $fileName")
    }


    fun storeFileOnDisk(pathOnDisk: String) {
        if (fileDTO == null) {
            logger.warn("FileDTO is null")
            return
        }
        val file = File("$pathOnDisk\\${fileSavedName}.json")
        file.writeText(KoleSerializer.serialize(fileDTO!!))
        fileDTO = null
        logger.debug("Stored file $fileSavedName on disk")
    }

    fun hasSamePackage(packageName: String): Boolean {
        return filePackage == packageName
    }

}