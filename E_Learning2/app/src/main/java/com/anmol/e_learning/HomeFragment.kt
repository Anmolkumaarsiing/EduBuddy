package com.anmol.e_learning

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3



class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicator: CircleIndicator3
    private lateinit var imageList: List<Int>
    private var currentPage = 0
    private val handler = Handler(Looper.getMainLooper())

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

        // Now these views should be recognized
        viewPager = view.findViewById(R.id.bannerViewPager)
        indicator = view.findViewById(R.id.indicator)


        // Add sample images to slider
        imageList = listOf(
            R.drawable.img,
            R.drawable.img_1,
            R.drawable.img_2
        )

        val adapter = ImageSliderAdapter(imageList)
        viewPager.adapter = adapter
        indicator.setViewPager(viewPager)

        // Start auto sliding
        handler.postDelayed(sliderRunnable, 3000)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(sliderRunnable)
    }
}
