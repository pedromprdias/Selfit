package ipvc.estg.selfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.adapters.MusculoAdapter
import ipvc.estg.selfit.api.Endpoints
import ipvc.estg.selfit.api.ExercicioOutput
import ipvc.estg.selfit.api.Musculo
import ipvc.estg.selfit.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetalhesExercicio : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var listaMusculos: List<Musculo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_exercicio)

        //set the recycler's adapter
        val musculosRecycler = findViewById<RecyclerView>(R.id.maquinaDetalhesExerciciosRecycler)
        val adapter = MusculoAdapter(this)
        musculosRecycler.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        musculosRecycler.layoutManager = linearLayoutManager
        adapter.setMusculos(listOf())

        //call toolbar setup function
        setUpToolbar()
        navigationView = findViewById(R.id.navigation_menu)
        //when a navigation drawer item is clicked do the respective action
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //when clicking home page
                R.id.nav_home -> {
                    //go to home page activity
                    val intent = Intent(this@DetalhesExercicio, HomePage::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking training plans
                R.id.nav_reconhecimento -> {
                    //go to training plans activity
                    val intent = Intent(this@DetalhesExercicio, ReconhecimentoMaquina::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking progress
                R.id.nav_progresso -> {
                    //go to progress activity
                    val intent = Intent(this@DetalhesExercicio, Progresso::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking exercises
                R.id.nav_exercicios -> {
                    //go to exercises activity
                    val intent = Intent(this@DetalhesExercicio, ListaExercicios::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking food
                R.id.nav_alimentos -> {
                    //go to food activity
                    val intent = Intent(this@DetalhesExercicio, ListaAlimentos::class.java)
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
                    val intent = Intent(this@DetalhesExercicio, MainActivity::class.java)
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
        //get the clicked exercise id passed from the exercise list activity
        val id = intent.getIntExtra("ID", 0)

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getExercicio(id, authorization)

        //make request to get all the information about this page's exercise
        call.enqueue(object : Callback<ExercicioOutput> {
            override fun onResponse(call: Call<ExercicioOutput>, response: Response<ExercicioOutput>) {
                //if the request is successful display all the exercise's information on the page's elements
                if(response.isSuccessful) {
                    findViewById<TextView>(R.id.exercicioDetalhesName).text = response.body()!!.exercicio!!.nome
                    findViewById<TextView>(R.id.exercicioDetalhesDescription).text = response.body()!!.exercicio!!.descricao

                    var bitmap: Bitmap = BitmapFactory.decodeByteArray(response.body()!!.exercicio!!.imagem!!.data, 0, response.body()!!.exercicio!!.imagem!!.data.size)

                    findViewById<ImageView>(R.id.exercicioDetalhesImage).setImageBitmap(bitmap)

                    findViewById<TextView>(R.id.exercicioDetalhesMaxPeso).text = "Peso máximo: " + response.body()!!.exercicio!!.maxPeso.toString() + "kg"

                    findViewById<TextView>(R.id.exercicioDetalhesMaquinaName).text = response.body()!!.exercicio!!.maquina!!.nome
                    findViewById<TextView>(R.id.exercicioDetalhesMaquinaId).text = response.body()!!.exercicio!!.maquina!!.id.toString()

                    bitmap = BitmapFactory.decodeByteArray(response.body()!!.exercicio!!.maquina!!.imagem.data, 0, response.body()!!.exercicio!!.maquina!!.imagem.data.size)

                    findViewById<ImageView>(R.id.exercicioDetalhesMaquinaImage).setImageBitmap(bitmap)

                    if(response.body()!!.exercicio!!.dataMaxPeso != ""){
                        findViewById<TextView>(R.id.exercicioDetalhesMaxPesoData).text = "No dia:   " + response.body()!!.exercicio!!.dataMaxPeso!!.dropLast(14)

                    } else {
                        findViewById<TextView>(R.id.exercicioDetalhesMaxPesoData).visibility = View.INVISIBLE

                    }

                    listaMusculos = response.body()!!.exercicio!!.musculos!!
                    adapter.setMusculos(listaMusculos)
                } else {
                    //if the call is not successful, check the error code, warn the user accordingly and close this activity
                    when (response.code()){
                        400 -> Toast.makeText(this@DetalhesExercicio, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@DetalhesExercicio, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@DetalhesExercicio, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            }

            //if there is a connection error warn the user and close this activity
            override fun onFailure(call: Call<ExercicioOutput>, t: Throwable) {
                Toast.makeText(this@DetalhesExercicio, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    //called when the machine is clicked
    fun moveToMaquina(view: View) {

        //get id of the machine
        val id: Int = (view as ViewGroup).findViewById<TextView>(R.id.exercicioDetalhesMaquinaId).text.toString().toInt()

        //move to machine details page and send the it id
        val intent = Intent(this, DetalhesMaquina::class.java).apply {
            putExtra("ID", id)
        }
        startActivity(intent)
        finish()
    }

    //change app toolbar on this activity to custom toolbar
    fun setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayoutDetalhesExercicio)
        val toolbar: Toolbar = findViewById(R.id.toolbarDetalhesExercicio)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolbar_title).text = " - Detalhes de Exercício"
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }
}