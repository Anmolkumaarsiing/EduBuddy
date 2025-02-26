package com.anmol.e_learning

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CoursesDao {
    @Query("SELECT * FROM courses ORDER BY unitNumber ASC")
    fun getAllCourses(): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCourses(courses: List<CourseEntity>)

    @Query("DELETE FROM courses")
    fun clearCourses()
}
