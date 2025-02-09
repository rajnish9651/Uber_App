package com.trainee.project.uberapp.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.trainee.project.uberapp.drivers.DriverActivity
import com.trainee.project.uberapp.MainActivity
import com.trainee.project.uberapp.R

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var userRadio: RadioButton
    private lateinit var driverRadio: RadioButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fireStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        // Initialize Views
        emailEditText = findViewById(R.id.editTextEmailLogin)
        passwordEditText = findViewById(R.id.editTextPasswordLogin)
        loginButton = findViewById(R.id.buttonLogin)
        signupButton = findViewById(R.id.buttonSignup)
        radioGroup = findViewById(R.id.radioGroup)
        userRadio = findViewById(R.id.userRadio)
        driverRadio = findViewById(R.id.driverRadio)

        // Check if user is already logged in
        val sharedPreferences = getSharedPreferences("LogStatus", MODE_PRIVATE)
        val userType = sharedPreferences.getString("UserType", "")

        if (mAuth.currentUser != null) {
            when (userType) {
                "User" -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                "Driver" -> {
                    startActivity(Intent(this, DriverActivity::class.java))
                    finish()
                }
            }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if a role is selected
            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select User or Driver", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = mAuth.currentUser?.uid
                        if (userId != null) {
                            val role = if (userRadio.isChecked) "users" else "drivers"

                            fireStore.collection(role).document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        // Save login state
                                        sharedPreferences.edit()
                                            .putString("UserType", if (userRadio.isChecked) "User" else "Driver")
                                            .apply()

                                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                        val intent = if (userRadio.isChecked) {
                                            Intent(this, MainActivity::class.java)
                                        } else {
                                            Intent(this, DriverActivity::class.java)
                                        }
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "No account found in the selected role", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            finish()
        }
    }
}
