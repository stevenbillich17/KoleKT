package org.dxworks.kolekt.testpackage

import org.dxworks.kolekt.testpackage.malware.MalwareWriter
import org.dxworks.kolekt.testpackage.malware.writeMalwareOutside

class TestClass(var age: Int, var name: String) {
    var address = "Default Address"
    var phoneNumber: String? = null
    var height: Double = 5.0

    fun callOutsideFunction(message: String) {
        outsideFunction()
    }

    fun fun2(m1: String, m2: Double? = 5.0) {
        val x = "Hello"
        var y: String? = null
        val z = MalwareWriter()
        writeMalwareOutside("ceva" + "wow", "altceva")
    }
}

fun outsideFunction() {

}