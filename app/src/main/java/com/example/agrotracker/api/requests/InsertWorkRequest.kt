package com.example.agrotracker.api.requests

import com.google.gson.annotations.SerializedName
import retrofit2.http.Query

data class InsertWorkRequest (
    @SerializedName("culture_id")
    val cultureId: Int,
    @SerializedName("technic_id")
    val technicId: Int,
    @SerializedName("field_id")
    val fieldId: Int,
    @SerializedName("work_type_id")
    val workTypeId: Int,
    @SerializedName("creator_id")
    val creatorId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("start_time")
    val startTime: String,
)