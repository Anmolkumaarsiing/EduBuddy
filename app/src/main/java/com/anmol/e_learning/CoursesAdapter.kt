package com.anmol.e_learning

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class CoursesAdapter(
    private val context: Context,
    private val courses: List<CourseData>,
    private val purchasedCourses: List<String>, // ✅ Added purchasedCourses list
    private val onBuyClick: (CourseData) -> Unit
) : RecyclerView.Adapter<CoursesAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.imageView)
        val titleText: TextView = view.findViewById(R.id.title)
        val detailsText: TextView = view.findViewById(R.id.details)
        val buttonBuy: Button = view.findViewById(R.id.buttonBuy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.titleText.text = course.unitName
        holder.detailsText.text = "Unit ${course.unitNumber}"
        holder.coverImage.load(course.urlToImg)

        // ✅ Check if course is already purchased
        if (purchasedCourses.contains(course.unitName)) {
            holder.buttonBuy.text = "Purchased"
            holder.buttonBuy.isEnabled = false // Disable button for purchased courses
        } else {
            holder.buttonBuy.text = "Buy Now ₹99"
            holder.buttonBuy.isEnabled = true
            holder.buttonBuy.setOnClickListener { onBuyClick(course) }
        }
    }

    override fun getItemCount(): Int = courses.size
}
