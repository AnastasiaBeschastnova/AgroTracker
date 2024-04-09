package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName

data class TechnicsResponse(
    @SerializedName("technic_id")
    val id: Int? = null,
    @SerializedName("technic_name")
    val technicName: String? = null
)