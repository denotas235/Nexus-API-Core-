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
                val gson = Gson()
                val type = object : TypeToken<Map<String, List<ExtensionDef>>>() {}.type
                
                // Carregar base de dados principal
                val mainJson = ExtensionDatabase::class.java.classLoader
                    .getResourceAsStream("extensions.json")?.bufferedReader()?.readText()
                    ?: throw RuntimeException("extensions.json not found")
                val mainMap: Map<String, List<ExtensionDef>> = gson.fromJson(mainJson, type)
                val main = mainMap["extensions"] ?: emptyList()
                
                // Carregar extensões extra do sistema, se existir
                val extra = try {
                    val extraJson = ExtensionDatabase::class.java.classLoader
                        .getResourceAsStream("extensoes_extra.json")?.bufferedReader()?.readText()
                    if (extraJson != null) {
                        val extraMap: Map<String, List<ExtensionDef>> = gson.fromJson(extraJson, type)
                        extraMap["extensions"] ?: emptyList()
                    } else emptyList()
                } catch (e: Exception) {
                    println("[Nexus] Ficheiro de extensões extra não encontrado ou inválido: ${e.message}")
                    emptyList()
                }
                
                _extensions = main + extra
                println("[Nexus] ExtensionDatabase: ${main.size} principais + ${extra.size} extra = ${_extensions!!.size}")
            }
            return _extensions!!
        }

    fun getCapabilitiesFor(extName: String): List<String> {
        return extensions.find { it.name == extName }?.capabilities ?: emptyList()
    }

    fun getAllExtensions(): List<ExtensionDef> = extensions
}
