package com.trainee.project.uberapp.drivers

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trainee.project.uberapp.R
import com.trainee.project.uberapp.model.Booking

class DriverActivity : AppCompatActivity() {
    private lateinit var driversProfile: ImageView
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)

        driversProfile = findViewById(R.id.driversProfile)
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        driversProfile.setOnClickListener {
            startActivity(Intent(this@DriverActivity, DriverProfile::class.java))
        }

        listenForNewBookings()
    }

    private fun listenForNewBookings() {
        fireStore.collection("bookings")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("DriverActivity", "Error fetching bookings", error)
                    return@addSnapshotListener
                }

                for (doc in value!!.documents) {
                    val booking = doc.toObject(Booking::class.java)
                    if (booking != null) {
                        showBookingDialog(booking)
                    }
                }
            }
    }

    private fun showBookingDialog(booking: Booking) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Ride Request")
        builder.setMessage("From: ${booking.fromLocation}\nTo: ${booking.toLocation}")

        builder.setPositiveButton("Accept") { _, _ ->
            acceptBooking(booking.id, auth.currentUser?.uid ?: "")
        }

        builder.setNegativeButton("Reject") { _, _ ->
            rejectBooking(booking.id)
        }

        builder.setCancelable(false)
        builder.create().show()
    }

    private fun acceptBooking(bookingId: String, driverId: String) {
        val fireStore = FirebaseFirestore.getInstance()

        // Fetch driver details
        fireStore.collection("drivers").document(driverId).get()
            .addOnSuccessListener { document ->
                val driverName = document.getString("name") ?: "Unknown"
                val driverPhone = document.getString("phone") ?: "Not available"

                // Update booking with driver details
                fireStore.collection("bookings").document(bookingId)
                    .update(
                        "driverId", driverId,
                        "status", "accepted",
                        "driverName", driverName,
                        "driverPhone", driverPhone
                    )
                    .addOnSuccessListener {
                        Toast.makeText(this, "Booking Accepted!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get driver details", Toast.LENGTH_SHORT).show()
            }
    }


    private fun rejectBooking(bookingId: String) {
        fireStore.collection("bookings").document(bookingId)
            .update("status", "rejected")
            .addOnSuccessListener {
                Toast.makeText(this, "Booking Rejected", Toast.LENGTH_SHORT).show()
            }
    }
}
