package ipvc.estg.selfit.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import ipvc.estg.selfit.R
import ipvc.estg.selfit.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdicionarExercicioFragment: DialogFragment() {

    lateinit var listener: AdicionarExercicioFragmentListener
    lateinit var dialogView: View

    //define interface to communicate with activity
    interface AdicionarExercicioFragmentListener {
        fun onAdicionarExercicio(dialog: DialogFragment, exercicio: Exercicio)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            dialogView = inflater.inflate(R.layout.fragment_adicionar_exercicio, null)

            builder.setView(dialogView)

            //open shared preferences and get the access token to make a request
            var sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

            val accessToken: String? = sharedPreferences.getString("accessToken", "")

            val authorization = "Bearer $accessToken"
            //get the clicked exercise id passed from the exercise list activity
            val id: Int = requireArguments().getInt("id")

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.getExercicio(id, authorization)

            //make request to get all the information about this page's exercise
            call.enqueue(object : Callback<ExercicioOutput> {
                override fun onResponse(call: Call<ExercicioOutput>, response: Response<ExercicioOutput>) {
                    //if the request is successful display all the exercise's information on the page's elements
                    if(response.isSuccessful) {

                        var exercicio: Exercicio = response.body()!!.exercicio!!

                        dialogView.findViewById<TextView>(R.id.exercicioDetalhesName).text = exercicio.nome
                        dialogView.findViewById<TextView>(R.id.exercicioDetalhesDescription).text = exercicio.descricao

                        var bitmap: Bitmap = BitmapFactory.decodeByteArray(exercicio.imagem!!.data, 0, exercicio.imagem!!.data.size)

                        dialogView.findViewById<ImageView>(R.id.exercicioDetalhesImage).setImageBitmap(bitmap)

                        dialogView.findViewById<Button>(R.id.fragmentAdicionarExercicioAddBtn).setOnClickListener {

                            val id: Int = exercicio.id
                            val peso: String = dialogView.findViewById<EditText>(R.id.fragmentAdicionarExercicioPeso).text.toString()
                            val repeticoes: String = dialogView.findViewById<EditText>(R.id.fragmentAdicionarExercicioRepeticoes).text.toString()
                            val series: String = dialogView.findViewById<EditText>(R.id.fragmentAdicionarExercicioSeries).text.toString()

                            when {
                                peso == "" -> {
                                    Toast.makeText(requireActivity(), getString(R.string.noPeso), Toast.LENGTH_SHORT).show()
                                }
                                repeticoes == "" -> {
                                    Toast.makeText(requireActivity(), getString(R.string.noRepeticoes), Toast.LENGTH_SHORT).show()
                                }
                                series == "" -> {
                                    Toast.makeText(requireActivity(), getString(R.string.noSeries), Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    exercicio.valores = Peso(peso.toFloat(), series.toInt(), repeticoes.toInt())
                                    listener.onAdicionarExercicio(this@AdicionarExercicioFragment, response.body()!!.exercicio!!)
                                }
                            }
                        }
                    } else {
                        //if the call is not successful, check the error code and warn the user accordingly
                        when (response.code()){
                            400 -> Toast.makeText(requireActivity(), getString(R.string.erro), Toast.LENGTH_SHORT).show()
                            401 -> Toast.makeText(requireActivity(), getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                            403 -> Toast.makeText(requireActivity(), getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                //if there is a connection error warn the user and close this activity
                override fun onFailure(call: Call<ExercicioOutput>, t: Throwable) {
                    Toast.makeText(requireActivity(), getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                }
            })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //attach parent as listener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as AdicionarExercicioFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement AdicionarExercicioFragmentListener"))
        }
    }
}