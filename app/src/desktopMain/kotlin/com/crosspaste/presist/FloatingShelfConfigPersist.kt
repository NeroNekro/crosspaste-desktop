package com.crosspaste.presist

import com.crosspaste.app.AppFileType
import com.crosspaste.path.AppPathProvider
import com.crosspaste.ui.floating.FloatingShelfConfig
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class FloatingShelfConfigPersist(
    private val path: java.nio.file.Path,
) {
    private val json = Json { prettyPrint = true }

    companion object {
        fun create(appPathProvider: AppPathProvider): FloatingShelfConfigPersist {
            val path = appPathProvider.resolve("floatingShelfConfig.json", AppFileType.USER).toNioPath()
            return FloatingShelfConfigPersist(path)
        }
    }

    fun read(): FloatingShelfConfig? {
        return try {
            if (!Files.exists(path)) return null
            val jsonContent = Files.readString(path)
            json.decodeFromString<FloatingShelfConfig>(jsonContent)
        } catch (e: SerializationException) {
            println("Failed to deserialize FloatingShelfConfig: ${e.message}")
            null
        } catch (e: IOException) {
            println("Failed to read FloatingShelfConfig: ${e.message}")
            null
        }
    }

    fun save(config: FloatingShelfConfig) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.parent)
                Files.createFile(path)
            }
            val jsonContent = json.encodeToString(config)
            Files.writeString(path, jsonContent)
        } catch (e: IOException) {
            println("Failed to save FloatingShelfConfig: ${e.message}")
        }
    }
}
