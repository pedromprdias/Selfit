package ipvc.estg.selfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomePage : AppCompatActivity(),
        ListaAlimentosFragment.ListaAlimentosFragmentListener,
        ListaExerciciosFragment.ListaExerciciosFragmentListener,
        AlterarAlimentoFragment.AlterarAlimentoFragmentListener,
        AlterarExercicioFragment.AlterarExercicioFragmentListener,
        AdicionarAlimentoFragment.AdicionarAlimentoFragmentListener,
        AdicionarExercicioFragment.AdicionarExercicioFragmentListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var adaptersRefeicoes: MutableList<RefeicaoAdapter>
    private lateinit var adapterTreino: TreinoAdapter
    private lateinit var alimentosRefeicoes: MutableList<MutableList<Alimento>>
    private lateinit var exerciciosTreinoDiario: MutableList<Exercicio>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

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
                R.id.nav_treinos -> {
                    //go to training plans activity
                    val intent = Intent(this@HomePage, ListaTreinos::class.java)
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
        val peso: Float = view.findViewById<TextView>(R.id.treinoRecyclerPeso).text.toString().split(" ")[1].toFloat()
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

    }

    fun guardarRefeicao(view: View){

    }

    fun guardarTreino(view: View){

    }

    override fun onClickAlimento(dialog: DialogFragment, alimento: Int){

        val adicionarAlimentoFragment = AdicionarAlimentoFragment()

        val args = Bundle()

        args.putInt("id", alimento)

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

        alimentosRefeicoes[index].add(Alimento(alimento.id))

        adaptersRefeicoes[0].setAlimentos(alimentosRefeicoes[index].toList())
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

    }

    override fun onDeleteAlimento(dialog: DialogFragment, id: Int, refeicao: String){

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

        exerciciosTreinoDiario.forEach{
            if(it.id == id){
                exerciciosTreinoDiario.remove(it)
            }
        }

        adapterTreino.setExercicios(exerciciosTreinoDiario.toList())

        dialog.dismiss()
    }
}