package com.trainee.project.uberapp.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trainee.project.uberapp.MyLocation
import com.trainee.project.uberapp.R

class SignupActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var carEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var radioGroup: RadioGroup
    private lateinit var userRadio: RadioButton
    private lateinit var driverRadio: RadioButton
    private lateinit var myLocation: MyLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        radioGroup = findViewById(R.id.radioGroup)
        userRadio = findViewById(R.id.userRadio)
        driverRadio = findViewById(R.id.driverRadio)
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        nameEditText = findViewById(R.id.nameTextEmail)
        phoneEditText = findViewById(R.id.phoneText)
        carEditText = findViewById(R.id.carText)
        signupButton = findViewById(R.id.buttonSignup)

        myLocation = MyLocation(this)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        // Initially hide the car input field
        carEditText.visibility = View.GONE

        // Listen for role selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.driverRadio) {
                carEditText.visibility = View.VISIBLE
            } else {
                carEditText.visibility = View.GONE
            }
        }

        // Get saved latitude and longitude
        val sharedPreferences = getSharedPreferences("address", Context.MODE_PRIVATE)
        val savedLat = sharedPreferences.getFloat("latitude", 0.0f)
        val savedLng = sharedPreferences.getFloat("longitude", 0.0f)

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val vehicle = carEditText.text.toString().trim()
            val isDriver = driverRadio.isChecked

            // Validate required fields
            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty() || (isDriver && vehicle.isEmpty())) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user in Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = mAuth.currentUser?.uid
                        if (userId != null) {
                            val collection = if (isDriver) "drivers" else "users"
                            val userDetails = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "phone" to phone,
                                "latitude" to savedLat,
                                "longitude" to savedLng
                            ).apply {
                                if (isDriver) put("car", vehicle)
                            }

                            // Save to Firestore
                            fireStore.collection(collection).document(userId)
                                .set(userDetails)
                                .addOnSuccessListener {
                                    val role = if (isDriver) "Driver" else "User"
                                    Toast.makeText(this, "Signup Successful as $role", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
