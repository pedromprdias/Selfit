package ipvc.estg.selfit.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.adapters.AlimentoAdapter
import ipvc.estg.selfit.api.Alimento
import ipvc.estg.selfit.api.AllAlimentosOutput
import ipvc.estg.selfit.api.Endpoints
import ipvc.estg.selfit.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListaAlimentosFragment: DialogFragment() {

    lateinit var listener: ListaAlimentosFragmentListener
    private lateinit var listaAlimentos: List<Alimento>
    lateinit var dialogView: View

    //define interface to communicate with activity
    interface ListaAlimentosFragmentListener {
        fun onClickAlimento(dialog: DialogFragment, alimento: Int)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            dialogView = inflater.inflate(R.layout.fragment_lista_alimentos, null)

            var searchBtn: ImageButton = dialogView.findViewById(R.id.fragmentListaExerciciosSearchBtn)

            searchBtn.setOnClickListener { filterAlimentos(it) }

            //set the recycler's adapter
            val alimentosRecycler = dialogView.findViewById<RecyclerView>(R.id.fragmentListaExerciciosRecycler)
            val adapter = AlimentoAdapter(requireActivity(), this)
            alimentosRecycler.adapter = adapter
            alimentosRecycler.layoutManager = LinearLayoutManager(requireActivity())
            adapter.setAlimentos(listOf())

            //open shared preferences and get the access token to make a request
            var sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

            val accessToken: String? = sharedPreferences.getString("accessToken", "")

            val authorization = "Bearer $accessToken"

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.getAllAlimentos(authorization)

            //make request to get information about all food items using the access token
            call.enqueue(object : Callback<AllAlimentosOutput> {
                override fun onResponse(call: Call<AllAlimentosOutput>, response: Response<AllAlimentosOutput>) {
                    //if the request is successful store all food items info and display it on the recycler
                    if(response.isSuccessful) {
                        listaAlimentos = response.body()!!.listaAlimentos!!
                        adapter.setAlimentos(listaAlimentos)
                    } else {
                        listaAlimentos = listOf()
                        //if the call is not successful, check the error code and warn the user accordingly
                        when (response.code()){
                            400 -> Toast.makeText(requireActivity(), getString(R.string.erro), Toast.LENGTH_SHORT).show()
                            401 -> Toast.makeText(requireActivity(), getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                            403 -> Toast.makeText(requireActivity(), getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                //if there is a connection error warn the user
                override fun onFailure(call: Call<AllAlimentosOutput>, t: Throwable) {
                    listaAlimentos = listOf()
                    Toast.makeText(requireActivity(), getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                }
            })

            builder.setView(dialogView)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //called when the user clicks on the search button
    fun filterAlimentos(view: View) {

        //takes the user search input
        val search = dialogView.findViewById<EditText>(R.id.fragmentListaExerciciosSearchText).text.toString()

        val alimentosRecycler = dialogView.findViewById<RecyclerView>(R.id.fragmentListaExerciciosRecycler)
        val adapter = AlimentoAdapter(requireActivity(), this)
        alimentosRecycler.adapter = adapter
        alimentosRecycler.layoutManager = LinearLayoutManager(requireActivity())

        //compares the input to all the existing food items, displaying only the ones that contain the input in their names
        if(search != "") {
            var novaLista: MutableList<Alimento> = mutableListOf()

            listaAlimentos.forEach{
                if(it.nome!!.contains(search, true)) {
                    novaLista.add(it)
                }
            }

            adapter.setAlimentos(novaLista)
            //if no input is given, display all food items
        } else {
            adapter.setAlimentos(listaAlimentos)
        }
    }

    fun moveToAlimentoFragment(id: String){
        listener.onClickAlimento(this, id.toInt())
    }

    //attach parent as listener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as ListaAlimentosFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement ListaAlimentosFragmentListener"))
        }
    }
}