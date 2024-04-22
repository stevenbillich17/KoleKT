package org.dxworks.kolekt.dtos

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MethodCallDTO(val methodName: String, val parameters: List<String>) {
    var referenceName : String? = null
    private var classThatIsCalled : String? = null
    private var fileThatIsCalled : String? = null

    @Transient
    private var fileThatIsCalledDTO : FileDTO? = null
    @Transient
    private var classThatIsCalledDTO : ClassDTO? = null
    @Transient
    private var methodThatIsCalledDTO : MethodDTO? = null


    fun addReference(referenceName: String) {
        this.referenceName = referenceName
    }

    fun setMethodThatIsCalled(methodThatIsCalled: MethodDTO?) {
        if (methodThatIsCalled != null) {
            this.methodThatIsCalledDTO = methodThatIsCalled
        }
    }

    fun setClassThatIsCalled(classThatIsCalled: ClassDTO?) {
        if (classThatIsCalled != null) {
            this.classThatIsCalled = classThatIsCalled.getFQN()
            this.classThatIsCalledDTO = classThatIsCalled
        }
    }

    fun setFileThatIsCalled(fileThatIsCalled: FileDTO?) {
        if (fileThatIsCalled != null) {
            this.fileThatIsCalled = fileThatIsCalled.filePath
            this.fileThatIsCalledDTO = fileThatIsCalled
        }
    }

    fun getFileThatIsCalledDTO(): FileDTO? {
        return fileThatIsCalledDTO
    }

    fun getClassThatIsCalledDTO(): ClassDTO? {
        return classThatIsCalledDTO
    }

    fun getMethodThatIsCalledDTO(): MethodDTO? {
        return methodThatIsCalledDTO
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