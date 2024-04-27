package runnables.perclass

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.analyze.KoleClazzAnalyzer
import org.dxworks.kolekt.details.FileController
import runnables.Helper
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMegaType {

    @Test
    fun testWithoutCache() {
        val projectExtractor =
            ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
        projectExtractor.simpleParse()
        projectExtractor.bindAllClasses()
        runTests()
    }

    @Test
    fun testUsingCache() {
        val projectExtractor =
            ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
        projectExtractor.parseAndSaveToDisk("E:\\AA.Faculta\\LICENTA\\A.KoleKT-Generated-for-test\\test-1")
        projectExtractor.bindFromDisk(10, "E:\\AA.Faculta\\LICENTA\\A.KoleKT-Generated-for-test\\test-1")
        runTests()
    }

    private fun runTests() {
        testFileNameAndPackage()
        testTypeAndImports()
        testFieldTypes()
        testMetrics()
    }

    private fun testFileNameAndPackage() {
        val megaType = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.fieldtypes.MegaType")
            ?: throw Exception("Class not found")
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes", megaType.classPackage)
    }

    private fun testMetrics() {
        val jsonObj: JsonObject = KoleClazzAnalyzer.analyze("org.dxworks.kolekt.testpackage.fieldtypes.MegaType")
        println("jsonObj: $jsonObj")
        testIntFromJson(jsonObj, "NOM", 0)
        testIntFromJson(jsonObj, "WMC", 0)
        testIntFromJson(jsonObj, "NOPA", 3)
        testIntFromJson(jsonObj, "CC", 0)
        testIntFromJson(jsonObj, "CM", 0)
        //testIntFromJson(jsonObj, "CINT", 2) // todo: possible problem with the value
        testIntFromJson(jsonObj, "HIT", 0)
        testIntFromJson(jsonObj, "DIT", 0)
        testIntFromJson(jsonObj, "NOC", 0)
        testDoubleFromJson(jsonObj, "AMW", 0.0)
        testDoubleFromJson(jsonObj, "WOC", 1.0)
        testDoubleFromJson(jsonObj, "NProtM", 0.0)
        testDoubleFromJson(jsonObj, "BOvR", 0.0)
        // testDoubleFromJson(jsonObj, "CDISP", 1.0) // todo same problem as CINT
    }

    private fun testIntFromJson(jsonObj: JsonObject, metricName: String, metricValue: Int) {
        val metricJsonValue = jsonObj[metricName]?.jsonPrimitive?.int ?: throw Exception("$metricName not found")
        assertEquals(metricValue, metricJsonValue)
    }

    private fun testDoubleFromJson(jsonObj: JsonObject, metricName: String, metricValue: Double) {
        val metricJsonValue = jsonObj[metricName]?.jsonPrimitive?.double ?: throw Exception("$metricName not found")
        assertEquals(metricValue, metricJsonValue)
    }

    private fun testFieldTypes() {
        val megaType = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.fieldtypes.MegaType")
            ?: throw Exception("Class not found")
        val field = Helper.findField(megaType, "megaField")
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.OmegaType", field.type)
        val megaField2 = Helper.findField(megaType, "megaField2")
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.OmegaType", megaField2.type)
        val megaHiddenField = Helper.findField(megaType, "megaHiddenOmega")
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega", megaHiddenField.type)
    }

    private fun testTypeAndImports() {
        val classDTO = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.fieldtypes.MegaType")
            ?: throw Exception("Class not found")
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.MegaType", classDTO.getFQN())
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega", classDTO.getImports().first())
    }
}