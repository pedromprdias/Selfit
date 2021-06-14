package ipvc.estg.selfit.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.adapters.RefeicaoAdapter
import ipvc.estg.selfit.adapters.TreinoAdapter
import ipvc.estg.selfit.api.*
import ipvc.estg.selfit.fragments.*
import ipvc.estg.selfit.notifications.AlarmReceiver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HomePage : AppCompatActivity(),
        ListaAlimentosFragment.ListaAlimentosFragmentListener,
        ListaExerciciosFragment.ListaExerciciosFragmentListener,
        AlterarAlimentoFragment.AlterarAlimentoFragmentListener,
        AlterarExercicioFragment.AlterarExercicioFragmentListener,
        AdicionarAlimentoFragment.AdicionarAlimentoFragmentListener,
        AdicionarExercicioFragment.AdicionarExercicioFragmentListener,
        PedirMedidasFragment.PedirMedidasFragmentListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var adaptersRefeicoes: MutableList<RefeicaoAdapter>
    private lateinit var adapterTreino: TreinoAdapter
    private lateinit var alimentosRefeicoes: MutableList<MutableList<Alimento>>
    private lateinit var exerciciosTreinoDiario: MutableList<Exercicio>
    private var ultimaMedida: Medida? = null
    private var notifValues: MutableMap<String, Boolean> = mutableMapOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        notifValues.put("Pequeno Almoço", false)
        notifValues.put("Almoço", false)
        notifValues.put("Lanche", false)
        notifValues.put("Jantar", false)
        notifValues.put("Treino Diário", false)

        alimentosRefeicoes = mutableListOf()

        for(i in 1..4){
            alimentosRefeicoes.add(mutableListOf())
        }

        exerciciosTreinoDiario = mutableListOf()

        //set the recycler's adapter
        adaptersRefeicoes = mutableListOf()

        var recyclersRefeicoes: MutableList<RecyclerView> = mutableListOf()
        recyclersRefeicoes.add(findViewById(R.id.homePequenoAlmocoRecyclerList))
        recyclersRefeicoes.add(findViewById(R.id.homeAlmocoRecyclerList))
        recyclersRefeicoes.add(findViewById(R.id.homeLancheRecyclerList))
        recyclersRefeicoes.add(findViewById(R.id.homeJantarRecyclerList))
        var treinoRecycler = findViewById<RecyclerView>(R.id.homeTreinoRecyclerList)

        adapterTreino = TreinoAdapter(this)
        treinoRecycler.adapter = adapterTreino
        treinoRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclersRefeicoes.forEach{
            var adapterIt = RefeicaoAdapter(this)
            adaptersRefeicoes.add(adapterIt)
            it.adapter = adapterIt
            it.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        adapterTreino.setExercicios(listOf())

        adaptersRefeicoes.forEach{
            it.setAlimentos(listOf())
        }

        //call toolbar setup function
        setUpToolbar()
        navigationView = findViewById(R.id.navigation_menu)
        //when a navigation drawer item is clicked do the respective action
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //when clicking home page
                R.id.nav_home -> {
                    //do nothing (already there)
                }
                //when clicking training plans
                R.id.nav_reconhecimento -> {
                    //go to training plans activity
                    val intent = Intent(this@HomePage, ReconhecimentoMaquina::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking progress
                R.id.nav_progresso -> {
                    //go to progress activity
                    val intent = Intent(this@HomePage, Progresso::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking exercises
                R.id.nav_exercicios -> {
                    //go to exercises activity
                    val intent = Intent(this@HomePage, ListaExercicios::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking food
                R.id.nav_alimentos -> {
                    //go to food activity
                    val intent = Intent(this@HomePage, ListaAlimentos::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking logout
                R.id.nav_logout -> {
                    val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

                    //disable autologin and enable logout messages
                    with (sharedPreferences.edit()) {
                        putBoolean("autoLogin", false)
                        putBoolean("displayLogout", true)
                        commit()
                    }

                    //go to login activity
                    val intent = Intent(this@HomePage, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            false
        }

        val data: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        getRegistoDia(data)
        getUltimaMedida()
        createNotifChannel()
    }

    override fun onStart() {

        var eventos: MutableList<String> = mutableListOf("Pequeno Almoço", "Almoço", "Lanche", "Jantar", "Treino Diário", "Registos")

        for(i in 1..6) {
            var intent: Intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("tipoLembrete", eventos[i - 1])
            }
            PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_CANCEL_CURRENT).cancel()
        }

        super.onStart()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {

        var horas: Int = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH")).toInt() + 1
        var minutos: Int = LocalDateTime.now().format(DateTimeFormatter.ofPattern("mm")).toInt()
        var segundos: Int = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ss")).toInt()

        var alarmManager: AlarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        var pendingIntents: MutableMap<String, PendingIntent> = mutableMapOf()
        var eventos: MutableList<String> = mutableListOf("Pequeno Almoço", "Almoço", "Lanche", "Jantar", "Treino Diário", "Registos")

        for(i in 1..6) {
            var intent: Intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("tipoLembrete", eventos[i - 1])
            }
            pendingIntents[eventos[i - 1]] = PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }

        when {
            horas < 12 -> {
                eventos.forEach {
                    if(!notifValues[it]!!) alarmManager.set(AlarmManager.RTC_WAKEUP, getMillisToEvent(it, horas, minutos, segundos), pendingIntents.getValue(it))
                }
            }
            horas < 16 -> {
                for(i in 1..5){
                    if(!notifValues[eventos[i-1]]!!) alarmManager.set(AlarmManager.RTC_WAKEUP, getMillisToEvent(eventos[i], horas, minutos, segundos), pendingIntents.getValue(eventos[i]))
                }
            }
            horas < 20 -> {
                for(i in 2..5){
                    if(!notifValues[eventos[i-1]]!!) alarmManager.setExact(AlarmManager.RTC_WAKEUP, getMillisToEvent(eventos[i], horas, minutos, segundos), pendingIntents.getValue(eventos[i]))
                }
            }
            horas < 23 -> {
                for(i in 3..5){
                    if(!notifValues[eventos[i-1]]!!) alarmManager.set(AlarmManager.RTC_WAKEUP, getMillisToEvent(eventos[i], horas, minutos, segundos), pendingIntents.getValue(eventos[i]))
                }
            }
        }

        super.onStop()
    }

    private fun getMillisToEvent(evento: String, horas: Int, minutos: Int, segundos: Int): Long{

        var difMinutos = 59 - minutos
        var difSegundos = 60 - segundos
        var difHoras = 0

        when(evento){
            "Pequeno Almoço" -> {
                difHoras = 12 - horas - 1
            }
            "Almoço" -> {
                difHoras = 16 - horas - 1
            }
            "Lanche" -> {
                difHoras = 19 - horas - 1
            }
            "Jantar" -> {
                difHoras = 23 - horas - 1
            }
            "Treino Diário" -> {
                difHoras = 23 - horas - 1
            }
            "Registos" -> {
                difHoras = 23 - horas - 1 + 24
            }
        }

        return ((Calendar.getInstance().timeInMillis + difHoras * 60 * 60 * 1000 + difMinutos * 60 * 1000 + difSegundos * 1000))
    }

    //change app toolbar on this activity to custom toolbar
    fun setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayoutHome)
        val toolbar: Toolbar = findViewById(R.id.toolbarHome)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolbar_title).text = " - Página Principal"
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    fun adicionarAlimento(view: View){
        val listaAlimentosFragment = ListaAlimentosFragment()

        val args = Bundle()

        when(view){
            findViewById<Button>(R.id.homePequenoAlmocoAdicionarBtn) -> args.putString("refeicao", "Pequeno Almoço")
            findViewById<Button>(R.id.homeAlmocoAdicionarBtn) -> args.putString("refeicao", "Almoço")
            findViewById<Button>(R.id.homeLancheAdicionarBtn) -> args.putString("refeicao", "Lanche")
            findViewById<Button>(R.id.homeJantarAdicionarBtn) -> args.putString("refeicao", "Jantar")
        }

        listaAlimentosFragment.arguments = args
        listaAlimentosFragment.show(supportFragmentManager, "ListaAlimentosFragment")
    }

    fun adicionarExercicio(view: View){
        ListaExerciciosFragment().show(supportFragmentManager, "ListaExerciciosFragment")
    }

    fun alterarExercicio(view: View){

        val alterarExercicioFragment = AlterarExercicioFragment()

        val args = Bundle()

        val id: Int = view.findViewById<TextView>(R.id.treinoRecyclerId).text.toString().toInt()
        val peso: Float = view.findViewById<TextView>(R.id.treinoRecyclerPeso).text.toString().split(" ")[1].dropLast(2).toFloat()
        val series: Int = view.findViewById<TextView>(R.id.treinoRecyclerSeries).text.toString().split(" ")[1].toInt()
        val repeticoes: Int = view.findViewById<TextView>(R.id.treinoRecyclerRepeticoes).text.toString().split(" ")[1].toInt()

        args.putInt("id", id)
        args.putFloat("peso", peso)
        args.putInt("series", series)
        args.putInt("repeticoes", repeticoes)

        alterarExercicioFragment.arguments = args
        alterarExercicioFragment.show(supportFragmentManager, "AlterarExercicioFragment")
    }

    fun alterarAlimento(view: View){

        val alterarAlimentoFragment = AlterarAlimentoFragment()

        val args = Bundle()

        var quantidade: String = view.findViewById<TextView>(R.id.refeicaoRecyclerQuantity).text.toString()

        val id: Int = view.findViewById<TextView>(R.id.refeicaoRecyclerId).text.toString().toInt()

        quantidade = quantidade.substring(0, quantidade.indexOf(' ', 0, false))

        args.putInt("id", id)
        args.putFloat("quantidade", quantidade.toFloat())

        when(view.parent){
            findViewById<RecyclerView>(R.id.homePequenoAlmocoRecyclerList) -> args.putString("refeicao", "Pequeno Almoço")
            findViewById<RecyclerView>(R.id.homeAlmocoRecyclerList) -> args.putString("refeicao", "Almoço")
            findViewById<RecyclerView>(R.id.homeLancheRecyclerList) -> args.putString("refeicao", "Lanche")
            findViewById<RecyclerView>(R.id.homeJantarRecyclerList) -> args.putString("refeicao", "Jantar")
        }

        alterarAlimentoFragment.arguments = args
        alterarAlimentoFragment.show(supportFragmentManager, "AlterarAlimentoFragment")
    }

    fun guardarRefeicao(view: View){

        var tipo: String = ""
        var idHolder: TextView? = null
        var id: Int = -1
        var data: String = findViewById<TextView>(R.id.homeData).text.toString()
        var alimentos: MutableList<AlimentoInput> = mutableListOf()

        when(view){
            findViewById<Button>(R.id.homePequenoAlmocoGuardarBtn) -> {
                tipo = "Pequeno Almoço"
                idHolder = findViewById(R.id.homePequenoAlmocoId)
                Log.i("a", idHolder.text.toString())
                id = idHolder.text.toString().toInt()
                alimentosRefeicoes[0].forEach {
                    alimentos.add(AlimentoInput(id = it.id, quantidade = it.quantidade!!))
                }
            }
            findViewById<Button>(R.id.homeAlmocoGuardarBtn) -> {
                tipo = "Almoço"
                idHolder = findViewById(R.id.homeAlmocoId)
                id = idHolder.text.toString().toInt()
                alimentosRefeicoes[1].forEach {
                    alimentos.add(AlimentoInput(id = it.id, quantidade = it.quantidade!!))
                }
            }
            findViewById<Button>(R.id.homeLancheGuardarBtn) -> {
                tipo = "Lanche"
                idHolder = findViewById(R.id.homeLancheId)
                id = idHolder.text.toString().toInt()
                alimentosRefeicoes[2].forEach {
                    alimentos.add(AlimentoInput(id = it.id, quantidade = it.quantidade!!))
                }
            }
            findViewById<Button>(R.id.homeJantarGuardarBtn) -> {
                tipo = "Jantar"
                idHolder = findViewById(R.id.homeJantarId)
                id = idHolder.text.toString().toInt()
                alimentosRefeicoes[3].forEach {
                    alimentos.add(AlimentoInput(id = it.id, quantidade = it.quantidade!!))
                }
            }
            else -> ""
        }

        if(alimentos.isNotEmpty()){
            //open shared preferences and get the access token to make a request
            var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

            val accessToken: String? = sharedPreferences.getString("accessToken", "")

            val authorization = "Bearer $accessToken"

            val request = ServiceBuilder.buildService(Endpoints::class.java)

            var call: Call<PostOutput>

            if(id == -1) call = request.postRefeicao(authorization, RefeicaoInput(tipo, data, alimentos.toList()))
            else call = request.putRefeicao(authorization, id, RefeicaoInput(tipo, data, alimentos.toList()))

            call.enqueue(object : Callback<PostOutput> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<PostOutput>, response: Response<PostOutput>) {
                    if(response.isSuccessful) {
                        if(id == null){
                            idHolder!!.text = response.body()!!.id.toString()
                        }

                        if(data == LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))){
                            updateNotifValues()
                        }

                        Toast.makeText(this@HomePage, getString(R.string.registoSucesso), Toast.LENGTH_SHORT).show()
                    } else {
                        //if the call is not successful, check the error code and warn the user accordingly
                        when (response.code()){
                            400 -> Toast.makeText(this@HomePage, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                            401 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                            403 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                //if there is a connection error warn the user
                override fun onFailure(call: Call<PostOutput>, t: Throwable) {
                    Toast.makeText(this@HomePage, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun guardarTreino(view: View){

        var idHolder: TextView? = null
        var id: Int = -1
        var data: String = findViewById<TextView>(R.id.homeData).text.toString()
        var exercicios: MutableList<ExercicioInput> = mutableListOf()

        idHolder = findViewById(R.id.homeTreinoId)
        id = idHolder.text.toString().toInt()
        exerciciosTreinoDiario.forEach {
            exercicios.add(ExercicioInput(id = it.id, series = it.valores!!.series, peso = it.valores!!.peso, repeticoes = it.valores!!.repeticoes))
        }

        //open shared preferences and get the access token to make a request
        var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

        val accessToken: String? = sharedPreferences.getString("accessToken", "")

        val authorization = "Bearer $accessToken"

        val request = ServiceBuilder.buildService(Endpoints::class.java)

        var call: Call<PostOutput>

        if(id == -1) call = request.postTreinoDiario(authorization, TreinoDiarioInput(data, exercicios.toList()))
        else call = request.putTreinoDiario(authorization, id, TreinoDiarioInput(data, exercicios.toList()))

        if(exercicios.isNotEmpty()){
            call.enqueue(object : Callback<PostOutput> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<PostOutput>, response: Response<PostOutput>) {
                    if(response.isSuccessful) {
                        if(id == null){
                            idHolder!!.text = response.body()!!.id.toString()
                        }

                        if(data == LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))){
                            updateNotifValues()
                        }
                        Toast.makeText(this@HomePage, getString(R.string.registoSucesso), Toast.LENGTH_SHORT).show()
                    } else {
                        //if the call is not successful, check the error code and warn the user accordingly
                        when (response.code()){
                            400 -> Toast.makeText(this@HomePage, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                            401 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                            403 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                //if there is a connection error warn the user
                override fun onFailure(call: Call<PostOutput>, t: Throwable) {
                    Toast.makeText(this@HomePage, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onClickAlimento(dialog: DialogFragment, alimento: Int, refeicao: String){

        val adicionarAlimentoFragment = AdicionarAlimentoFragment()

        val args = Bundle()

        args.putInt("id", alimento)
        args.putString("refeicao", refeicao)

        adicionarAlimentoFragment.arguments = args
        adicionarAlimentoFragment.show(supportFragmentManager, "AdicionarAlimentoFragment")

        dialog.dismiss()
    }

    override fun onClickExercicio(dialog: DialogFragment, exercicio: Int){

        val adicionarExercicioFragment = AdicionarExercicioFragment()

        val args = Bundle()

        args.putInt("id", exercicio)

        adicionarExercicioFragment.arguments = args
        adicionarExercicioFragment.show(supportFragmentManager, "AdicionarExercicioFragment")

        dialog.dismiss()
    }

    override fun onAdicionarAlimento(dialog: DialogFragment, alimento: Alimento, refeicao: String){

        when(refeicao){
            "Pequeno Almoço" -> addAlimento(0, alimento)
            "Almoço" -> addAlimento(1, alimento)
            "Lanche" -> addAlimento(2, alimento)
            "Jantar" -> addAlimento(3, alimento)
        }

        dialog.dismiss()
    }

    private fun addAlimento(index: Int, alimento: Alimento){

        var existe = false

        alimentosRefeicoes[index].forEach {
            if(it.id == alimento.id){
                existe = true
            }
        }

        if(existe){
            Toast.makeText(this@HomePage, getString(R.string.alimentoJaNaRefeicao), Toast.LENGTH_SHORT).show()
        } else {
            alimentosRefeicoes[index].add(alimento)
            adaptersRefeicoes[index].setAlimentos(alimentosRefeicoes[index].toList())
        }

        updateValores(index)
    }

    override fun onAdicionarExercicio(dialog: DialogFragment, exercicio: Exercicio){

        var existe = false

        exerciciosTreinoDiario.forEach {
            if(it.id == exercicio.id){
                existe = true
            }
        }

        if(existe){
            Toast.makeText(this@HomePage, getString(R.string.exercicioJaNoTreino), Toast.LENGTH_SHORT).show()
        } else {
            exerciciosTreinoDiario.add(Exercicio(id = exercicio.id, nome = exercicio.nome, imagem = exercicio.imagem, valores = exercicio.valores))
            adapterTreino.setExercicios(exerciciosTreinoDiario.toList())
        }

        dialog.dismiss()
    }

    override fun onEditAlimento(dialog: DialogFragment, alimento: Alimento, refeicao: String){

        when(refeicao){
            "Pequeno Almoço" -> editAlimento(0, alimento)
            "Almoço" -> editAlimento(1, alimento)
            "Lanche" -> editAlimento(2, alimento)
            "Jantar" -> editAlimento(3, alimento)
        }

        dialog.dismiss()
    }

    override fun onDeleteAlimento(dialog: DialogFragment, id: Int, refeicao: String){

        when(refeicao){
            "Pequeno Almoço" -> rmAlimento(0, id)
            "Almoço" -> rmAlimento(1, id)
            "Lanche" -> rmAlimento(2, id)
            "Jantar" -> rmAlimento(3, id)
        }

        dialog.dismiss()
    }

    private fun editAlimento(index: Int, alimento: Alimento){

        alimentosRefeicoes[index].forEach {
            if(it.id == alimento.id){
                it.quantidade = alimento.quantidade
            }
        }

        adaptersRefeicoes[index].setAlimentos(alimentosRefeicoes[index].toList())

        updateValores(index)
    }

    private fun rmAlimento(index: Int, id: Int){

        var toRemove: Alimento? = null

        alimentosRefeicoes[index].forEach{
            if(it.id == id){
                toRemove = it
            }
        }

        if(toRemove != null) alimentosRefeicoes[index].remove(toRemove!!)

        adaptersRefeicoes[index].setAlimentos(alimentosRefeicoes[index].toList())

        updateValores(index)
    }

    override fun onEditExercicio(dialog: DialogFragment, exercicio: Exercicio){

        exerciciosTreinoDiario.forEach {
            if(it.id == exercicio.id){
                it.valores = exercicio.valores
            }
        }

        adapterTreino.setExercicios(exerciciosTreinoDiario.toList())

        dialog.dismiss()
    }

    override fun onDeleteExercicio(dialog: DialogFragment, id: Int){

        var toRemove: Exercicio? = null

        exerciciosTreinoDiario.forEach{
            if(it.id == id){
                toRemove = it
            }
        }

        if(toRemove != null) exerciciosTreinoDiario.remove(toRemove!!)

        adapterTreino.setExercicios(exerciciosTreinoDiario.toList())

        dialog.dismiss()
    }

    private fun updateValores(index: Int){

        var caloriasRefeicao: Float = 0F
        var caloriasTotal: Float = 0F
        var proteinasRefeicao: Float = 0F
        var proteinasTotal: Float = 0F
        var lipidosRefeicao: Float = 0F
        var lipidosTotal: Float = 0F
        var hidratosRefeicao: Float = 0F
        var hidratosTotal: Float = 0F

        alimentosRefeicoes[index].forEach {

            var tipo: Int = it.tipo!!.substring(0, it.tipo!!.indexOfAny(listOf(" ", "m", "g"), 0, false)).toInt()

            caloriasRefeicao += (it.calorias!!.times(it.quantidade!! / tipo))
            hidratosRefeicao += (it.hidratosCarbono!!.times(it.quantidade!! / tipo))
            proteinasRefeicao += (it.proteinas!!.times(it.quantidade!! / tipo))
            lipidosRefeicao += (it.lipidos!!.times(it.quantidade!! / tipo))
        }

        alimentosRefeicoes.forEach {
            it.forEach {
                var tipo: Int = it.tipo!!.substring(0, it.tipo!!.indexOfAny(listOf(" ", "m", "g"), 0, false)).toInt()

                caloriasTotal += (it.calorias!!.times(it.quantidade!! / tipo))
                hidratosTotal += (it.hidratosCarbono!!.times(it.quantidade!! / tipo))
                proteinasTotal += (it.proteinas!!.times(it.quantidade!! / tipo))
                lipidosTotal += (it.lipidos!!.times(it.quantidade!! / tipo))
            }
        }

        when(index){
            0 -> {
                findViewById<TextView>(R.id.homePequenoAlmocoCalorias).text = "Calorias: " + caloriasRefeicao.toString() + "kcal"
                findViewById<TextView>(R.id.homePequenoAlmocoHidratos).text = "Hidratos de carbono: " + hidratosRefeicao.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toString() + "g"
                findViewById<TextView>(R.id.homePequenoAlmocoProteinas).text = "Proteínas: " + proteinasRefeicao.toString() + "g"
                findViewById<TextView>(R.id.homePequenoAlmocoLipidos).text = "Lípidos: " + lipidosRefeicao.toString() + "g"
            }
            1 -> {
                findViewById<TextView>(R.id.homeAlmocoCalorias).text = "Calorias: " + caloriasRefeicao.toString() + "kcal"
                findViewById<TextView>(R.id.homeAlmocoHidratos).text = "Hidratos de carbono: " + hidratosRefeicao.toString() + "g"
                findViewById<TextView>(R.id.homeAlmocoProteinas).text = "Proteínas: " + proteinasRefeicao.toString() + "g"
                findViewById<TextView>(R.id.homeAlmocoLipidos).text = "Lípidos: " + lipidosRefeicao.toString() + "g"
            }
            2 -> {
                findViewById<TextView>(R.id.homeLancheCalorias).text = "Calorias: " + caloriasRefeicao.toString() + "kcal"
                findViewById<TextView>(R.id.homeLancheHidratos).text = "Hidratos de carbono: " + hidratosRefeicao.toString() + "g"
                findViewById<TextView>(R.id.homeLancheProteinas).text = "Proteínas: " + proteinasRefeicao.toString() + "g"
                findViewById<TextView>(R.id.homeLancheLipidos).text = "Lípidos: " + lipidosRefeicao.toString() + "g"
            }
            3 -> {
                findViewById<TextView>(R.id.homeJantarCalorias).text = "Calorias: " + caloriasRefeicao.toString() + "kcal"
                findViewById<TextView>(R.id.homeJantarHidratos).text = "Hidratos de carbono: " + hidratosRefeicao.toString() + "g"
                findViewById<TextView>(R.id.homeJantarProteinas).text = "Proteínas: " + proteinasRefeicao.toString() + "g"
                findViewById<TextView>(R.id.homeJantarLipidos).text = "Lípidos: " + lipidosRefeicao.toString() + "g"
            }
        }

        findViewById<TextView>(R.id.homeTotalCalorias).text = "Calorias: " + caloriasTotal.toString() + "kcal"
        findViewById<TextView>(R.id.homeTotalHidratos).text = "Hidratos de carbono: " + hidratosTotal.toString() + "g"
        findViewById<TextView>(R.id.homeTotalProteinas).text = "Proteínas: " + proteinasTotal.toString() + "g"
        findViewById<TextView>(R.id.homeTotalLipidos).text = "Lípidos: " + lipidosTotal.toString() + "g"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openCalendar(view: View){

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this@HomePage, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            changeDate(year, monthOfYear, dayOfMonth)
        }, year, month, day)

        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun changeDate(year: Int, month: Int, day: Int){

        var monthFromatted: String
        var dayFormatted: String
        val c = Calendar.getInstance()

        if(month + 1 < 10) monthFromatted = "0" + (month + 1).toString()
        else monthFromatted = (month + 1).toString()

        if(day < 10) dayFormatted = "0" + day.toString()
        else dayFormatted = day.toString()

        if(c.get(Calendar.YEAR) * 10000 + c.get(Calendar.MONTH) * 100 + c.get(Calendar.DAY_OF_MONTH) < year * 10000 + month * 100 + day){
            Toast.makeText(this@HomePage, getString(R.string.registoFuturo), Toast.LENGTH_SHORT).show()
        } else {
            getRegistoDia(year.toString() + "-" + monthFromatted + "-" + dayFormatted)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRegistoDia(data: String){

        findViewById<TextView>(R.id.homeData).text = data

        findViewById<TextView>(R.id.homePequenoAlmocoId).text = "-1"
        findViewById<TextView>(R.id.homeAlmocoId).text = "-1"
        findViewById<TextView>(R.id.homeLancheId).text = "-1"
        findViewById<TextView>(R.id.homeJantarId).text = "-1"
        findViewById<TextView>(R.id.homeTreinoId).text = "-1"

        //open shared preferences and get the access token to make a request
        var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

        val accessToken: String? = sharedPreferences.getString("accessToken", "")

        val authorization = "Bearer $accessToken"

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getRegisto(authorization, data)

        call.enqueue(object : Callback<RegistoOutput> {
            override fun onResponse(call: Call<RegistoOutput>, response: Response<RegistoOutput>) {
                if(response.isSuccessful) {

                    alimentosRefeicoes.forEach {
                        it.clear()
                    }

                    exerciciosTreinoDiario.clear()

                    if(response.body()!!.registo!!.pequenoAlmoco.id != null){
                        findViewById<TextView>(R.id.homePequenoAlmocoId).text = response.body()!!.registo!!.pequenoAlmoco.id.toString()
                    }
                    response.body()!!.registo!!.pequenoAlmoco.alimentos.forEach {
                        alimentosRefeicoes[0].add(it)
                    }

                    findViewById<TextView>(R.id.homeAlmocoId).text = response.body()!!.registo!!.almoco.id.toString()
                    response.body()!!.registo!!.almoco.alimentos.forEach {
                        alimentosRefeicoes[1].add(it)
                    }

                    findViewById<TextView>(R.id.homeLancheId).text = response.body()!!.registo!!.lanche.id.toString()
                    response.body()!!.registo!!.lanche.alimentos.forEach {
                        alimentosRefeicoes[2].add(it)
                    }

                    findViewById<TextView>(R.id.homeJantarId).text = response.body()!!.registo!!.jantar.id.toString()
                    response.body()!!.registo!!.jantar.alimentos.forEach {
                        alimentosRefeicoes[3].add(it)
                    }

                    findViewById<TextView>(R.id.homeTreinoId).text = response.body()!!.registo!!.treino.id.toString()
                    response.body()!!.registo!!.treino.exercicios.forEach {
                        exerciciosTreinoDiario.add(it)
                    }

                    for(i in 0..3){
                        adaptersRefeicoes[i].setAlimentos(alimentosRefeicoes[i].toList())
                        updateValores(i)
                    }

                    adapterTreino.setExercicios(exerciciosTreinoDiario)

                    if(data == LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))){
                        updateNotifValues()
                    }

                } else {
                    //if the call is not successful, check the error code, warn the user accordingly and close this activity
                    when (response.code()){
                        400 -> Toast.makeText(this@HomePage, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        404 -> {
                            alimentosRefeicoes.forEach {
                                it.clear()
                            }

                            exerciciosTreinoDiario.clear()

                            for(i in 0..3){
                                adaptersRefeicoes[i].setAlimentos(alimentosRefeicoes[i].toList())
                                updateValores(i)
                            }

                            adapterTreino.setExercicios(exerciciosTreinoDiario)
                        }
                    }
                }
            }

            //if there is a connection error warn the user and close this activity
            override fun onFailure(call: Call<RegistoOutput>, t: Throwable) {
                Toast.makeText(this@HomePage, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
            }
        })

        val call2 = request.getValoresNutricionais(authorization, data)

        call2.enqueue(object : Callback<ValoresNutricionaisOutput> {
            override fun onResponse(call: Call<ValoresNutricionaisOutput>, response: Response<ValoresNutricionaisOutput>) {
                if(response.isSuccessful) {
                    if(response.body()!!.msg != null){
                        Toast.makeText(this@HomePage, getString(R.string.noMeasurements), Toast.LENGTH_SHORT).show()
                        findViewById<TextView>(R.id.homeRecCalorias).text = ""
                        findViewById<TextView>(R.id.homeRecHidratos).text = ""
                        findViewById<TextView>(R.id.homeRecLipidos).text = ""
                        findViewById<TextView>(R.id.homeRecProteinas).text = ""
                    } else {
                        findViewById<TextView>(R.id.homeRecCalorias).text = "/" + response.body()!!.valores!!.calorias.toString() + "kcal"
                        findViewById<TextView>(R.id.homeRecHidratos).text = "/" + response.body()!!.valores!!.hidratosCarbono.toString() + "g"
                        findViewById<TextView>(R.id.homeRecLipidos).text = "/" + response.body()!!.valores!!.lipidos.toString() + "g"
                        findViewById<TextView>(R.id.homeRecProteinas).text = "/" + response.body()!!.valores!!.proteinas.toString() + "g"
                    }
                } else {
                    //if the call is not successful, check the error code, warn the user accordingly and close this activity
                    when (response.code()){
                        400 -> Toast.makeText(this@HomePage, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //if there is a connection error warn the user and close this activity
            override fun onFailure(call: Call<ValoresNutricionaisOutput>, t: Throwable) {
                Toast.makeText(this@HomePage, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUltimaMedida(){

        //open shared preferences and get the access token to make a request
        var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

        val accessToken: String? = sharedPreferences.getString("accessToken", "")

        val authorization = "Bearer $accessToken"

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getUltimaMedida(authorization)

        //make request to get all the information about this page's food item
        call.enqueue(object : Callback<MedidaOutput> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<MedidaOutput>, response: Response<MedidaOutput>) {
                //if the request is successful display all the item's information on the page's elements
                if(response.isSuccessful) {

                    var pedir: Boolean = sharedPreferences.getBoolean("pedirMedidas", true)

                    if(response.body()!!.medida != null){
                        ultimaMedida = response.body()!!.medida

                        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                        if(LocalDate.parse(Instant.now().toString().substring(0, 10), formatter).isAfter(LocalDate.parse(Instant.parse(ultimaMedida!!.data).toString().substring(0, 10), formatter).plusDays(7))){
                            pedirMedida(pedir)
                        }

                    } else pedirMedida(pedir)
                } else {
                    //if the call is not successful, check the error code, warn the user accordingly and close this activity
                    when (response.code()){
                        400 -> Toast.makeText(this@HomePage, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //if there is a connection error warn the user and close this activity
            override fun onFailure(call: Call<MedidaOutput>, t: Throwable) {
                Toast.makeText(this@HomePage, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun clickPedirMedidas(view: View){
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        if(LocalDate.parse(Instant.now().toString().substring(0, 10), formatter).isAfter(LocalDate.parse(Instant.parse(ultimaMedida!!.data).toString().substring(0, 10), formatter).plusDays(7))){
            pedirMedida(true)
        } else {
            Toast.makeText(this@HomePage, getString(R.string.naoPassouUmaSemana), Toast.LENGTH_SHORT).show()
        }
    }

    private fun pedirMedida(pedir: Boolean){

        if(pedir){
            PedirMedidasFragment().show(supportFragmentManager, "PedirMedidasFragment")
        }
    }

    override fun onGuardarMedidas(dialog: DialogFragment, medida: Medida, naoPedir: Boolean) {

        //open shared preferences and get the access token to make a request
        var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

        val accessToken: String? = sharedPreferences.getString("accessToken", "")

        val authorization = "Bearer $accessToken"

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.postMedida(authorization, medida)

        call.enqueue(object : Callback<PostOutput> {
            override fun onResponse(call: Call<PostOutput>, response: Response<PostOutput>) {
                if(response.isSuccessful) {
                    Toast.makeText(this@HomePage, getString(R.string.registoSucesso), Toast.LENGTH_SHORT).show()

                    getUltimaMedida()
                } else {
                    //if the call is not successful, check the error code, warn the user accordingly and close this activity
                    when (response.code()){
                        400 -> Toast.makeText(this@HomePage, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@HomePage, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //if there is a connection error warn the user and close this activity
            override fun onFailure(call: Call<PostOutput>, t: Throwable) {
                Toast.makeText(this@HomePage, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
            }
        })

        with (sharedPreferences.edit()) {
            putBoolean("pedirMedidas", !naoPedir)
            commit()
        }

        dialog.dismiss()
    }

    override fun onCancelarGuardarMedidas(dialog: DialogFragment, naoPedir: Boolean) {

        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

        with (sharedPreferences.edit()) {
            putBoolean("pedirMedidas", !naoPedir)
            commit()
        }

        dialog.dismiss()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateNotifValues(){

        if(alimentosRefeicoes[0].isNotEmpty()) notifValues.replace("Pequeno Almoço", false, true)
        if(alimentosRefeicoes[1].isNotEmpty()) notifValues.replace("Almoço", false, true)
        if(alimentosRefeicoes[2].isNotEmpty()) notifValues.replace("Lanche", false, true)
        if(alimentosRefeicoes[3].isNotEmpty()) notifValues.replace("Jantar", false, true)
        if(exerciciosTreinoDiario.isNotEmpty()) notifValues.replace("Treino Diário", false, true)
    }

    private fun createNotifChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("8", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}