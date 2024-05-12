package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName

data class UserKeyResponse(
    @SerializedName("user_id")
    val userId: Int?=null,

)