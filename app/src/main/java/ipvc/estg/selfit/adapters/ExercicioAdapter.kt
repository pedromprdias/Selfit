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
import ipvc.estg.selfit.fragments.ListaExerciciosFragment

class ExercicioAdapter constructor(context: Context, fragment: ListaExerciciosFragment?) : RecyclerView.Adapter<ExercicioAdapter.ExercicioViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var exercicios = listOf<Exercicio>()
    private val fragment = fragment

    class ExercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exercicioNome: TextView = itemView.findViewById(R.id.exerciciosRecyclerNome)
        val exercicioImage: ImageView = itemView.findViewById(R.id.exerciciosRecyclerImage)
        val exercicioId: TextView = itemView.findViewById(R.id.exerciciosRecyclerId)
        val line: View = itemView.findViewById(R.id.exerciciosRecyclerLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercicioViewHolder {
        val itemView = inflater.inflate(R.layout.exercicios_recycler_line, parent, false)
        return ExercicioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExercicioViewHolder, position: Int) {
        val current = exercicios[position]

        val bitmap: Bitmap = BitmapFactory.decodeByteArray(current.imagem!!.data, 0, current.imagem!!.data.size)

        holder.exercicioNome.text = current.nome
        holder.exercicioId.text = current.id.toString()
        holder.exercicioImage.setImageBitmap(bitmap)
        if(fragment != null){
            holder.line.setOnClickListener {
                val id: String = it.findViewById<TextView>(R.id.exerciciosRecyclerId).text.toString()
                fragment.moveToExercicioFragment(id)
            }
        }
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