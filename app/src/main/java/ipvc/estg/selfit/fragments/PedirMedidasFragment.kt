package ipvc.estg.selfit.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import ipvc.estg.selfit.R
import ipvc.estg.selfit.api.Medida
import java.time.Instant


class PedirMedidasFragment: DialogFragment() {

    lateinit var listener: PedirMedidasFragmentListener
    lateinit var dialogView: View

    //define interface to communicate with activity
    interface PedirMedidasFragmentListener {
        fun onGuardarMedidas(dialog: DialogFragment, medida: Medida, naoPedir: Boolean)
        fun onCancelarGuardarMedidas(dialog: DialogFragment, naoPedir: Boolean)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            dialogView = inflater.inflate(R.layout.fragment_pedir_medidas, null)

            var sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

            val check: Boolean = sharedPreferences.getBoolean("pedirMedidas", true)

            dialogView.findViewById<CheckBox>(R.id.fragmentPedirMedidasCheckbox).isChecked = !check

            dialogView.findViewById<Button>(R.id.fragmentPedirMedidasGuardarBtn).setOnClickListener {

                var naoPedir: Boolean = dialogView.findViewById<CheckBox>(R.id.fragmentPedirMedidasCheckbox).isChecked

                var altura: String = dialogView.findViewById<EditText>(R.id.fragmentPedirMedidasAlturaValue).text.toString()
                var peso: String = dialogView.findViewById<EditText>(R.id.fragmentPedirMedidasPesoValue).text.toString()
                var pesoMeta: String = dialogView.findViewById<EditText>(R.id.fragmentPedirMedidasMetaPesoValue).text.toString()
                var gordura: String = dialogView.findViewById<EditText>(R.id.fragmentPedirMedidasGorduraValue).text.toString()
                var gorduraMeta: String = dialogView.findViewById<EditText>(R.id.fragmentPedirMedidasMetaGorduraValue).text.toString()

                var data: String = Instant.now().toString().substring(0, 10)

                when {
                    altura == "" -> Toast.makeText(requireActivity(), getString(R.string.noAltura), Toast.LENGTH_SHORT).show()
                    peso == "" -> Toast.makeText(requireActivity(), getString(R.string.noPeso), Toast.LENGTH_SHORT).show()
                    pesoMeta == "" -> Toast.makeText(requireActivity(), getString(R.string.noPesoMeta), Toast.LENGTH_SHORT).show()
                    gordura == "" -> Toast.makeText(requireActivity(), getString(R.string.noGordura), Toast.LENGTH_SHORT).show()
                    gorduraMeta == "" -> Toast.makeText(requireActivity(), getString(R.string.noMetaGordura), Toast.LENGTH_SHORT).show()
                    else -> {
                        var medida: Medida = Medida(data = data, peso = peso.toFloat(), pesoMeta = pesoMeta.toFloat(), altura = altura.toInt(), gordura = gordura.toFloat(), gorduraMeta = gorduraMeta.toFloat())

                        listener.onGuardarMedidas(this, medida, naoPedir)
                    }
                }
            }

            dialogView.findViewById<Button>(R.id.fragmentPedirMedidasCancelarBtn).setOnClickListener {

                var naoPedir: Boolean = dialogView.findViewById<CheckBox>(R.id.fragmentPedirMedidasCheckbox).isChecked

                listener.onCancelarGuardarMedidas(this, naoPedir)
            }

            builder.setView(dialogView)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //attach parent as listener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as PedirMedidasFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement PedirMedidasFragmentListener"))
        }
    }
}