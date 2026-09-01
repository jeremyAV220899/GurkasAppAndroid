package com.cloud.gurkasapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        super.onViewCreated(view, savedInstanceState)

        val btnRealizarMarcacion =
            view.findViewById<View>(R.id.btnRealizarMarcacion)

        btnRealizarMarcacion.setOnClickListener {

            val intent = Intent(
                requireContext(),
                ReconocimientoActivity::class.java
            )

            startActivity(intent)
        }
    }
}