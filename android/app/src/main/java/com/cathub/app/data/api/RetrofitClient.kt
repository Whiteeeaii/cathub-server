package com.cathub.app.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端配置
 */
object RetrofitClient {
    
    // Zeabur 云端服务器地址（上海节点 - 华为云）
    // 本地测试: http://10.0.2.2:5000/ (模拟器) 或 http://192.168.x.x:5000/ (真机)
    // Render: https://cathub.onrender.com/
    // Zeabur: https://cathub.preview.huawei-zeabur.cn/ (上海 → 杭州 5-10ms 延迟)
    private const val BASE_URL = "https://cathub.preview.huawei-zeabur.cn/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)  // 增加到 60 秒
        .readTimeout(120, TimeUnit.SECONDS)    // 增加到 120 秒（AI 识别需要时间）
        .writeTimeout(60, TimeUnit.SECONDS)    // 增加到 60 秒
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: CathubApi = retrofit.create(CathubApi::class.java)
    
    /**
     * 更新服务器地址（用于动态配置 ngrok URL）
     */
    fun updateBaseUrl(newBaseUrl: String): CathubApi {
        val newRetrofit = Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return newRetrofit.create(CathubApi::class.java)
    }
}

