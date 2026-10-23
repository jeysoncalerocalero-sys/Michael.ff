package com.miapp.freefirevip

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var txtEstado: TextView
    private val CODIGO_SHIZUKU = 1001

    private val oyenteShizuku = Shizuku.OnRequestPermissionResultListener { _, grant ->
        if (grant == PackageManager.PERMISSION_GRANTED) {
            TouchInjector.conectar()
            Toast.makeText(this, "Shizuku conectado", Toast.LENGTH_SHORT).show()
        }
    }

    private val pedirCaptura = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == RESULT_OK && res.data != null) {
            val i = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra("code", res.resultCode)
                putExtra("data", res.data)
            }
            ContextCompat.startForegroundService(this, i)
            txtEstado.text = "TODO ACTIVO - abri Free Fire"
        } else {
            txtEstado.text = "Cancelaste la captura de pantalla"
        }
    }

    private val pedirNoti = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtEstado = findViewById(R.id.txtEstado)
        val boton = findViewById<Button>(R.id.btnActivar)

        Shizuku.addRequestPermissionResultListener(oyenteShizuku)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                pedirNoti.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        boton.setOnClickListener {
            if (!puedeDibujar()) {
                pedirOverlay()
                txtEstado.text = "Activa 'Aparecer encima' y toca otra vez"
                return@setOnClickListener
            }
            if (!shizukuListo()) {
                txtEstado.text = "Acepta el permiso de Shizuku y toca otra vez"
                return@setOnClickListener
            }
            if (!TouchInjector.conectar()) {
                txtEstado.text = "Shizuku no responde. Abrilo de nuevo."
                return@setOnClickListener
            }
            startService(Intent(this, OverlayService::class.java))
            val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            pedirCaptura.launch(mgr.createScreenCaptureIntent())
        }
    }

    private fun shizukuListo(): Boolean {
        if (!Shizuku.pingBinder()) {
            txtEstado.text = "Instala y activa Shizuku primero"
            return false
        }
        if (Shizuku.isPreV11()) return true
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) return true
        Shizuku.requestPermission(CODIGO_SHIZUKU)
        return false
    }

    private fun puedeDibujar(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            Settings.canDrawOverlays(this) else true

    private fun pedirOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(oyenteShizuku)
    }
}
