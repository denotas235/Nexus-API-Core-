package com.nexus.modules.tdbr

import org.lwjgl.opengles.GLES20

object PLSManager {
    var enabled = false
        private set

    fun detect() {
        val exts = GLES20.glGetString(GLES20.GL_EXTENSIONS) ?: ""
        enabled = exts.contains("GL_EXT_shader_pixel_local_storage")
        println("[PLSManager] PLS available: $enabled")
    }
}
