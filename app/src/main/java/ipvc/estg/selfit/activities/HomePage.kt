package ipvc.estg.selfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import ipvc.estg.selfit.R
import ipvc.estg.selfit.api.Endpoints
import ipvc.estg.selfit.api.LogoutOutput
import ipvc.estg.selfit.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomePage : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        //call toolbar setup function
        setUpToolbar()
        navigationView = findViewById(R.id.navigation_menu)
        //when a navigation drawer item is clicked do the respective action
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_sus -> Toast.makeText(this@HomePage, "SUS", Toast.LENGTH_SHORT).show()
                R.id.nav_amogus -> Toast.makeText(this@HomePage, "AMOGUS", Toast.LENGTH_SHORT).show()
                //when clicking logout, disable autologin and enable logout messages
                R.id.nav_logout -> {
                    val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

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
        drawerLayout = findViewById(R.id.drawerLayout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.toolbar_title).text = " - Página Principal"
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }
}