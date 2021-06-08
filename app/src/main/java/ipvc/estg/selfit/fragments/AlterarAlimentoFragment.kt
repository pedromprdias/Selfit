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
import ipvc.estg.selfit.api.Alimento
import ipvc.estg.selfit.api.AlimentoOutput
import ipvc.estg.selfit.api.Endpoints
import ipvc.estg.selfit.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AlterarAlimentoFragment: DialogFragment() {

    lateinit var listener: AlterarAlimentoFragmentListener
    lateinit var dialogView: View

    //define interface to communicate with activity
    interface AlterarAlimentoFragmentListener {
        fun onEditAlimento(dialog: DialogFragment, alimento: Alimento, refeicao: String)
        fun onDeleteAlimento(dialog: DialogFragment, id: Int, refeicao: String)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            dialogView = inflater.inflate(R.layout.fragment_adicionar_alimento, null)

            builder.setView(dialogView)

            dialogView.findViewById<Button>(R.id.fragmentAdicionarAlimentoAddBtn).visibility = View.INVISIBLE
            dialogView.findViewById<Button>(R.id.fragmentAdicionarAlimentoEditBtn).visibility = View.VISIBLE
            dialogView.findViewById<Button>(R.id.fragmentAdicionarAlimentoRmBtn).visibility = View.VISIBLE
            dialogView.findViewById<EditText>(R.id.fragmentAdicionarAlimentoQtdInput).setText(requireArguments().getFloat("quantidade").toString())

            //open shared preferences and get the access token to make a request
            var sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

            val accessToken: String? = sharedPreferences.getString("accessToken", "")

            val authorization = "Bearer $accessToken"
            //get the clicked food item id passed from the food items list activity
            val id = requireArguments().getInt("id")

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.getAlimento(id, authorization)

            //make request to get all the information about this page's food item
            call.enqueue(object : Callback<AlimentoOutput> {
                override fun onResponse(call: Call<AlimentoOutput>, response: Response<AlimentoOutput>) {
                    //if the request is successful display all the item's information on the page's elements
                    if(response.isSuccessful) {

                        var alimento: Alimento = response.body()!!.alimento!!

                        dialogView.findViewById<TextView>(R.id.fragmentAdicionarAlimentoName).text = alimento.nome
                        dialogView.findViewById<TextView>(R.id.fragmentAdicionarAlimentoDescription).text = alimento.descricao

                        val bitmap: Bitmap = BitmapFactory.decodeByteArray(alimento.imagem!!.data, 0, alimento.imagem!!.data.size)

                        dialogView.findViewById<ImageView>(R.id.fragmentAdicionarAlimentoImage).setImageBitmap(bitmap)

                        dialogView.findViewById<TextView>(R.id.fragmentAdicionarAlimentoCaloriasValue).text = alimento.calorias.toString() + "kcal"
                        dialogView.findViewById<TextView>(R.id.fragmentAdicionarAlimentoProteinasValue).text = alimento.proteinas.toString() + "g"
                        dialogView.findViewById<TextView>(R.id.fragmentAdicionarAlimentoHidratosValue).text = alimento.hidratosCarbono.toString() + "g"
                        dialogView.findViewById<TextView>(R.id.fragmentAdicionarAlimentoLipidosValue).text = alimento.lipidos.toString() + "g"
                        dialogView.findViewById<TextView>(R.id.fragmentAdicionarAlimentoTipo).text = "Por cada " + alimento.tipo + ":"

                        dialogView.findViewById<TextView>(R.id.fragmentAdicionarAlimentoQtdTipo).text = when(alimento.tipo!!.last()){
                            'e' -> "unidades"
                            'g' -> "gramas"
                            'l' -> "mililitros"
                            else -> ""
                        }

                        dialogView.findViewById<Button>(R.id.fragmentAdicionarAlimentoEditBtn).setOnClickListener {

                            val quantidade: String = dialogView.findViewById<EditText>(R.id.fragmentAdicionarAlimentoQtdInput).text.toString()

                            if(quantidade == ""){
                                Toast.makeText(requireActivity(), getString(R.string.noQuantidade), Toast.LENGTH_SHORT).show()
                            } else {
                                alimento.quantidade = quantidade.toFloat()
                                listener.onEditAlimento(this@AlterarAlimentoFragment, alimento, requireArguments().getString("refeicao")!!)
                            }
                        }

                        dialogView.findViewById<Button>(R.id.fragmentAdicionarAlimentoRmBtn).setOnClickListener {

                            listener.onDeleteAlimento(this@AlterarAlimentoFragment, alimento.id, requireArguments().getString("refeicao")!!)
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

                //if there is a connection error warn the user
                override fun onFailure(call: Call<AlimentoOutput>, t: Throwable) {
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
            listener = context as AlterarAlimentoFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement AlterarAlimentoFragmentListener"))
        }
    }
}