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

    private var recyclerView: RecyclerView? = null
    private var adapter: CoursesAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
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
        recyclerView?.layoutManager = LinearLayoutManager(context)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fetchPurchasedCourses()

        swipeRefreshLayout?.setOnRefreshListener {
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
                context?.let { ctx ->
                    Toast.makeText(ctx, "Failed to load purchased courses", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchCourses() {
        swipeRefreshLayout?.isRefreshing = true

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://67b9a31351192bd378ddfbe4.mockapi.io/api/v1/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(CoursesApiService::class.java)

        apiService.getCourses().enqueue(object : retrofit2.Callback<CoursesResponse> {
            override fun onResponse(call: retrofit2.Call<CoursesResponse>, response: retrofit2.Response<CoursesResponse>) {
                swipeRefreshLayout?.isRefreshing = false
                if (response.isSuccessful) {
                    val courses = response.body() ?: emptyList()

                    if (!isAdded) return // Prevent crash if fragment is detached

                    context?.let { ctx ->
                        adapter = CoursesAdapter(ctx, courses, purchasedCourses) { course ->
                            selectedCourse = course
                            startPayment()
                        }
                        recyclerView?.adapter = adapter
                    }
                } else {
                    context?.let { ctx ->
                        Toast.makeText(ctx, "Failed to fetch courses", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<CoursesResponse>, t: Throwable) {
                swipeRefreshLayout?.isRefreshing = false

                context?.let { ctx ->
                    Toast.makeText(ctx, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
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
            context?.let { ctx ->
                Toast.makeText(ctx, "Payment Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ Called by MainActivity after successful payment
    fun handlePaymentSuccess(paymentId: String?) {
        context?.let { ctx ->
            Toast.makeText(ctx, "Payment Successful! ID: $paymentId", Toast.LENGTH_LONG).show()
        }

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
                context?.let { ctx ->
                    Toast.makeText(ctx, "Course Unlocked!", Toast.LENGTH_SHORT).show()
                }
                fetchPurchasedCourses()
            }
            .addOnFailureListener {
                context?.let { ctx ->
                    Toast.makeText(ctx, "Error unlocking course", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun handlePaymentError(errorCode: Int, errorMessage: String?) {
        context?.let { ctx ->
            Toast.makeText(ctx, "Payment Failed: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
        swipeRefreshLayout = null
        adapter = null
    }
}
