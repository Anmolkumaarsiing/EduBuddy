package com.anmol.e_learning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class CoursesAdapter(private val courses: List<CourseData>) :
    RecyclerView.Adapter<CoursesAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.imageView)
        val titleText: TextView = view.findViewById(R.id.title)
        val detailsText: TextView = view.findViewById(R.id.details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.titleText.text = course.title

        // Combine authors and publish year into details
        val authors = course.authorNames?.joinToString(", ") ?: "Unknown Author"
        val publishYear = course.firstPublishYear?.toString() ?: "N/A"
        holder.detailsText.text = "By $authors | Published: $publishYear"

        // Construct cover image URL if coverId is available, else show a placeholder
        if (course.coverId != null) {
            val coverUrl = "https://covers.openlibrary.org/b/id/${course.coverId}-L.jpg"
            holder.coverImage.load(coverUrl) {
                crossfade(true)
                placeholder(R.drawable.placeholder_cover)  // Ensure this placeholder exists in your drawable folder
            }
        } else {
            holder.coverImage.setImageResource(R.drawable.placeholder_cover)
        }
    }

    override fun getItemCount(): Int = courses.size
}
