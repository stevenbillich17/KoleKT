package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.dtos.ClassDTO

class LazyMethodDetector : CodeSmellDetector {
    override fun detect(clazz: ClassDTO): String? {
        val methods = clazz.classMethods
        val lazyMethods = methods.filter { it.getCyclomaticComplexity() < 2 }
        return if (lazyMethods.isNotEmpty()) {
            return "Class ${clazz.getFQN()} has methods with low cyclomatic complexity: ${lazyMethods.joinToString { it.methodName }}"
        } else null
    }
}