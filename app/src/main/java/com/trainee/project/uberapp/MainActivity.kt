package com.trainee.project.uberapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.trainee.project.uberapp.login.UserProfile

class MainActivity : AppCompatActivity() {

    lateinit var myLocation: MyLocation
    lateinit var currentLocation: ImageView
    lateinit var searchViewFrom: EditText
    lateinit var searchViewTo: EditText
    lateinit var userProfile: ImageView
    lateinit var suggestionrecylerview: RecyclerView

    //    lateinit var locationAdapter: LocationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        myLocation = MyLocation(this)
        currentLocation = findViewById(R.id.currentLocation)
        searchViewTo = findViewById(R.id.searchViewTo)
        searchViewFrom = findViewById(R.id.searchViewFrom)
        userProfile = findViewById(R.id.userProfile)
        suggestionrecylerview = findViewById(R.id.suggestionrecylerview)
        var sharedPreferences = getSharedPreferences("LogStatus", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isFirstTime", true).apply()


        // Get the current location of the device
        if (isInternetAvailable()) {
            myLocation.getCurrentLocation()
        } else {

            Toast.makeText(this, "No intenet connection", Toast.LENGTH_SHORT).show()
            showNoInternetDialogBox()
        }

        currentLocation.setOnClickListener {
            val sharedPreferences =
                getSharedPreferences("address", Context.MODE_PRIVATE)
            var curr = sharedPreferences.getString("location", null)
//        Log.d("cityyg", curr.toString())
            searchViewFrom.setText(curr)
        }


        userProfile.setOnClickListener {

            var intent = Intent(this@MainActivity, UserProfile::class.java)
            startActivity(intent)
        }


    }


    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    }

    fun showNoInternetDialogBox() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("your internet is Off. please On it")
        builder.setTitle("No internet connection")
        builder.setCancelable(false)
        builder.setPositiveButton("On") { diallog, which ->
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.create().show()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,

        ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        myLocation.onRequestPermissionsResultMyLocation(requestCode, permissions, grantResults)
    }

    override fun onRestart() {
        super.onRestart()

        Log.d("dataa", "onrestart")
        if (isInternetAvailable()) {
            myLocation.getCurrentLocation()
        } else {
            Toast.makeText(this, "No internet connection ", Toast.LENGTH_SHORT).show()
            showNoInternetDialogBox()
        }
    }
}