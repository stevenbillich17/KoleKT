package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.dtos.ClassDTO

class LongParameterListDetector : CodeSmellDetector {
    override fun detect(clazz: ClassDTO): String? {
        val methods = clazz.classMethods
        val longParameterListMethods = methods.filter { it.methodParameters.size > 5 }
        return if (longParameterListMethods.isNotEmpty()) {
            return "Class ${clazz.className} has methods with long parameter lists: ${longParameterListMethods.joinToString { it.methodName }}"
        } else null
    }
}