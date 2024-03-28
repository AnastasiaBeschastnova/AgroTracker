package com.example.agrotracker.admin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class WorklistItemModel (
    val workId: Int,
    val fieldName: String,
    val workType: String,
    val endTime: String,
    val technic: String,
    val startTime: String
): Parcelable
