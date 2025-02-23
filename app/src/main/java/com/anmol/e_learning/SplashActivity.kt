package com.anmol.e_learning

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot


class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        val logo: ImageView = findViewById(R.id.img)
        val appName: TextView = findViewById(R.id.appName)

        // Logo Scale Animation
        val scaleAnimation = ScaleAnimation(
            0.5f, 1.0f,
            0.5f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1200
            fillAfter = true
        }

        // Text Fade-In Animation
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1500
            fillAfter = true
        }

        // Start animations
        logo.startAnimation(scaleAnimation)
        Handler(Looper.getMainLooper()).postDelayed({
            appName.visibility = View.VISIBLE
            appName.startAnimation(fadeIn)
        }, 700)

        // Check if user is logged in and load user data
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLogin()
        }, 3000)
    }

    // Check Firebase authentication and fetch user data
    private fun checkUserLogin() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user != null) {
            val userId = user.uid

            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document: com.google.firebase.firestore.DocumentSnapshot ->
                    if (document.exists()) {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    } else {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    finish()
                }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

}
