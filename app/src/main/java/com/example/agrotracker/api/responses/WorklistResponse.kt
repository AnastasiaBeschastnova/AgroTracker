package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName
import java.util.Date

data class WorklistResponse(
    @SerializedName("work_id")
    val id: Int? = null,
    @SerializedName("field_name")
    val fieldName: String? = null,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("work_type_name")
    val workTypeName: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("technic_name")
    val technicName: String? = null
)