package com.cathub.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * 健康上报
 */
data class HealthReport(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("cat_id")
    val catId: Int,
    
    @SerializedName("type")
    val type: String, // "injury", "sick", "neutered", "other"
    
    @SerializedName("severity")
    val severity: String?, // "low", "medium", "high"
    
    @SerializedName("note")
    val note: String?,
    
    @SerializedName("photos")
    val photos: List<String> = emptyList(),
    
    @SerializedName("reporter")
    val reporter: String,
    
    @SerializedName("ts")
    val timestamp: Long,
    
    @SerializedName("status")
    val status: String // "pending", "resolved"
)

data class CreateHealthReportRequest(
    val cat_id: Int,
    val type: String,
    val severity: String?,
    val note: String?,
    val photos: List<String> = emptyList(),
    val reporter: String = "anonymous"
)

