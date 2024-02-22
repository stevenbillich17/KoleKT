package org.dxworks.kolekt.testpackage.detector

open class BaseDetector {
    //todo adauga detectie pentru open keywords
    open fun detectMalware() {
        println("Detecting malware")
    }

    fun detectMalwareWithParameters(x: String, y: String) {
        println(x + y)
    }

}