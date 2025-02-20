package com.anmol.e_learning

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CoursesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CoursesAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_courses, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(context)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutCourses)

        // Fetch course data on initial load
        fetchCourses()

        // Setup swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener {
            fetchCourses()
        }

        return view
    }

    private fun fetchCourses() {
        swipeRefreshLayout.isRefreshing = true

        val retrofit = Retrofit.Builder()
            .baseUrl("https://openlibrary.org/")  // Base URL for Open Library
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(CoursesApiService::class.java)

        // Use "python" as your query. You can parameterize this if needed.
        val call = apiService.searchCourses("python")
        call.enqueue(object : Callback<CoursesResponse> {
            override fun onResponse(call: Call<CoursesResponse>, response: Response<CoursesResponse>) {
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val coursesResponse = response.body()
                    if (coursesResponse != null) {
                        // Pass the list of courses (docs) to your adapter
                        adapter = CoursesAdapter(coursesResponse.docs)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(context, "No courses found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch courses", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CoursesResponse>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Log.e("CoursesFragment", "Error fetching courses", t)
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
