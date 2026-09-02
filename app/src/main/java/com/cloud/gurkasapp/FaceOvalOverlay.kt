package com.cloud.gurkasapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class FaceOvalOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val fondoPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            color =
                Color.parseColor(
                    "#AA000000"
                )
        }

    private val limpiarPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            xfermode =
                PorterDuffXfermode(
                    PorterDuff.Mode.CLEAR
                )
        }

    private val bordePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            style =
                Paint.Style.STROKE

            strokeWidth =
                6f

            color =
                Color.WHITE
        }


    init {

        setLayerType(
            LAYER_TYPE_SOFTWARE,
            null
        )
    }


    override fun onDraw(
        canvas: Canvas
    ) {

        super.onDraw(canvas)


        val w =
            width.toFloat()

        val h =
            height.toFloat()


        // =====================================================
        // FONDO OSCURO
        // =====================================================

        canvas.drawRect(
            0f,
            0f,
            w,
            h,
            fondoPaint
        )


        // =====================================================
        // MISMO ÓVALO DEL REGISTRO
        // =====================================================

        val anchoOval =
            w * 0.72f


        val altoOval =
            anchoOval * 1.35f


        val izquierda =
            (w - anchoOval) /
                    2f


        val arriba =
            h * 0.12f


        val derecha =
            izquierda +
                    anchoOval


        val abajo =
            arriba +
                    altoOval


        val oval =
            RectF(
                izquierda,
                arriba,
                derecha,
                abajo
            )


        // =====================================================
        // HUECO TRANSPARENTE
        // =====================================================

        canvas.drawOval(
            oval,
            limpiarPaint
        )


        // =====================================================
        // BORDE
        // =====================================================

        canvas.drawOval(
            oval,
            bordePaint
        )
    }


    fun cambiarColorBorde(
        color: Int
    ) {

        bordePaint.color =
            color

        invalidate()
    }
}