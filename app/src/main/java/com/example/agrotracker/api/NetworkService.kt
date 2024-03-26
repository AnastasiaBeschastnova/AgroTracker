package com.example.agrotracker.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkService private constructor() {
    private val mRetrofit: Retrofit

    init {
        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val agroTrackerApi: AgroTrackerApi
        get() = mRetrofit.create(AgroTrackerApi::class.java)

    companion object {
        private var mInstance: NetworkService? = null
        private const val BASE_URL = "http://192.168.0.104:5000/"
        val instance: NetworkService?
            get() {
                if (mInstance == null) {
                    mInstance = NetworkService()
                }
                return mInstance
            }
    }
}

