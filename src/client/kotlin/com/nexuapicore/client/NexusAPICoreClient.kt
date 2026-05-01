package com.nexuapicore.client

import com.nexuapicore.NexusAPI
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents

class NexusAPICoreClient : ClientModInitializer {
    override fun onInitializeClient() {
        // CLIENT_STARTED dispara depois do contexto GL estar activo
        // É o único momento seguro para chamar glGetString(GL_EXTENSIONS)
        ClientLifecycleEvents.CLIENT_STARTED.register {
            println("[Nexus] GL context ready — iniciando API")
            NexusAPI.init()
        }
    }
}
