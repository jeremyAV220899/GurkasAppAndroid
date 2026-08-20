package com.cloud.gurkasapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val scrollHome =
            view.findViewById<NestedScrollView>(
                R.id.scrollHome
            )

        scrollHome.setOnScrollChangeListener {
                _,
                _,
                scrollY,
                _,
                _ ->

            val mainActivity = requireActivity() as MainActivity

            if (scrollY > dpToPx(30)) {
                mainActivity.mostrarStatusBarBlanca()
            } else {
                mainActivity.mostrarHeaderRojo()
            }
        }
    }

    private fun dpToPx(dp: Int): Int {

        return (dp * resources.displayMetrics.density).toInt()
    }
}