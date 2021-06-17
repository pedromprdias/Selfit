package ipvc.estg.selfit.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.adapters.AlimentoAdapter
import ipvc.estg.selfit.adapters.ExercicioAdapter
import ipvc.estg.selfit.api.AllExerciciosOutput
import ipvc.estg.selfit.api.Endpoints
import ipvc.estg.selfit.api.Exercicio
import ipvc.estg.selfit.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListaExerciciosFragment: DialogFragment() {

    lateinit var listener: ListaExerciciosFragmentListener
    private lateinit var listaExercicios: List<Exercicio>
    lateinit var dialogView: View

    //define interface to communicate with activity
    interface ListaExerciciosFragmentListener {
        fun onClickExercicio(dialog: DialogFragment, exercicio: Int)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            listaExercicios = listOf()

            dialogView = inflater.inflate(R.layout.fragment_lista_exercicios, null)

            builder.setView(dialogView)

            var searchBtn: ImageButton = dialogView.findViewById(R.id.fragmentListaExerciciosSearchBtn)

            searchBtn.setOnClickListener { filterExercicios(it) }

            //set the recycler's adapter
            val exerciciosRecycler = dialogView.findViewById<RecyclerView>(R.id.fragmentListaExerciciosRecycler)
            val adapter = ExercicioAdapter(requireActivity(), this)
            exerciciosRecycler.adapter = adapter
            exerciciosRecycler.layoutManager = LinearLayoutManager(requireActivity())
            adapter.setExercicios(listOf())

            //open shared preferences and get the access token to make a request
            var sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

            val accessToken: String? = sharedPreferences.getString("accessToken", "")

            val authorization = "Bearer $accessToken"

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.getAllExercicios(authorization)

            //make request to get information about all exercises using the access token
            call.enqueue(object : Callback<AllExerciciosOutput> {
                override fun onResponse(call: Call<AllExerciciosOutput>, response: Response<AllExerciciosOutput>) {
                    //if the request is successful store all exercises info and display it on the recycler
                    if(response.isSuccessful) {
                        listaExercicios = response.body()!!.listaExercicios!!
                        adapter.setExercicios(listaExercicios)
                    } else {
                        listaExercicios = listOf()
                        //if the call is not successful, check the error code and warn the user accordingly
                        when (response.code()){
                            400 -> Toast.makeText(requireActivity(), getString(R.string.erro), Toast.LENGTH_SHORT).show()
                            401 -> Toast.makeText(requireActivity(), getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                            403 -> Toast.makeText(requireActivity(), getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                //if there is a connection error warn the user
                override fun onFailure(call: Call<AllExerciciosOutput>, t: Throwable) {
                    listaExercicios = listOf()
                    Log.i("aa", t.message.toString())
                    Toast.makeText(requireActivity(), getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                }
            })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun moveToExercicioFragment(id: String){
        listener.onClickExercicio(this, id.toInt())
    }

    //called when the user clicks on the search button
    fun filterExercicios(view: View) {

        //takes the user search inputs
        val search = dialogView.findViewById<EditText>(R.id.fragmentListaExerciciosSearchText).text.toString()
        val muscle = dialogView.findViewById<Spinner>(R.id.fragmentListaExerciciosSearchMusculos).selectedItem.toString()

        val exerciciosRecycler = dialogView.findViewById<RecyclerView>(R.id.fragmentListaExerciciosRecycler)
        val adapter = ExercicioAdapter(requireActivity(), this)
        exerciciosRecycler.adapter = adapter
        exerciciosRecycler.layoutManager = LinearLayoutManager(requireActivity())

        //compares the input to all the existing exercises, displaying only the ones that contain the input in their names
        if(search != "") {
            var novaLista: MutableList<Exercicio> = mutableListOf()

            listaExercicios.forEach{
                if(it.nome!!.contains(search, true)) {
                    if(muscle == "Todos"){
                        novaLista.add(it)
                    } else {
                        var existe: Boolean = false
                        it.musculos!!.forEach{
                            if(it.nome == muscle) existe = true
                        }

                        if(existe) novaLista.add(it)
                    }
                }
            }

            adapter.setExercicios(novaLista)
        } else {
            if(muscle == "Todos"){
                adapter.setExercicios(listaExercicios)
            } else {
                var novaLista: MutableList<Exercicio> = mutableListOf()

                listaExercicios.forEach{
                    var existe: Boolean = false
                    it.musculos!!.forEach{
                        if(it.nome == muscle) existe = true
                    }

                    if(existe) novaLista.add(it)
                }

                adapter.setExercicios(novaLista)
            }
        }
    }

    //attach parent as listener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as ListaExerciciosFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement ListaExerciciosFragmentListener"))
        }
    }
}