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

class ExerciciosMaquinaAdapter constructor(context: Context) : RecyclerView.Adapter<ExerciciosMaquinaAdapter.ExerciciosMaquinaViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var exercicios = listOf<Exercicio>()

    class ExerciciosMaquinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exercicioNome: TextView = itemView.findViewById(R.id.exerciciosMaquinaRecyclerName)
        val exercicioImagem: ImageView = itemView.findViewById(R.id.exerciciosMaquinaRecyclerImage)
        val exercicioId: TextView = itemView.findViewById(R.id.exerciciosMaquinaRecyclerId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciciosMaquinaViewHolder {
        val itemView = inflater.inflate(R.layout.exercicios_maquina_recycler_line, parent, false)
        return ExerciciosMaquinaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExerciciosMaquinaViewHolder, position: Int) {
        val current = exercicios[position]

        val bitmap: Bitmap = BitmapFactory.decodeByteArray(current.imagem!!.data, 0, current.imagem!!.data.size)

        holder.exercicioNome.text = current.nome
        holder.exercicioImagem.setImageBitmap(bitmap)
        holder.exercicioId.text = current.id.toString()
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