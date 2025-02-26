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

class CoursesFragment : Fragment(), PaymentCallback {

    private var recyclerView: RecyclerView? = null
    private var adapter: CoursesAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedCourse: CourseData? = null
    private var purchasedCourses: MutableList<String> = mutableListOf()

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
                val newPurchasedCourses = documents.map { it.id }
                Log.d("Firestore", "Purchased courses fetched: $newPurchasedCourses")

                purchasedCourses.clear()
                purchasedCourses.addAll(newPurchasedCourses)

                fetchCourses()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch purchased courses: ${e.message}")
                Toast.makeText(context, "Failed to load purchased courses", Toast.LENGTH_SHORT).show()
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
            override fun onResponse(
                call: retrofit2.Call<CoursesResponse>,
                response: retrofit2.Response<CoursesResponse>
            ) {
                swipeRefreshLayout?.isRefreshing = false

                if (response.isSuccessful) {
                    val courses = response.body() ?: emptyList()
                    Log.d("API", "Courses fetched: ${courses.size}")

                    if (!isAdded) return

                    adapter = CoursesAdapter(requireContext(), courses, purchasedCourses) { course ->
                        selectedCourse = course
                        if (course.unitNumber == 0) {
                            unlockFreeCourse(course)
                        } else {
                            startPayment()
                        }
                    }
                    recyclerView?.adapter = adapter
                    adapter?.notifyDataSetChanged()
                } else {
                    Log.e("API", "Failed to fetch courses: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Failed to fetch courses", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<CoursesResponse>, t: Throwable) {
                swipeRefreshLayout?.isRefreshing = false
                Log.e("API", "Error fetching courses: ${t.message}")
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun unlockFreeCourse(course: CourseData) {
        val userId = auth.currentUser?.uid ?: return

        val freeCourse = hashMapOf(
            "courseId" to course.unitName,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .collection("purchased_courses")
            .document(course.unitName)
            .set(freeCourse)
            .addOnSuccessListener {
                purchasedCourses.add(course.unitName)
                adapter?.notifyDataSetChanged()
                Toast.makeText(context, "Syllabus Unlocked!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("Free Course", "Error unlocking syllabus: ${e.message}")
            }
    }

    private fun startPayment() {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_4A8Ub8SegRrx0U")

        try {
            val options = JSONObject()
            options.put("name", "E-Learning")
            options.put("description", "Purchase ${selectedCourse?.unitName}")
            options.put("currency", "INR")
            options.put("amount", 9900)
            options.put("prefill.email", auth.currentUser?.email ?: "test@example.com")
            options.put("prefill.contact", "9999999999")

            val activity = requireActivity()
            checkout.open(activity, options)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Payment Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        requireActivity().runOnUiThread {
            Toast.makeText(context, "Payment Successful! ID: $paymentId", Toast.LENGTH_LONG).show()

            val userId = auth.currentUser?.uid ?: return@runOnUiThread
            val course = selectedCourse ?: return@runOnUiThread

            val purchasedCourse = hashMapOf(
                "courseId" to course.unitName,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(userId)
                .collection("purchased_courses")
                .document(course.unitName)
                .set(purchasedCourse)
                .addOnSuccessListener {
                    Log.d("Payment", "Course saved in Firestore: ${course.unitName}")

                    purchasedCourses.add(course.unitName)
                    adapter?.notifyDataSetChanged()
                    fetchPurchasedCourses()

                    Toast.makeText(context, "Course Unlocked!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("Payment", "Error saving course: ${e.message}")
                    Toast.makeText(context, "Error unlocking course", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onPaymentError(errorCode: Int, errorMessage: String?) {
        Toast.makeText(context, "Payment Failed: $errorMessage", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        fetchPurchasedCourses()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
        swipeRefreshLayout = null
        adapter = null
    }
}
