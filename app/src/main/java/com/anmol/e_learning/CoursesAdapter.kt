package com.anmol.e_learning

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import coil.load

class CoursesAdapter(private val context: Context, private val courses: List<CourseData>) :
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
        holder.titleText.text = course.unitName

        // Details
        holder.detailsText.text = "Unit ${course.unitNumber}"

        // Load Image
        holder.coverImage.load(course.urlToImg) {
            crossfade(true)
            placeholder(R.drawable.placeholder_cover)
        }

        // Open Google Drive link in-app on Image Click
        holder.coverImage.setOnClickListener {
            val driveLink = course.driveLink
            if (driveLink.isNotEmpty()) {
                val intent = Intent(context, DriveWebViewActivity::class.java)
                intent.putExtra("DRIVE_URL", driveLink)
                context.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int = courses.size

    private fun openInCustomTab(context: Context, url: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}
