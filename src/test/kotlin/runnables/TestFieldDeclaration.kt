package runnables

import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.enums.CollectionType
import kotlin.test.Test
import kotlin.test.assertEquals

class TestFieldDeclaration {

    @Test
    fun testFieldDeclaration() {
        val projectExtractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage\\fieldtypes")
        projectExtractor.simpleParse()
        testFieldTypesExtraction()
        projectExtractor.bindAllClasses()
        testFieldTypes()
        testFieldTypesExtractionBinding()
    }

    private fun testFieldTypesExtraction() {
        // find different method of declaring
        val differentMethodOfDeclaring = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.fieldtypes.declarations.DifferentMethodOfDeclaring", false)
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.declarations.DifferentMethodOfDeclaring", differentMethodOfDeclaring.getFQN())

        // test constructor parameter to be String
        val constructorParameter = differentMethodOfDeclaring.classFields.find { it.name == "constructorParameter"}
        assertEquals("String?", constructorParameter?.type)

        // test nullableString to be String?
        val nullableString = differentMethodOfDeclaring.classFields.find { it.name == "nullableString"}
        assertEquals("String", nullableString?.type) // todo: make it nullable

        // test nonNullableString to be String
        val nonNullableString = differentMethodOfDeclaring.classFields.find { it.name == "nonNullableString"}
        assertEquals("String", nonNullableString?.type)

        // test implicitlyTypedString to be String
        val implicitlyTypedString = differentMethodOfDeclaring.classFields.find { it.name == "implicitlyTypedString"}
        assertEquals("String", implicitlyTypedString?.type)

        // test array to be Array<Int>
        val array = differentMethodOfDeclaring.classFields.find { it.name == "array"}
        assertEquals(true, array?.isCollectionType())
        assertEquals(CollectionType.ARRAY, array?.typeOfCollection)
        assertEquals("Int", array?.collectionType?.get(0))

        // test arrayOfHiddenOmega to be Array<HiddenOmega>
        val arrayOfHiddenOmega = differentMethodOfDeclaring.classFields.find { it.name == "arrayOfHiddenOmega"}
        assertEquals(true, arrayOfHiddenOmega?.isCollectionType())
        assertEquals(CollectionType.ARRAY, arrayOfHiddenOmega?.typeOfCollection)
        assertEquals("HiddenOmega", arrayOfHiddenOmega?.collectionType?.get(0)) // todo: should be fqn

        // test list to be List<Int>
        val list = differentMethodOfDeclaring.classFields.find { it.name == "list"}
        assertEquals(true, list?.isCollectionType())
        assertEquals(CollectionType.LIST, list?.typeOfCollection)
        assertEquals("Int", list?.collectionType?.get(0))

        // test set to be Set<Int>
        val set = differentMethodOfDeclaring.classFields.find { it.name == "set"}
        assertEquals(true, set?.isCollectionType())
        assertEquals(CollectionType.SET, set?.typeOfCollection)
        assertEquals("Int", set?.collectionType?.get(0))


        // test map to be Map<Int, String>
        val map = differentMethodOfDeclaring.classFields.find { it.name == "map"}
        assertEquals(true, map?.isCollectionType())
        assertEquals(CollectionType.MAP, map?.typeOfCollection)
        assertEquals("Int", map?.collectionType?.get(0))
        assertEquals("String", map?.collectionType?.get(1))

        // test mutableList to be MutableList<Int>
        val mutableList = differentMethodOfDeclaring.classFields.find { it.name == "mutableList"}
        assertEquals(true, mutableList?.isCollectionType())
        assertEquals(CollectionType.MUTABLE_LIST, mutableList?.typeOfCollection)
        assertEquals("Int", mutableList?.collectionType?.get(0))

        // test mutableSet to be MutableSet<Long>
        val mutableSet = differentMethodOfDeclaring.classFields.find { it.name == "mutableSet"}
        assertEquals(true, mutableSet?.isCollectionType())
        assertEquals(CollectionType.MUTABLE_SET, mutableSet?.typeOfCollection)
        assertEquals("Long", mutableSet?.collectionType?.get(0))

        // test mutableMap to be MutableMap<Int, String>
        val mutableMap = differentMethodOfDeclaring.classFields.find { it.name == "mutableMap"}
        assertEquals(true, mutableMap?.isCollectionType())
        assertEquals(CollectionType.MUTABLE_MAP, mutableMap?.typeOfCollection)
        assertEquals("Int", mutableMap?.collectionType?.get(0))

        // test collection to be Collection<String>
        val collection = differentMethodOfDeclaring.classFields.find { it.name == "collection"}
        assertEquals(true, collection?.isCollectionType())
        assertEquals(CollectionType.COLLECTION, collection?.typeOfCollection)
        assertEquals("String", collection?.collectionType?.get(0))

        // test mutableCollection to be MutableCollection<String>
        val mutableCollection = differentMethodOfDeclaring.classFields.find { it.name == "mutableCollection"}
        assertEquals(true, mutableCollection?.isCollectionType())
        assertEquals(CollectionType.MUTABLE_COLLECTION, mutableCollection?.typeOfCollection)
        assertEquals("String", mutableCollection?.collectionType?.get(0))

        // test iterable to be Iterable<String>
        val iterable = differentMethodOfDeclaring.classFields.find { it.name == "iterable"}
        assertEquals(true, iterable?.isCollectionType())
        assertEquals(CollectionType.ITERABLE, iterable?.typeOfCollection)
        assertEquals("String", iterable?.collectionType?.get(0))

        // test sequence to be Sequence<String>
        val sequence = differentMethodOfDeclaring.classFields.find { it.name == "sequence"}
        assertEquals(true, sequence?.isCollectionType())
        assertEquals(CollectionType.SEQUENCE, sequence?.typeOfCollection)
        assertEquals("String", sequence?.collectionType?.get(0))
    }

    private fun testFieldTypesExtractionBinding() {
        val differentMethodOfDeclaring = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.fieldtypes.declarations.DifferentMethodOfDeclaring", false)
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.declarations.DifferentMethodOfDeclaring", differentMethodOfDeclaring.getFQN())

        // test constructor parameters to be basic class dto
        val constructorParameter = differentMethodOfDeclaring.classFields.find { it.name == "constructorParameter"}
        assertEquals(DictionariesController.BASIC_CLASS, constructorParameter?.getClassDTO())

        // test nullableString
        val nullableString = differentMethodOfDeclaring.classFields.find { it.name == "nullableString"}
        assertEquals(DictionariesController.BASIC_CLASS, nullableString?.getClassDTO())

        // test nonNullableString
        val nonNullableString = differentMethodOfDeclaring.classFields.find { it.name == "nonNullableString"}
        assertEquals(DictionariesController.BASIC_CLASS, nonNullableString?.getClassDTO())

        // test implicitlyTypedString
        val implicitlyTypedString = differentMethodOfDeclaring.classFields.find { it.name == "implicitlyTypedString"}
        assertEquals(DictionariesController.BASIC_CLASS, implicitlyTypedString?.getClassDTO())
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