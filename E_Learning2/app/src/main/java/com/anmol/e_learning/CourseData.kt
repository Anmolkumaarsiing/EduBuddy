package com.anmol.e_learning

import com.google.gson.annotations.SerializedName

data class CourseData(
    val title: String,
    @SerializedName("first_publish_year")
    val firstPublishYear: Int?,
    @SerializedName("cover_i")
    val coverId: Int?,
    @SerializedName("author_name")
    val authorNames: List<String>?
)
data class CoursesResponse(
    @SerializedName("docs")
    val docs: List<CourseData>
)
