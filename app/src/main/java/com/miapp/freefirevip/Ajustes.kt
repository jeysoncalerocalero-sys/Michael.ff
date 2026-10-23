package com.miapp.freefirevip

object Ajustes {

    fun gfxActivar() {
        TouchInjector.enviarComando("wm size 1280x720")
        TouchInjector.enviarComando("wm density 240")
    }

    fun gfxDesactivar() {
        TouchInjector.enviarComando("wm size reset")
        TouchInjector.enviarComando("wm density reset")
    }

    fun dnsActivar() {
        TouchInjector.enviarComando("settings put global private_dns_mode hostname")
        TouchInjector.enviarComando("settings put global private_dns_specifier dns.google")
    }

    fun dnsDesactivar() {
        TouchInjector.enviarComando("settings put global private_dns_mode off")
    }
}
