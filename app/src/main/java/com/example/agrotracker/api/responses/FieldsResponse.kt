package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName

data class FieldsResponse(
    @SerializedName("field_id")
    val id: Int? = null,
    @SerializedName("field_name")
    val fieldName: String? = null
)