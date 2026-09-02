package com.cloud.gurkasapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cloud.gurkasapp.api.RetrofitClient
import com.cloud.gurkasapp.facerecognition.EmbeddingUtils
import com.cloud.gurkasapp.facerecognition.FaceComparator
import com.cloud.gurkasapp.facerecognition.FaceEmbeddingManager
import com.cloud.gurkasapp.models.FeriadoResponse
import com.cloud.gurkasapp.models.ObtenerPersonalFacialResponse
import com.cloud.gurkasapp.models.Sede
import com.cloud.gurkasapp.models.SedeResponse
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class ReconocimientoActivity : AppCompatActivity() {

    // =========================================================
    // VISTAS
    // =========================================================

    private lateinit var previewCamara: PreviewView
    private lateinit var contenedorCamara: FrameLayout
    private lateinit var imgRostroConfirmado: ImageView

    private lateinit var txtEstadoRostro: TextView
    private lateinit var txtResultado: TextView

    private lateinit var txtHora: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtUbicacion: TextView

    private lateinit var txtCodigoSede: TextView
    private lateinit var txtCodigoUnidad: TextView
    private lateinit var txtSede: TextView
    private lateinit var txtNombreComercial: TextView

    private lateinit var txtCodigoTipoAsistencia: TextView
    private lateinit var txtTipoAsistencia: TextView

    private lateinit var txtDni: TextView
    private lateinit var txtEmpleado: TextView

    // =========================================================
    // USUARIO
    // =========================================================

    private var codigoUsuario: String = ""

    // =========================================================
    // FACIAL
    // =========================================================

    private lateinit var faceEmbeddingManager: FaceEmbeddingManager
    private lateinit var faceDetector: FaceDetector
    private lateinit var cameraExecutor: ExecutorService

    private var embeddingRegistrado: FloatArray? = null

    @Volatile
    private var facialCargado = false

    @Volatile
    private var identidadConfirmada = false

    private val procesandoFrame =
        AtomicBoolean(false)

    // =========================================================
    // FRAME CONGELADO
    // =========================================================

    private var ultimoFrameCamara: Bitmap? = null

    // =========================================================
    // VALIDACIÓN FACIAL
    // =========================================================

    private var coincidenciasConsecutivas = 0

    private val umbralCosenoPrueba = 0.985f
    private val umbralEuclidianaPrueba = 0.18f
    private val coincidenciasRequeridas = 5

    // =========================================================
    // DATOS PERSONAL
    // =========================================================

    @Volatile
    private var datosPersonalCargados = false

    @Volatile
    private var consultandoDatosPersonal = false

    // =========================================================
    // ESTADOS
    // =========================================================

    private enum class EstadoFacial {
        ESPERANDO,
        RECONOCIDO,
        NO_RECONOCIDO
    }

    private var ultimoEstadoFacial =
        EstadoFacial.ESPERANDO

    // =========================================================
    // SEDE
    // =========================================================

    private var sedeSeleccionada: Sede? = null

    // =========================================================
    // RELOJ
    // =========================================================

    private val handler =
        Handler(
            Looper.getMainLooper()
        )

    private val actualizarReloj =
        object : Runnable {

            override fun run() {

                mostrarFechaHora()

                handler.postDelayed(
                    this,
                    1000
                )
            }
        }

    companion object {

        private const val REQUEST_CAMERA = 100
        private const val REQUEST_LOCATION = 101
    }

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_reconocimiento
        )

        // =====================================================
        // RECIBIR CÓDIGO
        // =====================================================

        codigoUsuario =
            intent
                .getStringExtra("codigo")
                ?.trim()
                ?: ""

        if (codigoUsuario.isEmpty()) {

            Toast.makeText(
                this,
                "No se recibió el código del usuario.",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        Log.d(
            "GURKAS_FACIAL",
            "Código recibido=$codigoUsuario"
        )

        // =====================================================
        // VISTAS
        // =====================================================

        previewCamara =
            findViewById(
                R.id.previewCamara
            )

        contenedorCamara =
            findViewById(
                R.id.contenedorCamara
            )

        imgRostroConfirmado =
            findViewById(
                R.id.imgRostroConfirmado
            )

        txtEstadoRostro =
            findViewById(
                R.id.txtEstadoRostro
            )

        txtResultado =
            findViewById(
                R.id.txtResultado
            )

        txtHora =
            findViewById(
                R.id.txtHora
            )

        txtFecha =
            findViewById(
                R.id.txtFecha
            )

        txtUbicacion =
            findViewById(
                R.id.txtUbicacion
            )

        txtCodigoSede =
            findViewById(
                R.id.txtCodigoSede
            )

        txtCodigoUnidad =
            findViewById(
                R.id.txtCodigoUnidad
            )

        txtSede =
            findViewById(
                R.id.txtSede
            )

        txtNombreComercial =
            findViewById(
                R.id.txtUnidad
            )

        txtCodigoTipoAsistencia =
            findViewById(
                R.id.txtCodigoTipoAsistencia
            )

        txtTipoAsistencia =
            findViewById(
                R.id.txtTipoAsistencia
            )

        txtDni =
            findViewById(
                R.id.txtDni
            )

        txtEmpleado =
            findViewById(
                R.id.txtEmpleado
            )

        // =====================================================
        // INICIAL
        // =====================================================

        imgRostroConfirmado.visibility =
            View.GONE

        txtCodigoSede.text =
            "Código Sede: --"

        txtCodigoUnidad.text =
            "Código Unidad: --"

        txtSede.text =
            "Sede: --"

        txtNombreComercial.text =
            "Unidad: --"

        txtCodigoTipoAsistencia.text =
            "Código: --"

        txtTipoAsistencia.text =
            "Tipo de asistencia: --"

        txtDni.text =
            "DNI: --"

        txtEmpleado.text =
            "Nombre: --"

        mostrarEsperandoRostro()

        // =====================================================
        // MOTOR FACIAL
        // =====================================================

        try {

            faceEmbeddingManager =
                FaceEmbeddingManager(this)

            val opcionesDetector =
                FaceDetectorOptions
                    .Builder()
                    .setPerformanceMode(
                        FaceDetectorOptions.PERFORMANCE_MODE_FAST
                    )
                    .setLandmarkMode(
                        FaceDetectorOptions.LANDMARK_MODE_NONE
                    )
                    .setContourMode(
                        FaceDetectorOptions.CONTOUR_MODE_NONE
                    )
                    .setClassificationMode(
                        FaceDetectorOptions.CLASSIFICATION_MODE_NONE
                    )
                    .enableTracking()
                    .build()

            faceDetector =
                FaceDetection.getClient(
                    opcionesDetector
                )

            cameraExecutor =
                Executors.newSingleThreadExecutor()

        } catch (e: Exception) {

            Log.e(
                "GURKAS_FACIAL",
                "Error iniciando reconocimiento",
                e
            )

            Toast.makeText(
                this,
                "Error iniciando reconocimiento facial: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        // =====================================================
        // VOLVER
        // =====================================================

        findViewById<TextView>(
            R.id.btnVolver
        ).setOnClickListener {

            finish()
        }

        // =====================================================
        // OTROS DATOS
        // =====================================================

        mostrarFechaHora()
        obtenerTipoAsistencia()

        // =====================================================
        // CARGAR EMBEDDING
        // =====================================================

        obtenerPersonalFacial()

        // =====================================================
        // CÁMARA
        // =====================================================

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

        // =====================================================
        // UBICACIÓN
        // =====================================================

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

    // =========================================================
    // OBTENER PATRÓN FACIAL
    // =========================================================

    private fun obtenerPersonalFacial() {

        facialCargado = false
        embeddingRegistrado = null

        identidadConfirmada = false

        coincidenciasConsecutivas = 0

        datosPersonalCargados = false

        imgRostroConfirmado.visibility =
            View.GONE

        txtEstadoRostro.text =
            "Cargando patrón facial..."

        RetrofitClient
            .apiService
            .obtenerPersonalFacial(
                codigoUsuario
            )
            .enqueue(

                object :
                    Callback<ObtenerPersonalFacialResponse> {

                    override fun onResponse(
                        call: Call<ObtenerPersonalFacialResponse>,
                        response: Response<ObtenerPersonalFacialResponse>
                    ) {

                        Log.d(
                            "GURKAS_FACIAL",
                            "HTTP Facial=${response.code()}"
                        )

                        if (!response.isSuccessful) {

                            mostrarErrorCargaFacial(
                                "Error cargando patrón facial"
                            )

                            return
                        }

                        val lista =
                            response
                                .body()
                                ?.data
                                ?: emptyList()

                        if (lista.isEmpty()) {

                            mostrarNoRegistrado()
                            return
                        }

                        val facial =
                            lista[0]

                        if (facial.activo == false) {

                            mostrarNoRegistrado()
                            return
                        }

                        val textoEmbedding =
                            facial
                                .embedding
                                ?.trim()
                                ?: ""

                        if (textoEmbedding.isEmpty()) {

                            mostrarErrorCargaFacial(
                                "Embedding vacío"
                            )

                            return
                        }

                        try {

                            val embedding =
                                EmbeddingUtils
                                    .convertirStringAFloatArray(
                                        textoEmbedding
                                    )

                            if (embedding.isEmpty()) {

                                mostrarErrorCargaFacial(
                                    "Embedding inválido"
                                )

                                return
                            }

                            val dimensionServidor =
                                facial.dimensionEmbedding
                                    ?: embedding.size

                            if (
                                dimensionServidor !=
                                embedding.size
                            ) {

                                mostrarErrorCargaFacial(
                                    "Dimensión facial incorrecta"
                                )

                                return
                            }

                            if (
                                faceEmbeddingManager.embeddingDimension !=
                                embedding.size
                            ) {

                                mostrarErrorCargaFacial(
                                    "Modelo facial incompatible"
                                )

                                return
                            }

                            embeddingRegistrado =
                                embedding

                            facialCargado =
                                true

                            Log.d(
                                "GURKAS_FACIAL",
                                "Facial cargado correctamente. " +
                                        "Dimensión=${embedding.size}"
                            )

                            mostrarEsperandoRostro()

                        } catch (e: Exception) {

                            Log.e(
                                "GURKAS_FACIAL",
                                "Error embedding",
                                e
                            )

                            mostrarErrorCargaFacial(
                                "Error procesando facial"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ObtenerPersonalFacialResponse>,
                        t: Throwable
                    ) {

                        Log.e(
                            "GURKAS_FACIAL",
                            "Error conexión facial",
                            t
                        )

                        mostrarErrorCargaFacial(
                            "Error de conexión"
                        )
                    }
                }
            )
    }

    // =========================================================
    // ERROR FACIAL
    // =========================================================

    private fun mostrarErrorCargaFacial(
        mensaje: String
    ) {

        facialCargado = false
        embeddingRegistrado = null

        txtEstadoRostro.text =
            mensaje

        txtResultado.text =
            "FACIAL NO DISPONIBLE"

        actualizarBordeCamara(
            EstadoFacial.NO_RECONOCIDO
        )
    }

    // =========================================================
    // CÁMARA
    // =========================================================

    private fun iniciarCamara() {

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(this)

        cameraProviderFuture.addListener({

            try {

                val cameraProvider =
                    cameraProviderFuture.get()

                val preview =
                    Preview
                        .Builder()
                        .build()
                        .also {

                            it.setSurfaceProvider(
                                previewCamara.surfaceProvider
                            )
                        }

                val imageAnalysis =
                    ImageAnalysis
                        .Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis
                                .STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build()

                imageAnalysis.setAnalyzer(
                    cameraExecutor
                ) { imageProxy ->

                    analizarRostro(
                        imageProxy
                    )
                }

                val cameraSelector =
                    CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (e: Exception) {

                Log.e(
                    "GURKAS_FACIAL",
                    "Error iniciando cámara",
                    e
                )

                Toast.makeText(
                    this,
                    "No se pudo iniciar la cámara.",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // =========================================================
    // ANALIZAR ROSTRO
    // =========================================================

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class)
    private fun analizarRostro(
        imageProxy: ImageProxy
    ) {

        /*
         * Si ya reconocimos a la persona,
         * no seguimos analizando.
         */
        if (identidadConfirmada) {

            imageProxy.close()
            return
        }

        if (
            !facialCargado ||
            embeddingRegistrado == null
        ) {

            imageProxy.close()
            return
        }

        if (
            !procesandoFrame.compareAndSet(
                false,
                true
            )
        ) {

            imageProxy.close()
            return
        }

        val mediaImage =
            imageProxy.image

        if (mediaImage == null) {

            procesandoFrame.set(false)
            imageProxy.close()

            return
        }

        try {

            val rotationDegrees =
                imageProxy
                    .imageInfo
                    .rotationDegrees

            val bitmapOriginal =
                imageProxy.toBitmap()

            val inputImage =
                InputImage.fromMediaImage(
                    mediaImage,
                    rotationDegrees
                )

            faceDetector
                .process(inputImage)
                .addOnSuccessListener { rostros ->

                    if (identidadConfirmada) {
                        return@addOnSuccessListener
                    }

                    if (rostros.isEmpty()) {

                        coincidenciasConsecutivas = 0

                        runOnUiThread {

                            mostrarEsperandoRostro()
                        }

                        return@addOnSuccessListener
                    }

                    if (rostros.size > 1) {

                        coincidenciasConsecutivas = 0

                        runOnUiThread {

                            mostrarMultiplesRostros()
                        }

                        return@addOnSuccessListener
                    }

                    val rostro =
                        rostros[0]

                    val boundingBox =
                        rostro.boundingBox

                    if (
                        boundingBox.width() < 150 ||
                        boundingBox.height() < 150
                    ) {

                        coincidenciasConsecutivas = 0

                        runOnUiThread {

                            mostrarRostroMuyLejano()
                        }

                        return@addOnSuccessListener
                    }

                    try {

                        val bitmapRotado =
                            rotarBitmap(
                                bitmapOriginal,
                                rotationDegrees.toFloat()
                            )

                        /*
                         * Guardamos el frame completo.
                         * Este será el que se congela
                         * cuando la identidad se confirme.
                         */
                        ultimoFrameCamara =
                            bitmapRotado

                        val rostroBitmap =
                            recortarRostro(
                                bitmapRotado,
                                boundingBox
                            )

                        if (rostroBitmap == null) {

                            coincidenciasConsecutivas = 0
                            return@addOnSuccessListener
                        }

                        val embeddingCapturado =
                            faceEmbeddingManager
                                .generarEmbedding(
                                    rostroBitmap
                                )

                        val registrado =
                            embeddingRegistrado
                                ?: return@addOnSuccessListener

                        if (
                            embeddingCapturado.size !=
                            registrado.size
                        ) {

                            coincidenciasConsecutivas = 0

                            runOnUiThread {

                                mostrarRostroNoReconocido()
                            }

                            return@addOnSuccessListener
                        }

                        val similitud =
                            FaceComparator
                                .similitudCoseno(
                                    embeddingCapturado,
                                    registrado
                                )

                        val distancia =
                            FaceComparator
                                .distanciaEuclidiana(
                                    embeddingCapturado,
                                    registrado
                                )

                        Log.d(
                            "GURKAS_FACIAL",
                            "Código=$codigoUsuario | " +
                                    "Coseno=$similitud | " +
                                    "Euclidiana=$distancia"
                        )

                        runOnUiThread {

                            procesarResultadoFacial(
                                similitud,
                                distancia
                            )
                        }

                    } catch (e: Exception) {

                        coincidenciasConsecutivas = 0

                        Log.e(
                            "GURKAS_FACIAL",
                            "Error procesando rostro",
                            e
                        )
                    }
                }
                .addOnFailureListener { e ->

                    coincidenciasConsecutivas = 0

                    Log.e(
                        "GURKAS_FACIAL",
                        "Error ML Kit",
                        e
                    )
                }
                .addOnCompleteListener {

                    procesandoFrame.set(false)

                    imageProxy.close()
                }

        } catch (e: Exception) {

            procesandoFrame.set(false)

            Log.e(
                "GURKAS_FACIAL",
                "Error analizando frame",
                e
            )

            imageProxy.close()
        }
    }

    // =========================================================
    // PROCESAR RESULTADO
    // =========================================================

    private fun procesarResultadoFacial(
        similitud: Float,
        distancia: Float
    ) {

        if (identidadConfirmada) {
            return
        }

        val cumpleCoseno =
            similitud >=
                    umbralCosenoPrueba

        val cumpleDistancia =
            distancia <=
                    umbralEuclidianaPrueba

        val coincide =
            cumpleCoseno &&
                    cumpleDistancia

        Log.d(
            "GURKAS_FACIAL",
            "VALIDACIÓN " +
                    "Coseno=$similitud " +
                    "Euclidiana=$distancia " +
                    "Coincide=$coincide"
        )

        if (coincide) {

            coincidenciasConsecutivas++

            Log.d(
                "GURKAS_FACIAL",
                "Coincidencias=" +
                        "$coincidenciasConsecutivas/" +
                        "$coincidenciasRequeridas"
            )

            if (
                coincidenciasConsecutivas >=
                coincidenciasRequeridas
            ) {

                identidadConfirmada =
                    true

                Log.d(
                    "GURKAS_FACIAL",
                    "IDENTIDAD CONFIRMADA"
                )

                /*
                 * CONGELAMOS LA CÁMARA
                 */
                congelarCamara()

                mostrarRostroReconocido()

                if (
                    !datosPersonalCargados &&
                    !consultandoDatosPersonal
                ) {

                    obtenerDatosPersonal()
                }
            }

        } else {

            coincidenciasConsecutivas = 0

            mostrarRostroNoReconocido()
        }
    }

    // =========================================================
    // CONGELAR CÁMARA
    // =========================================================

    private fun congelarCamara() {

        val frame =
            ultimoFrameCamara
                ?: return

        runOnUiThread {

            try {

                imgRostroConfirmado
                    .setImageBitmap(
                        frame
                    )

                imgRostroConfirmado.visibility =
                    View.VISIBLE

                Log.d(
                    "GURKAS_FACIAL",
                    "CÁMARA CONGELADA"
                )

            } catch (e: Exception) {

                Log.e(
                    "GURKAS_FACIAL",
                    "Error congelando cámara",
                    e
                )
            }
        }
    }

    // =========================================================
    // ROSTRO RECONOCIDO
    // =========================================================

    private fun mostrarRostroReconocido() {

        ultimoEstadoFacial =
            EstadoFacial.RECONOCIDO

        actualizarBordeCamara(
            EstadoFacial.RECONOCIDO
        )

        txtEstadoRostro.text =
            "Rostro reconocido"

        txtEstadoRostro.setTextColor(
            Color.parseColor(
                "#00C853"
            )
        )

        txtResultado.text =
            "IDENTIDAD CONFIRMADA"

        txtResultado.setTextColor(
            Color.parseColor(
                "#00C853"
            )
        )

        if (!datosPersonalCargados) {

            txtDni.text =
                "DNI: buscando..."

            txtEmpleado.text =
                "Nombre: buscando..."
        }
    }

    // =========================================================
    // NO RECONOCIDO
    // =========================================================

    private fun mostrarRostroNoReconocido() {

        if (identidadConfirmada) {
            return
        }

        ultimoEstadoFacial =
            EstadoFacial.NO_RECONOCIDO

        actualizarBordeCamara(
            EstadoFacial.NO_RECONOCIDO
        )

        txtEstadoRostro.text =
            "Rostro no reconocido"

        txtEstadoRostro.setTextColor(
            Color.parseColor(
                "#D50000"
            )
        )

        txtResultado.text =
            "IDENTIDAD NO CONFIRMADA"

        txtResultado.setTextColor(
            Color.parseColor(
                "#D50000"
            )
        )

        txtDni.text =
            "DNI: --"

        txtEmpleado.text =
            "Nombre: --"
    }

    // =========================================================
    // ESPERANDO
    // =========================================================

    private fun mostrarEsperandoRostro() {

        if (identidadConfirmada) {
            return
        }

        ultimoEstadoFacial =
            EstadoFacial.ESPERANDO

        actualizarBordeCamara(
            EstadoFacial.ESPERANDO
        )

        txtEstadoRostro.text =
            "Coloca tu rostro frente a la cámara"

        txtEstadoRostro.setTextColor(
            Color.parseColor(
                "#A8ADB0"
            )
        )

        txtResultado.text =
            "Esperando marcación"

        txtResultado.setTextColor(
            Color.parseColor(
                "#27AE60"
            )
        )

        txtDni.text =
            "DNI: --"

        txtEmpleado.text =
            "Nombre: --"
    }

    // =========================================================
    // MÚLTIPLES ROSTROS
    // =========================================================

    private fun mostrarMultiplesRostros() {

        if (identidadConfirmada) {
            return
        }

        ultimoEstadoFacial =
            EstadoFacial.ESPERANDO

        actualizarBordeCamara(
            EstadoFacial.ESPERANDO
        )

        txtEstadoRostro.text =
            "Debe aparecer una sola persona"

        txtEstadoRostro.setTextColor(
            Color.parseColor(
                "#FFC107"
            )
        )

        txtResultado.text =
            "Esperando marcación"
    }

    // =========================================================
    // ROSTRO LEJANO
    // =========================================================

    private fun mostrarRostroMuyLejano() {

        if (identidadConfirmada) {
            return
        }

        ultimoEstadoFacial =
            EstadoFacial.ESPERANDO

        actualizarBordeCamara(
            EstadoFacial.ESPERANDO
        )

        txtEstadoRostro.text =
            "Acércate un poco más"

        txtEstadoRostro.setTextColor(
            Color.parseColor(
                "#FFC107"
            )
        )

        txtResultado.text =
            "Esperando marcación"
    }

    // =========================================================
    // NO REGISTRADO
    // =========================================================

    private fun mostrarNoRegistrado() {

        facialCargado = false
        embeddingRegistrado = null

        ultimoEstadoFacial =
            EstadoFacial.NO_RECONOCIDO

        actualizarBordeCamara(
            EstadoFacial.NO_RECONOCIDO
        )

        txtEstadoRostro.text =
            "Usuario sin registro facial"

        txtEstadoRostro.setTextColor(
            Color.parseColor(
                "#D50000"
            )
        )

        txtResultado.text =
            "ROSTRO NO REGISTRADO"

        txtResultado.setTextColor(
            Color.parseColor(
                "#D50000"
            )
        )
    }

    // =========================================================
    // DATOS PERSONAL
    // =========================================================

    private fun obtenerDatosPersonal() {

        Log.d(
            "GURKAS_PERSONAL",
            "Consultando personal código=$codigoUsuario"
        )

        if (!identidadConfirmada) {
            return
        }

        if (datosPersonalCargados) {
            return
        }

        if (consultandoDatosPersonal) {
            return
        }

        consultandoDatosPersonal =
            true

        Thread {

            var conexion:
                    HttpURLConnection? =
                null

            try {

                val codigoCodificado =
                    URLEncoder.encode(
                        codigoUsuario,
                        "UTF-8"
                    )

                val direccion =
                    "https://grupogurkas.site/" +
                            "apersonal/DatosPersonalMovil" +
                            "?codigo=$codigoCodificado"

                conexion =
                    URL(direccion)
                        .openConnection()
                            as HttpURLConnection

                conexion.requestMethod =
                    "GET"

                conexion.connectTimeout =
                    15000

                conexion.readTimeout =
                    15000

                conexion.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                val codigoHttp =
                    conexion.responseCode

                Log.d(
                    "GURKAS_PERSONAL",
                    "HTTP=$codigoHttp"
                )

                if (codigoHttp !in 200..299) {

                    runOnUiThread {

                        consultandoDatosPersonal =
                            false

                        txtDni.text =
                            "DNI: error"

                        txtEmpleado.text =
                            "Nombre: error"
                    }

                    return@Thread
                }

                val textoJson =
                    conexion
                        .inputStream
                        .bufferedReader()
                        .use {

                            it.readText()
                        }

                Log.d(
                    "GURKAS_PERSONAL",
                    "RESPUESTA DATOS PERSONAL=$textoJson"
                )

                val raiz =
                    JSONObject(
                        textoJson
                    )

                val lista =
                    raiz.optJSONArray(
                        "lista"
                    )

                if (
                    lista == null ||
                    lista.length() == 0
                ) {

                    runOnUiThread {

                        consultandoDatosPersonal =
                            false

                        txtDni.text =
                            "DNI: --"

                        txtEmpleado.text =
                            "Nombre: no encontrado"
                    }

                    return@Thread
                }

                val personal =
                    lista.getJSONObject(
                        0
                    )

                /*
                 * CAMPOS REALES DE TU API
                 */
                val dni =
                    personal
                        .optString(
                            "doctidentidad",
                            ""
                        )
                        .trim()

                val nombreCompleto =
                    personal
                        .optString(
                            "nombrecompleto",
                            ""
                        )
                        .trim()

                Log.d(
                    "GURKAS_PERSONAL",
                    "DNI=$dni | NOMBRE=$nombreCompleto"
                )

                runOnUiThread {

                    consultandoDatosPersonal =
                        false

                    if (!identidadConfirmada) {
                        return@runOnUiThread
                    }

                    txtDni.text =
                        if (dni.isBlank()) {

                            "DNI: --"

                        } else {

                            "DNI: $dni"
                        }

                    txtEmpleado.text =
                        if (
                            nombreCompleto.isBlank()
                        ) {

                            "Nombre: --"

                        } else {

                            "Nombre: $nombreCompleto"
                        }

                    datosPersonalCargados =
                        true

                    Log.d(
                        "GURKAS_PERSONAL",
                        "Datos mostrados correctamente"
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "GURKAS_PERSONAL",
                    "Error consultando personal",
                    e
                )

                runOnUiThread {

                    consultandoDatosPersonal =
                        false

                    txtDni.text =
                        "DNI: error"

                    txtEmpleado.text =
                        "Nombre: error"
                }

            } finally {

                conexion
                    ?.disconnect()
            }

        }.start()
    }

    // =========================================================
    // BORDE
    // =========================================================

    private fun actualizarBordeCamara(
        estado: EstadoFacial
    ) {

        val color =
            when (estado) {

                EstadoFacial.RECONOCIDO ->
                    Color.parseColor(
                        "#00C853"
                    )

                EstadoFacial.NO_RECONOCIDO ->
                    Color.parseColor(
                        "#D50000"
                    )

                EstadoFacial.ESPERANDO ->
                    Color.parseColor(
                        "#607D8B"
                    )
            }

        val drawable =
            GradientDrawable()

        drawable.shape =
            GradientDrawable.OVAL

        drawable.setColor(
            Color.TRANSPARENT
        )

        drawable.setStroke(
            8,
            color
        )

        contenedorCamara.foreground =
            drawable
    }

    // =========================================================
    // ROTAR
    // =========================================================

    private fun rotarBitmap(
        bitmap: Bitmap,
        grados: Float
    ): Bitmap {

        if (grados == 0f) {
            return bitmap
        }

        val matrix =
            Matrix()

        matrix.postRotate(
            grados
        )

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    // =========================================================
    // RECORTAR ROSTRO
    // =========================================================

    private fun recortarRostro(
        bitmap: Bitmap,
        boundingBox: Rect
    ): Bitmap? {

        return try {

            val margenX =
                (
                        boundingBox.width() *
                                0.15f
                        )
                    .toInt()

            val margenY =
                (
                        boundingBox.height() *
                                0.20f
                        )
                    .toInt()

            val izquierda =
                (
                        boundingBox.left -
                                margenX
                        )
                    .coerceAtLeast(
                        0
                    )

            val arriba =
                (
                        boundingBox.top -
                                margenY
                        )
                    .coerceAtLeast(
                        0
                    )

            val derecha =
                (
                        boundingBox.right +
                                margenX
                        )
                    .coerceAtMost(
                        bitmap.width
                    )

            val abajo =
                (
                        boundingBox.bottom +
                                margenY
                        )
                    .coerceAtMost(
                        bitmap.height
                    )

            val ancho =
                derecha -
                        izquierda

            val alto =
                abajo -
                        arriba

            if (
                ancho <= 0 ||
                alto <= 0
            ) {

                null

            } else {

                Bitmap.createBitmap(
                    bitmap,
                    izquierda,
                    arriba,
                    ancho,
                    alto
                )
            }

        } catch (e: Exception) {

            Log.e(
                "GURKAS_FACIAL",
                "Error recortando rostro",
                e
            )

            null
        }
    }

    // =========================================================
    // FECHA / HORA
    // =========================================================

    private fun mostrarFechaHora() {

        val ahora =
            Date()

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
            formatoHora.format(
                ahora
            )

        txtFecha.text =
            formatoFecha.format(
                ahora
            )
    }

    // =========================================================
    // UBICACIÓN
    // =========================================================

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

                locationManager
                    .getLastKnownLocation(
                        LocationManager.GPS_PROVIDER
                    )

            } catch (e: Exception) {

                null
            }

        val ubicacionRed: Location? =
            try {

                locationManager
                    .getLastKnownLocation(
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

            obtenerSedes(
                latitud,
                longitud
            )

        } else {

            txtUbicacion.text =
                "📍 Ubicación no disponible"
        }
    }

    // =========================================================
    // SEDES
    // =========================================================

    private fun obtenerSedes(
        latitud: Double,
        longitud: Double
    ) {

        txtSede.text =
            "Sede: Buscando..."

        RetrofitClient
            .apiService
            .obtenerSedes(
                latitud,
                longitud
            )
            .enqueue(

                object :
                    Callback<SedeResponse> {

                    override fun onResponse(
                        call: Call<SedeResponse>,
                        response: Response<SedeResponse>
                    ) {

                        if (!response.isSuccessful) {

                            txtSede.text =
                                "Sede: Error"

                            return
                        }

                        val lista =
                            response
                                .body()
                                ?.lista
                                ?: emptyList()

                        if (lista.isEmpty()) {

                            txtSede.text =
                                "Sede: No disponible"

                            return
                        }

                        if (lista.size == 1) {

                            seleccionarSede(
                                lista[0]
                            )

                        } else {

                            mostrarSelectorSedes(
                                lista
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<SedeResponse>,
                        t: Throwable
                    ) {

                        txtSede.text =
                            "Sede: Error de conexión"
                    }
                }
            )
    }

    // =========================================================
    // SELECTOR SEDES
    // =========================================================

    private fun mostrarSelectorSedes(
        sedes: List<Sede>
    ) {

        val vista =
            layoutInflater.inflate(
                R.layout.dialog_seleccionar_sede,
                null
            )

        val contenedorSedes =
            vista.findViewById<LinearLayout>(
                R.id.contenedorSedes
            )

        val dialog =
            AlertDialog
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
                sede.aliasSede
                    ?: "Sede sin nombre"

            txtCodigoSedeDialog.text =
                sede.codsede
                    ?: "--"

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

        dialog.show()
    }

    // =========================================================
    // SELECCIONAR SEDE
    // =========================================================

    private fun seleccionarSede(
        sede: Sede
    ) {

        sedeSeleccionada =
            sede

        txtCodigoSede.text =
            "Código Sede: ${
                sede.codsede ?: "--"
            }"

        txtCodigoUnidad.text =
            "Código Unidad: ${
                sede.codUnidad ?: "--"
            }"

        txtSede.text =
            "Sede: ${
                sede.aliasSede ?: "--"
            }"

        txtNombreComercial.text =
            "Unidad: ${
                sede.nombreComercial ?: "--"
            }"
    }

    // =========================================================
    // TIPO ASISTENCIA
    // =========================================================

    private fun obtenerTipoAsistencia() {

        val fechaTexto =
            txtFecha
                .text
                .toString()
                .trim()

        try {

            val formatoPantalla =
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                )

            val formatoApi =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )

            val fechaConvertida =
                formatoPantalla.parse(
                    fechaTexto
                )
                    ?: return

            val fechaApi =
                formatoApi.format(
                    fechaConvertida
                )

            RetrofitClient
                .apiService
                .obtenerFeriado(
                    fechaApi
                )
                .enqueue(

                    object :
                        Callback<FeriadoResponse> {

                        override fun onResponse(
                            call: Call<FeriadoResponse>,
                            response: Response<FeriadoResponse>
                        ) {

                            if (!response.isSuccessful) {
                                return
                            }

                            val lista =
                                response
                                    .body()
                                    ?.lista
                                    ?: emptyList()

                            if (lista.isNotEmpty()) {

                                val feriado =
                                    lista[0]

                                txtCodigoTipoAsistencia.text =
                                    "Código: ${
                                        feriado.codigoAsistencia
                                            ?: "--"
                                    }"

                                txtTipoAsistencia.text =
                                    "Tipo de asistencia: ${
                                        feriado.tipoAsistencia
                                            ?: "--"
                                    }"
                            }
                        }

                        override fun onFailure(
                            call: Call<FeriadoResponse>,
                            t: Throwable
                        ) {
                        }
                    }
                )

        } catch (e: Exception) {

            Log.e(
                "GURKAS",
                "Error tipo asistencia",
                e
            )
        }
    }

    // =========================================================
    // PERMISOS
    // =========================================================

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

                val concedido =
                    grantResults.any {

                        it ==
                                PackageManager.PERMISSION_GRANTED
                    }

                if (concedido) {

                    obtenerUbicacion()
                }
            }
        }
    }

    // =========================================================
    // CICLO DE VIDA
    // =========================================================

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

        if (
            ::faceDetector.isInitialized
        ) {

            faceDetector.close()
        }

        if (
            ::faceEmbeddingManager.isInitialized
        ) {

            faceEmbeddingManager.cerrar()
        }

        if (
            ::cameraExecutor.isInitialized
        ) {

            cameraExecutor.shutdown()
        }

        /*
         * Liberamos la referencia del bitmap.
         */
        ultimoFrameCamara = null

        embeddingRegistrado = null
        facialCargado = false
        identidadConfirmada = false
    }
}