package com.fasttimes.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fasting_profiles")
data class FastingProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val duration: Long?,
    val description: String,
    val isFavorite: Boolean = false,
)
