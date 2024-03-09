package com.example.agrotracker.admin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class WorklistItemModel (
    val fieldName: String,
    val workType: String,
    val culture: String,
    val technic: String,
    val fuel: String,
    val startTime: Date,
    val endTime: Date
): Parcelable
