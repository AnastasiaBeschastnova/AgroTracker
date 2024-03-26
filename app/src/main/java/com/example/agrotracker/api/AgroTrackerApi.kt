package com.example.agrotracker.api

import com.example.agrotracker.api.responses.AuthInfoResponse
import retrofit2.http.GET
import retrofit2.http.Query

public interface AgroTrackerApi {
    @GET("/agro_tracker/users")
    suspend fun login(@Query("login") login: String, @Query("password") password: String) : AuthInfoResponse
}