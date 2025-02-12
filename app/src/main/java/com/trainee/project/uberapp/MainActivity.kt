package com.trainee.project.uberapp

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trainee.project.uberapp.login.UserProfile
import com.trainee.project.uberapp.model.Booking
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var myLocation: MyLocation
    private lateinit var currentLocation: ImageView
    private lateinit var searchViewFrom: EditText
    private lateinit var searchViewTo: EditText
    private lateinit var userProfile: ImageView
    private lateinit var bookRide: Button  // "Book" button
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myLocation = MyLocation(this)
        currentLocation = findViewById(R.id.currentLocation)
        searchViewFrom = findViewById(R.id.searchViewFrom)
        searchViewTo = findViewById(R.id.searchViewTo)
        userProfile = findViewById(R.id.userProfile)
        bookRide = findViewById(R.id.buttonBook)  // Book button
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val sharedPreferences = getSharedPreferences("LogStatus", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isFirstTime", true).apply()

        listenForBookingUpdates()
        // Check internet and get location
        if (isInternetAvailable()) {
            myLocation.getCurrentLocation()
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            showNoInternetDialogBox()
        }

        currentLocation.setOnClickListener {
            val sharedPrefs = getSharedPreferences("address", Context.MODE_PRIVATE)
            val curr = sharedPrefs.getString("location", null)
            searchViewFrom.setText(curr)
        }

        userProfile.setOnClickListener {
            startActivity(Intent(this@MainActivity, UserProfile::class.java))
        }

        // Book ride and send request to Firestore
        bookRide.setOnClickListener {
            val fromLoc = searchViewFrom.text.toString().trim()
            val toLoc = searchViewTo.text.toString().trim()
            val userId = auth.currentUser?.uid ?: ""

            if (fromLoc.isEmpty() || toLoc.isEmpty()) {
                Toast.makeText(this, "Please enter both locations", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val booking = Booking(
                id = UUID.randomUUID().toString(),  // Generate unique booking ID
                userId = userId,
                driverId = "",
                fromLocation = fromLoc,
                toLocation = toLoc,
                status = "pending"
            )

            fireStore.collection("bookings").document(booking.id)
                .set(booking)
                .addOnSuccessListener {
                    Toast.makeText(this, "Booking request sent to drivers", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Booking failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showNotificationDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ride Update")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.setCancelable(false)
        builder.create().show()
    }


    private fun listenForBookingUpdates() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("MainActivity", "Error fetching booking updates", error)
                    return@addSnapshotListener
                }

                for (doc in value!!.documents) {
                    val status = doc.getString("status")

                    if (status == "accepted") {
                        val driverName = doc.getString("driverName") ?: "Unknown"
                        val driverPhone = doc.getString("driverPhone") ?: "Not available"

                        if (isAppInForeground()) {
                            // Show dialog if app is open
                            showNotificationDialog("Ride Accepted!\nDriver: $driverName\nPhone: $driverPhone")
                        } else {
                            // Send background notification
                            sendBackgroundNotification("Ride Accepted", "Driver: $driverName\nPhone: $driverPhone")
                        }

                        // Remove booking after notifying user
                        FirebaseFirestore.getInstance().collection("bookings").document(doc.id).delete()
                    }
                }
            }
    }

    private fun sendBackgroundNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ride_updates", "Ride Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, "ride_updates")
            .setSmallIcon(R.drawable.user_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses ?: return false

        for (process in runningProcesses) {
            if (process.processName == packageName &&
                process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }


    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun showNoInternetDialogBox() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Your internet is Off. Please turn it on.")
            .setTitle("No internet connection")
            .setCancelable(false)
            .setPositiveButton("On") { _, _ ->
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
