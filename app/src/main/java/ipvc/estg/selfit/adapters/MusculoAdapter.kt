package ipvc.estg.selfit.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.api.Musculo

class MusculoAdapter constructor(context: Context) : RecyclerView.Adapter<MusculoAdapter.MusculoViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var musculos = listOf<Musculo>()

    class MusculoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val musculoNome: TextView = itemView.findViewById(R.id.musculosRecyclerName)
        val musculoImagem: ImageView = itemView.findViewById(R.id.musculosRecyclerImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusculoViewHolder {
        val itemView = inflater.inflate(R.layout.musculos_recycler_line, parent, false)
        return MusculoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MusculoViewHolder, position: Int) {
        val current = musculos[position]

        val bitmap: Bitmap = BitmapFactory.decodeByteArray(current.imagem.data, 0, current.imagem.data.size)

        holder.musculoNome.text = current.nome
        holder.musculoImagem.setImageBitmap(bitmap)
    }

    //change data set of the adapter
    fun setMusculos(musculos: List<Musculo>) {
        this.musculos = musculos
        notifyDataSetChanged()
    }

    //get data set size
    override fun getItemCount(): Int {
        return musculos.size
    }
}