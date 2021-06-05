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
import ipvc.estg.selfit.api.Alimento

class AlimentoAdapter constructor(context: Context) : RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var alimentos = listOf<Alimento>()

    class AlimentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alimentoNome: TextView = itemView.findViewById(R.id.alimentosRecyclerNome)
        val alimentoImage: ImageView = itemView.findViewById(R.id.alimentosRecyclerImage)
        val alimentoId: TextView = itemView.findViewById(R.id.alimentosRecyclerId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder {
        val itemView = inflater.inflate(R.layout.alimentos_recycler_line, parent, false)
        return AlimentoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder, position: Int) {
        val current = alimentos[position]

        val bitmap: Bitmap = BitmapFactory.decodeByteArray(current.imagem.data, 0, current.imagem.data.size)

        holder.alimentoNome.text = current.nome
        holder.alimentoId.text = current.id.toString()
        holder.alimentoImage.setImageBitmap(bitmap)
    }

    //change data set of the adapter
    fun setAlimentos(notes: List<Alimento>) {
        this.alimentos = notes
        notifyDataSetChanged()
    }

    //get data set size
    override fun getItemCount(): Int {
        return alimentos.size
    }
}