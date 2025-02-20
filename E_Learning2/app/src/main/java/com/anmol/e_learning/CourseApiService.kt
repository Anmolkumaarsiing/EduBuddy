package com.anmol.e_learning

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CoursesApiService {
    @GET("search.json")
    fun searchCourses(
        @Query("q") query: String
    ): Call<CoursesResponse>
}
