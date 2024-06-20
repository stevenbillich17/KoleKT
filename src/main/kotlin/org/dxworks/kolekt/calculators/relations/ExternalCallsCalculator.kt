package org.dxworks.kolekt.calculators.relations
import org.dxworks.kolekt.calculators.utils.CommonFunctions
import org.dxworks.kolekt.dtos.*
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
        logger.trace("Checking method call: ${methodCall.methodName} to $targetFileSavedName")
        val matchFile =  methodCall.getFileThatIsCalled() == targetFileSavedName
        return matchFile && !isAccessor(CommonFunctions.getCalledClass(methodCall), methodCall.methodName)
    }

    private fun isAccessor(calledClass: ClassDTO?, methodName: String): Boolean {
        if (calledClass == null) {
            return false
        }
        for (method in calledClass.classMethods) {
            if (method.methodName == methodName) {
                val startSpecific = methodName.startsWith("get") || methodName.startsWith("set")
                val ccIsOne = method.getCyclomaticComplexity() == 1
                val noParameters = method.methodParameters.isEmpty()
                val noMethodCalls = method.methodCalls.isEmpty()
                val noLocalVariables = method.methodLocalVariables.isEmpty()
                val noAnnotations = method.methodAnnotations.isEmpty()
                return startSpecific && ccIsOne && noParameters && noMethodCalls && noLocalVariables && noAnnotations
            }
        }
        return false
    }

}
