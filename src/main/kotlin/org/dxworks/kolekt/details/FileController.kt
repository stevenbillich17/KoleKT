package org.dxworks.kolekt.details

import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.FileDTO
import org.slf4j.LoggerFactory
import java.io.File

object FileController {
    private var pathOnDisk: String? = null
    private val allFiles = mutableMapOf<String, FileContainer>()
    private var maximumNumberOfFiles = 100
    private var filesThatAreCached = ArrayDeque<String?>(100)
    private var classesDictionaryCache = FQNClassesDictionary()
    private val logger = LoggerFactory.getLogger(FileController::class.java)


    fun clean() {
        allFiles.clear()
        filesThatAreCached.clear()
        classesDictionaryCache = FQNClassesDictionary()
    }

    fun getNamesOfAllTheFiles(): List<String> {
        return allFiles.keys.toList()
    }

    fun setPathOnDisk(path: String?) {
        pathOnDisk = path
    }

    fun getFileNames(): List<String> {
        return allFiles.keys.toList()
    }

    fun storeAllFilesOnDisk() {
        removeOldestFilesFromCache(filesThatAreCached.size)
        logger.debug("Stored all files on disk")
    }

    fun loadFilesFromDisk() {
        // read the folder that is set as pathOnDisk and load all the files
        val folder = File(pathOnDisk)
        checkIfFolder(folder)
        val files = folder.listFiles()
        for (file in files) {
            if (file.isDirectory || file.extension != "json") {
                continue
            }
            val fileContainer = FileContainer()
            fileContainer.setFileSavedName(file.nameWithoutExtension)
            allFiles[file.nameWithoutExtension] = fileContainer
            addFileToCache(fileContainer)
        }
        logger.debug("Loaded all files from disk")
    }

    private fun checkIfFolder(folder: File) {
        if (!folder.exists()) {
            throw IllegalArgumentException("Folder does not exist")
        }
        if (!folder.isDirectory) {
            throw IllegalArgumentException("Path is not a folder")
        }
    }

    fun setMaximumNumberOfFiles(maximumNumberOfFiles: Int) {
        this.maximumNumberOfFiles = maximumNumberOfFiles
        if (filesThatAreCached.size > maximumNumberOfFiles) {
            // remove oldest files
            val numOfRemovedFiles = filesThatAreCached.size - maximumNumberOfFiles
            removeOldestFilesFromCache(numOfRemovedFiles)
            val newFilesCache = ArrayDeque<String?>(maximumNumberOfFiles)
            for (i in 0..<maximumNumberOfFiles) {
                newFilesCache.addLast(filesThatAreCached[i])
            }
            filesThatAreCached = newFilesCache
        } else {
            // add empty slots
            for (i in 0..maximumNumberOfFiles - filesThatAreCached.size) {
                filesThatAreCached.addLast(null)
            }
        }
    }

    private fun removeOldestFilesFromCache(n: Int) {
        for (i in 0..<n) {
            val fileToBeRemoved = filesThatAreCached.removeLast() ?: continue
            try {
                classesDictionaryCache.removeClassesForFile(fileToBeRemoved!!)
            } catch (e: IllegalArgumentException) {
                logger.warn("CLasses not found for file $fileToBeRemoved")
            }
            if (allFiles[fileToBeRemoved] == null) {
                throw IllegalArgumentException("File not found")
            }
            allFiles[fileToBeRemoved]!!.storeFileOnDisk(pathOnDisk!!)
        }
    }

    private fun addFileToCache(fileContainer: FileContainer): FileDTO {
        if (filesThatAreCached.size >= maximumNumberOfFiles) {
            removeOldestFilesFromCache(1)
        }
        val fileDTO = fileContainer.getFileDTO(pathOnDisk)
        for (classDTO in fileDTO.classes) {
            classesDictionaryCache.addClassDTO(classDTO, fileDTO.getFileSavedName())
        }
        filesThatAreCached.addFirst(fileDTO.getFileSavedName())
        return fileDTO
    }

    fun getFileFromCache(fileSavedName: String): FileDTO {
        // searched if the file is in the cache
        val index = filesThatAreCached.indexOf(fileSavedName)
        // if it is, move it to the front
        if (index != -1) {
            moveToFrontOfCache(fileSavedName)
            return allFiles[fileSavedName]!!.getFileDTO(pathOnDisk)
        }
        // if it is not, load it from disk and add it to the cache, we should have an FileContainer object for it already
        val fileContainer = allFiles[fileSavedName]!!
        return addFileToCache(fileContainer)
    }

    private fun moveToFrontOfCache(fileSavedName: String?) {
        if (fileSavedName == null) {
            throw IllegalArgumentException("fileSavedName cannot be null")
        }
        val index = filesThatAreCached.indexOf(fileSavedName)
        filesThatAreCached.removeAt(index)
        filesThatAreCached.addFirst(fileSavedName)
    }

    /**
     * Method intended to be used when a new file is added to the project at parsing time
     */
    fun addFileDTO(fileDTO: FileDTO) {
        val fileContainer = FileContainer(fileDTO)
        allFiles[fileDTO.getFileSavedName()] = fileContainer
        addFileToCache(fileContainer)
    }

    fun getFilesWithSamePackageName(packageName: String): Map<String, FileDTO> {
        // search the cache for the files with the same package name
        val cachedFilesWithSamePackage = getCachedFilesNamesThatMatchThePackage(packageName)

        // get files that match the package but are not cached
        val notCachedFilesWithSamePackage =
            allFiles.filter { it.value.hasSamePackage(packageName) && !cachedFilesWithSamePackage.contains(it.key) }

        cachedFilesWithSamePackage.forEach { moveToFrontOfCache(it) }

        // load the not cached files from disk and add them to the cache
        notCachedFilesWithSamePackage.forEach {
            addFileToCache(it.value)
        }

        // now we should return the union but make it clean without special kotlin functions
        val result = mutableMapOf<String, FileDTO>()
        cachedFilesWithSamePackage.forEach { result[it] = allFiles[it]!!.getFileDTO(pathOnDisk) }
        notCachedFilesWithSamePackage.forEach { result[it.key] = it.value.getFileDTO(pathOnDisk) }

        return result
    }

    private fun getCachedFilesNamesThatMatchThePackage(packageName: String): Set<String> {
        val cachedFilesWithSamePackage = mutableSetOf<String>()
        for (i in 0..<filesThatAreCached.size) {
            if (filesThatAreCached[i] == null) {
                continue
            }
            if (allFiles[filesThatAreCached[i]!!]!!.hasSamePackage(packageName)) {
                cachedFilesWithSamePackage.add(filesThatAreCached[i]!!)
            }
        }
        return cachedFilesWithSamePackage
    }

    fun findClassInFiles(classFQN: String): ClassDTO? {
        if (classFQN == "") {
            return null // todo: this if is needed due to the json deserialization
        }
        val classDTO = classesDictionaryCache.findClassDTO(classFQN)
        if (classDTO != null) {
            return classDTO
        } else {
            try {
                populateDictionaryWithClassesFromSaveFile(classFQN)
            } catch (e: IllegalArgumentException) {
                logger.warn("Class not found in files")
                return null
            }
            return classesDictionaryCache.findClassDTO(classFQN)
        }
    }

    private fun populateDictionaryWithClassesFromSaveFile(classFQN: String) {
        val fileSavedName =
            classesDictionaryCache.findClassFile(classFQN) ?: throw IllegalArgumentException("Class file not found")
        val fileDTO = getFileFromCache(fileSavedName)
        for (cls in fileDTO.classes) {
            classesDictionaryCache.addClassDTO(cls, fileDTO.getFileSavedName())
        }
    }

    fun getClass(classFQN: String?): ClassDTO? {
        if (classFQN == null) {
            return null
        }
        return findClassInFiles(classFQN)
    }

    fun getFile(fileSavedName: String?): FileDTO? {
        if (fileSavedName == null) {
            return null
        }
        return getFileFromCache(fileSavedName)
    }
}
