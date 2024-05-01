package runnables

import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.MethodDTO

object Helper {

    fun findField(testClass: ClassDTO, fieldName: String): AttributeDTO {
        return testClass.classFields.find { it.name == fieldName } ?: throw Exception("Field not found")
    }

    fun findParameter(methodDTO: MethodDTO, parameterName: String): AttributeDTO {
        return methodDTO.methodParameters.find { it.name == parameterName } ?: throw Exception("Parameter not found")
    }

    fun findMethodLocalVariable(methodDTO: MethodDTO, variableName: String): AttributeDTO {
        return methodDTO.methodLocalVariables.find { it.name == variableName }
            ?: throw Exception("Local variable not found")
    }
}