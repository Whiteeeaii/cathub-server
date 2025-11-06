package com.cathub.app.data.api

import com.cathub.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * Cathub API 接口定义
 */
interface CathubApi {
    
    // ========== 猫咪档案 ==========
    @GET("api/cats")
    suspend fun getCats(): List<Cat>
    
    @GET("api/cats/{id}")
    suspend fun getCat(@Path("id") id: Int): Cat
    
    @POST("api/cats")
    suspend fun createCat(@Body request: CreateCatRequest): CreateCatResponse
    
    @PUT("api/cats/{id}")
    suspend fun updateCat(@Path("id") id: Int, @Body request: CreateCatRequest): MessageResponse
    
    @Multipart
    @POST("api/cats/{id}/photos")
    suspend fun uploadCatPhoto(
        @Path("id") id: Int,
        @Part photo: MultipartBody.Part
    ): UploadPhotoResponse
    
    // ========== 目击记录 ==========
    @POST("api/sightings")
    suspend fun createSighting(@Body request: CreateSightingRequest): CreateSightingResponse
    
    @GET("api/sightings")
    suspend fun getSightings(@Query("cat_id") catId: Int? = null): List<Sighting>
    
    // ========== 健康上报 ==========
    @POST("api/health_reports")
    suspend fun createHealthReport(@Body request: CreateHealthReportRequest): CreateHealthReportResponse
    
    @GET("api/health_reports")
    suspend fun getHealthReports(@Query("cat_id") catId: Int? = null): List<HealthReport>
    
    // ========== 投喂记录 ==========
    @POST("api/feed_logs")
    suspend fun createFeedLog(@Body request: CreateFeedLogRequest): CreateFeedLogResponse
    
    @GET("api/feed_logs")
    suspend fun getFeedLogs(@Query("cat_id") catId: Int? = null): List<FeedLog>
    
    // ========== 健康检查 ==========
    @GET("api/health")
    suspend fun healthCheck(): HealthCheckResponse
}

// 响应数据类
data class CreateCatResponse(val id: Int, val message: String)
data class CreateSightingResponse(val id: Int, val message: String)
data class CreateHealthReportResponse(val id: Int, val message: String)
data class CreateFeedLogResponse(val id: Int, val message: String)
data class MessageResponse(val message: String)
data class UploadPhotoResponse(val path: String, val message: String)
data class HealthCheckResponse(val status: String, val message: String)

