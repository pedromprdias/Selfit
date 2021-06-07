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

class AlterarAlimentoFragment: DialogFragment() {

    lateinit var listener: AlterarAlimentoFragmentListener

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

            /*val dialogView = inflater.inflate(R.layout.create_note_dialog, null)

             val createBtn = dialogView.findViewById<Button>(R.id.createBtn)
             val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)

             //add listeners to the dialog's custom button and make them communicate with the parent activity
             cancelBtn.setOnClickListener {
                 listener.onCancelCreate(this)
             }

             builder.setView(dialogView)*/

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