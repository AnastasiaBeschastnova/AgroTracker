package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName

data class WorktypesResponse(
    @SerializedName("worktype_id")
    val id: Int? = null,
    @SerializedName("worktype_name")
    val worktypeName: String? = null
){
    override fun toString(): String {
        return this.id.toString().orEmpty()+","+this.worktypeName.orEmpty()
    }
}