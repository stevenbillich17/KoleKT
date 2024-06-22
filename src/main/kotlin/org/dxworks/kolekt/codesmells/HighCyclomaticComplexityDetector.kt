package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.dtos.ClassDTO

class HighCyclomaticComplexityDetector : CodeSmellDetector {
    override fun detect(clazz: ClassDTO): String? {
        val methods = clazz.classMethods
        val highCyclomaticComplexityMethods = methods.filter { it.getCyclomaticComplexity() > 10 }
        return if (highCyclomaticComplexityMethods.isNotEmpty()) {
            return "Class ${clazz.getFQN()} has methods with high cyclomatic complexity: ${highCyclomaticComplexityMethods.joinToString { it.methodName }}"
        } else null
    }

}