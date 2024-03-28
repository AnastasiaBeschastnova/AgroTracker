package com.example.agrotracker.converters

import com.example.agrotracker.admin.WorklistItemModel
import com.example.agrotracker.api.responses.WorklistResponse
import java.util.Date

fun WorklistResponse.toWorklistItemModel(): WorklistItemModel {
    return WorklistItemModel(
        fieldName = this.fieldName.orEmpty(),
        workType = this.workTypeName.orEmpty(),
        endTime = this.endTime.toString(),//.orEmpty(),
        technic= this.technicName.orEmpty(),
        startTime = this.startTime.orEmpty(),
        workId = this.id ?: 0,
    )
}

