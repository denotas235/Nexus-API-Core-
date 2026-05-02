package com.nexus.modules.tdbr

import net.fabricmc.api.ClientModInitializer
import com.nexuapicore.NexusAPI
import com.nexuapicore.core.module.NexusModule

class TDBREntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        println("[TDBR] Registering module with Nexus API...")
        NexusAPI.registerModule(TDBRModule())
    }
}
