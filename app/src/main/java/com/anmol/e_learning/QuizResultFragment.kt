package com.anmol.e_learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class QuizResultFragment : Fragment() {

    companion object {
        fun newInstance(score: Int, results: ArrayList<QuizResultData>): QuizResultFragment {
            val fragment = QuizResultFragment()
            val args = Bundle()
            args.putInt("score", score)
            args.putSerializable("results", results)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var scoreTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonRetry: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz_result, container, false)

        scoreTextView = view.findViewById(R.id.textViewScore)
        recyclerView = view.findViewById(R.id.recyclerViewResults)
        buttonRetry = view.findViewById(R.id.buttonRetryQuiz)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchLatestQuizResult() // ✅ Ensure only the latest quiz result is fetched

        buttonRetry.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QuizFragment()) // Reload QuizFragment
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    /**
     * Fetch the latest quiz result from Firestore to ensure only the most recent attempt is shown.
     */
    private fun fetchLatestQuizResult() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("quizResults")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING) // ✅ Fetch the latest quiz attempt
            .limit(1) // ✅ Ensures we only retrieve the latest quiz
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    val score = document.getLong("score")?.toInt() ?: 0
                    val answersList = document["answers"] as? List<Map<String, Any>> ?: emptyList()

                    val quizResults = answersList.map {
                        QuizResultData(
                            question = it["question"] as? String ?: "Unknown Question",
                            selectedAnswer = it["selectedAnswer"] as? String ?: "No Answer",
                            correctAnswer = it["correctAnswer"] as? String ?: "Unknown",
                            description = it["description"] as? String ?: "No Explanation",
                            marks = (it["marks"] as? Long)?.toInt() ?: 0
                        )
                    }

                    scoreTextView.text = "Your Score: $score / ${quizResults.size}"
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = QuizResultAdapter(quizResults)
                } else {
                    Toast.makeText(context, "No past quiz results found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load results: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
