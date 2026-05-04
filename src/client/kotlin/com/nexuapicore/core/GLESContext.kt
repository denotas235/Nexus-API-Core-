package com.nexuapicore.core

import org.lwjgl.opengles.GLES
import org.lwjgl.opengles.GLES20
import org.lwjgl.system.FunctionProvider
import org.lwjgl.system.Library
import org.lwjgl.system.MemoryStack
import java.lang.reflect.Field
import java.nio.ByteBuffer

object GLESContext {
    private var initialized = false

    fun init() {
        if (initialized) return
        try {
            val osName = System.getProperty("os.name", "").lowercase()
            if (!osName.contains("windows") && !osName.contains("mac")) {
                println("[GLESContext] Ambiente Mobile detectado")
                setupAndroidGLES()
            } else {
                println("[GLESContext] Ambiente PC detectado")
                try {
                    GLES.create()
                    GLES.createCapabilities()
                } catch (t: Throwable) {
                    System.err.println("[GLESContext] Falha ao criar GLES: ${t.message}")
                }
            }
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER)
            println("[GLESContext] Renderer Ativo: $renderer")
            initialized = true
        } catch (t: Throwable) {
            System.err.println("[GLESContext] Erro crítico: ${t.message}")
        }
    }

    private fun setupAndroidGLES() {
        try {
            resetGLESInternalState()
            System.setProperty("org.lwjgl.opengles.libname", "libGLESv2.so")
            System.setProperty("org.lwjgl.egl.libname", "libEGL.so")

            val provider = object : FunctionProvider {
                private val glesLib = Library.loadNative(GLES::class.java, "org.lwjgl.opengles", "libGLESv2.so")
                override fun getFunctionAddress(name: CharSequence): Long {
                    MemoryStack.stackPush().use { stack -> return glesLib.getFunctionAddress(stack.ASCII(name)) }
                }
                override fun getFunctionAddress(name: ByteBuffer): Long {
                    return glesLib.getFunctionAddress(name)
                }
            }
            GLES.create(provider)
            GLES.createCapabilities()
        } catch (t: Throwable) {
            throw RuntimeException("[GLESContext] Falha crítica ao injetar GLES", t)
        }
    }

    private fun resetGLESInternalState() {
        try {
            val f = GLES::class.java.getDeclaredField("functionProvider")
            f.isAccessible = true
            f.set(null, null)
            val c = GLES::class.java.getDeclaredField("caps")
            c.isAccessible = true
            c.set(null, null)
        } catch (_: Exception) {}
    }
}
