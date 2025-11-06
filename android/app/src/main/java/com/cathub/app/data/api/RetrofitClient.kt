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
    
    // Render 云端服务器地址
    // 本地测试: http://10.0.2.2:5000/ (模拟器) 或 http://192.168.x.x:5000/ (真机)
    // Render: https://cathub.onrender.com/
    private const val BASE_URL = "https://cathub.onrender.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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

