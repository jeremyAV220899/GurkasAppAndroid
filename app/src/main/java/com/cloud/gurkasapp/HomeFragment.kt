package com.cloud.gurkasapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )


        // =====================================================
        // BOTÓN REALIZAR MARCACIÓN
        // =====================================================

        val btnRealizarMarcacion =
            view.findViewById<View>(
                R.id.btnRealizarMarcacion
            )


        btnRealizarMarcacion.setOnClickListener {

            // =================================================
            // OBTENER CÓDIGO DEL USUARIO DESDE LA ACTIVITY
            // =================================================

            val txtUsuario =
                requireActivity()
                    .findViewById<TextView>(
                        R.id.txtUsuario
                    )


            val codigoUsuario =
                txtUsuario
                    ?.text
                    ?.toString()
                    ?.trim()
                    ?: ""


            // =================================================
            // VALIDAR
            // =================================================

            if (codigoUsuario.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "No se encontró el código del usuario.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }


            // =================================================
            // ABRIR RECONOCIMIENTO FACIAL
            // =================================================

            val intent =
                Intent(
                    requireContext(),
                    ReconocimientoFacialActivity::class.java
                )


            // =================================================
            // ENVIAR CÓDIGO
            // =================================================

            intent.putExtra(
                "codigo",
                codigoUsuario
            )


            startActivity(
                intent
            )
        }
    }
}