package com.cloud.gurkasapp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class NoticiasFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_noticias,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // NOTICIA 1
        val imgNoticia1 = view.findViewById<ImageView>(R.id.imgNoticia1)
        val loadingNoticia1 = view.findViewById<View>(R.id.loadingNoticia1)
        val errorNoticia1 = view.findViewById<View>(R.id.errorNoticia1)
        val btnReintentar1 = view.findViewById<View>(R.id.btnReintentar1)
        val urlImagen1 =
            "https://scontent.flim3-2.fna.fbcdn.net/v/t39.30808-6/778543605_1706247601504379_5021565678769334473_n.jpg?stp=dst-jpg_tt6&cstp=mx1641x2048&ctp=s1641x2048&_nc_cat=102&ccb=1-7&_nc_sid=127cfc&_nc_ohc=HKapREQWcQEQ7kNvwHm3rWz&_nc_oc=Ados4jRq-SfzBJiMNoFdbfO_4HhkReovI7DMEJwZygA4p9Elnl-LZBmjkc8u16hL4L8&_nc_zt=23&_nc_ht=scontent.flim3-2.fna&_nc_gid=fNXUt0De_LV0ePuTZrGpQw&_nc_ss=7b2a8&oh=00_AQGFEg02ZHukzXQ3c5HEnDbl7eK6nxXl8jFkunTO3-DAHQ&oe=6A8D531D"

        cargarImagen(
            urlImagen = urlImagen1,
            imageView = imgNoticia1,
            loadingView = loadingNoticia1,
            errorView = errorNoticia1
        )

        btnReintentar1.setOnClickListener {
            cargarImagen(
                urlImagen = urlImagen1,
                imageView = imgNoticia1,
                loadingView = loadingNoticia1,
                errorView = errorNoticia1
            )
        }

        // NOTICIA 2
        val imgNoticia2 = view.findViewById<ImageView>(R.id.imgNoticia2)
        val loadingNoticia2 = view.findViewById<View>(R.id.loadingNoticia2)
        val errorNoticia2 = view.findViewById<View>(R.id.errorNoticia2)
        val btnReintentar2 = view.findViewById<View>(R.id.btnReintentar2)
        val urlImagen2 =
            "https://scontent.flim3-2.fna.fbcdn.net/v/t39.30808-6/778957725_1705220994940373_2564050184819553798_n.jpg?stp=dst-jpg_tt6&cstp=mx1639x2048&ctp=s1639x2048&_nc_cat=100&ccb=1-7&_nc_sid=127cfc&_nc_ohc=uLJmgXv-vpkQ7kNvwGg16FT&_nc_oc=AdrgbrM1gEn2y9G9N6GXLRqYf-zPQtQnNELDXrf7Sq08hfS2Cl4_tDDs-Ov3OFVSeps&_nc_zt=23&_nc_ht=scontent.flim3-2.fna&_nc_gid=avW7k4Ntnz-dEsleSMseyw&_nc_ss=7b2a8&oh=00_AQEgbCDVQHN8NwwD8h8zXdC-MTp6-2jPfEP4pWxFjH9pIQ&oe=6A8D3B2E"

        cargarImagen(
            urlImagen = urlImagen2,
            imageView = imgNoticia2,
            loadingView = loadingNoticia2,
            errorView = errorNoticia2
        )

        btnReintentar2.setOnClickListener {
            cargarImagen(
                urlImagen = urlImagen2,
                imageView = imgNoticia2,
                loadingView = loadingNoticia2,
                errorView = errorNoticia2
            )
        }

        // NOTICIA 3

        val imgNoticia3 = view.findViewById<ImageView>(R.id.imgNoticia3)
        val loadingNoticia3 = view.findViewById<View>(R.id.loadingNoticia3)
        val errorNoticia3 = view.findViewById<View>(R.id.errorNoticia3)
        val btnReintentar3 = view.findViewById<View>(R.id.btnReintentar3)
        val urlImagen3 =
            "https://scontent.flim3-2.fna.fbcdn.net/v/t39.30808-6/775183695_1057756220168295_511697126033392785_n.jpg?stp=dst-jpg_tt6&cstp=mx1639x2048&ctp=s1639x2048&_nc_cat=102&ccb=1-7&_nc_sid=127cfc&_nc_ohc=Ngkba-JwbcoQ7kNvwF0V5ZG&_nc_oc=AdqjJI49a-yiQdq3UWnKhyRRAIPyloFxUz0mKQaDee-MW5zIrA0VxnbbjjhwzGpBHrs&_nc_zt=23&_nc_ht=scontent.flim3-2.fna&_nc_gid=IvSNd3ujZ5Broc3ELoYZtA&_nc_ss=7b2a8&oh=00_AQG2vOMcuTz1Ikdh_wmw2Ix4nhfUH3JGTWjhri5BNkfwBQ&oe=6A8D504B"

        cargarImagen(
            urlImagen = urlImagen3,
            imageView = imgNoticia3,
            loadingView = loadingNoticia3,
            errorView = errorNoticia3
        )

        btnReintentar3.setOnClickListener {
            cargarImagen(
                urlImagen = urlImagen3,
                imageView = imgNoticia3,
                loadingView = loadingNoticia3,
                errorView = errorNoticia3
            )
        }
    }

    // CARGAR IMAGEN
    private fun cargarImagen(urlImagen: String, imageView: ImageView, loadingView: View, errorView: View) {

        // ESTADO CARGANDO
        loadingView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        imageView.visibility = View.INVISIBLE

        thread {
            try {
                val url = URL(urlImagen)
                val conexion = url.openConnection() as HttpURLConnection
                conexion.connectTimeout = 10000
                conexion.readTimeout = 10000
                conexion.doInput = true
                conexion.connect()

                if (conexion.responseCode !in 200..299) {
                    throw Exception(
                        "Error HTTP: ${conexion.responseCode}"
                    )
                }

                val inputStream = conexion.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)

                inputStream.close()
                conexion.disconnect()

                if (bitmap == null) {
                    throw Exception(
                        "No se pudo decodificar imagen"
                    )
                }

                activity?.runOnUiThread {

                    // Si el fragment ya no existe,
                    // evitamos tocar las vistas.
                    if (!isAdded) {
                        return@runOnUiThread
                    }

                    imageView.setImageBitmap(bitmap)

                    // ESTADO CARGADO
                    loadingView.visibility = View.GONE
                    errorView.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {

                    if (!isAdded) {
                        return@runOnUiThread
                    }

                    // ESTADO ERROR
                    loadingView.visibility = View.GONE
                    imageView.visibility = View.INVISIBLE
                    errorView.visibility = View.VISIBLE
                }
            }
        }
    }
}