package com.nexuapicore.core

import com.nexuapicore.core.module.NexusModule

object ModuleLoader {
    fun discoverModules(): List<NexusModule> {
        val modules = mutableListOf<NexusModule>()
        try {
            // Carrega dinamicamente o módulo do MaliOpt, se presente no classpath
            val clazz = Class.forName("com.nexus.modules.maliopt.MaliOptNexusModule")
            val instance = clazz.getDeclaredConstructor().newInstance() as NexusModule
            modules.add(instance)
            println("[Nexus] ModuleLoader: MaliOptNexusModule carregado dinamicamente")
        } catch (e: Exception) {
            println("[Nexus] ModuleLoader: MaliOptNexusModule não encontrado (normal em dev)")
        }
        return modules
    }
}
