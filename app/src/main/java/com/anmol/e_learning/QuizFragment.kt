package com.anmol.e_learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class QuizFragment : Fragment() {

    private var questionContainer: LinearLayout? = null
    private lateinit var buttonSubmit: Button
    private var quizQuestions: List<QuizData> = emptyList()
    private val selectedAnswers = mutableMapOf<Int, String>() // Stores selected answers
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz, container, false)

        questionContainer = view.findViewById(R.id.questionContainer)
        buttonSubmit = view.findViewById(R.id.buttonSubmitQuiz)

        // Initialize Firestore & Auth
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchQuestions()

        buttonSubmit.setOnClickListener {
            saveAnswersAndShowResults()
        }

        return view
    }

    private fun fetchQuestions() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://67b9a31351192bd378ddfbe4.mockapi.io/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(QuizApiService::class.java)

        apiService.getQuizQuestions().enqueue(object : Callback<List<QuizData>> {
            override fun onResponse(call: Call<List<QuizData>>, response: Response<List<QuizData>>) {
                if (response.isSuccessful) {
                    quizQuestions = response.body()?.shuffled()?.take(10) ?: emptyList()
                    if (isAdded) {
                        displayQuestions()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load questions", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<QuizData>>, t: Throwable) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Error loading questions", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun displayQuestions() {
        questionContainer?.removeAllViews()

        for ((index, question) in quizQuestions.withIndex()) {
            val questionTextView = TextView(requireContext()).apply {
                text = "Q${index + 1}: ${question.question}"
                textSize = 18f
                setTextColor(resources.getColor(android.R.color.black, null))
                setPadding(8, 8, 8, 8)
            }

            val radioGroup = RadioGroup(requireContext()).apply {
                orientation = RadioGroup.VERTICAL
            }

            val options = listOf(question.option1, question.option2, question.option3, question.option4)
            for (option in options) {
                val radioButton = RadioButton(requireContext()).apply {
                    text = option
                    setTextColor(resources.getColor(android.R.color.black, null))
                    setOnClickListener { selectedAnswers[index] = option }
                }
                radioGroup.addView(radioButton)
            }

            questionContainer?.addView(questionTextView)
            questionContainer?.addView(radioGroup)
        }
    }

    private fun saveAnswersAndShowResults() {
        buttonSubmit.isEnabled = false // Prevent multiple clicks

        val score = calculateScore(selectedAnswers)
        val userId = auth.currentUser?.uid ?: return
        val questionResults = ArrayList<QuizResultData>()

        for ((index, question) in quizQuestions.withIndex()) {
            val selectedAnswer = selectedAnswers[index] ?: "No Answer"
            val correct = question.correctAnswer
            val isCorrect = selectedAnswer == correct
            val marks = if (isCorrect) 1 else 0

            questionResults.add(
                QuizResultData(
                    question = question.question,
                    selectedAnswer = selectedAnswer,
                    correctAnswer = correct,
                    description = question.description,
                    marks = marks
                )
            )
        }

        saveQuizResultsToFirestore(score, questionResults)
    }

    private fun saveQuizResultsToFirestore(score: Int, results: ArrayList<QuizResultData>) {
        val userId = auth.currentUser?.uid ?: return

        val quizResult = hashMapOf(
            "userId" to userId,
            "score" to score,
            "totalQuestions" to results.size,
            "timestamp" to System.currentTimeMillis(),
            "answers" to results
        )

        db.collection("quizResults")
            .add(quizResult)
            .addOnSuccessListener {
                if (isAdded) navigateToResults(score, results)
            }
            .addOnFailureListener {
                if (isAdded) Toast.makeText(requireContext(), "Failed to save results", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToResults(score: Int, results: ArrayList<QuizResultData>) {
        val fragment = QuizResultFragment.newInstance(score, results)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun calculateScore(selectedAnswers: Map<Int, String>): Int {
        return selectedAnswers.count { (index, answer) -> quizQuestions[index].correctAnswer == answer }
    }
}
