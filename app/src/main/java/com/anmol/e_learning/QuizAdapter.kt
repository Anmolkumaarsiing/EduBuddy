package com.anmol.e_learning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuizAdapter(private val questions: List<QuizData>) :
    RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    // Store selected answers using question text as the key
    private val selectedAnswers = mutableMapOf<String, String>()  // Use question as key

    inner class QuizViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewQuestion: TextView = view.findViewById(R.id.textViewQuestion)
        val radioGroupOptions: RadioGroup = view.findViewById(R.id.radioGroupOptions)
        val radioOption1: RadioButton = view.findViewById(R.id.radioOption1)
        val radioOption2: RadioButton = view.findViewById(R.id.radioOption2)
        val radioOption3: RadioButton = view.findViewById(R.id.radioOption3)
        val radioOption4: RadioButton = view.findViewById(R.id.radioOption4)

        fun bind(question: QuizData) {
            textViewQuestion.text = question.question

            val options = listOf(radioOption1, radioOption2, radioOption3, radioOption4)
            val optionTexts = listOf(question.option1, question.option2, question.option3, question.option4)

            // Set option texts
            for (i in options.indices) {
                options[i].text = optionTexts[i]
            }

            // Remove previous listener and clear the current selection
            radioGroupOptions.setOnCheckedChangeListener(null)
            radioGroupOptions.clearCheck()

            // Restore previously selected answer if it exists
            selectedAnswers[question.question]?.let { savedAnswer ->
                options.find { it.text.toString() == savedAnswer }?.isChecked = true
            }

            // Save selected answer when changed
            radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
                val selectedOption = options.find { it.id == checkedId }?.text.toString()
                selectedAnswers[question.question] = selectedOption
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_question, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(questions[position])  // Now works correctly!
    }


    override fun getItemCount(): Int = questions.size

    // Get selected answers when the user submits the quiz
    fun getSelectedAnswers(): Map<String, String> = selectedAnswers
}
