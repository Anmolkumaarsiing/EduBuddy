package com.anmol.e_learning

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView  // Import this line
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import me.relex.circleindicator.CircleIndicator3

class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicator: CircleIndicator3
    private lateinit var imageList: List<Int>
    private var currentPage = 0
    private val handler = Handler(Looper.getMainLooper())

    // Runnable to automatically change the image every 3 seconds
    private val sliderRunnable = object : Runnable {
        override fun run() {
            if (viewPager.adapter != null) {
                currentPage = (currentPage + 1) % imageList.size
                viewPager.currentItem = currentPage
                handler.postDelayed(this, 3000) // Change image every 3 sec
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize ViewPager and Indicator
        viewPager = view.findViewById(R.id.bannerViewPager)
        indicator = view.findViewById(R.id.indicator)

        // Add sample images to the slider
        imageList = listOf(
            R.drawable.img,      // Replace with your actual drawable resources
            R.drawable.img_1,
            R.drawable.img_2
        )

        // Set up the ImageSliderAdapter for ViewPager2
        val adapter = ImageSliderAdapter(imageList)
        viewPager.adapter = adapter
        indicator.setViewPager(viewPager)

        // Start auto sliding
        handler.postDelayed(sliderRunnable, 3000)

        // Handle Start Learning Now click to navigate to CoursesFragment
        val startLearningText: TextView = view.findViewById(R.id.startLearningText)
        startLearningText.setOnClickListener {
            // Check if the fragment is already loaded before proceeding
            val fragmentTransaction = parentFragmentManager.beginTransaction()

            // Avoid re-adding the fragment if it's already present
            val existingFragment = parentFragmentManager.findFragmentByTag(CoursesFragment::class.java.simpleName)
            if (existingFragment == null) {
                fragmentTransaction.replace(R.id.fragment_container, CoursesFragment(), CoursesFragment::class.java.simpleName) // Replace with your fragment container ID
                fragmentTransaction.addToBackStack(null)  // Add to back stack for navigation
                fragmentTransaction.commit()
            }

            // Update BottomNavigationView to reflect "Courses" selected
            val bottomNavigationView: BottomNavigationView? = activity?.findViewById(R.id.bottom_navigation)
            bottomNavigationView?.selectedItemId = R.id.nav_courses  // Set the selected item to Courses
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(sliderRunnable)  // Remove the sliding handler to avoid memory leaks
    }
}
