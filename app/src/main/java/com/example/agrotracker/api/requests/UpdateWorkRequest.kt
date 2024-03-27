package com.example.agrotracker.api.requests

import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

data class UpdateWorkRequest (
    @SerializedName("work_id")
    val workId: Int,
    @SerializedName("end_time")
    val endTime: String,
)