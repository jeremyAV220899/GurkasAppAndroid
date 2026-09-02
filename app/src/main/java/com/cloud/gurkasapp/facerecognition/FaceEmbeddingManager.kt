package com.cloud.gurkasapp.facerecognition

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt


class FaceEmbeddingManager(
    private val context: Context
) {

    companion object {

        private const val TAG =
            "ARCFACE_MODEL"
    }


    // =========================================================
    // MODELO
    // =========================================================

    private val nombreModelo =
        "arcface.tflite"


    // =========================================================
    // INTERPRETER
    // =========================================================

    private var interpreter: Interpreter


    // =========================================================
    // DIMENSIONES DEL MODELO
    // =========================================================

    val inputWidth: Int

    val inputHeight: Int

    val embeddingDimension: Int


    // =========================================================
    // TIPOS
    // =========================================================

    private val inputDataType: DataType

    private val outputDataType: DataType


    // =========================================================
    // INIT
    // =========================================================

    init {

        // =====================================================
        // CARGAR MODELO
        // =====================================================

        val modelo =
            cargarModeloDesdeAssets(
                nombreModelo
            )


        // =====================================================
        // OPCIONES
        // =====================================================

        val opciones =
            Interpreter.Options()


        opciones.setNumThreads(
            4
        )


        // =====================================================
        // CREAR INTERPRETER
        // =====================================================

        interpreter =
            Interpreter(
                modelo,
                opciones
            )


        // =====================================================
        // TENSOR ENTRADA
        // =====================================================

        val inputTensor =
            interpreter
                .getInputTensor(
                    0
                )


        val inputShape =
            inputTensor.shape()


        inputDataType =
            inputTensor.dataType()


        if (
            inputShape.size != 4
        ) {

            throw IllegalStateException(
                "El modelo debe tener entrada [1, alto, ancho, 3]. " +
                        "Shape recibido: ${inputShape.contentToString()}"
            )
        }


        if (
            inputShape[0] != 1
        ) {

            throw IllegalStateException(
                "El modelo debe trabajar con batch 1."
            )
        }


        if (
            inputShape[3] != 3
        ) {

            throw IllegalStateException(
                "El modelo debe recibir imagen RGB de 3 canales."
            )
        }


        inputHeight =
            inputShape[1]


        inputWidth =
            inputShape[2]


        // =====================================================
        // TENSOR SALIDA
        // =====================================================

        val outputTensor =
            interpreter
                .getOutputTensor(
                    0
                )


        val outputShape =
            outputTensor.shape()


        outputDataType =
            outputTensor.dataType()


        if (
            outputShape.size != 2
        ) {

            throw IllegalStateException(
                "La salida debe tener forma [1, N]. " +
                        "Shape recibido: ${outputShape.contentToString()}"
            )
        }


        if (
            outputShape[0] != 1
        ) {

            throw IllegalStateException(
                "La salida debe tener batch 1."
            )
        }


        embeddingDimension =
            outputShape[1]


        // =====================================================
        // VALIDAR FLOAT32
        // =====================================================

        if (
            inputDataType !=
            DataType.FLOAT32
        ) {

            throw IllegalStateException(
                "La entrada del modelo debe ser FLOAT32. " +
                        "Tipo recibido: $inputDataType"
            )
        }


        if (
            outputDataType !=
            DataType.FLOAT32
        ) {

            throw IllegalStateException(
                "La salida del modelo debe ser FLOAT32. " +
                        "Tipo recibido: $outputDataType"
            )
        }


        // =====================================================
        // LOG
        // =====================================================

        Log.d(
            TAG,
            "Modelo=$nombreModelo"
        )


        Log.d(
            TAG,
            "Entrada=${inputWidth}x${inputHeight}"
        )


        Log.d(
            TAG,
            "Dimensión embedding=$embeddingDimension"
        )


        Log.d(
            TAG,
            "InputType=$inputDataType | OutputType=$outputDataType"
        )
    }


    // =========================================================
    // CARGAR MODELO DESDE ASSETS
    // =========================================================

    private fun cargarModeloDesdeAssets(
        nombreArchivo: String
    ): ByteBuffer {

        val fileDescriptor =
            context
                .assets
                .openFd(
                    nombreArchivo
                )


        val inputStream =
            FileInputStream(
                fileDescriptor.fileDescriptor
            )


        val fileChannel =
            inputStream.channel


        val startOffset =
            fileDescriptor.startOffset


        val declaredLength =
            fileDescriptor.declaredLength


        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
    }


    // =========================================================
    // GENERAR EMBEDDING
    // =========================================================

    fun generarEmbedding(
        bitmap: Bitmap
    ): FloatArray {

        if (
            bitmap.width <= 0 ||
            bitmap.height <= 0
        ) {

            throw IllegalArgumentException(
                "Bitmap inválido."
            )
        }


        // =====================================================
        // ADAPTAR AL TAMAÑO DEL MODELO
        // =====================================================

        val bitmapEscalado =
            if (
                bitmap.width ==
                inputWidth &&
                bitmap.height ==
                inputHeight
            ) {

                bitmap

            } else {

                Bitmap.createScaledBitmap(
                    bitmap,
                    inputWidth,
                    inputHeight,
                    true
                )
            }


        try {

            // =================================================
            // BUFFER FLOAT32
            //
            // 4 bytes por float
            // RGB = 3 canales
            // =================================================

            val inputBuffer =
                ByteBuffer
                    .allocateDirect(
                        1 *
                                inputWidth *
                                inputHeight *
                                3 *
                                4
                    )


            inputBuffer.order(
                ByteOrder.nativeOrder()
            )


            inputBuffer.rewind()


            // =================================================
            // LEER PIXELES
            // =================================================

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


            // =================================================
            // RGB -> FLOAT
            //
            // Normalización:
            //
            // (pixel - 127.5) / 127.5
            //
            // resultado:
            // -1.0 ... +1.0
            // =================================================

            var index =
                0


            for (
            y in 0 until inputHeight
            ) {

                for (
                x in 0 until inputWidth
                ) {

                    val pixel =
                        pixels[index++]

                    val r =
                        (
                                (
                                        pixel shr 16
                                        ) and 0xFF
                                )

                    val g =
                        (
                                (
                                        pixel shr 8
                                        ) and 0xFF
                                )

                    val b =
                        (
                                pixel and 0xFF
                                )


                    val rf =
                        (
                                r -
                                        127.5f
                                ) /
                                127.5f


                    val gf =
                        (
                                g -
                                        127.5f
                                ) /
                                127.5f


                    val bf =
                        (
                                b -
                                        127.5f
                                ) /
                                127.5f


                    inputBuffer.putFloat(
                        rf
                    )

                    inputBuffer.putFloat(
                        gf
                    )

                    inputBuffer.putFloat(
                        bf
                    )
                }
            }


            inputBuffer.rewind()


            // =================================================
            // SALIDA
            // =================================================

            val output =
                Array(
                    1
                ) {

                    FloatArray(
                        embeddingDimension
                    )
                }


            // =================================================
            // EJECUTAR
            // =================================================

            interpreter.run(
                inputBuffer,
                output
            )


            val embedding =
                output[0]


            // =================================================
            // VALIDAR
            // =================================================

            if (
                embedding.size !=
                embeddingDimension
            ) {

                throw IllegalStateException(
                    "Dimensión inesperada del embedding: " +
                            "${embedding.size}"
                )
            }


            for (
            valor in embedding
            ) {

                if (
                    valor.isNaN() ||
                    valor.isInfinite()
                ) {

                    throw IllegalStateException(
                        "El embedding contiene NaN o Infinity."
                    )
                }
            }


            // =================================================
            // L2
            // =================================================

            val normalizado =
                normalizarL2(
                    embedding
                )


            Log.d(
                TAG,
                "Embedding generado | " +
                        "Dim=${normalizado.size} | " +
                        "Norma=${calcularNorma(normalizado)}"
            )


            return normalizado


        } finally {

            if (
                bitmapEscalado !== bitmap &&
                !bitmapEscalado.isRecycled
            ) {

                bitmapEscalado.recycle()
            }
        }
    }


    // =========================================================
    // NORMALIZACIÓN L2
    // =========================================================

    private fun normalizarL2(
        embedding: FloatArray
    ): FloatArray {

        if (
            embedding.isEmpty()
        ) {

            throw IllegalArgumentException(
                "Embedding vacío."
            )
        }


        var suma =
            0.0


        for (
        valor in embedding
        ) {

            suma +=
                valor.toDouble() *
                        valor.toDouble()
        }


        val norma =
            sqrt(
                suma
            )


        if (
            norma <=
            1e-12
        ) {

            throw IllegalStateException(
                "La norma del embedding es cero."
            )
        }


        return FloatArray(
            embedding.size
        ) { i ->

            (
                    embedding[i] /
                            norma
                    ).toFloat()
        }
    }


    // =========================================================
    // NORMA
    // =========================================================

    private fun calcularNorma(
        embedding: FloatArray
    ): Float {

        var suma =
            0.0


        for (
        valor in embedding
        ) {

            suma +=
                valor.toDouble() *
                        valor.toDouble()
        }


        return sqrt(
            suma
        ).toFloat()
    }


    // =========================================================
    // PROMEDIAR EMBEDDINGS
    //
    // En reconocimiento normalmente no lo necesitaremos,
    // pero lo dejamos idéntico al registro.
    // =========================================================

    fun promediarEmbeddings(
        embeddings: List<FloatArray>
    ): FloatArray {

        if (
            embeddings.isEmpty()
        ) {

            throw IllegalArgumentException(
                "No existen embeddings para promediar."
            )
        }


        val dimension =
            embeddings[0]
                .size


        if (
            dimension <= 0
        ) {

            throw IllegalArgumentException(
                "Dimensión de embedding inválida."
            )
        }


        for (
        embedding in embeddings
        ) {

            if (
                embedding.size !=
                dimension
            ) {

                throw IllegalArgumentException(
                    "Todos los embeddings deben tener la misma dimensión."
                )
            }
        }


        val promedio =
            FloatArray(
                dimension
            )


        for (
        embedding in embeddings
        ) {

            for (
            i in embedding.indices
            ) {

                promedio[i] +=
                    embedding[i]
            }
        }


        val cantidad =
            embeddings.size
                .toFloat()


        for (
        i in promedio.indices
        ) {

            promedio[i] /=
                cantidad
        }


        return normalizarL2(
            promedio
        )
    }


    // =========================================================
    // SIMILITUD COSENO
    // =========================================================

    fun similitudCoseno(
        embedding1: FloatArray,
        embedding2: FloatArray
    ): Float {

        if (
            embedding1.size !=
            embedding2.size
        ) {

            throw IllegalArgumentException(
                "Los embeddings tienen dimensiones diferentes."
            )
        }


        var producto =
            0.0


        var norma1 =
            0.0


        var norma2 =
            0.0


        for (
        i in embedding1.indices
        ) {

            producto +=
                embedding1[i] *
                        embedding2[i]


            norma1 +=
                embedding1[i] *
                        embedding1[i]


            norma2 +=
                embedding2[i] *
                        embedding2[i]
        }


        val denominador =
            sqrt(
                norma1
            ) *
                    sqrt(
                        norma2
                    )


        if (
            denominador <=
            1e-12
        ) {

            return 0f
        }


        return (
                producto /
                        denominador
                ).toFloat()
    }


    // =========================================================
    // DISTANCIA EUCLIDIANA
    // =========================================================

    fun distanciaEuclidiana(
        embedding1: FloatArray,
        embedding2: FloatArray
    ): Float {

        if (
            embedding1.size !=
            embedding2.size
        ) {

            throw IllegalArgumentException(
                "Los embeddings tienen dimensiones diferentes."
            )
        }


        var suma =
            0.0


        for (
        i in embedding1.indices
        ) {

            val diferencia =
                embedding1[i] -
                        embedding2[i]


            suma +=
                diferencia *
                        diferencia
        }


        return sqrt(
            suma
        ).toFloat()
    }


    // =========================================================
    // CERRAR
    // =========================================================

    fun cerrar() {

        try {

            interpreter.close()

        } catch (
            e: Exception
        ) {

            Log.e(
                TAG,
                "Error cerrando Interpreter",
                e
            )
        }
    }
}