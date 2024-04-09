package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName

data class StartFormResponse (
    @SerializedName("worktypes")
    val workTypes: List<WorktypesResponse>,
    @SerializedName("cultures")
    val cultures: List<CulturesResponse>,
    @SerializedName("technics")
    val technics: List<TechnicsResponse>,
    @SerializedName("fields")
    val fields: List<FieldsResponse>,
)