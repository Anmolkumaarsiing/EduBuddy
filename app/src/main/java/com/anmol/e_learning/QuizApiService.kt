package com.anmol.e_learning

import retrofit2.Call
import retrofit2.http.GET

interface QuizApiService {
    @GET("Quiz")
    fun getQuizQuestions(): Call<QuizResponse>
}
