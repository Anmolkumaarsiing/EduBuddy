package com.anmol.e_learning

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameInput = findViewById<TextInputEditText>(R.id.name)
        val emailInput = findViewById<TextInputEditText>(R.id.email)
        val passwordInput = findViewById<TextInputEditText>(R.id.password)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPassword)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val loginLink = findViewById<TextView>(R.id.loginLink)

        registerBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (name.isEmpty()) {
                nameInput.error = "Enter your name"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailInput.error = "Enter your email"
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                passwordInput.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (confirmPassword != password) {
                confirmPasswordInput.error = "Passwords do not match"
                return@setOnClickListener
            }

            registerUser(name, email, password)
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser!!.uid
                    val user = hashMapOf(
                        "userId" to userId,
                        "name" to name,
                        "email" to email
                    )

                    // Store user details in Firestore
                    db.collection("Users").document(userId).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
