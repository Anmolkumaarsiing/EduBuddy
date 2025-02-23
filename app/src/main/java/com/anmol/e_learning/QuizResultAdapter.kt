package com.anmol.e_learning

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuizResultAdapter(private val results: List<QuizResultData>) :
    RecyclerView.Adapter<QuizResultAdapter.ResultViewHolder>() {

    class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewQuestion: TextView = view.findViewById(R.id.textViewQuestion)
        val textViewSelectedAnswer: TextView = view.findViewById(R.id.textViewSelectedAnswer)
        val textViewCorrectAnswer: TextView = view.findViewById(R.id.textViewCorrectAnswer)
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
        val textViewMarks: TextView = view.findViewById(R.id.textViewMarks)
        val imageCorrect: ImageView = view.findViewById(R.id.imageCorrect)
        val imageWrong: ImageView = view.findViewById(R.id.imageWrong)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        holder.textViewQuestion.text = "Q${position + 1}: ${result.question}"
        holder.textViewSelectedAnswer.text = "Your Answer: ${result.selectedAnswer}"
        holder.textViewCorrectAnswer.text = "Correct Answer: ${result.correctAnswer}"
        holder.textViewDescription.text = "Explanation: ${result.description}"
        holder.textViewMarks.text = "Marks: ${result.marks} / 1"

        // Show correct ✅ or wrong ❌ icon
        if (result.selectedAnswer == result.correctAnswer) {
            holder.imageCorrect.visibility = View.VISIBLE
            holder.imageWrong.visibility = View.GONE
        } else {
            holder.imageCorrect.visibility = View.GONE
            holder.imageWrong.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = results.size
}
