package ipvc.estg.selfit.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ipvc.estg.selfit.R
import ipvc.estg.selfit.activities.MainActivity
import java.util.*

class AlarmReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("mongo", Calendar.getInstance().timeInMillis.toString())
        var tipoLembrete: String = intent!!.getStringExtra("tipoLembrete").toString()

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        var lembrete: String

        if(tipoLembrete == "Registos") lembrete = "Lembrete: Não efetuou nenhum registo hoje."
        else lembrete = "Lembrete: Não efetuou registo relativo a " + tipoLembrete + " hoje."

        val builder = NotificationCompat.Builder(context!!, "8")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Lembrete Selfit")
                .setContentText(lembrete)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        val id = when(tipoLembrete){
            "Pequeno Almoço" -> 0
            "Almoço" -> 1
            "Lanche" -> 2
            "Jantar" -> 3
            "Treino Diário" -> 4
            "Registos" -> 5
            else -> -1
        }

        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
}