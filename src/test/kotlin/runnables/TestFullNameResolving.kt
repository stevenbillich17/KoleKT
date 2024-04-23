package runnables

import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import kotlin.test.Test

class TestFullNameResolving {
    @Test
    fun testFieldDeclaration() {
        val projectExtractor =
            ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
        projectExtractor.simpleParse()
        projectExtractor.bindAllClasses()
        testFullNameResolving()
    }

    private fun testFullNameResolving() {
        // create test for TestClass
        testFieldForTestClass()
        testFieldsForMegaType()
        testFieldsResolvedNamesForMalwareWriter()
    }

    private fun testFieldsResolvedNamesForMalwareWriter() {
        val malwareWriter: ClassDTO = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.malware.MalwareWriter")
            ?: throw Exception("Class not found")
        assert(malwareWriter.getFQN() == "org.dxworks.kolekt.testpackage.malware.MalwareWriter")
        assert(findField(malwareWriter, "s").type == "String")
    }

    private fun testFieldsForMegaType() {
        val megaType: ClassDTO = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.fieldtypes.MegaType")
            ?: throw Exception("Class not found")
        assert(megaType.getFQN() == "org.dxworks.kolekt.testpackage.fieldtypes.MegaType")
        assert(findField(megaType, "megaField").type == "org.dxworks.kolekt.testpackage.fieldtypes.OmegaType")
        assert(findField(megaType, "megaField2").type == "org.dxworks.kolekt.testpackage.fieldtypes.OmegaType")
        assert(findField(megaType, "megaHiddenOmega").type == "org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega")
    }

    private fun testFieldForTestClass() {
        val testClass: ClassDTO = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.TestClass")
            ?: throw Exception("Class not found")
        assert(testClass.getFQN() == "org.dxworks.kolekt.testpackage.TestClass")
        assert(findField(testClass, "age").type == "Int")
        assert(findField(testClass, "name").type == "String")
        assert(findField(testClass, "address").type == "String")
        assert(findField(testClass, "phoneNumber").type == "String")
        assert(findField(testClass, "height").type == "Double")
        assert(findField(testClass, "mwWriter").type == "org.dxworks.kolekt.testpackage.malware.MalwareWriter")
        assert(findField(testClass, "counter").type == "Int")
        assert(findField(testClass, "mwWriterStringAccessed").type == "String")
        assert(findField(testClass, "amazingMalware").type == "org.dxworks.kolekt.testpackage.malware.AmazingMalware")
        assert(findField(testClass, "weight").type == "Double")
        assert(findField(testClass, "weight2").type == "String")
        assert(findField(testClass, "weight3").type == "String")
        assert(findField(testClass, "weight4").type == "String")
    }

    private fun findField(testClass: ClassDTO, fieldName: String): AttributeDTO {
        return testClass.classFields.find { it.name == fieldName } ?: throw Exception("Field not found")
    }
}