package com.anmol.e_learning

import com.google.gson.annotations.SerializedName

data class QuizData(
    @SerializedName("question") val question: String,
    @SerializedName("option1") val option1: String,
    @SerializedName("option2") val option2: String,
    @SerializedName("option3") val option3: String,
    @SerializedName("option4") val option4: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("description") val description: String
)

typealias QuizResponse = List<QuizData>
