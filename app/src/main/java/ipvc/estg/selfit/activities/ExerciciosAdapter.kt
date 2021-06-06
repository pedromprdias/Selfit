package ipvc.estg.selfit.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.selfit.R


class ExerciciosAdapter(private val exerciciosList:List<ExercicoExemplo>): RecyclerView.Adapter<ExerciciosAdapter.ExerciciosViewHolder>(){

    class ExerciciosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val exeImagem :ImageView = itemView.findViewById(R.id.exercicioImagem)
        val exeTitulo:TextView = itemView.findViewById(R.id.tituloExercicio)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciciosViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.exercicios_line,
        parent, false)

        return ExerciciosViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ExerciciosViewHolder, position: Int) {
        val currentItem = exerciciosList[position]

        holder.exeImagem.setImageResource(currentItem.imageResource)
        holder.exeTitulo.text = currentItem.text
    }

    override fun getItemCount() = exerciciosList.size

}