package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName

data class CulturesResponse(
    @SerializedName("culture_id")
    val id: Int? = null,
    @SerializedName("culture_name")
    val cultureName: String? = null
)