package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.dtos.ClassDTO

class ShotgunSurgeryDetector : CodeSmellDetector {
    override fun detect(clazz: ClassDTO): String? {
        val methods = clazz.classMethods
        val shotgunMethods = mutableListOf<String>()
        for (method in methods) {
            val callingMethods = method.getMethodsThatCallThisMethod()
            val outsideClassMethods = callingMethods.filter { !it.startsWith(clazz.getFQN()) }
            val setOfOtherClasses = mutableSetOf<String>()
            setOfOtherClasses.addAll(outsideClassMethods)
            if (setOfOtherClasses.size > 5) {
                shotgunMethods.add(method.methodName)
            }
        }
        return if (shotgunMethods.isNotEmpty()) {
            "Class ${clazz.getFQN()} has methods with shotgun surgery: ${shotgunMethods.joinToString()}"
        } else
        return null
    }

}