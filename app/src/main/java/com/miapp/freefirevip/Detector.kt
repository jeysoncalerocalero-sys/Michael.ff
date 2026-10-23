package com.miapp.freefirevip

import android.graphics.Bitmap

object Detector {

    data class Objetivo(val x: Int, val y: Int)

    fun buscar(bitmap: Bitmap, centroX: Int, centroY: Int): Objetivo? {
        val w = bitmap.width
        val h = bitmap.height
        val px = IntArray(w * h)
        bitmap.getPixels(px, 0, w, 0, 0, w, h)

        val paso = 3
        var mejorDist = Int.MAX_VALUE
        var sx = -1
        var sy = -1

        var y = 0
        while (y < h) {
            val fila = y * w
            var x = 0
            while (x < w) {
                if (esRojo(px[fila + x])) {
                    val dx = x - centroX
                    val dy = y - centroY
                    val d = dx * dx + dy * dy
                    if (d < mejorDist) {
                        mejorDist = d
                        sx = x
                        sy = y
                    }
                }
                x += paso
            }
            y += paso
        }

        if (sx < 0) return null

        val radio = (w / 5).coerceAtLeast(50)
        var sumaX = 0L
        var sumaY = 0L
        var cuenta = 0

        var yy = (sy - radio).coerceAtLeast(0)
        val yyFin = (sy + radio).coerceAtMost(h - 1)
        while (yy <= yyFin) {
            val fila = yy * w
            var xx = (sx - radio).coerceAtLeast(0)
            val xxFin = (sx + radio).coerceAtMost(w - 1)
            while (xx <= xxFin) {
                if (esRojo(px[fila + xx])) {
                    sumaX += xx
                    sumaY += yy
                    cuenta++
                }
                xx += 2
            }
            yy += 2
        }

        if (cuenta < 10) return null

        val barraX = (sumaX / cuenta).toInt()
        val barraY = (sumaY / cuenta).toInt()
        val cabezaY = barraY + (Estado.desfaseCabeza * h / 1080)

        return Objetivo(barraX, cabezaY)
    }

    private fun esRojo(p: Int): Boolean {
        val r = (p shr 16) and 0xFF
        val g = (p shr 8) and 0xFF
        val b = p and 0xFF
        return r > 135 && (r - g) > 50 && (r - b) > 40 && g < 125
    }
}
