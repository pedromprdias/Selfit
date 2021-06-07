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
import ipvc.estg.selfit.api.Exercicio

class TreinoAdapter constructor(context: Context) : RecyclerView.Adapter<TreinoAdapter.TreinoViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var exercicios = listOf<Exercicio>()

    class TreinoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exercicioNome: TextView = itemView.findViewById(R.id.treinoRecyclerName)
        val exercicioImage: ImageView = itemView.findViewById(R.id.treinoRecyclerImage)
        val exercicioPeso: TextView = itemView.findViewById(R.id.treinoRecyclerPeso)
        val exercicioSeries: TextView = itemView.findViewById(R.id.treinoRecyclerSeries)
        val exercicioRepeticoes: TextView = itemView.findViewById(R.id.treinoRecyclerRepeticoes)
        val exercicioId: TextView = itemView.findViewById(R.id.treinoRecyclerId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreinoViewHolder {
        val itemView = inflater.inflate(R.layout.treino_recycler_line, parent, false)
        return TreinoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TreinoViewHolder, position: Int) {
        val current = exercicios[position]

        val bitmap: Bitmap = BitmapFactory.decodeByteArray(current.imagem!!.data, 0, current.imagem!!.data.size)

        holder.exercicioNome.text = current.nome
        holder.exercicioId.text = current.id.toString()
        holder.exercicioImage.setImageBitmap(bitmap)
        holder.exercicioPeso.text = "Peso: " + current.valores!!.peso.toString()
        holder.exercicioSeries.text = "Séries: " + current.valores!!.series.toString()
        holder.exercicioRepeticoes.text = "Repetições: " + current.valores!!.repeticoes.toString()
    }

    //change data set of the adapter
    fun setExercicios(exercicios: List<Exercicio>) {
        this.exercicios = exercicios
        notifyDataSetChanged()
    }

    //get data set size
    override fun getItemCount(): Int {
        return exercicios.size
    }
}