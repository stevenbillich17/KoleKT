package org.dxworks.kolekt.calculators.relations
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.FileDTO
import org.dxworks.kolekt.dtos.MethodCallDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExternalCallsCalculator {
    val logger: Logger = LoggerFactory.getLogger(ExternalCallsCalculator::class.java)

    fun computeExternalCalls(sourceFileDTO: FileDTO, targetFileDTO: FileDTO): Int {
        logger.debug("Computing external calls")
        var numberOfCalledMethods = 0
        for (classDTO in sourceFileDTO.classes) {
            numberOfCalledMethods += countNumberOfCallsFromAttributes(classDTO.classFields, targetFileDTO.getFileSavedName())
            numberOfCalledMethods += countNumberOfCallsFromFunctions(classDTO.classMethods, targetFileDTO.getFileSavedName())
        }
        numberOfCalledMethods += countNumberOfCallsFromFunctions(sourceFileDTO.functions, targetFileDTO.getFileSavedName())
        logger.info("Number of calls from ${sourceFileDTO.getFileSavedName()} to ${targetFileDTO.getFileSavedName()}: $numberOfCalledMethods")
        return numberOfCalledMethods
    }

    private fun countNumberOfCallsFromFunctions(classMethods: MutableList<MethodDTO>, fileSavedName: String): Int {
        var numberOfCalls = 0
        for (methodDTO in classMethods) {
            numberOfCalls += countNumberOfMethodCallsToFile(methodDTO.methodCalls, fileSavedName)
        }
        return numberOfCalls
    }

    private fun countNumberOfMethodCallsToFile(methodCalls: MutableList<MethodCallDTO>, fileSavedName: String): Int {
        var numberOfCalls = 0
        for (methodCall in methodCalls) {
            if (checkTheMethodCall(methodCall, fileSavedName)) {
                numberOfCalls++
            }
        }
        return numberOfCalls
    }

    private fun countNumberOfCallsFromAttributes(classFields: MutableList<AttributeDTO>, targetFileSavedName: String): Int {
        var numberOfCalls = 0
        for (attributeDTO in classFields) {
            if (!attributeDTO.isSetByMethodCall) {
                continue
            }
            val methodCall = attributeDTO.methodCallDTO ?: throw IllegalArgumentException("Method call not found")
            if (checkTheMethodCall(methodCall, targetFileSavedName)) {
                numberOfCalls++
            }
        }
        return numberOfCalls
    }

    private fun checkTheMethodCall(methodCall: MethodCallDTO, targetFileSavedName: String): Boolean {
        val matchFile =  methodCall.getFileThatIsCalled() == targetFileSavedName
        return matchFile // todo: should also check if the method is accessor
    }

}
