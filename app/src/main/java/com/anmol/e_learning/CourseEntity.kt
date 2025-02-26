package com.anmol.e_learning

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val unitName: String,
    val unitNumber: Int,
    val urlToImg: String,
    val driveLink: String
)

// ✅ Convert CourseData to CourseEntity (For Room Database Storage)
fun CourseData.toCourseEntity(): CourseEntity {
    return CourseEntity(
        unitName = this.unitName,
        unitNumber = this.unitNumber,
        urlToImg = this.urlToImg ?: "",
        driveLink = this.driveLink ?: ""
    )
}

// ✅ Convert CourseEntity to CourseData (For Displaying in UI)
fun CourseEntity.toCourseData(): CourseData {
    return CourseData(
        unitName = this.unitName,
        unitNumber = this.unitNumber,
        urlToImg = this.urlToImg,
        driveLink = this.driveLink
    )
}
