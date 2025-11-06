package com.cathub.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * 事件数据模型
 */
data class Event(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("event_type")
    val eventType: String, // "sighting", "health_report", etc.
    
    @SerializedName("cat_id")
    val catId: Int?,
    
    @SerializedName("cat_name")
    val catName: String?,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("location")
    val location: String?,
    
    @SerializedName("latitude")
    val latitude: Double?,
    
    @SerializedName("longitude")
    val longitude: Double?,
    
    @SerializedName("created_at")
    val createdAt: Long
)

