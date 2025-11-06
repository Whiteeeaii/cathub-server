package com.cathub.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * 投喂记录
 */
data class FeedLog(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("cat_id")
    val catId: Int,
    
    @SerializedName("food")
    val food: String,
    
    @SerializedName("qty")
    val quantity: String?,
    
    @SerializedName("note")
    val note: String?,
    
    @SerializedName("reporter")
    val reporter: String,
    
    @SerializedName("ts")
    val timestamp: Long
)

data class CreateFeedLogRequest(
    val cat_id: Int,
    val food: String,
    val qty: String?,
    val note: String?,
    val reporter: String = "anonymous"
)

