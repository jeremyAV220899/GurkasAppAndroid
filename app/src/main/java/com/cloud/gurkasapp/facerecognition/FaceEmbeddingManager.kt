package com.cloud.gurkasapp.facerecognition

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class FaceEmbeddingManager(
    context: Context
) {

    private val nombreModelo =
        "mobile_face_net.tflite"

    private val interpreter: Interpreter

    val inputWidth: Int
    val inputHeight: Int
    val embeddingDimension: Int


    init {

        val modelo =
            cargarModelo(
                context,
                nombreModelo
            )


        val opciones =
            Interpreter.Options().apply {

                setNumThreads(4)
            }


        interpreter =
            Interpreter(
                modelo,
                opciones
            )


        // =============================================
        // LEER DIMENSIONES DE ENTRADA
        // =============================================

        val inputShape =
            interpreter
                .getInputTensor(0)
                .shape()


        if (
            inputShape.size != 4 ||
            inputShape[0] != 1 ||
            inputShape[3] != 3
        ) {

            throw IllegalStateException(
                "Formato de entrada no soportado: " +
                        inputShape.contentToString()
            )
        }


        inputHeight =
            inputShape[1]


        inputWidth =
            inputShape[2]


        // =============================================
        // LEER DIMENSIÓN DEL EMBEDDING
        // =============================================

        val outputShape =
            interpreter
                .getOutputTensor(0)
                .shape()


        if (
            outputShape.size != 2 ||
            outputShape[0] != 1
        ) {

            throw IllegalStateException(
                "Formato de salida no soportado: " +
                        outputShape.contentToString()
            )
        }


        embeddingDimension =
            outputShape[1]
    }


    // =================================================
    // CARGAR MODELO TFLITE
    // =================================================

    private fun cargarModelo(
        context: Context,
        nombre: String
    ): ByteBuffer {

        val descriptor =
            context
                .assets
                .openFd(nombre)


        FileInputStream(
            descriptor.fileDescriptor
        ).use { inputStream ->

            val canal =
                inputStream.channel


            return canal.map(
                FileChannel.MapMode.READ_ONLY,
                descriptor.startOffset,
                descriptor.declaredLength
            )
        }
    }


    // =================================================
    // GENERAR EMBEDDING DEL ROSTRO
    // =================================================

    fun generarEmbedding(
        rostroBitmap: Bitmap
    ): FloatArray {

        // =============================================
        // ESCALAR AL TAMAÑO QUE PIDE EL MODELO
        // =============================================

        val bitmapEscalado =
            Bitmap.createScaledBitmap(
                rostroBitmap,
                inputWidth,
                inputHeight,
                true
            )


        // =============================================
        // BUFFER FLOAT32
        // =============================================

        val inputBuffer =
            ByteBuffer.allocateDirect(
                inputWidth *
                        inputHeight *
                        3 *
                        4
            )


        inputBuffer.order(
            ByteOrder.nativeOrder()
        )


        inputBuffer.rewind()


        // =============================================
        // OBTENER PIXELES
        // =============================================

        val pixels =
            IntArray(
                inputWidth *
                        inputHeight
            )


        bitmapEscalado.getPixels(
            pixels,
            0,
            inputWidth,
            0,
            0,
            inputWidth,
            inputHeight
        )


        var pixelIndex = 0


        // =============================================
        // NORMALIZACIÓN RGB
        //
        // (valor - 127.5) / 127.5
        //
        // rango aproximado:
        // -1 a 1
        // =============================================

        for (
        y in 0 until inputHeight
        ) {

            for (
            x in 0 until inputWidth
            ) {

                val pixel =
                    pixels[
                        pixelIndex++
                    ]


                val r =
                    (
                            pixel shr 16
                            ) and 0xFF


                val g =
                    (
                            pixel shr 8
                            ) and 0xFF


                val b =
                    pixel and 0xFF


                inputBuffer.putFloat(
                    (r - 127.5f) /
                            127.5f
                )


                inputBuffer.putFloat(
                    (g - 127.5f) /
                            127.5f
                )


                inputBuffer.putFloat(
                    (b - 127.5f) /
                            127.5f
                )
            }
        }


        inputBuffer.rewind()


        // =============================================
        // SALIDA
        // =============================================

        val output =
            Array(1) {

                FloatArray(
                    embeddingDimension
                )
            }


        // =============================================
        // EJECUTAR MODELO
        // =============================================

        interpreter.run(
            inputBuffer,
            output
        )


        // =============================================
        // NORMALIZAR EMBEDDING
        // =============================================

        return normalizarL2(
            output[0]
        )
    }


    // =================================================
    // NORMALIZACIÓN L2
    // =================================================

    private fun normalizarL2(
        embedding: FloatArray
    ): FloatArray {

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
            norma == 0f
        ) {

            return embedding
        }


        val resultado =
            FloatArray(
                embedding.size
            )


        for (
        i in embedding.indices
        ) {

            resultado[i] =
                embedding[i] /
                        norma
        }


        return resultado
    }


    // =================================================
    // CERRAR MODELO
    // =================================================

    fun cerrar() {

        interpreter.close()
    }
}