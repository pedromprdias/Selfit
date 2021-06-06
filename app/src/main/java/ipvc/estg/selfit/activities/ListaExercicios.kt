package ipvc.estg.selfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.adapters.ExercicioAdapter
import ipvc.estg.selfit.api.AllExerciciosOutput
import ipvc.estg.selfit.api.Endpoints
import ipvc.estg.selfit.api.Exercicio
import ipvc.estg.selfit.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListaExercicios : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var listaExercicios: List<Exercicio>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_exercicios)

        //set the recycler's adapter
        val exerciciosRecycler = findViewById<RecyclerView>(R.id.exerciciosListRecycler)
        val adapter = ExercicioAdapter(this)
        exerciciosRecycler.adapter = adapter
        exerciciosRecycler.layoutManager = LinearLayoutManager(this)
        adapter.setExercicios(listOf())

        //call toolbar setup function
        setUpToolbar()
        navigationView = findViewById(R.id.navigation_menu)
        //when a navigation drawer item is clicked do the respective action
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //when clicking home page
                R.id.nav_home -> {
                    //go to home page activity
                    val intent = Intent(this@ListaExercicios, HomePage::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking training plans
                R.id.nav_treinos -> {
                    //go to training plans activity
                    val intent = Intent(this@ListaExercicios, ListaTreinos::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking progress
                R.id.nav_progresso -> {
                    //go to progress activity
                    val intent = Intent(this@ListaExercicios, Progresso::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking exercises
                R.id.nav_exercicios -> {
                    //do nothing (already there)
                }
                //when clicking food
                R.id.nav_alimentos -> {
                    //go to food activity
                    val intent = Intent(this@ListaExercicios, ListaAlimentos::class.java)
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
                    val intent = Intent(this@ListaExercicios, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            false
        }

        //open shared preferences and get the access token to make a request
        var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

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
                        400 -> Toast.makeText(this@ListaExercicios, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@ListaExercicios, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@ListaExercicios, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //if there is a connection error warn the user
            override fun onFailure(call: Call<AllExerciciosOutput>, t: Throwable) {
                listaExercicios = listOf()
                Toast.makeText(this@ListaExercicios, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
            }
        })
    }

    //called when the user clicks on the search button
    fun filterExercicios(view: View) {

        //takes the user search inputs
        val search = findViewById<EditText>(R.id.listaExerciciosNomeSearch).text.toString()
        //val muscle = findViewById<Spinner>(R.id.listaExerciciosMusculosSearch).selectedItem.toString()
        val muscle = "Todos"

        val exerciciosRecycler = findViewById<RecyclerView>(R.id.exerciciosListRecycler)
        val adapter = ExercicioAdapter(this)
        exerciciosRecycler.adapter = adapter
        exerciciosRecycler.layoutManager = LinearLayoutManager(this)

        //compares the input to all the existing exercises, displaying only the ones that contain the input in their names
        if(search != "") {
            var novaLista: MutableList<Exercicio> = mutableListOf()

            listaExercicios.forEach{
                if(it.nome.contains(search, true)) {
                    if(muscle == "Todos"){
                        novaLista.add(it)
                    } else {
                        var existe: Boolean = false
                        it.musculos.forEach{
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
                    it.musculos.forEach{
                        if(it.nome == muscle) existe = true
                    }

                    if(existe) novaLista.add(it)
                }

                adapter.setExercicios(novaLista)
            }
        }
    }

    //called when an exercise is clicked
    fun moveToExercicioDetails(view: View) {

        //get id of clicked item
        val id: Int = (view as ViewGroup).findViewById<TextView>(R.id.exerciciosRecyclerId).text.toString().toInt()

        //move to exercise details page and send the clicked exercise id
        val intent = Intent(this, DetalhesExercicio::class.java).apply {
            putExtra("ID", id)
        }
        startActivity(intent)
    }

    //change app toolbar on this activity to custom toolbar
    fun setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayoutListaExercicios)
        val toolbar: Toolbar = findViewById(R.id.toolbarListaExercicios)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolbar_title).text = " - Listagem de Exercícios"
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }
}