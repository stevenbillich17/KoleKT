package runnables

import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.details.DictionariesController
import kotlin.test.Test
import kotlin.test.assertEquals

class TestFieldDeclaration {

    @Test
    fun testFieldDeclaration() {
        val projectExtractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage\\fieldtypes")
        projectExtractor.simpleParse()
        projectExtractor.bindAllClasses()
        testFieldTypes()
    }

    private fun testFieldTypes() {
        val megaType = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.fieldtypes.MegaType", false)

        val megaField = megaType.classFields.find { it.name == "megaField" }
        assertEquals( DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.fieldtypes.OmegaType", false), megaField?.getClassDTO())
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.OmegaType", megaField?.type)


        /* todo: fix this because we do not treat the constructors
        val megaField2 = megaType.classFields.find { it.name == "megaField2" }
        assertEquals(DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.fieldtypes.OmegaType", false), megaField2?.getClassDTO())
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.OmegaType", megaField2?.type)
         */

        val megaHiddenOmega = megaType.classFields.find { it.name == "megaHiddenOmega" }
        assertEquals(DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega", false), megaHiddenOmega?.getClassDTO())
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega", megaHiddenOmega?.type)
    }
}