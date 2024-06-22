package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.dtos.ClassDTO

class LazyClassDetector : CodeSmellDetector {
    override fun detect(clazz: ClassDTO): String? {
        val methods = clazz.classMethods
        val fields = clazz.classFields
        return if (methods.size < 3 && fields.size < 3) {
            "Class ${clazz.className} has less than 3 methods and less than 3 fields, smells like a lazy class"
        } else null
    }
}