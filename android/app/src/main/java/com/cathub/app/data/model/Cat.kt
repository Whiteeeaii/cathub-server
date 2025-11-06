package com.cathub.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * 猫咪档案数据模型
 */
data class Cat(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("sex")
    val sex: String, // "male", "female", "unknown"
    
    @SerializedName("age_months")
    val ageMonths: Int?,
    
    @SerializedName("pattern")
    val pattern: String?,
    
    @SerializedName("activity_areas")
    val activityAreas: List<String> = emptyList(),
    
    @SerializedName("personality")
    val personality: List<String> = emptyList(),
    
    @SerializedName("food_preferences")
    val foodPreferences: List<String> = emptyList(),
    
    @SerializedName("feeding_tips")
    val feedingTips: String?,
    
    @SerializedName("photos")
    val photos: List<Photo> = emptyList(),
    
    @SerializedName("embeddings")
    val embeddings: List<Embedding> = emptyList(),
    
    @SerializedName("created_at")
    val createdAt: Long = 0,
    
    @SerializedName("updated_at")
    val updatedAt: Long = 0
)

data class Photo(
    @SerializedName("path")
    val path: String,
    
    @SerializedName("uploaded_at")
    val uploadedAt: Long
)

data class Embedding(
    @SerializedName("vec")
    val vector: List<Float>,
    
    @SerializedName("source_photo")
    val sourcePhoto: String,
    
    @SerializedName("ts")
    val timestamp: Long
)

/**
 * 创建猫咪档案的请求
 */
data class CreateCatRequest(
    val name: String,
    val sex: String,
    val age_months: Int?,
    val pattern: String?,
    val activity_areas: List<String>,
    val personality: List<String>,
    val food_preferences: List<String>,
    val feeding_tips: String?,
    val photos: List<Photo> = emptyList(),
    val embeddings: List<Embedding> = emptyList()
)

