package ipvc.estg.selfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.navigation.NavigationView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.api.AllMedidasOutput
import ipvc.estg.selfit.api.Endpoints
import ipvc.estg.selfit.api.Medida
import ipvc.estg.selfit.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Progresso : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private var listaMedidas: MutableList<Medida> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progresso)

        //call toolbar setup function
        setUpToolbar()
        navigationView = findViewById(R.id.navigation_menu)
        //when a navigation drawer item is clicked do the respective action
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //when clicking home page
                R.id.nav_home -> {
                    //go to home page activity
                    val intent = Intent(this@Progresso, HomePage::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking training plans
                R.id.nav_reconhecimento -> {
                    //go to Reconhecimento de Maquina activity
                    val intent = Intent(this@Progresso, ReconhecimentoMaquina::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking progress
                R.id.nav_progresso -> {
                    //do nothing (already there)
                }
                //when clicking exercises
                R.id.nav_exercicios -> {
                    //go to exercises activity
                    val intent = Intent(this@Progresso, ListaExercicios::class.java)
                    startActivity(intent)
                    finish()
                }
                //when clicking food
                R.id.nav_alimentos -> {
                    //go to food activity
                    val intent = Intent(this@Progresso, ListaAlimentos::class.java)
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
                    val intent = Intent(this@Progresso, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            false
        }

        findViewById<Spinner>(R.id.progressSpinner).onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0 -> updateCharts(1)
                    1 -> updateCharts(6)
                    2 -> updateCharts(12)
                    3 -> updateCharts(-1)
                }
            }
        }

        //open shared preferences and get the access token to make a request
        var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

        val accessToken: String? = sharedPreferences.getString("accessToken", "")

        val authorization = "Bearer $accessToken"

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getAllMedidas(authorization)

        call.enqueue(object : Callback<AllMedidasOutput> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<AllMedidasOutput>, response: Response<AllMedidasOutput>) {
                if(response.isSuccessful) {
                    response.body()!!.listaMedidas!!.forEach {
                        listaMedidas.add(it)
                    }

                    chartStyling()
                    updateCharts(1)
                } else {
                    //if the call is not successful, check the error code and warn the user accordingly
                    when (response.code()){
                        400 -> Toast.makeText(this@Progresso, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                        401 -> Toast.makeText(this@Progresso, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this@Progresso, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //if there is a connection error warn the user
            override fun onFailure(call: Call<AllMedidasOutput>, t: Throwable) {
                Toast.makeText(this@Progresso, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
            }
        })
    }

    //change app toolbar on this activity to custom toolbar
    fun setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayoutProgresso)
        val toolbar: Toolbar = findViewById(R.id.toolbarProgresso)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolbar_title).text = " - Progresso"
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCharts(meses: Int){

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val chartPeso: LineChart = findViewById(R.id.progressChartPeso)
        val chartGordura: LineChart = findViewById(R.id.progressChartGordura)

        val listaPesoChart: MutableList<Entry> = mutableListOf()
        val listaPesoMetaChart: MutableList<Entry> = mutableListOf()
        val listaGorduraChart: MutableList<Entry> = mutableListOf()
        val listaGorduraMetaChart: MutableList<Entry> = mutableListOf()

        var i = 0

        while(i < listaMedidas.size) {
            if(LocalDate.parse(Instant.parse(listaMedidas[i].data).toString().substring(0, 10), formatter).isAfter(LocalDate.parse(Instant.now().toString().substring(0, 10), formatter).plusMonths(meses.toLong() * (-1))) || meses == -1){
                listaPesoChart.add(Entry(i.toFloat(), listaMedidas[i].peso))
                listaPesoMetaChart.add(Entry(i.toFloat(), listaMedidas[i].pesoMeta))
                listaGorduraChart.add(Entry(i.toFloat(), listaMedidas[i].gordura))
                listaGorduraMetaChart.add(Entry(i.toFloat(), listaMedidas[i].gorduraMeta))
            }
            i++
        }

        val lineDataSetPeso = LineDataSet(listaPesoChart, "Peso (kg)")
        lineDataSetPeso.color = Color.YELLOW
        lineDataSetPeso.lineWidth = 3f
        lineDataSetPeso.valueTextSize = 12f
        lineDataSetPeso.circleRadius = 8f
        lineDataSetPeso.circleHoleRadius = 4f
        val lineDataSetPesoMeta = LineDataSet(listaPesoMetaChart, "Meta de Peso (kg)")
        lineDataSetPesoMeta.color = Color.GREEN
        lineDataSetPesoMeta.lineWidth = 3f
        lineDataSetPesoMeta.valueTextSize = 12f
        lineDataSetPesoMeta.valueTextSize = 12f
        lineDataSetPesoMeta.circleRadius = 8f
        lineDataSetPesoMeta.circleHoleRadius = 4f
        val lineDataSetGordura = LineDataSet(listaGorduraChart, "Gordura Corporal (%)")
        lineDataSetGordura.color = Color.YELLOW
        lineDataSetGordura.lineWidth = 3f
        lineDataSetGordura.valueTextSize = 12f
        lineDataSetGordura.valueTextSize = 12f
        lineDataSetGordura.circleRadius = 8f
        lineDataSetGordura.circleHoleRadius = 4f
        val lineDataSetGorduraMeta = LineDataSet(listaGorduraMetaChart, "Meta de Gordura (%)")
        lineDataSetGorduraMeta.color = Color.GREEN
        lineDataSetGorduraMeta.lineWidth = 3f
        lineDataSetGorduraMeta.valueTextSize = 12f
        lineDataSetGorduraMeta.valueTextSize = 12f
        lineDataSetGorduraMeta.circleRadius = 8f
        lineDataSetGorduraMeta.circleHoleRadius = 4f

        var lineDataPesoChart = LineData(listOf(lineDataSetPeso, lineDataSetPesoMeta))
        var lineDataGorduraChart = LineData(listOf(lineDataSetGordura, lineDataSetGorduraMeta))

        chartPeso.data = lineDataPesoChart
        chartGordura.data = lineDataGorduraChart

        chartPeso.invalidate()
        chartGordura.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun chartStyling(){

        val chartPeso: LineChart = findViewById(R.id.progressChartPeso)
        val chartGordura: LineChart = findViewById(R.id.progressChartGordura)

        chartPeso.legend.formSize = 22f
        chartGordura.legend.formSize = 22f

        chartPeso.legend.form = Legend.LegendForm.SQUARE
        chartGordura.legend.form = Legend.LegendForm.SQUARE

        chartPeso.legend.textSize = 14f
        chartGordura.legend.textSize = 14f

        chartPeso.legend.xEntrySpace = 50f
        chartGordura.legend.xEntrySpace = 50f

        chartPeso.setExtraOffsets(5f,5f,5f,15f)
        chartGordura.setExtraOffsets(5f,5f,5f,15f)

        chartPeso.xAxis.textSize = 11f
        chartGordura.xAxis.textSize = 11f

        chartPeso.xAxis.axisLineWidth = 2f
        chartGordura.xAxis.axisLineWidth = 2f

        chartPeso.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartGordura.xAxis.position = XAxis.XAxisPosition.BOTTOM

        chartPeso.axisLeft.textSize = 11f
        chartGordura.axisLeft.textSize = 11f

        chartPeso.axisLeft.axisLineWidth = 2f
        chartGordura.axisLeft.axisLineWidth = 2f

        chartPeso.axisRight.isEnabled = false
        chartGordura.axisRight.isEnabled = false

        chartPeso.description.text = "Peso/Meta de peso ao longo do tempo"
        chartGordura.description.text = "% de gordura/Meta de gordura ao longo do tempo"

        chartPeso.description.textSize = 14f
        chartGordura.description.textSize = 14f

        chartPeso.description.xOffset = 130f
        chartGordura.description.xOffset = 65f

        chartPeso.xAxis.granularity = 1f
        chartGordura.xAxis.granularity = 1f

        chartPeso.xAxis.setValueFormatter(object : ValueFormatter() {

            val formatter = DateTimeFormatter.ofPattern("dd MMM YY")

            override fun getFormattedValue(value: Float): String {

                var date = SimpleDateFormat("yyyy-MM-dd").parse(listaMedidas[value.toInt()].data.substring(0, 10))

                return SimpleDateFormat("dd MMM yy").format(date)
            }
        })

        chartGordura.xAxis.setValueFormatter(object : ValueFormatter() {

            val formatter = DateTimeFormatter.ofPattern("dd MMM YY")

            override fun getFormattedValue(value: Float): String {

                var date = SimpleDateFormat("yyyy-MM-dd").parse(listaMedidas[value.toInt()].data.substring(0, 10))

                return SimpleDateFormat("dd MMM yy").format(date)
            }
        })
    }
}