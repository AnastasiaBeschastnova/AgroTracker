package com.example.agrotracker.api.requests

import com.google.gson.annotations.SerializedName

data class InsertWorkParameterValuesRequest (
    @SerializedName("work_id")
    val workId: Int,
    @SerializedName("fuel")
    val fuel: Int,
    @SerializedName("second_parameter_value")
    val secondParameterValue: Int,
)