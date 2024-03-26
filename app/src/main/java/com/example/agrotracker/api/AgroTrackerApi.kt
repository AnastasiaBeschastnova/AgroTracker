package com.example.agrotracker.api

import com.example.agrotracker.api.responses.AuthInfoResponse
import com.example.agrotracker.api.responses.WorklistResponse
import com.example.agrotracker.api.responses.WorkInfoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface AgroTrackerApi {
    @GET("/agro_tracker/users")
    suspend fun login(@Query("login") login: String, @Query("password") password: String) : AuthInfoResponse


    @GET("/agro_tracker/works")
    suspend fun getWorklist() : List<WorklistResponse>

    @GET("/agro_tracker/works/{work_id}")
    suspend fun workInfo(@Path("work_id") workId: Int) : WorkInfoResponse

}