package com.cloud.gurkasapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cloud.gurkasapp.api.RetrofitClient
import com.cloud.gurkasapp.models.Sede
import com.cloud.gurkasapp.models.SedeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReconocimientoActivity : AppCompatActivity() {

    private lateinit var previewCamara: PreviewView
    private lateinit var txtHora: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtUbicacion: TextView

    private lateinit var txtCodigoSede: TextView
    private lateinit var txtCodigoUnidad: TextView
    private lateinit var txtSede: TextView
    private lateinit var txtNombreComercial: TextView

    private var sedeSeleccionada: Sede? = null

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val REQUEST_CAMERA = 100
        private const val REQUEST_LOCATION = 101
    }

    private val actualizarReloj = object : Runnable {
        override fun run() {

            mostrarFechaHora()

            handler.postDelayed(
                this,
                1000
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_reconocimiento
        )

        // ==============================
        // VISTAS
        // ==============================

        previewCamara =
            findViewById(R.id.previewCamara)

        txtHora =
            findViewById(R.id.txtHora)

        txtFecha =
            findViewById(R.id.txtFecha)

        txtUbicacion =
            findViewById(R.id.txtUbicacion)

        txtCodigoSede =
            findViewById(R.id.txtCodigoSede)

        txtCodigoUnidad =
            findViewById(R.id.txtCodigoUnidad)

        txtSede =
            findViewById(R.id.txtSede)

        txtNombreComercial =
            findViewById(R.id.txtUnidad)


        // ==============================
        // VALORES INICIALES
        // ==============================

        txtCodigoSede.text = "Código Sede: --"
        txtCodigoUnidad.text = "Código Unidad: --"
        txtSede.text = "Sede: --"
        txtNombreComercial.text = "Unidad: --"


        // ==============================
        // BOTÓN VOLVER
        // ==============================

        findViewById<TextView>(
            R.id.btnVolver
        ).setOnClickListener {

            finish()
        }


        // ==============================
        // HORA Y FECHA
        // ==============================

        mostrarFechaHora()


        // ==============================
        // PERMISO CÁMARA
        // ==============================

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            iniciarCamara()

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                REQUEST_CAMERA
            )
        }


        // ==============================
        // PERMISO UBICACIÓN
        // ==============================

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            obtenerUbicacion()

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION
            )
        }
    }


    // ==========================================================
    // MOSTRAR HORA Y FECHA
    // ==========================================================

    private fun mostrarFechaHora() {

        val ahora = Date()

        val formatoHora =
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            )

        val formatoFecha =
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

        txtHora.text =
            formatoHora.format(ahora)

        txtFecha.text =
            formatoFecha.format(ahora)
    }


    // ==========================================================
    // INICIAR CÁMARA
    // ==========================================================

    private fun iniciarCamara() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(
                this
            )

        cameraProviderFuture.addListener({

            try {

                val cameraProvider =
                    cameraProviderFuture.get()

                val preview =
                    Preview.Builder()
                        .build()
                        .also {

                            it.setSurfaceProvider(
                                previewCamara.surfaceProvider
                            )
                        }

                val cameraSelector =
                    CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview
                )

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    this,
                    "No se pudo iniciar la cámara",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }


    // ==========================================================
    // OBTENER UBICACIÓN
    // ==========================================================

    private fun obtenerUbicacion() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        txtUbicacion.text =
            "📍 Obteniendo ubicación..."

        val locationManager =
            getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager


        val ubicacionGps: Location? =
            try {

                locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER
                )

            } catch (e: Exception) {

                null
            }


        val ubicacionRed: Location? =
            try {

                locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
                )

            } catch (e: Exception) {

                null
            }


        val ubicacion =
            when {

                ubicacionGps == null ->
                    ubicacionRed

                ubicacionRed == null ->
                    ubicacionGps

                ubicacionGps.time >
                        ubicacionRed.time ->
                    ubicacionGps

                else ->
                    ubicacionRed
            }


        if (ubicacion != null) {

            val latitud =
                ubicacion.latitude

            val longitud =
                ubicacion.longitude


            txtUbicacion.text =
                String.format(
                    Locale.getDefault(),
                    "📍 %.6f, %.6f",
                    latitud,
                    longitud
                )


            // ==========================================
            // AQUÍ CONSUMIMOS TU API
            // ==========================================

            obtenerSedes(
                latitud,
                longitud
            )

        } else {

            txtUbicacion.text =
                "📍 Ubicación no disponible"

            Toast.makeText(
                this,
                "No se pudo obtener la ubicación",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // ==========================================================
    // CONSUMIR API DE SEDES
    // ==========================================================

    private fun obtenerSedes(
        latitud: Double,
        longitud: Double
    ) {

        txtSede.text =
            "Sede: Buscando..."

        txtCodigoSede.text =
            "Código Sede: --"


        RetrofitClient
            .apiService
            .obtenerSedes(
                latitud,
                longitud
            )
            .enqueue(

                object : Callback<SedeResponse> {

                    override fun onResponse(
                        call: Call<SedeResponse>,
                        response: Response<SedeResponse>
                    ) {

                        if (response.isSuccessful) {

                            val lista =
                                response.body()?.lista
                                    ?: emptyList()


                            if (lista.isEmpty()) {

                                txtSede.text =
                                    "Sede: No disponible"

                                txtCodigoSede.text =
                                    "Código Sede: --"

                                Toast.makeText(
                                    this@ReconocimientoActivity,
                                    "No hay sedes disponibles",
                                    Toast.LENGTH_SHORT
                                ).show()

                                return
                            }


                            // SI SOLO VIENE UNA
                            if (lista.size == 1) {

                                seleccionarSede(
                                    lista[0]
                                )

                            } else {

                                // SI VIENEN 2 O MÁS
                                mostrarSelectorSedes(
                                    lista
                                )
                            }

                        } else {

                            txtSede.text =
                                "Sede: Error"

                            Toast.makeText(
                                this@ReconocimientoActivity,
                                "Error del servidor: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


                    override fun onFailure(
                        call: Call<SedeResponse>,
                        t: Throwable
                    ) {

                        txtSede.text =
                            "Sede: Error de conexión"

                        Toast.makeText(
                            this@ReconocimientoActivity,
                            "Error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
    }


    // ==========================================================
    // MOSTRAR SELECTOR
    // ==========================================================

    private fun mostrarSelectorSedes(
        sedes: List<Sede>
    ) {

        val vista =
            layoutInflater.inflate(
                R.layout.dialog_seleccionar_sede,
                null
            )

        val contenedorSedes =
            vista.findViewById<android.widget.LinearLayout>(
                R.id.contenedorSedes
            )

        val dialog =
            androidx.appcompat.app.AlertDialog
                .Builder(this)
                .setView(vista)
                .setCancelable(false)
                .create()


        sedes.forEach { sede ->

            val item =
                layoutInflater.inflate(
                    R.layout.item_sede,
                    contenedorSedes,
                    false
                )


            val txtNombreSede =
                item.findViewById<TextView>(
                    R.id.txtNombreSede
                )


            val txtCodigoSedeDialog =
                item.findViewById<TextView>(
                    R.id.txtCodigoSedeDialog
                )


            txtNombreSede.text =
                sede.aliasSede ?: "Sede sin nombre"


            txtCodigoSedeDialog.text =
                sede.codsede ?: "--"


            item.setOnClickListener {

                seleccionarSede(
                    sede
                )

                dialog.dismiss()
            }


            contenedorSedes.addView(
                item
            )
        }


        dialog.setOnShowListener {

            dialog.window?.setBackgroundDrawableResource(
                android.R.color.transparent
            )

            val ancho =
                (resources.displayMetrics.widthPixels * 0.90)
                    .toInt()

            dialog.window?.setLayout(
                ancho,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }


        dialog.show()
    }


    // ==========================================================
    // SELECCIONAR SEDE
    // ==========================================================

    private fun seleccionarSede(
        sede: Sede
    ) {

        // Guardamos toda la sede seleccionada
        sedeSeleccionada = sede

        // Código de sede
        txtCodigoSede.text =
            "Código Sede: ${sede.codsede ?: "--"}"

        // Código de unidad
        txtCodigoUnidad.text =
            "Código Unidad: ${sede.codUnidad ?: "--"}"

        // Alias / nombre de sede
        txtSede.text =
            "Sede: ${sede.aliasSede ?: "--"}"

        // Nombre comercial
        txtNombreComercial.text =
            "Unidad: ${sede.nombreComercial ?: "--"}"

        Toast.makeText(
            this,
            "Sede seleccionada: ${sede.aliasSede ?: ""}",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ==========================================================
    // RESULTADO DE PERMISOS
    // ==========================================================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )


        when (requestCode) {

            REQUEST_CAMERA -> {

                if (
                    grantResults.isNotEmpty() &&
                    grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {

                    iniciarCamara()
                }
            }


            REQUEST_LOCATION -> {

                val permisoConcedido =
                    grantResults.any {

                        it ==
                                PackageManager.PERMISSION_GRANTED
                    }


                if (permisoConcedido) {

                    obtenerUbicacion()

                } else {

                    txtUbicacion.text =
                        "📍 Permiso de ubicación denegado"
                }
            }
        }
    }


    // ==========================================================
    // CICLO DE VIDA
    // ==========================================================

    override fun onResume() {
        super.onResume()

        handler.removeCallbacks(
            actualizarReloj
        )

        handler.post(
            actualizarReloj
        )
    }


    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(
            actualizarReloj
        )
    }


    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(
            actualizarReloj
        )
    }
}