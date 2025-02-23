package com.anmol.e_learning

import com.google.gson.annotations.SerializedName

data class CourseData(
    @SerializedName("unit_number") val unitNumber: Int,
    @SerializedName("unit_name") val unitName: String,
    @SerializedName("url_to_img") val urlToImg: String,  // Correct field name
    @SerializedName("drive_link") val driveLink: String  // Correct field name
)


typealias CoursesResponse = List<CourseData>
