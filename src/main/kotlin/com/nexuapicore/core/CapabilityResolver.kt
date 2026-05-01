package com.nexuapicore.core

class CapabilityResolver(private val availableExtensions: List<String>) {
    fun resolve(): Map<String, Boolean> {
        val capMap = mutableMapOf<String, Boolean>()
        for (ext in availableExtensions) {
            val caps = ExtensionDatabase.getCapabilitiesFor(ext)
            for (c in caps) {
                capMap[c] = true
            }
        }
        ExtensionDatabase.extensions.flatMap { it.capabilities }.distinct().forEach {
            if (!capMap.containsKey(it)) capMap[it] = false
        }
        return capMap
    }
}
