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

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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

        // Redirect to LoginActivity after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}
