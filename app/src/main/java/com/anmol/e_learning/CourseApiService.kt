package com.anmol.e_learning

import retrofit2.Call
import retrofit2.http.GET

interface CoursesApiService {
    @GET("Django")
    fun getCourses(): Call<CoursesResponse>
}
