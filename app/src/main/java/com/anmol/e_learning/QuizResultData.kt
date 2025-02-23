package com.anmol.e_learning

import java.io.Serializable

data class QuizResultData(
    val question: String = "",
    val selectedAnswer: String = "",
    val correctAnswer: String = "",
    val description: String = "",
    val marks: Int = 0,
    val timestamp: Long = System.currentTimeMillis() // Store quiz attempt timestamp
) : Serializable
