package com.cloud.gurkasapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DiasAsistenciaAdapter(private val listaDias: List<DiaAsistencia>) : RecyclerView.Adapter<DiasAsistenciaAdapter.DiaViewHolder>()
{
    class DiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val txtNumeroDia: TextView = itemView.findViewById(R.id.txtNumeroDia)
        val txtNombreDia: TextView = itemView.findViewById(R.id.txtNombreDia)
        val txtDescripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiaViewHolder {

        val vista = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_dia_asistencia,
                parent,
                false
            )

        return DiaViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: DiaViewHolder,
        position: Int
    ) {

        val dia = listaDias[position]
        holder.txtNumeroDia.text = dia.numero
        holder.txtNombreDia.text = dia.nombre
        holder.txtDescripcion.text = dia.descripcion
    }

    override fun getItemCount(): Int {
        return listaDias.size
    }
}