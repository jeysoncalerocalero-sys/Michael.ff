package com.miapp.freefirevip

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var menu: View
    private lateinit var capa: View
    private lateinit var anillo: View
    private lateinit var pMenu: WindowManager.LayoutParams
    private lateinit var pCapa: WindowManager.LayoutParams

    private val h = Handler(Looper.getMainLooper())
    private var tamAnillo = 100

    private val refresco = object : Runnable {
        override fun run() {
            pintar()
            h.postDelayed(this, 33)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        tamAnillo = (resources.displayMetrics.widthPixels * 0.09f).toInt().coerceAtLeast(60)

        crearCapa()
        crearMenu()

        h.post(refresco)
    }

    private fun crearCapa() {
        capa = LayoutInflater.from(this).inflate(R.layout.overlay_capa, null)
        anillo = capa.findViewById(R.id.anillo)

        val lp = anillo.layoutParams
        lp.width = tamAnillo
        lp.height = tamAnillo
        anillo.layoutParams = lp

        pCapa = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        pCapa.gravity = Gravity.TOP or Gravity.START
        pCapa.x = 0
        pCapa.y = 0

        wm.addView(capa, pCapa)
    }

    private fun crearMenu() {
        menu = LayoutInflater.from(this).inflate(R.layout.overlay_menu, null)

        pMenu = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        pMenu.gravity = Gravity.TOP or Gravity.START
        pMenu.x = 20
        pMenu.y = 180

        wm.addView(menu, pMenu)

        val barra = menu.findViewById<TextView>(R.id.tituloMenu)
        barra.setOnTouchListener(object : View.OnTouchListener {
            private var xi = 0
            private var yi = 0
            private var dx = 0f
            private var dy = 0f

            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        xi = pMenu.x
                        yi = pMenu.y
                        dx = e.rawX
                        dy = e.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        pMenu.x = xi + (e.rawX - dx).toInt()
                        pMenu.y = yi + (e.rawY - dy).toInt()
                        wm.updateViewLayout(menu, pMenu)
                        return true
                    }
                }
                return false
            }
        })

        menu.findViewById<Switch>(R.id.swAimbot).setOnCheckedChangeListener { _, on ->
            Estado.aimbot = on
        }
        menu.findViewById<Switch>(R.id.swAutoFire).setOnCheckedChangeListener { _, on ->
            Estado.autoFire = on
        }
        menu.findViewById<Switch>(R.id.swRecoil).setOnCheckedChangeListener { _, on ->
            Estado.antiRecoil = on
        }
        menu.findViewById<Switch>(R.id.swGfx).setOnCheckedChangeListener { _, on ->
            if (on) Ajustes.gfxActivar() else Ajustes.gfxDesactivar()
        }
        menu.findViewById<Switch>(R.id.swDns).setOnCheckedChangeListener { _, on ->
            if (on) Ajustes.dnsActivar() else Ajustes.dnsDesactivar()
        }

        val lblSens = menu.findViewById<TextView>(R.id.lblSens)
        menu.findViewById<SeekBar>(R.id.sbSens).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, usuario: Boolean) {
                    Estado.sensibilidad = valor / 100f
                    lblSens.text = "Sens: " + String.format("%.2f", Estado.sensibilidad)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

        val lblAltura = menu.findViewById<TextView>(R.id.lblAltura)
        menu.findViewById<SeekBar>(R.id.sbAltura).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, usuario: Boolean) {
                    Estado.desfaseCabeza = valor
                    lblAltura.text = "Altura cabeza: $valor"
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
    }

    private fun pintar() {
        if (Estado.detectando && Estado.aimbot) {
            anillo.visibility = View.VISIBLE
            anillo.x = Estado.objetivoX.toFloat() - tamAnillo / 2f
            anillo.y = Estado.objetivoY.toFloat() - tamAnillo / 2f
        } else {
            anillo.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        h.removeCallbacks(refresco)
        if (::capa.isInitialized) wm.removeView(capa)
        if (::menu.isInitialized) wm.removeView(menu)
    }
}
