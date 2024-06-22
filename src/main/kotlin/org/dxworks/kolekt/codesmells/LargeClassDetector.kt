package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.dtos.ClassDTO

class LargeClassDetector : CodeSmellDetector {
    override fun detect(clazz: ClassDTO): String? {
        val methods = clazz.classMethods
        return if (methods.size > 10) {
            "Class ${clazz.className} has more than 10 methods, smells like a large class"
        } else null
    }

}