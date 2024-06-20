package org.dxworks.kolekt.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.FileDTO

object KoleSerializer {
    fun serialize(classDTO: ClassDTO): String {
        val json = Json.encodeToString(classDTO)
        return json
    }

    fun serialize(fileDTO: FileDTO): String {
        val json = Json.encodeToString(fileDTO)
        return json
    }

    inline fun <reified T> deserialize(json: String): T {
        return Json.decodeFromString(json)
    }
}