package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.dtos.ClassDTO

interface CodeSmellDetector {
    fun detect(clazz: ClassDTO) : String?
}