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
    val extensions: List<ExtensionDef> by lazy {
        val json = javaClass.classLoader.getResourceAsStream("extensions.json")?.bufferedReader()?.readText()
            ?: throw RuntimeException("extensions.json not found")
        Gson().fromJson(json, object : TypeToken<Map<String, List<ExtensionDef>>>() {}.type)["extensions"] ?: emptyList()
    }

    fun getCapabilitiesFor(extName: String): List<String> {
        return extensions.find { it.name == extName }?.capabilities ?: emptyList()
    }

    fun getAllExtensions(): List<ExtensionDef> = extensions
}
