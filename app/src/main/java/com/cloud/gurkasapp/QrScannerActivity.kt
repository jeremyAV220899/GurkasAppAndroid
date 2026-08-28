package com.cloud.gurkasapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class QrScannerActivity : ComponentActivity() {

    private lateinit var scanLine: View
    private lateinit var scannerFrame: View

    private var scanAnimator: ObjectAnimator? = null
    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    private var qrDetectado = false

    private val requestCameraPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                iniciarCamara()
            } else {
                Toast.makeText(
                    this,
                    "Se necesita permiso de cámara para escanear el QR",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // PRIMERO cargar el XML
        setContentView(R.layout.activity_qr_scanner)

        // DESPUÉS buscar las vistas
        previewView = findViewById(R.id.previewView)
        scanLine = findViewById(R.id.scanLine)
        scannerFrame = findViewById(R.id.scannerFrame)

        // Crear executor de cámara
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Botón cerrar
        findViewById<View>(R.id.btnCerrar).setOnClickListener {
            finish()
        }

        // Iniciar animación
        iniciarAnimacionScanner()

        // Verificar permiso y abrir cámara
        verificarPermisoCamara()
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    private fun iniciarAnimacionScanner() {

        scannerFrame.post {

            val limite =
                scannerFrame.height -
                        scanLine.height -
                        dpToPx(24)

            scanAnimator = ObjectAnimator.ofFloat(
                scanLine,
                View.TRANSLATION_Y,
                0f,
                limite.toFloat()
            ).apply {

                duration = 1800

                repeatCount = ValueAnimator.INFINITE

                repeatMode = ValueAnimator.REVERSE

                interpolator =
                    AccelerateDecelerateInterpolator()

                start()
            }
        }
    }
    private fun verificarPermisoCamara() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            iniciarCamara()

        } else {

            requestCameraPermission.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun iniciarCamara() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()

            val preview =
                Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider =
                            previewView.surfaceProvider
                    }

            val options =
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE
                    )
                    .build()

            val scanner =
                BarcodeScanning.getClient(options)

            val imageAnalysis =
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

            imageAnalysis.setAnalyzer(
                cameraExecutor
            ) { imageProxy ->

                val mediaImage =
                    imageProxy.image

                if (mediaImage == null) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val image =
                    InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                scanner.process(image)

                    .addOnSuccessListener { barcodes ->

                        if (!qrDetectado) {

                            for (barcode in barcodes) {

                                val contenido =
                                    barcode.rawValue

                                if (!contenido.isNullOrBlank()) {

                                    qrDetectado = true

                                    procesarQr(contenido)

                                    break
                                }
                            }
                        }
                    }

                    .addOnFailureListener {
                        // Ignorar este frame
                    }

                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }

            val cameraSelector =
                CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "No se pudo iniciar la cámara",
                    Toast.LENGTH_LONG
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun procesarQr(contenido: String) {

        runOnUiThread {

            Toast.makeText(
                this,
                "QR detectado: $contenido",
                Toast.LENGTH_LONG
            ).show()

            /*
             * AQUÍ REALIZARÁS TU MARCACIÓN.
             *
             * Por ejemplo:
             *
             * enviarMarcacion(contenido)
             *
             * No recomiendo confiar directamente
             * en cualquier URL/contenido recibido del QR.
             */
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        scanAnimator?.cancel()
        cameraExecutor.shutdown()
    }
}