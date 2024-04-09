package com.example.agrotracker.api.utils

import com.example.agrotracker.api.responses.CulturesResponse
import com.example.agrotracker.api.responses.FieldsResponse
import com.example.agrotracker.api.responses.TechnicsResponse
import com.example.agrotracker.api.responses.WorktypesResponse

fun List<WorktypesResponse>.getWIdByName(name: String): Int? {
    return this.find { it.worktypeName == name }?.id
}

fun List<TechnicsResponse>.getTIdByName(name: String): Int? {
    return this.find { it.technicName == name }?.id
}

fun List<CulturesResponse>.getCIdByName(name: String): Int? {
    return this.find { it.cultureName == name }?.id
}

fun List<FieldsResponse>.getFIdByName(name: String): Int? {
    return this.find { it.fieldName == name }?.id
}