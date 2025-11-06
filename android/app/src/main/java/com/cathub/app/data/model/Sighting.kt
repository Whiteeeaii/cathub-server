package com.cathub.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * 目击记录
 */
data class Sighting(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("cat_id")
    val catId: Int,
    
    @SerializedName("photo")
    val photo: String?,
    
    @SerializedName("location")
    val location: String?,
    
    @SerializedName("similarity")
    val similarity: Float?,
    
    @SerializedName("device")
    val device: String?,
    
    @SerializedName("reporter")
    val reporter: String,
    
    @SerializedName("ts")
    val timestamp: Long
)

data class CreateSightingRequest(
    val cat_id: Int,
    val photo: String?,
    val location: String?,
    val similarity: Float?,
    val device: String?,
    val reporter: String = "anonymous"
)

