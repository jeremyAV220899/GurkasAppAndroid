package com.cloud.gurkasapp.facerecognition

import kotlin.math.sqrt

object FaceComparator {

    fun similitudCoseno(
        embedding1: FloatArray,
        embedding2: FloatArray
    ): Float {

        if (embedding1.size != embedding2.size) {
            return -1f
        }

        if (embedding1.isEmpty()) {
            return -1f
        }

        var productoPunto = 0f
        var norma1 = 0f
        var norma2 = 0f

        for (i in embedding1.indices) {

            val valor1 =
                embedding1[i]

            val valor2 =
                embedding2[i]

            productoPunto +=
                valor1 * valor2

            norma1 +=
                valor1 * valor1

            norma2 +=
                valor2 * valor2
        }

        if (
            norma1 == 0f ||
            norma2 == 0f
        ) {
            return -1f
        }

        return productoPunto /
                (
                        sqrt(norma1) *
                                sqrt(norma2)
                        )
    }


    fun distanciaEuclidiana(
        embedding1: FloatArray,
        embedding2: FloatArray
    ): Float {

        if (embedding1.size != embedding2.size) {
            return Float.MAX_VALUE
        }

        if (embedding1.isEmpty()) {
            return Float.MAX_VALUE
        }

        var suma = 0f

        for (i in embedding1.indices) {

            val diferencia =
                embedding1[i] -
                        embedding2[i]

            suma +=
                diferencia * diferencia
        }

        return sqrt(suma)
    }
}