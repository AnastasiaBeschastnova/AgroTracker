package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName

data class SelectOperatorWorks(
    @SerializedName("work_id")
    val workId: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("work_type_id")
    val workTypeId: Int? = null,
)