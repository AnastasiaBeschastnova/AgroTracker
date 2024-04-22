package com.example.agrotracker.api.responses

import com.google.gson.annotations.SerializedName
import org.osmdroid.util.GeoPoint

//import org.osmdroid.views.overlay.Polygon

data class WorkInfoResponse(
    @SerializedName("work_id")
    val workId: Int?=null,
    @SerializedName("technic_name")
    val technicName: String?=null,
    @SerializedName("culture_name")
    val cultureName: String?=null,
    @SerializedName("field_name")
    val fieldName: String?=null,
    @SerializedName("work_type_name")
    val workTypeName: String?=null,
    @SerializedName("creator_name")
    val creatorName: String?=null,
    @SerializedName("name")
    val name: String?=null,
    @SerializedName("start_time")
    val startTime: String?=null,
    @SerializedName("end_time")
    val endTime: String?=null,
    @SerializedName("fuel")
    val fuel: Int?=null,
    @SerializedName("second_parameter_name")
    val secondParameterName: String?=null,
    @SerializedName("second_parameter_value")
    val secondParameterValue: Int?=null,
    @SerializedName("points")
    val points: List<PointlistResponse>,
    @SerializedName("field_area")
    val fieldArea: List<List<Double>>,

)