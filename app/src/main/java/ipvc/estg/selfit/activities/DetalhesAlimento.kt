package ipvc.estg.selfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetalhesAlimento : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_alimento)

        //call toolbar setup function
        setUpToolbar()
        navigationView = findViewById(R.id.navigation_menu)
        //when a navigation drawer item is clicked do the respective action
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //when clicking home page
                R.id.nav_home -> {
                    //go to home page activity
                    val intent = Intent(this@DetalhesAlimento, HomePage::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking training plans
                R.id.nav_reconhecimento -> {
                    //go to training plans activity
                    val intent = Intent(this@DetalhesAlimento, ReconhecimentoMaquina::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking progress
                R.id.nav_progresso -> {
                    //go to progress activity
                    val intent = Intent(this@DetalhesAlimento, Progresso::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking exercises
                R.id.nav_exercicios -> {
                    //go to exercises activity
                    val intent = Intent(this@DetalhesAlimento, ListaExercicios::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking food
                R.id.nav_alimentos -> {
                    //go to food activity
                    val intent = Intent(this@DetalhesAlimento, ListaAlimentos::class.java)
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
                    val intent = Intent(this@DetalhesAlimento, MainActivity::class.java)
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
        //get the clicked food item id passed from the food items list activity
        val id = intent.getIntExtra("ID", 0)

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getAlimento(id, authorization)

        //make request to get all the information about this page's food item
        call.enqueue(object : Callback<AlimentoOutput> {
            override fun onResponse(call: Call<AlimentoOutput>, response: Response<AlimentoOutput>) {
                //if the request is successful display all the item's information on the page's elements
                if(response.isSuccessful) {
                    findViewById<TextView>(R.id.alimentoDetalhesName).text = response.body()!!.alimento!!.nome
                    findViewById<TextView>(R.id.alimentoDetalhesDescription).text = response.body()!!.alimento!!.descricao

                    val bitmap: Bitmap = BitmapFactory.decodeByteArray(response.body()!!.alimento!!.imagem!!.data, 0, response.body()!!.alimento!!.imagem!!.data.size)

                    findViewById<ImageView>(R.id.alimentoDetalhesImage).setImageBitmap(bitmap)

                    findViewById<TextView>(R.id.alimentosDetalhesCaloriasValue).text = response.body()!!.alimento!!.calorias.toString() + "kcal"
                    findViewById<TextView>(R.id.alimentosDetalhesProteinasValue).text = response.body()!!.alimento!!.proteinas.toString() + "g"
                    findViewById<TextView>(R.id.alimentosDetalhesHidratosValue).text = response.body()!!.alimento!!.hidratosCarbono.toString() + "g"
                    findViewById<TextView>(R.id.alimentosDetalhesLipidosValue).text = response.body()!!.alimento!!.lipidos.toString() + "g"
                    findViewById<TextView>(R.id.alimentoDetalhesTipo).text = "Por cada " + response.body()!!.alimento!!.tipo + ":"
                } else {
                    //if the call is not successful, check the error code, warn the user accordingly and close this activity
                    when (response.code()){
                        400 -> Toast.makeText(this@DetalhesAlimento, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@DetalhesAlimento, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@DetalhesAlimento, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
            }

            //if there is a connection error warn the user and close this activity
            override fun onFailure(call: Call<AlimentoOutput>, t: Throwable) {
                Toast.makeText(this@DetalhesAlimento, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    //change app toolbar on this activity to custom toolbar
    fun setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayoutDetalhesAlimento)
        val toolbar: Toolbar = findViewById(R.id.toolbarDetalhesAlimento)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolbar_title).text = " - Detalhes de Alimento"
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }
}