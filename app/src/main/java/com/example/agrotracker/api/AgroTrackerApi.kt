package com.example.agrotracker.api

import com.example.agrotracker.api.requests.InsertPointRequest
import com.example.agrotracker.api.requests.InsertWorkParameterValuesRequest
import com.example.agrotracker.api.requests.InsertWorkRequest
import com.example.agrotracker.api.requests.UpdateWorkRequest
import com.example.agrotracker.api.responses.AuthInfoResponse
import com.example.agrotracker.api.responses.CulturesResponse
import com.example.agrotracker.api.responses.FieldsResponse
import com.example.agrotracker.api.responses.PointlistResponse
import com.example.agrotracker.api.responses.SelectWorkIdResponse
import com.example.agrotracker.api.responses.StartFormResponse
import com.example.agrotracker.api.responses.TechnicsResponse
import com.example.agrotracker.api.responses.WorklistResponse
import com.example.agrotracker.api.responses.WorkInfoResponse
import com.example.agrotracker.api.responses.WorktypesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

public interface AgroTrackerApi {
    @GET("/agro_tracker/users")
    suspend fun login(@Query("login") login: String, @Query("password") password: String) : AuthInfoResponse


    @GET("/agro_tracker/works")
    suspend fun getWorklist() : List<WorklistResponse>

    @GET("/agro_tracker/start_form")
    suspend fun getStartForm() : StartFormResponse

    @POST("/agro_tracker/points/insert")
    suspend fun insertPoint(@Body insertPoint: InsertPointRequest) : Any

    @GET("/agro_tracker/works/{work_id}")
    suspend fun workInfo(@Path("work_id") workId: Int) : WorkInfoResponse

    @POST("/agro_tracker/works/insert")
    suspend fun insertWork(@Body insertWork: InsertWorkRequest) : Any

    @GET("/agro_tracker/works/{creator_id}&{start_time}")
    suspend fun selectWorkId(@Path("creator_id") creatorId: Int,
                             @Path("start_time") startTime: String,) : SelectWorkIdResponse

    @POST("/agro_tracker/works/update")
    suspend fun updateWork(@Body updateWork: UpdateWorkRequest) : Any

    @POST("/agro_tracker/work_parameter_values")
    suspend fun insertWorkParameterValues(@Body insertWorkParameterValuesRequest: InsertWorkParameterValuesRequest) : Any

    @GET("/agro_tracker/user_info")
    suspend fun selectUserInfo(@Query("token") token: String) : AuthInfoResponse


}