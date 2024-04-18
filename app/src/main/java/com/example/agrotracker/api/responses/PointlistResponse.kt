package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName
import java.util.Date

data class PointlistResponse(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("latitude")
    val lat: Double? = null,
    @SerializedName("longitude")
    val lon: Double? = null,
    @SerializedName("point_time")
    val pointTime: String? = null,
)