package com.miapp.freefirevip

object Estado {

    @Volatile var aimbot = false
    @Volatile var autoFire = false
    @Volatile var antiRecoil = false

    @Volatile var detectando = false
    @Volatile var objetivoX = 0
    @Volatile var objetivoY = 0

    @Volatile var ancho = 1080
    @Volatile var alto = 2400

    var sensibilidad = 1.0f
    var desfaseCabeza = 70
    var offsetRecoil = 14
    var pausaDisparo = 190L

    // Posicion del boton de disparo (0 a 1 respecto a la pantalla)
    var botonFuegoX = 0.86f
    var botonFuegoY = 0.80f
}
