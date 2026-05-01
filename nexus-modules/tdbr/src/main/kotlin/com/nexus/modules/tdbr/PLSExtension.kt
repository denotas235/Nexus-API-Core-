package com.nexus.modules.tdbr

/**
 * Stub de compilação para GL_EXT_shader_pixel_local_storage.
 * Em runtime no Android, as chamadas reais são feitas via JNI/nativo.
 */
object PLSExtension {
    fun isAvailable(): Boolean = false
    fun glFramebufferPixelLocalStorageSize(size: Int, width: Int, height: Int) {}
}
