package com.cloud.gurkasapp.facerecognition

object EmbeddingUtils {

    fun convertirStringAFloatArray(
        embeddingTexto: String
    ): FloatArray {

        val limpio =
            embeddingTexto
                .trim()
                .removePrefix("[")
                .removeSuffix("]")

        if (limpio.isBlank()) {
            return floatArrayOf()
        }

        return limpio
            .split(",")
            .map { valor ->
                valor.trim().toFloat()
            }
            .toFloatArray()
    }
}