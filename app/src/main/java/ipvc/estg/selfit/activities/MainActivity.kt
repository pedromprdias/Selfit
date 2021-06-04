package ipvc.estg.selfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import ipvc.estg.selfit.R
import ipvc.estg.selfit.api.Endpoints
import ipvc.estg.selfit.api.LoginOutput
import ipvc.estg.selfit.api.LogoutOutput
import ipvc.estg.selfit.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //read shared preferences file and get check if auto login is enable
        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

        val autoLogin: Boolean = sharedPreferences.getBoolean("autoLogin", false)

        //if so, move to home page
        if(autoLogin) {
            val intent = Intent(this@MainActivity, HomePage::class.java)
            startActivity(intent)
            finish()
            //else, open the shared preferences and get the token and if messages should be displayed
        } else {
            var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferencesFile), Context.MODE_PRIVATE)

            val accessToken: String? = sharedPreferences.getString("accessToken", "")
            val displayLogout: Boolean = sharedPreferences.getBoolean("displayLogout", false)
            val authorization = "Bearer $accessToken"

            //clear access token and message display boolean from the preferences
            with (sharedPreferences.edit()) {
                putString("accessToken", "")
                putBoolean("displayLogout", false)
                commit()
            }

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.logout(authorization)

            //make logout request to the api (delete token from database) using the access token
            call.enqueue(object : Callback<LogoutOutput> {
                override fun onResponse(call: Call<LogoutOutput>, response: Response<LogoutOutput>) {
                    //if the request is successful and the user just logged out display success message
                    if(response.isSuccessful) {
                        if(displayLogout) Toast.makeText(this@MainActivity, getString(R.string.loggedOut), Toast.LENGTH_SHORT).show()
                    } else {
                        //if the call is not successful, check the error code and warn the user accordingly
                        when (response.code()){
                            400 -> Toast.makeText(this@MainActivity, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                            401 -> if(displayLogout) Toast.makeText(this@MainActivity, getString(R.string.invalidToken), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                //if there is a connection error warn the user
                override fun onFailure(call: Call<LogoutOutput>, t: Throwable) {
                    Toast.makeText(this@MainActivity, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    //when clicking login button
    fun login(view: View) {

        //get inserted credentials
        var username: String = findViewById<EditText>(R.id.inserir_nome).text.toString()
        var password: String = findViewById<EditText>(R.id.inserir_pass).text.toString()

        //if one of them is empty warn the user and stop
        if(username == "") {
            Toast.makeText(this@MainActivity, getString(R.string.noUsername), Toast.LENGTH_SHORT).show()
        } else if(password == "") {
            Toast.makeText(this@MainActivity, getString(R.string.noPassword), Toast.LENGTH_SHORT).show()
            //else, hash the password
        } else {
            val md: MessageDigest = MessageDigest.getInstance("SHA-256")

            md.update(password.toByteArray(Charsets.UTF_8))
            val clone: MessageDigest = md.clone() as MessageDigest
            val digest: ByteArray = clone.digest()

            val hashedPassword = StringBuilder()

            digest.forEach { byte -> hashedPassword.append(String.format("%02X", byte)) }

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.login(username, hashedPassword.toString())

            //make login request to the api using the inserted credentials
            call.enqueue(object : Callback<LoginOutput> {
                override fun onResponse(call: Call<LoginOutput>, response: Response<LoginOutput>) {
                    //if the call is successful, open the shared preferences and store the access token
                    if(response.isSuccessful) {
                        var sharedPreferences: SharedPreferences = getSharedPreferences(
                            getString(R.string.preferencesFile),
                            Context.MODE_PRIVATE
                        )
                        with(sharedPreferences.edit()) {
                            putString("accessToken", response.body()!!.accessToken)
                            commit()
                        }
                        //if auto login is enabled, store that info in the preferences
                        if (findViewById<CheckBox>(R.id.keepLoggedCheckbox).isChecked) {
                            sharedPreferences = getSharedPreferences(
                                getString(R.string.preferencesFile),
                                Context.MODE_PRIVATE
                            )
                            with(sharedPreferences.edit()) {
                                putBoolean("autoLogin", true)
                                commit()
                            }
                        }
                        //go to the home page
                        val intent = Intent(this@MainActivity, HomePage::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        //if the call is not successful, check the error code and warn the user accordingly
                        when (response.code()){
                            400 -> Toast.makeText(this@MainActivity, getString(R.string.erro), Toast.LENGTH_SHORT).show()
                            401 -> Toast.makeText(this@MainActivity, getString(R.string.credenciaisInvalidas), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                //if there is a connection error warn the user
                override fun onFailure(call: Call<LoginOutput>, t: Throwable) {
                    Toast.makeText(this@MainActivity, getString(R.string.connectionError), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}