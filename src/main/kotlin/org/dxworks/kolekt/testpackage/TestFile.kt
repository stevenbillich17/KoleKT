package org.dxworks.kolekt.testpackage

import org.dxworks.kolekt.testpackage.malware.MalwareWriter
import org.dxworks.kolekt.testpackage.malware.writeMalwareOutside

class TestClass(var age: Int, var name: String) {
    var address = "Default Address"
    var phoneNumber: String? = null
    var height: Double = 5.0
    var weight = getWeightValue()

    private fun getWeightValue(): Double {
        return 5.0
    }

    constructor(address: String, nothing: Int, name: String, anotherInt: Int ) : this(nothing, name) {
        this.address = address
    }

    @Deprecated("Use the other constructor")
    fun callOutsideFunction(message: String) {
        outsideFunction()
    }

    fun fun2(m1: String, m2: Double? = 5.0) {
        val x = "Hello"

        val xTurbat = 5
        var y: String? = null
        val z = MalwareWriter()
        z.writeMalware()
        z.writeMalwareWithParameters("ceva", "altceva")
        writeMalwareOutside("ceva" + "wow", "altceva")
    }
}

fun outsideFunction() {

}