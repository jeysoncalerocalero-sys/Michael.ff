package com.miapp.freefirevip

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class ScreenCaptureService : Service() {

    private var proyeccion: MediaProjection? = null
    private var pantalla: VirtualDisplay? = null
    private var lector: ImageReader? = null
    private var hilo: Handler? = null

    private var capAncho = 480
    private var capAlto = 1066
    private var factor = 2.25f

    private var ultimoFrame = 0L
    private var ultimoDisparo = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val code = intent?.getIntExtra("code", 0) ?: 0
        @Suppress("DEPRECATION")
        val data = intent?.getParcelableExtra<Intent>("data")

        if (data != null) {
            hilo = Handler(Looper.getMainLooper())
            medirPantalla()
            iniciar(code, data)
            notificacion()
            crearPantalla()
        }
        return START_STICKY
    }

    private fun medirPantalla() {
        val dm = DisplayMetrics()
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(dm)
        Estado.ancho = dm.widthPixels
        Estado.alto = dm.heightPixels
        capAncho = 480
        capAlto = (dm.heightPixels * 480f / dm.widthPixels).toInt()
        factor = dm.widthPixels / 480f
    }

    private fun iniciar(code: Int, data: Intent) {
        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        proyeccion = mgr.getMediaProjection(code, data)
        proyeccion?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                detener()
            }
        }, hilo)
    }

    private fun notificacion() {
        val canal = NotificationChannel("ffvip", "Michael FF", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(canal)

        val n = NotificationCompat.Builder(this, "ffvip")
            .setContentTitle("Michael FF activo")
            .setContentText("Escaneando la pantalla")
            .setSmallIcon(R.drawable.ic_noti)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(1, n)
        }
    }

    private fun crearPantalla() {
        lector = ImageReader.newInstance(capAncho, capAlto, PixelFormat.RGBA_8888, 2)
        pantalla = proyeccion?.createVirtualDisplay(
            "MichaelFF",
            capAncho,
            capAlto,
            resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            lector?.surface,
            null,
            hilo
        )
        lector?.setOnImageAvailableListener({ r -> procesar(r) }, hilo)
    }

    private fun procesar(reader: ImageReader) {
        val img = reader.acquireLatestImage() ?: return
        try {
            val ahora = System.currentTimeMillis()
            if (ahora - ultimoFrame < 60) return
            ultimoFrame = ahora

            val plano = img.planes[0]
            val ps = plano.pixelStride
            val rs = plano.rowStride
            val anchoReal = rs / ps

            val bmp = Bitmap.createBitmap(anchoReal, capAlto, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(plano.buffer)

            val cx = capAncho / 2
            val cy = capAlto / 2
            val obj = Detector.buscar(bmp, cx, cy)

            if (obj != null) {
                val rx = (obj.x * factor).toInt()
                val ry = (obj.y * factor).toInt()
                Estado.objetivoX = rx
                Estado.objetivoY = ry
                Estado.detectando = true
                if (Estado.aimbot) apuntar(rx, ry)
            } else {
                Estado.detectando = false
            }

            if (Estado.autoFire && Estado.detectando && ahora - ultimoDisparo > Estado.pausaDisparo) {
                ultimoDisparo = ahora
                disparar()
            }
        } catch (e: Exception) {
        } finally {
            img.close()
        }
    }

    private fun apuntar(objetivoX: Int, objetivoY: Int) {
        var dx = (objetivoX - Estado.ancho / 2f) * Estado.sensibilidad
        var dy = (objetivoY - Estado.alto / 2f) * Estado.sensibilidad

        if (Estado.antiRecoil) dy += Estado.offsetRecoil

        if (abs(dx) < 5f && abs(dy) < 5f) return

        val ax = (Estado.ancho * 0.62f).toInt()
        val ay = (Estado.alto * 0.45f).toInt()
        TouchInjector.swipe(ax, ay, ax + dx.toInt(), ay + dy.toInt(), 40)
    }

    private fun disparar() {
        val bx = (Estado.ancho * Estado.botonFuegoX).toInt()
        val by = (Estado.alto * Estado.botonFuegoY).toInt()
        TouchInjector.tap(bx, by)
    }

    private fun detener() {
        try {
            pantalla?.release()
            lector?.close()
            proyeccion?.stop()
        } catch (e: Exception) {
        }
        pantalla = null
        lector = null
        proyeccion = null
    }

    override fun onDestroy() {
        super.onDestroy()
        detener()
    }
}
