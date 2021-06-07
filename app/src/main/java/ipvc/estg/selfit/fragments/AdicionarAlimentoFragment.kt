package ipvc.estg.selfit.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import ipvc.estg.selfit.R
import ipvc.estg.selfit.api.Alimento

class AdicionarAlimentoFragment: DialogFragment() {

    lateinit var listener: AdicionarAlimentoFragmentListener

    //define interface to communicate with activity
    interface AdicionarAlimentoFragmentListener {
        fun onAdicionarAlimento(dialog: DialogFragment, alimento: Alimento, refeicao: String)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val dialogView = inflater.inflate(R.layout.activity_main, null)

            builder.setView(dialogView)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //attach parent as listener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as AdicionarAlimentoFragmentListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement AdicionarAlimentoFragmentListener"))
        }
    }
}