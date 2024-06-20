package runnables.perclass

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.analyze.KoleClazzAnalyzer
import org.dxworks.kolekt.details.FileController
import kotlin.test.Test
import kotlin.test.assertEquals

class TestInheritance {

    @Test
    fun testWithoutCache() {
//        val projectExtractor =
//            ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
//        projectExtractor.simpleParse()
//        projectExtractor.bindAllClasses()
//        runTests()
    }

    @Test
    fun testUsingCache() {
//        val projectExtractor =
//            ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
//        projectExtractor.parseAndSaveToDisk("E:\\AA.Faculta\\LICENTA\\A.KoleKT-Generated-for-test\\test-1")
//        projectExtractor.bindFromDisk(10, "E:\\AA.Faculta\\LICENTA\\A.KoleKT-Generated-for-test\\test-1")
//        runTests()
    }

    fun runTests() {
        testDITandHITforSonClass()
        testDITandHITforParentClass()
        testDITandHITforGrandparentClass()
        testDITandHITforSonBrotherClass()
        testDITandHITforSonOfSonBrotherClass()
    }

    private fun testDITandHITforSonOfSonBrotherClass() {
        // find the son of son brother class and check if it has the correct parent
        val sonOfSonBrotherClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.SonOfSonBrother")
        val sonBrotherClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.SonBrother")
        assert(sonOfSonBrotherClass?.superClass == sonBrotherClass!!.getFQN())

        val jsonObj: JsonObject = KoleClazzAnalyzer.analyze("org.dxworks.kolekt.testpackage.inheritance.SonOfSonBrother")
        testIntFromJson(jsonObj, "HIT", 0)
        testIntFromJson(jsonObj, "DIT", 3)
    }

    private fun testDITandHITforSonBrotherClass() {
        // find the son brother class and check if it has the correct parent
        val sonBrotherClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.SonBrother")
        val parentClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.Parent")
        assert(sonBrotherClass?.superClass == parentClass!!.getFQN())

        val jsonObj: JsonObject = KoleClazzAnalyzer.analyze("org.dxworks.kolekt.testpackage.inheritance.SonBrother")
        testIntFromJson(jsonObj, "HIT", 1)
        testIntFromJson(jsonObj, "DIT", 2)
    }

    private fun testDITandHITforGrandparentClass() {
        // find the grandparent class and check if it has no parent
        val grandparentClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.Grandparent")
        assert(grandparentClass?.superClass == "")

        val jsonObj: JsonObject = KoleClazzAnalyzer.analyze("org.dxworks.kolekt.testpackage.inheritance.Grandparent")
        testIntFromJson(jsonObj, "HIT", 3)
        testIntFromJson(jsonObj, "DIT", 0)
    }

    private fun testDITandHITforParentClass() {
        // find the parent class and check if it has no parent
        val parentClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.Parent")
        val grandparentClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.Grandparent")
        assert(parentClass?.superClass == grandparentClass!!.getFQN())

        val jsonObj: JsonObject = KoleClazzAnalyzer.analyze("org.dxworks.kolekt.testpackage.inheritance.Parent")
        testIntFromJson(jsonObj, "HIT", 2)
        testIntFromJson(jsonObj, "DIT", 1)
    }

    private fun testDITandHITforSonClass() {
        // find the son class and check if it has the correct parent
        val sonClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.Son")
        val parentClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.inheritance.Parent")
        assert(sonClass?.superClass == parentClass!!.getFQN())

        val jsonObj: JsonObject = KoleClazzAnalyzer.analyze("org.dxworks.kolekt.testpackage.inheritance.Son")
        testIntFromJson(jsonObj, "HIT", 0)
        testIntFromJson(jsonObj, "DIT", 2)
    }

    private fun testIntFromJson(jsonObj: JsonObject, metricName: String, metricValue: Int) {
        val metricJsonValue = jsonObj[metricName]?.jsonPrimitive?.int ?: throw Exception("$metricName not found")
        assertEquals(metricValue, metricJsonValue)
    }

    private fun testDoubleFromJson(jsonObj: JsonObject, metricName: String, metricValue: Double) {
        val metricJsonValue = jsonObj[metricName]?.jsonPrimitive?.double ?: throw Exception("$metricName not found")
        assertEquals(metricValue, metricJsonValue)
    }

}