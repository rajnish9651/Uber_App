package com.trainee.project.uberapp.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.trainee.project.uberapp.R

class SignupActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameTextEmail: EditText
    private lateinit var phoneText: EditText
    private lateinit var signupButton: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fireStore:FirebaseFirestore
    val userId:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mAuth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        signupButton = findViewById(R.id.buttonSignup)
        nameTextEmail = findViewById(R.id.nameTextEmail)
        phoneText = findViewById(R.id.phoneText)
        fireStore = FirebaseFirestore.getInstance()

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val name = nameTextEmail.text.toString()
            val phone = phoneText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = mAuth.currentUser?.uid
                        var documentRefernce:DocumentReference = fireStore.collection("users").document(userId!!)
                        val user = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "phone" to phone
                        )

                        documentRefernce.set(user).addOnSuccessListener {
                            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }

                    } else {
                        Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
