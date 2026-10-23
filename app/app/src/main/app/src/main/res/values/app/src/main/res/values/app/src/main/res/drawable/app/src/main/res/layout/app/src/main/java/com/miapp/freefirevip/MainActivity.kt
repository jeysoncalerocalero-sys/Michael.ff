package com.miapp.freefirevip

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val estado = findViewById<TextView>(R.id.txtEstado)
        val boton = findViewById<Button>(R.id.btnActivar)

        boton.setOnClickListener {
            estado.text = "FASE 0 COMPLETA - ya compila y corre"
        }
    }
}
