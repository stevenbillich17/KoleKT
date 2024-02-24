package org.dxworks.kolekt.testpackage.detector

open class BaseDetector {
    open val code = 123

    open fun detectMalware() {
        println("Detecting malware")
    }

    fun detectMalwareWithParameters(x: String, y: String) {
        println(x + y)
    }

}