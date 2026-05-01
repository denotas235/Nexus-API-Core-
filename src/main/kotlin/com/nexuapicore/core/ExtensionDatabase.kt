package com.nexuapicore.core

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ExtensionDef(
    val name: String,
    val group: String,
    val functions: List<String>,
    val capabilities: List<String>
)

object ExtensionDatabase {
    private var _extensions: List<ExtensionDef>? = null

    val extensions: List<ExtensionDef>
        get() {
            if (_extensions == null) {
                val json = ExtensionDatabase::class.java.classLoader
                    .getResourceAsStream("extensions.json")?.bufferedReader()?.readText()
                    ?: throw RuntimeException("extensions.json not found")
                val type = object : TypeToken<Map<String, List<ExtensionDef>>>() {}.type
                val map: Map<String, List<ExtensionDef>> = Gson().fromJson(json, type)
                _extensions = map["extensions"] ?: emptyList()
            }
            return _extensions!!
        }

    fun getCapabilitiesFor(extName: String): List<String> {
        return extensions.find { it.name == extName }?.capabilities ?: emptyList()
    }

    fun getAllExtensions(): List<ExtensionDef> = extensions
}
