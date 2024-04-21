package com.example.agrotracker.api.requests

import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

data class InsertPointRequest (
    @SerializedName("work_id")
    val workId: Int,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double,
    @SerializedName("point_time")
    val pointTime: String,
)