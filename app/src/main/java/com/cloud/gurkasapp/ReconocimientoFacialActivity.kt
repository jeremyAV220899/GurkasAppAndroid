package com.cloud.gurkasapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import android.graphics.drawable.GradientDrawable
import com.cloud.gurkasapp.api.RetrofitClient
import com.cloud.gurkasapp.facerecognition.EmbeddingUtils
import com.cloud.gurkasapp.facerecognition.FaceComparator
import com.cloud.gurkasapp.facerecognition.FaceEmbeddingManager
import com.cloud.gurkasapp.models.ObtenerPersonalFacialResponse
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.sqrt


class ReconocimientoFacialActivity :
    AppCompatActivity() {


    companion object {
        private const val TAG_ARCFACE =
            "RECONOCIMIENTO_ARCFACE"

        private const val TAMANO_ROSTRO_CANONICO =
            112
    }


    // =========================================================
    // VISTAS
    // =========================================================

    private lateinit var previewCamara:
            PreviewView


    private lateinit var faceOverlay:
            FaceOvalOverlay


    private lateinit var imgRostroConfirmado:
            ImageView


    private lateinit var txtDni:
            TextView


    private lateinit var txtNombre:
            TextView


    private lateinit var txtEstado:
            TextView

    private lateinit var txtTituloIdentidad: TextView

    private lateinit var txtDescripcion: TextView

    private lateinit var indicadorEstado: View

    private lateinit var btnValidar:
            Button


    // =========================================================
    // USUARIO
    // =========================================================

    private var codigoUsuario:
            String = ""


    private var dniEmpleado:
            String = ""


    private var nombreEmpleado:
            String = ""


    // =========================================================
    // MOTOR FACIAL
    // =========================================================

    private lateinit var faceEmbeddingManager:
            FaceEmbeddingManager


    private lateinit var faceDetector:
            FaceDetector


    private lateinit var cameraExecutor:
            ExecutorService


    // =========================================================
    // EMBEDDING REGISTRADO
    // =========================================================

    private var embeddingRegistrado:
            FloatArray? = null


    @Volatile
    private var facialCargado:
            Boolean = false


    // =========================================================
    // DATOS DEL PERSONAL
    // =========================================================

    @Volatile
    private var datosPersonalCargados:
            Boolean = false


    @Volatile
    private var consultandoDatosPersonal:
            Boolean = false


    // =========================================================
    // RECONOCIMIENTO
    // =========================================================

    @Volatile
    private var identidadConfirmada:
            Boolean = false


    private var coincidenciasConsecutivas:
            Int = 0


    /*
     * Por ahora mantenemos valores estrictos.
     *
     * Después los calibramos con tus datos reales.
     */
    // Umbral PROVISIONAL para las primeras pruebas con ArcFace/GhostFaceNet.
    // Debe calibrarse con tus scores genuinos e impostores antes de producción.
    private val umbralCosenoPrueba:
            Float = 0.70f


    private val coincidenciasRequeridas:
            Int = 5


    // =========================================================
    // CONTROL DE FRAMES
    // =========================================================

    private val procesandoFrame =
        AtomicBoolean(
            false
        )


    // =========================================================
    // ÚLTIMO FRAME CORRECTO
    // =========================================================

    @Volatile
    private var ultimoFrameValido:
            Bitmap? = null


    // =========================================================
    // COLORES
    // =========================================================

    private val colorEsperando =
        Color.parseColor(
            "#90A4AE"
        )


    private val colorCalidad =
        Color.parseColor(
            "#FFC107"
        )


    private val colorError =
        Color.parseColor(
            "#D50000"
        )


    private val colorCorrecto =
        Color.parseColor(
            "#00C853"
        )


    // =========================================================
    // PERMISO CÁMARA
    // =========================================================

    private val permisoCamara =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permitido ->


            if (
                permitido
            ) {

                iniciarCamara()


            } else {

                Toast.makeText(
                    this,
                    "Se necesita permiso de cámara.",
                    Toast.LENGTH_LONG
                ).show()


                finish()
            }
        }


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        setContentView(
            R.layout.activity_reconocimiento_facial
        )


        // =====================================================
        // VISTAS
        // =====================================================

        previewCamara =
            findViewById(
                R.id.previewCamara
            )


        faceOverlay =
            findViewById(
                R.id.faceOverlay
            )


        imgRostroConfirmado =
            findViewById(
                R.id.imgRostroConfirmado
            )


        txtDni =
            findViewById(
                R.id.txtDni
            )


        txtNombre =
            findViewById(
                R.id.txtNombre
            )


        txtEstado =
            findViewById(
                R.id.txtEstado
            )

        txtTituloIdentidad =
            findViewById(
                R.id.txtTituloIdentidad
            )

        txtDescripcion =
            findViewById(
                R.id.txtDescripcion
            )

        indicadorEstado =
            findViewById(
                R.id.indicadorEstado
            )

        btnValidar =
            findViewById(
                R.id.btnValidar
            )


        // =====================================================
        // RECIBIR CÓDIGO
        // =====================================================

        codigoUsuario =
            intent
                .getStringExtra(
                    "codigo"
                )
                ?.trim()
                ?: ""


        if (
            codigoUsuario.isEmpty()
        ) {

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
            "Código=$codigoUsuario"
        )


        // =====================================================
        // ESTADO INICIAL
        // =====================================================

        configurarEstadoInicial()


        // =====================================================
        // MOTOR FACIAL
        // =====================================================

        try {

            faceEmbeddingManager =
                FaceEmbeddingManager(
                    this
                )


            configurarDetectorFacial()


            cameraExecutor =
                Executors
                    .newSingleThreadExecutor()


        } catch (
            e: Exception
        ) {

            Log.e(
                "GURKAS_FACIAL",
                "Error iniciando motor facial",
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
        // CARGAR DATOS
        // =====================================================

        /*
         * Los consultamos desde el comienzo,
         * pero NO se muestran todavía.
         *
         * Solo se mostrarán cuando el rostro
         * sea confirmado.
         */
        obtenerDatosPersonal()


        // =====================================================
        // CARGAR EMBEDDING
        // =====================================================

        obtenerPersonalFacial()


        // =====================================================
        // CÁMARA
        // =====================================================

        verificarPermisoCamara()


        // =====================================================
        // BOTÓN VALIDACIÓN
        // =====================================================

        btnValidar
            .setOnClickListener {

                irAValidacion()
            }
    }

    private fun cambiarColorIndicador(
        color: Int
    ) {

        try {

            val drawable =
                GradientDrawable()

            drawable.shape =
                GradientDrawable.OVAL

            drawable.setColor(
                color
            )

            indicadorEstado.background =
                drawable

        } catch (
            e: Exception
        ) {

            Log.e(
                "GURKAS_FACIAL",
                "Error cambiando indicador",
                e
            )
        }
    }

    // =========================================================
    // ESTADO INICIAL
    // =========================================================

    private fun configurarEstadoInicial() {

        identidadConfirmada =
            false


        coincidenciasConsecutivas =
            0


        ultimoFrameValido =
            null


        imgRostroConfirmado.visibility =
            View.GONE


        previewCamara.visibility =
            View.VISIBLE


        // -----------------------------------------
        // DATOS
        // -----------------------------------------

        txtDni.text =
            "DNI: --"


        txtNombre.text =
            "Nombre: --"

        // -----------------------------------------
        // ESTADO
        // -----------------------------------------

        txtEstado.text =
            "Coloque su rostro dentro del óvalo"


        txtEstado.setTextColor(
            colorEsperando
        )

        cambiarColorIndicador(
            colorEsperando
        )

        // -----------------------------------------
        // TARJETA
        // -----------------------------------------

        txtTituloIdentidad.text =
            "Verificación de identidad"

        txtTituloIdentidad.visibility =
            View.VISIBLE


        txtDescripcion.text =
            "Mantén el rostro de frente y con buena iluminación."

        txtDescripcion.visibility =
            View.VISIBLE


        txtNombre.visibility =
            View.VISIBLE

        txtDni.visibility =
            View.VISIBLE

        // -----------------------------------------
        // ÓVALO
        // -----------------------------------------

        faceOverlay
            .cambiarColorBorde(
                colorEsperando
            )

        // -----------------------------------------
        // BOTÓN
        // -----------------------------------------

        btnValidar.isEnabled =
            false
    }


    // =========================================================
    // CONFIGURAR DETECTOR FACIAL
    // =========================================================

    private fun configurarDetectorFacial() {

        val opcionesDetector =
            FaceDetectorOptions
                .Builder()
                .setPerformanceMode(
                    FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
                )
                .setLandmarkMode(
                    FaceDetectorOptions.LANDMARK_MODE_ALL
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
    }


    // =========================================================
    // VERIFICAR PERMISO
    // =========================================================

    private fun verificarPermisoCamara() {

        val permiso =
            ContextCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                )


        if (
            permiso ==
            PackageManager.PERMISSION_GRANTED
        ) {

            iniciarCamara()


        } else {

            permisoCamara.launch(
                Manifest.permission.CAMERA
            )
        }
    }


    // =========================================================
    // CARGAR EMBEDDING REGISTRADO
    // =========================================================

    private fun obtenerPersonalFacial() {

        facialCargado =
            false


        embeddingRegistrado =
            null


        txtEstado.text =
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
                        call:
                        Call<ObtenerPersonalFacialResponse>,

                        response:
                        Response<ObtenerPersonalFacialResponse>
                    ) {

                        Log.d(
                            "GURKAS_FACIAL",
                            "HTTP facial=${response.code()}"
                        )


                        if (
                            !response.isSuccessful
                        ) {

                            mostrarErrorFacial(
                                "Error cargando patrón facial"
                            )

                            return
                        }


                        val lista =
                            response
                                .body()
                                ?.data
                                ?: emptyList()


                        if (
                            lista.isEmpty()
                        ) {

                            mostrarErrorFacial(
                                "Usuario sin registro facial"
                            )

                            return
                        }


                        val facial =
                            lista[0]


                        if (
                            facial.activo == false
                        ) {

                            mostrarErrorFacial(
                                "Registro facial inactivo"
                            )

                            return
                        }


                        val textoEmbedding =
                            facial
                                .embedding
                                ?.trim()
                                ?: ""


                        if (
                            textoEmbedding.isEmpty()
                        ) {

                            mostrarErrorFacial(
                                "Patrón facial vacío"
                            )

                            return
                        }


                        try {

                            val embedding =
                                EmbeddingUtils
                                    .convertirStringAFloatArray(
                                        textoEmbedding
                                    )


                            if (
                                embedding.isEmpty()
                            ) {

                                mostrarErrorFacial(
                                    "Patrón facial inválido"
                                )

                                return
                            }


                            // =================================
                            // DIMENSIÓN SERVIDOR
                            // =================================

                            val dimensionServidor =
                                facial
                                    .dimensionEmbedding
                                    ?: embedding.size


                            if (
                                dimensionServidor !=
                                embedding.size
                            ) {

                                mostrarErrorFacial(
                                    "Dimensión facial incorrecta"
                                )

                                return
                            }


                            // =================================
                            // DIMENSIÓN MODELO
                            // =================================

                            if (
                                faceEmbeddingManager
                                    .embeddingDimension !=
                                embedding.size
                            ) {

                                mostrarErrorFacial(
                                    "Modelo facial incompatible"
                                )

                                return
                            }


                            // =================================
                            // NORMALIZACIÓN L2
                            // =================================

                            embeddingRegistrado =
                                normalizarEmbedding(
                                    embedding
                                )


                            facialCargado =
                                true


                            Log.d(
                                "GURKAS_FACIAL",
                                "Embedding cargado. " +
                                        "Dimensión=${embedding.size}"
                            )


                            if (
                                !identidadConfirmada
                            ) {

                                txtEstado.text =
                                    "Coloque su rostro dentro del óvalo"


                                txtEstado.setTextColor(
                                    colorEsperando
                                )


                                faceOverlay
                                    .cambiarColorBorde(
                                        colorEsperando
                                    )
                            }


                        } catch (
                            e: Exception
                        ) {

                            Log.e(
                                "GURKAS_FACIAL",
                                "Error procesando embedding",
                                e
                            )


                            mostrarErrorFacial(
                                "Error procesando patrón facial"
                            )
                        }
                    }


                    override fun onFailure(
                        call:
                        Call<ObtenerPersonalFacialResponse>,

                        t:
                        Throwable
                    ) {

                        Log.e(
                            "GURKAS_FACIAL",
                            "Error conexión facial",
                            t
                        )


                        mostrarErrorFacial(
                            "Error de conexión"
                        )
                    }
                }
            )
    }


    // =========================================================
    // ERROR FACIAL
    // =========================================================

    private fun mostrarErrorFacial(
        mensaje: String
    ) {

        facialCargado =
            false

        embeddingRegistrado =
            null


        runOnUiThread {

            txtEstado.text =
                mensaje

            txtEstado.setTextColor(
                colorError
            )


            cambiarColorIndicador(
                colorError
            )


            faceOverlay
                .cambiarColorBorde(
                    colorError
                )


            txtTituloIdentidad.text =
                "Error de verificación"


            txtDescripcion.text =
                "No se pudo iniciar correctamente la validación facial."

            txtDescripcion.visibility =
                View.VISIBLE


            btnValidar.isEnabled =
                false
        }
    }

    // =========================================================
    // INICIAR CÁMARA
    // =========================================================

    private fun iniciarCamara() {

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(
                    this
                )


        cameraProviderFuture
            .addListener(
                {

                    try {

                        val cameraProvider =
                            cameraProviderFuture
                                .get()


                        // =====================================
                        // PREVIEW
                        // =====================================

                        val preview =
                            Preview
                                .Builder()
                                .build()
                                .also {

                                    it.setSurfaceProvider(
                                        previewCamara
                                            .surfaceProvider
                                    )
                                }


                        // =====================================
                        // ANALYSIS
                        // =====================================

                        val imageAnalysis =
                            ImageAnalysis
                                .Builder()

                                .setBackpressureStrategy(
                                    ImageAnalysis
                                        .STRATEGY_KEEP_ONLY_LATEST
                                )

                                .build()


                        imageAnalysis
                            .setAnalyzer(
                                cameraExecutor
                            ) { imageProxy ->

                                analizarRostro(
                                    imageProxy
                                )
                            }


                        // =====================================
                        // CÁMARA FRONTAL
                        // =====================================

                        val cameraSelector =
                            CameraSelector
                                .DEFAULT_FRONT_CAMERA


                        cameraProvider
                            .unbindAll()


                        cameraProvider
                            .bindToLifecycle(
                                this,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )


                    } catch (
                        e: Exception
                    ) {

                        Log.e(
                            "GURKAS_FACIAL",
                            "Error iniciando cámara",
                            e
                        )


                        runOnUiThread {

                            Toast.makeText(
                                this,
                                "Error iniciando cámara: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                },

                ContextCompat
                    .getMainExecutor(
                        this
                    )
            )
    }


    // =========================================================
    // ANALIZAR ROSTRO
    // =========================================================

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class)
    private fun analizarRostro(
        imageProxy: ImageProxy
    ) {

        if (identidadConfirmada) {
            imageProxy.close()
            return
        }

        if (!facialCargado || embeddingRegistrado == null) {
            imageProxy.close()
            return
        }

        if (!procesandoFrame.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        try {
            val rotationDegrees =
                imageProxy.imageInfo.rotationDegrees

            // IMPORTANTE:
            // primero dejamos el Bitmap en posición vertical y ML Kit analiza
            // exactamente ese mismo Bitmap con rotación 0. De esta forma el
            // boundingBox y los landmarks viven en el mismo sistema de coordenadas
            // que usamos para recortar y alinear.
            val bitmapOriginal =
                imageProxy.toBitmap()

            val bitmapVertical =
                rotarBitmap(
                    bitmapOriginal,
                    rotationDegrees.toFloat()
                )

            val inputImage =
                InputImage.fromBitmap(
                    bitmapVertical,
                    0
                )

            faceDetector
                .process(inputImage)
                .addOnSuccessListener { rostros ->

                    if (identidadConfirmada) {
                        return@addOnSuccessListener
                    }

                    if (rostros.isEmpty()) {
                        coincidenciasConsecutivas = 0
                        mostrarEstadoEsperando(
                            "Coloque su rostro dentro del óvalo"
                        )
                        return@addOnSuccessListener
                    }

                    if (rostros.size > 1) {
                        coincidenciasConsecutivas = 0
                        mostrarEstadoCalidad(
                            "Solo debe aparecer una persona"
                        )
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
                        mostrarEstadoCalidad(
                            "Acérquese un poco a la cámara"
                        )
                        return@addOnSuccessListener
                    }

                    if (
                        !rostroDentroDelOval(
                            boundingBox,
                            bitmapVertical.width,
                            bitmapVertical.height
                        )
                    ) {
                        coincidenciasConsecutivas = 0
                        mostrarEstadoCalidad(
                            "Coloque su rostro dentro del óvalo"
                        )
                        return@addOnSuccessListener
                    }

                    if (!tieneLandmarksNecesarios(rostro)) {
                        coincidenciasConsecutivas = 0
                        mostrarEstadoCalidad(
                            "Mire de frente y mantenga visibles ojos y nariz"
                        )
                        return@addOnSuccessListener
                    }

                    try {
                        val recorte =
                            recortarRostro(
                                bitmapVertical,
                                boundingBox
                            )

                        if (recorte == null) {
                            coincidenciasConsecutivas = 0
                            mostrarEstadoCalidad(
                                "No se pudo obtener el rostro"
                            )
                            return@addOnSuccessListener
                        }

                        val calidad =
                            validarCalidadRostro(
                                rostro,
                                recorte.bitmap
                            )

                        if (!calidad.first) {
                            coincidenciasConsecutivas = 0
                            mostrarEstadoCalidad(
                                calidad.second
                            )
                            return@addOnSuccessListener
                        }

                        val rostroAlineado =
                            alinearRostroCanonico(
                                recorte,
                                rostro
                            )

                        if (rostroAlineado == null) {
                            coincidenciasConsecutivas = 0
                            mostrarEstadoCalidad(
                                "No se pudo alinear el rostro"
                            )
                            return@addOnSuccessListener
                        }

                        // FaceEmbeddingManager ArcFace ya devuelve el vector L2-normalizado.
                        val embeddingCapturado =
                            faceEmbeddingManager.generarEmbedding(
                                rostroAlineado
                            )

                        val registrado =
                            embeddingRegistrado
                                ?: return@addOnSuccessListener

                        if (
                            embeddingCapturado.size !=
                            registrado.size
                        ) {
                            coincidenciasConsecutivas = 0

                            Log.e(
                                TAG_ARCFACE,
                                "Dimensión incompatible | " +
                                        "capturado=${embeddingCapturado.size} | " +
                                        "registrado=${registrado.size}"
                            )

                            mostrarErrorFacial(
                                "Modelo facial incompatible"
                            )
                            return@addOnSuccessListener
                        }

                        val similitud =
                            FaceComparator.similitudCoseno(
                                embeddingCapturado,
                                registrado
                            )

                        val distancia =
                            FaceComparator.distanciaEuclidiana(
                                embeddingCapturado,
                                registrado
                            )

                        val brillo =
                            calcularBrilloPromedio(
                                recorte.bitmap
                            )

                        Log.d(
                            TAG_ARCFACE,
                            "Código=$codigoUsuario | " +
                                    "Coseno=$similitud | " +
                                    "Euclidiana=$distancia | " +
                                    "Dim=${embeddingCapturado.size} | " +
                                    "Norma=${calcularNormaEmbedding(embeddingCapturado)} | " +
                                    "Brillo=$brillo | " +
                                    "X=${rostro.headEulerAngleX} | " +
                                    "Y=${rostro.headEulerAngleY} | " +
                                    "Z=${rostro.headEulerAngleZ}"
                        )

                        // La decisión usa coseno. La euclidiana queda registrada
                        // para diagnóstico/calibración, ya que con embeddings L2
                        // ambas medidas están fuertemente relacionadas.
                        val coincide =
                            similitud >= umbralCosenoPrueba

                        if (coincide) {
                            ultimoFrameValido =
                                bitmapVertical.copy(
                                    Bitmap.Config.ARGB_8888,
                                    false
                                )
                        }

                        procesarResultadoFacial(
                            similitud,
                            distancia
                        )

                    } catch (e: Exception) {
                        coincidenciasConsecutivas = 0

                        Log.e(
                            TAG_ARCFACE,
                            "Error procesando rostro",
                            e
                        )
                    }
                }
                .addOnFailureListener { e ->
                    coincidenciasConsecutivas = 0

                    Log.e(
                        TAG_ARCFACE,
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
            imageProxy.close()

            Log.e(
                TAG_ARCFACE,
                "Error analizando frame",
                e
            )
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

        val coincide =
            similitud >= umbralCosenoPrueba

        Log.d(
            TAG_ARCFACE,
            "VALIDACIÓN | " +
                    "Coseno=$similitud | " +
                    "UmbralCoseno=$umbralCosenoPrueba | " +
                    "Euclidiana=$distancia | " +
                    "Coincide=$coincide"
        )

        if (coincide) {
            coincidenciasConsecutivas++

            Log.d(
                TAG_ARCFACE,
                "Coincidencias=$coincidenciasConsecutivas/" +
                        "$coincidenciasRequeridas"
            )

            runOnUiThread {
                faceOverlay.cambiarColorBorde(
                    colorCorrecto
                )

                txtEstado.text =
                    "Validando identidad..."

                txtEstado.setTextColor(
                    colorCorrecto
                )

                cambiarColorIndicador(
                    colorCorrecto
                )

                txtTituloIdentidad.text =
                    "Verificando identidad"


                txtDescripcion.text =
                    "Mantén el rostro estable durante unos segundos."

                txtDescripcion.visibility =
                    View.VISIBLE
            }

            if (
                coincidenciasConsecutivas >=
                coincidenciasRequeridas
            ) {
                identidadConfirmada = true

                Log.d(
                    TAG_ARCFACE,
                    "IDENTIDAD CONFIRMADA | Coseno=$similitud"
                )

                congelarUltimoFrame()
                mostrarRostroReconocido()
            }

        } else {
            coincidenciasConsecutivas = 0
            mostrarNoReconocido()
        }
    }


    // =========================================================
    // RECONOCIDO
    // =========================================================

    private fun mostrarRostroReconocido() {

        runOnUiThread {


            // =====================================================
            // ÓVALO VERDE
            // =====================================================

            faceOverlay
                .cambiarColorBorde(
                    colorCorrecto
                )


            // =====================================================
            // ESTADO
            // =====================================================

            txtEstado.text =
                "Identidad verificada"

            txtEstado.setTextColor(
                colorCorrecto
            )


            cambiarColorIndicador(
                colorCorrecto
            )


            // =====================================================
            // TARJETA
            // =====================================================

            txtTituloIdentidad.visibility =
                View.GONE


            txtDescripcion.visibility =
                View.GONE


            // =====================================================
            // DATOS
            // =====================================================

            if (
                datosPersonalCargados
            ) {

                mostrarDatosPersonal()

            } else {

                txtDni.text =
                    "DNI: buscando..."

                txtNombre.text =
                    "Nombre: buscando..."
            }


            // =====================================================
            // BOTÓN
            // =====================================================

            btnValidar.isEnabled =
                true
        }
    }


    // =========================================================
    // NO RECONOCIDO
    // =========================================================

    private fun mostrarNoReconocido() {

        if (
            identidadConfirmada
        ) {

            return
        }


        runOnUiThread {

            faceOverlay
                .cambiarColorBorde(
                    colorError
                )


            txtEstado.text =
                "Rostro no reconocido"

            txtEstado.setTextColor(
                colorError
            )


            cambiarColorIndicador(
                colorError
            )


            txtTituloIdentidad.text =
                "No se pudo verificar"


            txtDescripcion.text =
                "Mantén el rostro dentro del óvalo e inténtalo nuevamente."

            txtDescripcion.visibility =
                View.VISIBLE


            txtDni.text =
                "DNI: --"

            txtNombre.text =
                "Nombre: --"


            btnValidar.isEnabled =
                false
        }
    }


    // =========================================================
    // ESPERANDO
    // =========================================================

    private fun mostrarEstadoEsperando(
        mensaje: String
    ) {

        if (
            identidadConfirmada
        ) {

            return
        }


        runOnUiThread {

            faceOverlay
                .cambiarColorBorde(
                    colorEsperando
                )


            txtEstado.text =
                mensaje

            txtEstado.setTextColor(
                colorEsperando
            )


            cambiarColorIndicador(
                colorEsperando
            )


            txtTituloIdentidad.text =
                "Verificación de identidad"

            txtTituloIdentidad.visibility =
                View.VISIBLE


            txtDescripcion.text =
                "Mantén el rostro de frente y con buena iluminación."

            txtDescripcion.visibility =
                View.VISIBLE


            txtDni.text =
                "DNI: --"

            txtNombre.text =
                "Nombre: --"


            btnValidar.isEnabled =
                false
        }
    }


    // =========================================================
    // PROBLEMA DE CALIDAD
    // =========================================================

    private fun mostrarEstadoCalidad(
        mensaje: String
    ) {

        if (
            identidadConfirmada
        ) {

            return
        }


        runOnUiThread {

            faceOverlay
                .cambiarColorBorde(
                    colorCalidad
                )


            txtEstado.text =
                mensaje

            txtEstado.setTextColor(
                colorCalidad
            )


            cambiarColorIndicador(
                colorCalidad
            )


            txtTituloIdentidad.text =
                "Ajusta tu rostro"


            txtDescripcion.text =
                "Sigue la indicación para continuar con la verificación."

            txtDescripcion.visibility =
                View.VISIBLE


            txtDni.text =
                "DNI: --"

            txtNombre.text =
                "Nombre: --"


            btnValidar.isEnabled =
                false
        }
    }


    // =========================================================
    // CONGELAR ÚLTIMO FRAME CORRECTO
    // =========================================================

    private fun congelarUltimoFrame() {

        val frame =
            ultimoFrameValido
                ?: return


        runOnUiThread {

            try {

                imgRostroConfirmado
                    .setImageBitmap(
                        frame
                    )


                imgRostroConfirmado.visibility =
                    View.VISIBLE


                /*
                 * Ocultamos la cámara viva.
                 */
                previewCamara.visibility =
                    View.INVISIBLE


                /*
                 * El overlay queda por encima.
                 */
                faceOverlay
                    .cambiarColorBorde(
                        colorCorrecto
                    )


            } catch (
                e: Exception
            ) {

                Log.e(
                    "GURKAS_FACIAL",
                    "Error congelando cámara",
                    e
                )
            }
        }
    }


    // =========================================================
    // MISMO ÓVALO DEL REGISTRO
    // =========================================================

    private fun rostroDentroDelOval(
        boundingBox: Rect,
        imageWidth: Int,
        imageHeight: Int
    ): Boolean {

        if (
            imageWidth <= 0 ||
            imageHeight <= 0
        ) {

            return false
        }


        // =====================================================
        // MISMA GEOMETRÍA
        // =====================================================

        val anchoOval =
            imageWidth *
                    0.72f


        val altoOval =
            anchoOval *
                    1.35f


        val izquierda =
            (
                    imageWidth -
                            anchoOval
                    ) /
                    2f


        val arriba =
            imageHeight *
                    0.12f


        val derecha =
            izquierda +
                    anchoOval


        val abajo =
            arriba +
                    altoOval


        // =====================================================
        // CENTRO DEL ROSTRO
        // =====================================================

        val centroRostroX =
            boundingBox
                .exactCenterX()


        val centroRostroY =
            boundingBox
                .exactCenterY()


        // =====================================================
        // CENTRO DEL ÓVALO
        // =====================================================

        val centroOvalX =
            (
                    izquierda +
                            derecha
                    ) /
                    2f


        val centroOvalY =
            (
                    arriba +
                            abajo
                    ) /
                    2f


        val radioX =
            anchoOval /
                    2f


        val radioY =
            altoOval /
                    2f


        // =====================================================
        // ECUACIÓN ELIPSE
        // =====================================================

        val dx =
            (
                    centroRostroX -
                            centroOvalX
                    ) /
                    radioX


        val dy =
            (
                    centroRostroY -
                            centroOvalY
                    ) /
                    radioY


        val centroDentro =
            (
                    dx * dx +
                            dy * dy
                    ) <=
                    1f


        // =====================================================
        // TAMAÑO
        // =====================================================

        val tamañoCorrecto =
            boundingBox.width() >
                    anchoOval *
                    0.35f &&

                    boundingBox.height() >
                    altoOval *
                    0.35f &&

                    boundingBox.width() <
                    anchoOval *
                    0.90f &&

                    boundingBox.height() <
                    altoOval *
                    0.90f


        return (
                centroDentro &&
                        tamañoCorrecto
                )
    }


    // =========================================================
    // CALIDAD DEL ROSTRO
    // =========================================================

    private fun validarCalidadRostro(
        rostro: Face,
        rostroBitmap: Bitmap
    ): Pair<Boolean, String> {

        // =====================================================
        // IZQUIERDA / DERECHA
        // =====================================================

        val giroHorizontal =
            abs(
                rostro.headEulerAngleY
            )


        // =====================================================
        // INCLINACIÓN LATERAL
        // =====================================================

        val inclinacion =
            abs(
                rostro.headEulerAngleZ
            )


        // =====================================================
        // ARRIBA / ABAJO
        // =====================================================

        val giroVertical =
            abs(
                rostro.headEulerAngleX
            )


        if (
            giroHorizontal > 15f
        ) {

            return Pair(
                false,
                "Mire de frente a la cámara"
            )
        }


        if (
            inclinacion > 12f
        ) {

            return Pair(
                false,
                "Mantenga la cabeza recta"
            )
        }


        if (
            giroVertical > 15f
        ) {

            return Pair(
                false,
                "Mantenga el rostro de frente"
            )
        }


        // =====================================================
        // ILUMINACIÓN
        // =====================================================

        val brillo =
            calcularBrilloPromedio(
                rostroBitmap
            )


        Log.d(
            "GURKAS_FACIAL",
            "Brillo=$brillo"
        )


        if (
            brillo < 50f
        ) {

            return Pair(
                false,
                "Hay poca iluminación"
            )
        }


        if (
            brillo > 215f
        ) {

            return Pair(
                false,
                "Hay demasiada iluminación"
            )
        }


        return Pair(
            true,
            "Rostro válido"
        )
    }


    // =========================================================
    // BRILLO PROMEDIO
    // =========================================================

    private fun calcularBrilloPromedio(
        bitmap: Bitmap
    ): Float {

        if (
            bitmap.width <= 0 ||
            bitmap.height <= 0
        ) {

            return 0f
        }


        var suma =
            0.0


        var cantidad =
            0


        /*
         * Muestreamos cada 4 píxeles.
         *
         * No necesitamos recorrer
         * absolutamente todos.
         */
        val paso =
            4


        for (
        y in 0 until bitmap.height step paso
        ) {

            for (
            x in 0 until bitmap.width step paso
            ) {

                val pixel =
                    bitmap.getPixel(
                        x,
                        y
                    )


                val r =
                    Color.red(
                        pixel
                    )


                val g =
                    Color.green(
                        pixel
                    )


                val b =
                    Color.blue(
                        pixel
                    )


                val luminancia =
                    0.299 * r +
                            0.587 * g +
                            0.114 * b


                suma +=
                    luminancia


                cantidad++
            }
        }


        if (
            cantidad == 0
        ) {

            return 0f
        }


        return (
                suma /
                        cantidad
                ).toFloat()
    }


    // =========================================================
    // ALINEACIÓN
    // =========================================================

    private fun tieneLandmarksNecesarios(
        rostro: Face
    ): Boolean {

        return rostro.getLandmark(
            FaceLandmark.LEFT_EYE
        ) != null &&
                rostro.getLandmark(
                    FaceLandmark.RIGHT_EYE
                ) != null &&
                rostro.getLandmark(
                    FaceLandmark.NOSE_BASE
                ) != null
    }


    // =========================================================
    // ALINEACIÓN CANÓNICA ARCFACE 112x112
    // =========================================================

    private fun alinearRostroCanonico(
        recorte: RecorteRostro,
        rostro: Face
    ): Bitmap? {

        return try {
            val ojoIzquierdo =
                rostro.getLandmark(
                    FaceLandmark.LEFT_EYE
                )?.position ?: return null

            val ojoDerecho =
                rostro.getLandmark(
                    FaceLandmark.RIGHT_EYE
                )?.position ?: return null

            val nariz =
                rostro.getLandmark(
                    FaceLandmark.NOSE_BASE
                )?.position ?: return null

            // ML Kit y el bitmap vertical están en el mismo sistema de coordenadas.
            // Restamos el origen del recorte para llevar los landmarks al ROI.
            val origen =
                floatArrayOf(
                    ojoIzquierdo.x - recorte.left,
                    ojoIzquierdo.y - recorte.top,
                    ojoDerecho.x - recorte.left,
                    ojoDerecho.y - recorte.top,
                    nariz.x - recorte.left,
                    nariz.y - recorte.top
                )

            val destino =
                floatArrayOf(
                    38.2946f,
                    51.6963f,
                    73.5318f,
                    51.5014f,
                    56.0252f,
                    71.7366f
                )

            for (i in origen.indices step 2) {
                val x = origen[i]
                val y = origen[i + 1]

                if (
                    x < 0f ||
                    y < 0f ||
                    x >= recorte.bitmap.width ||
                    y >= recorte.bitmap.height
                ) {
                    return null
                }
            }

            val matrix =
                Matrix()

            val transformacionValida =
                matrix.setPolyToPoly(
                    origen,
                    0,
                    destino,
                    0,
                    3
                )

            if (!transformacionValida) {
                return null
            }

            val resultado =
                Bitmap.createBitmap(
                    TAMANO_ROSTRO_CANONICO,
                    TAMANO_ROSTRO_CANONICO,
                    Bitmap.Config.ARGB_8888
                )

            val canvas =
                Canvas(resultado)

            canvas.drawColor(
                Color.BLACK
            )

            val paint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG or
                            Paint.FILTER_BITMAP_FLAG
                )

            canvas.drawBitmap(
                recorte.bitmap,
                matrix,
                paint
            )

            resultado

        } catch (e: Exception) {
            Log.e(
                TAG_ARCFACE,
                "Error alineando rostro",
                e
            )

            null
        }
    }


    // =========================================================
    // RECORTAR ROSTRO
    // =========================================================

    private data class RecorteRostro(
        val bitmap: Bitmap,
        val left: Int,
        val top: Int
    )


    // =========================================================
    // RECORTAR ROSTRO
    // =========================================================

    private fun recortarRostro(
        bitmap: Bitmap,
        boundingBox: Rect
    ): RecorteRostro? {

        return try {
            // Igual que registro: 15 % horizontal y 20 % vertical.
            val margenX =
                (boundingBox.width() * 0.15f)
                    .toInt()

            val margenY =
                (boundingBox.height() * 0.20f)
                    .toInt()

            val izquierda =
                (boundingBox.left - margenX)
                    .coerceAtLeast(0)

            val arriba =
                (boundingBox.top - margenY)
                    .coerceAtLeast(0)

            val derecha =
                (boundingBox.right + margenX)
                    .coerceAtMost(bitmap.width)

            val abajo =
                (boundingBox.bottom + margenY)
                    .coerceAtMost(bitmap.height)

            val ancho =
                derecha - izquierda

            val alto =
                abajo - arriba

            if (ancho <= 0 || alto <= 0) {
                null
            } else {
                RecorteRostro(
                    bitmap = Bitmap.createBitmap(
                        bitmap,
                        izquierda,
                        arriba,
                        ancho,
                        alto
                    ),
                    left = izquierda,
                    top = arriba
                )
            }

        } catch (e: Exception) {
            Log.e(
                TAG_ARCFACE,
                "Error recortando rostro",
                e
            )

            null
        }
    }


    // =========================================================
    // NORMALIZACIÓN L2
    // =========================================================

    private fun normalizarEmbedding(
        embedding: FloatArray
    ): FloatArray {

        if (
            embedding.isEmpty()
        ) {

            return embedding
        }


        var suma =
            0.0


        for (
        valor in embedding
        ) {

            suma +=
                valor *
                        valor
        }


        val norma =
            sqrt(
                suma
            )
                .toFloat()


        if (
            norma <= 0f
        ) {

            return embedding
        }


        return FloatArray(
            embedding.size
        ) { indice ->

            embedding[indice] /
                    norma
        }
    }


    // =========================================================
    // NORMA DEL EMBEDDING (SOLO DIAGNÓSTICO)
    // =========================================================

    private fun calcularNormaEmbedding(
        embedding: FloatArray
    ): Float {

        var suma = 0.0

        for (valor in embedding) {
            suma +=
                valor.toDouble() *
                        valor.toDouble()
        }

        return sqrt(suma).toFloat()
    }


    // =========================================================
    // ROTAR BITMAP
    // =========================================================

    private fun rotarBitmap(
        bitmap: Bitmap,
        grados: Float
    ): Bitmap {

        if (
            grados == 0f
        ) {

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
    // OBTENER DATOS PERSONAL
    // =========================================================

    private fun obtenerDatosPersonal() {

        if (
            consultandoDatosPersonal
        ) {

            return
        }


        consultandoDatosPersonal =
            true


        Thread {

            var conexion:
                    HttpURLConnection? = null


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
                    URL(
                        direccion
                    )
                        .openConnection()
                            as HttpURLConnection


                conexion.requestMethod =
                    "GET"


                conexion.connectTimeout =
                    15000


                conexion.readTimeout =
                    15000


                conexion
                    .setRequestProperty(
                        "Accept",
                        "application/json"
                    )


                val codigoHttp =
                    conexion.responseCode


                if (
                    codigoHttp !in
                    200..299
                ) {

                    consultandoDatosPersonal =
                        false


                    Log.e(
                        "GURKAS_PERSONAL",
                        "HTTP=$codigoHttp"
                    )


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
                    textoJson
                )


                val raiz =
                    JSONObject(
                        textoJson
                    )


                val lista =
                    raiz
                        .optJSONArray(
                            "lista"
                        )


                if (
                    lista == null ||
                    lista.length() == 0
                ) {

                    consultandoDatosPersonal =
                        false


                    return@Thread
                }


                val personal =
                    lista
                        .getJSONObject(
                            0
                        )


                dniEmpleado =
                    personal
                        .optString(
                            "doctidentidad",
                            ""
                        )
                        .trim()


                nombreEmpleado =
                    personal
                        .optString(
                            "nombrecompleto",
                            ""
                        )
                        .trim()


                datosPersonalCargados =
                    true


                consultandoDatosPersonal =
                    false


                Log.d(
                    "GURKAS_PERSONAL",
                    "DNI=$dniEmpleado | " +
                            "Nombre=$nombreEmpleado"
                )


                /*
                 * Si justo terminó de cargar después
                 * del reconocimiento, mostramos los datos.
                 */
                if (
                    identidadConfirmada
                ) {

                    runOnUiThread {

                        mostrarDatosPersonal()
                    }
                }


            } catch (
                e: Exception
            ) {

                consultandoDatosPersonal =
                    false


                Log.e(
                    "GURKAS_PERSONAL",
                    "Error obteniendo personal",
                    e
                )


            } finally {

                conexion
                    ?.disconnect()
            }

        }.start()
    }


    // =========================================================
    // MOSTRAR DATOS
    // =========================================================

    private fun mostrarDatosPersonal() {

        if (
            !identidadConfirmada
        ) {

            return
        }


        txtDni.text =
            if (
                dniEmpleado.isBlank()
            ) {

                "DNI: --"

            } else {

                "DNI: $dniEmpleado"
            }


        txtNombre.text =
            if (
                nombreEmpleado.isBlank()
            ) {

                "Nombre: --"

            } else {

                "Nombre: $nombreEmpleado"
            }
    }


    // =========================================================
    // IR A RESULTADO VALIDACIÓN
    // =========================================================

    private fun irAValidacion() {

        if (
            !identidadConfirmada
        ) {

            Toast.makeText(
                this,
                "Primero debe validar su rostro.",
                Toast.LENGTH_SHORT
            ).show()


            return
        }


        val intent =
            Intent(
                this,
                ResultadoValidacionActivity::class.java
            )


        intent.putExtra(
            "codigo",
            codigoUsuario
        )


        intent.putExtra(
            "dni",
            dniEmpleado
        )


        intent.putExtra(
            "nombre",
            nombreEmpleado
        )


        startActivity(
            intent
        )
    }


    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        super.onDestroy()


        try {

            if (
                ::faceDetector.isInitialized
            ) {

                faceDetector.close()
            }


        } catch (
            _: Exception
        ) {
        }


        try {

            if (
                ::faceEmbeddingManager.isInitialized
            ) {

                faceEmbeddingManager.cerrar()
            }


        } catch (
            _: Exception
        ) {
        }


        try {

            if (
                ::cameraExecutor.isInitialized
            ) {

                cameraExecutor.shutdown()
            }


        } catch (
            _: Exception
        ) {
        }


        ultimoFrameValido =
            null


        embeddingRegistrado =
            null
    }
}