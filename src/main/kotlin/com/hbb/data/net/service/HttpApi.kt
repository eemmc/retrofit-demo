package com.hbb.data.net.service

import com.hbb.data.net.request.Register
import com.hbb.data.net.response.UserInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface HttpApi {
    @POST("/info")
    suspend fun register(@Body req: Register): Result<UserInfo>

    @POST("/")
    suspend fun query(@Body req: Register): Result<List<UserInfo>>

    @GET("/home")
    suspend fun check(): Result<List<UserInfo>>
}