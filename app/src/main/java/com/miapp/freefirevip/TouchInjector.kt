package com.miapp.freefirevip

import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedWriter
import java.io.OutputStreamWriter

object TouchInjector {

    private var proceso: ShizukuRemoteProcess? = null
    private var entrada: BufferedWriter? = null

    fun conectar(): Boolean {
        desconectar()
        if (!Shizuku.pingBinder()) return false
        return try {
            val p = Shizuku.newProcess(arrayOf("sh"), null, null)
            proceso = p
            entrada = BufferedWriter(OutputStreamWriter(p.outputStream))
            enviarComando("echo listo")
            true
        } catch (e: Exception) {
            proceso = null
            false
        }
    }

    fun tap(x: Int, y: Int) {
        enviarComando("input tap $x $y")
    }

    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, ms: Int) {
        enviarComando("input swipe $x1 $y1 $x2 $y2 $ms")
    }

    fun enviarComando(cmd: String) {
        try {
            entrada?.apply {
                write(cmd)
                write("\n")
                flush()
            }
        } catch (e: Exception) {
        }
    }

    fun desconectar() {
        try {
            proceso?.destroy()
        } catch (e: Exception) {
        }
        proceso = null
        entrada = null
    }
}
