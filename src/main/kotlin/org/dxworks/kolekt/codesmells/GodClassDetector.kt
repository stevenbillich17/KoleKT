package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.dtos.ClassDTO

class GodClassDetector : CodeSmellDetector {
    override fun detect(clazz: ClassDTO): String? {
        val methods = clazz.classMethods
        val fields = clazz.classFields
        return if (methods.size > 20 && fields.size > 15) {
            "Class ${clazz.className} has more than 20 methods and more than 15 fields, smells like a god class"
        } else null
    }

}