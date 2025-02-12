package com.trainee.project.uberapp.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.trainee.project.uberapp.R

class UserProfile : AppCompatActivity() {

    lateinit var userName: TextInputEditText
    lateinit var userEmail: TextInputEditText
    lateinit var userMobile: TextInputEditText
    lateinit var backBtn: ImageView
    lateinit var logOut: Button

    lateinit var fireBaseAuth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        backBtn = findViewById(R.id.backArrow)
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        userMobile = findViewById(R.id.userMobile)
        logOut = findViewById(R.id.logOut)

        firestore = FirebaseFirestore.getInstance()
        fireBaseAuth = FirebaseAuth.getInstance()

        userId = fireBaseAuth.currentUser?.uid

        var sharedPreferences=getSharedPreferences("LogStatus", MODE_PRIVATE)


        if (userId != null) {
            val documentReference: DocumentReference = firestore.collection("users").document(userId!!)


            documentReference.addSnapshotListener(this) { documentSnapshot, e ->
                if (e != null) {

                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Fetch data from the document
                    val name = documentSnapshot.getString("name")
                    val email = documentSnapshot.getString("email")
                    val phone = documentSnapshot.getString("phone")


                    userName.setText(name)
                    userEmail.setText(email)
                    userMobile.setText(phone)
                }
            }
        }


        logOut.setOnClickListener {

            sharedPreferences.edit().clear().apply()
            var int=Intent(this@UserProfile,LoginActivity::class.java)
            startActivity(int)
            finish()
        }
    }
}
