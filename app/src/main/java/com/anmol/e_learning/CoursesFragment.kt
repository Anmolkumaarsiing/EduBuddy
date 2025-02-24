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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import org.json.JSONObject

class CoursesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CoursesAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedCourse: CourseData? = null
    private var purchasedCourses: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_courses, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewCourses)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutCourses)
        recyclerView.layoutManager = LinearLayoutManager(context)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fetchPurchasedCourses()

        swipeRefreshLayout.setOnRefreshListener {
            fetchPurchasedCourses()
        }

        return view
    }

    private fun fetchPurchasedCourses() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("purchased_courses")
            .get()
            .addOnSuccessListener { documents ->
                purchasedCourses = documents.map { it.id }
                fetchCourses()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load purchased courses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchCourses() {
        swipeRefreshLayout.isRefreshing = true

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://67b9a31351192bd378ddfbe4.mockapi.io/api/v1/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(CoursesApiService::class.java)

        apiService.getCourses().enqueue(object : retrofit2.Callback<CoursesResponse> {
            override fun onResponse(call: retrofit2.Call<CoursesResponse>, response: retrofit2.Response<CoursesResponse>) {
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful) {
                    val courses = response.body() ?: emptyList()
                    adapter = CoursesAdapter(requireContext(), courses, purchasedCourses) { course ->
                        selectedCourse = course
                        startPayment()
                    }
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(context, "Failed to fetch courses", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<CoursesResponse>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_4A8Ub8SegRrx0U") // ✅ Your Razorpay test key

        try {
            val options = JSONObject()
            options.put("name", "E-Learning")
            options.put("description", "Purchase ${selectedCourse?.unitName}")
            options.put("currency", "INR")
            options.put("amount", 9900) // ✅ 99 Rupees (Razorpay takes amount in paise)
            options.put("prefill.email", auth.currentUser?.email ?: "test@example.com")
            options.put("prefill.contact", "9999999999")

            // ✅ Open Razorpay Payment from Parent Activity
            val activity = requireActivity()
            checkout.open(activity, options)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Payment Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ✅ Called by MainActivity after successful payment
    fun handlePaymentSuccess(paymentId: String?) {
        Toast.makeText(context, "Payment Successful! ID: $paymentId", Toast.LENGTH_LONG).show()

        val userId = auth.currentUser?.uid ?: return
        val course = selectedCourse ?: return

        val purchasedCourse = hashMapOf(
            "courseId" to course.unitName,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .collection("purchased_courses")
            .document(course.unitName)
            .set(purchasedCourse)
            .addOnSuccessListener {
                Toast.makeText(context, "Course Unlocked!", Toast.LENGTH_SHORT).show()
                fetchPurchasedCourses()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error unlocking course", Toast.LENGTH_SHORT).show()
            }
    }

    fun handlePaymentError(errorCode: Int, errorMessage: String?) {
        Toast.makeText(context, "Payment Failed: $errorMessage", Toast.LENGTH_LONG).show()
    }

}
