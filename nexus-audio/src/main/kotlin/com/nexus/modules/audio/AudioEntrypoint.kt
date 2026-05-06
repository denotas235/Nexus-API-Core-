package com.nexus.modules.audio

import com.nexuapicore.NexusAPI
import net.fabricmc.api.ClientModInitializer

class AudioEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        NexusAPI.registerModule(AudioModule())
    }
}
