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

    @Multipart
    @POST("api/recognize")
    suspend fun recognizeCat(
        @Part photo: MultipartBody.Part
    ): RecognizeResponse

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
data class RecognizeResponse(val matches: List<CatMatch>, val count: Int)
data class CatMatch(
    val id: Int,
    val name: String,
    val sex: String,
    val age_months: Int?,
    val pattern: String?,
    val activity_areas: List<String>,
    val personality: List<String>,
    val food_preferences: List<String>,
    val feeding_tips: String?,
    val photos: List<Photo>,
    val embeddings: List<String>,
    val created_at: Long,
    val updated_at: Long,
    val similarity: Double
)

