package com.cloud.gurkasapp.facerecognition

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


object FacePreprocessor {

    // =========================================================
    // RESULTADO DE CALIDAD
    // =========================================================

    data class ResultadoCalidad(
        val valido: Boolean,
        val mensaje: String
    )


    // =========================================================
    // VALIDAR CALIDAD DEL ROSTRO
    // =========================================================

    fun validarCalidad(
        rostro: Face,
        rostroBitmap: Bitmap
    ): ResultadoCalidad {

        // =====================================================
        // GIRO HORIZONTAL
        // =====================================================

        val giroHorizontal =
            abs(
                rostro.headEulerAngleY
            )


        // =====================================================
        // INCLINACIÓN
        // =====================================================

        val inclinacion =
            abs(
                rostro.headEulerAngleZ
            )


        // =====================================================
        // CABEZA ARRIBA / ABAJO
        // =====================================================

        val giroVertical =
            abs(
                rostro.headEulerAngleX
            )


        if (
            giroHorizontal > 15f
        ) {

            return ResultadoCalidad(
                false,
                "Mire de frente a la cámara"
            )
        }


        if (
            inclinacion > 12f
        ) {

            return ResultadoCalidad(
                false,
                "Mantenga la cabeza recta"
            )
        }


        if (
            giroVertical > 15f
        ) {

            return ResultadoCalidad(
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


        if (
            brillo < 50f
        ) {

            return ResultadoCalidad(
                false,
                "Hay poca iluminación"
            )
        }


        if (
            brillo > 215f
        ) {

            return ResultadoCalidad(
                false,
                "Hay demasiada iluminación"
            )
        }


        return ResultadoCalidad(
            true,
            "Rostro válido"
        )
    }


    // =========================================================
    // BRILLO PROMEDIO
    // =========================================================

    fun calcularBrilloPromedio(
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
         * No necesitamos recorrer absolutamente
         * todos los píxeles.
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
    // ALINEAR ROSTRO USANDO LOS OJOS
    // =========================================================

    fun alinearRostro(
        bitmapCompleto: Bitmap,
        rostro: Face
    ): Bitmap? {

        val ojoIzquierdo =
            rostro.getLandmark(
                FaceLandmark.LEFT_EYE
            )
                ?.position


        val ojoDerecho =
            rostro.getLandmark(
                FaceLandmark.RIGHT_EYE
            )
                ?.position


        /*
         * Si ML Kit no entrega ojos,
         * hacemos recorte normal.
         */
        if (
            ojoIzquierdo == null ||
            ojoDerecho == null
        ) {

            return recortarRostro(
                bitmapCompleto,
                rostro.boundingBox
            )
        }


        // =====================================================
        // ÁNGULO ENTRE LOS OJOS
        // =====================================================

        val deltaY =
            ojoDerecho.y -
                    ojoIzquierdo.y


        val deltaX =
            ojoDerecho.x -
                    ojoIzquierdo.x


        val angulo =
            Math.toDegrees(
                atan2(
                    deltaY.toDouble(),
                    deltaX.toDouble()
                )
            )
                .toFloat()


        // =====================================================
        // CENTRO ENTRE LOS OJOS
        // =====================================================

        val centroX =
            (
                    ojoIzquierdo.x +
                            ojoDerecho.x
                    ) /
                    2f


        val centroY =
            (
                    ojoIzquierdo.y +
                            ojoDerecho.y
                    ) /
                    2f


        // =====================================================
        // ROTAR IMAGEN
        // =====================================================

        val matrix =
            Matrix()


        matrix.postRotate(
            -angulo,
            centroX,
            centroY
        )


        val bitmapAlineado =
            Bitmap.createBitmap(
                bitmapCompleto,
                0,
                0,
                bitmapCompleto.width,
                bitmapCompleto.height,
                matrix,
                true
            )


        // =====================================================
        // RECORTAR DESPUÉS DE ALINEAR
        // =====================================================

        return recortarRostro(
            bitmapAlineado,
            rostro.boundingBox
        )
    }


    // =========================================================
    // RECORTAR ROSTRO
    // =========================================================

    fun recortarRostro(
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


        } catch (
            e: Exception
        ) {

            null
        }
    }


    // =========================================================
    // AJUSTAR A TAMAÑO FIJO
    // =========================================================

    fun redimensionar(
        bitmap: Bitmap,
        ancho: Int,
        alto: Int
    ): Bitmap {

        return Bitmap.createScaledBitmap(
            bitmap,
            ancho,
            alto,
            true
        )
    }


    // =========================================================
    // NORMALIZACIÓN L2 DEL EMBEDDING
    // =========================================================

    fun normalizarEmbedding(
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
}