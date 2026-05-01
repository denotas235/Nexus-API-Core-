package com.nexuapicore.core

class FeatureRegistry(private val capabilities: Map<String, Boolean>) {
    fun isAvailable(cap: String): Boolean = capabilities[cap] == true
    fun getActiveCapabilities(): List<String> = capabilities.filter { it.value }.keys.toList()
}
