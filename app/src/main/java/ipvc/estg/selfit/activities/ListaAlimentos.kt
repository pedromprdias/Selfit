package ipvc.estg.selfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import ipvc.estg.selfit.adapters.AlimentoAdapter
import ipvc.estg.selfit.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListaAlimentos : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var listaAlimentos: List<Alimento>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_alimentos)

        //set the recycler's adapter
        val alimentosRecycler = findViewById<RecyclerView>(R.id.alimentosListRecycler)
        val adapter = AlimentoAdapter(this, null)
        alimentosRecycler.adapter = adapter
        alimentosRecycler.layoutManager = LinearLayoutManager(this)
        adapter.setAlimentos(listOf())

        //call toolbar setup function
        setUpToolbar()
        navigationView = findViewById(R.id.navigation_menu)
        //when a navigation drawer item is clicked do the respective action
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //when clicking home page
                R.id.nav_home -> {
                    //go to home page activity
                    val intent = Intent(this@ListaAlimentos, HomePage::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking training plans
                R.id.nav_treinos -> {
                    //go to training plans activity
                    val intent = Intent(this@ListaAlimentos, ListaTreinos::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking progress
                R.id.nav_progresso -> {
                    //go to progress activity
                    val intent = Intent(this@ListaAlimentos, Progresso::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking exercises
                R.id.nav_exercicios -> {
                    //go to exercises activity
                    val intent = Intent(this@ListaAlimentos, ListaExercicios::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking food
                R.id.nav_alimentos -> {
                    //do nothing (already there)
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
                    val intent = Intent(this@ListaAlimentos, MainActivity::class.java)
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
                        400 -> Toast.makeText(this@ListaAlimentos, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@ListaAlimentos, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@ListaAlimentos, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //if there is a connection error warn the user
            override fun onFailure(call: Call<AllAlimentosOutput>, t: Throwable) {
                listaAlimentos = listOf()
                Toast.makeText(this@ListaAlimentos, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
            }
        })
    }

    //called when the user clicks on the search button
    fun filterAlimentos(view: View) {

        //takes the user search input
        val search = findViewById<EditText>(R.id.listaAlimentosNomeSearch).text.toString()

        val alimentosRecycler = findViewById<RecyclerView>(R.id.alimentosListRecycler)
        val adapter = AlimentoAdapter(this, null)
        alimentosRecycler.adapter = adapter
        alimentosRecycler.layoutManager = LinearLayoutManager(this)

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

    //called when a food item is clicked
    fun moveToAlimentoDetails(view: View) {

        //get id of clicked item
        val id: Int = (view as  ViewGroup).findViewById<TextView>(R.id.alimentosRecyclerId).text.toString().toInt()

        //move to food item details page and send the clicked food item id
        val intent = Intent(this, DetalhesAlimento::class.java).apply {
            putExtra("ID", id)
        }
        startActivity(intent)
    }

    //change app toolbar on this activity to custom toolbar
    fun setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayoutListaAlimentos)
        val toolbar: Toolbar = findViewById(R.id.toolbarListaAlimentos)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolbar_title).text = " - Listagem de Alimentos"
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }
}