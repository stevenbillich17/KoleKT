package org.dxworks.kolekt.dtos

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MethodCallDTO(val methodName: String, val parameters: List<String>) {
    var referenceName : String? = null
    private var classThatIsCalled : String? = null
    private var fileThatIsCalled : String? = null

    fun addReference(referenceName: String) {
        this.referenceName = referenceName
    }

    fun setClassThatIsCalled(classThatIsCalled: ClassDTO?) {
        if (classThatIsCalled != null) {
            this.classThatIsCalled = classThatIsCalled.getFQN()
        }
    }

    fun setFileThatIsCalled(fileThatIsCalled: FileDTO?) {
        if (fileThatIsCalled != null) {
            this.fileThatIsCalled = fileThatIsCalled.getFileSavedName()
        }
    }


    fun getClassThatIsCalled(): String? {
        return classThatIsCalled
    }

    fun getFileThatIsCalled(): String? {
        return fileThatIsCalled
    }

    override fun toString(): String {
        return "MethodCallDTO(methodName='$methodName', parameters=$parameters, referenceName=$referenceName)"
    }
}