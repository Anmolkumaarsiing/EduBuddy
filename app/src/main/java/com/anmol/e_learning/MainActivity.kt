package com.anmol.e_learning

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.razorpay.PaymentResultListener

class MainActivity : AppCompatActivity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Load HomeFragment by default when the activity starts
        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), "HomeFragment")
        }

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment(), "HomeFragment")
                R.id.nav_courses -> loadFragment(CoursesFragment(), "CoursesFragment")
                R.id.nav_quiz -> loadFragment(QuizFragment(), "QuizFragment")
                R.id.nav_profile -> loadFragment(ProfileFragment(), "ProfileFragment")
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }

    override fun onPaymentSuccess(paymentId: String?) {
        Toast.makeText(this, "Payment Successful! ID: $paymentId", Toast.LENGTH_LONG).show()

        val fragment = supportFragmentManager.findFragmentByTag("CoursesFragment")
        if (fragment is PaymentCallback) {
            fragment.onPaymentSuccess(paymentId)
        }
    }

    override fun onPaymentError(errorCode: Int, errorMessage: String?) {
        Toast.makeText(this, "Payment Failed: $errorMessage", Toast.LENGTH_LONG).show()

        val fragment = supportFragmentManager.findFragmentByTag("CoursesFragment")
        if (fragment is PaymentCallback) {
            fragment.onPaymentError(errorCode, errorMessage)
        }
    }
}
