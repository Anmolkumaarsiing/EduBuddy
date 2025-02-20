package com.anmol.e_learning

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface QuizApiService {
    @GET("api/v1/questions")
    fun getQuestions(
        @Query("apiKey") apiKey: String,
        @Query("category") category: String,
        @Query("difficulty") difficulty: String,
        @Query("limit") limit: Int
    ): Call<List<QuizQuestion>>
}
