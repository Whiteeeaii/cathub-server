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
    
    // TODO: 替换为您的服务器地址
    // 本地测试: http://localhost:5000/
    // ngrok: https://your-ngrok-url.ngrok.io/
    private const val BASE_URL = "http://10.0.2.2:5000/" // Android 模拟器访问本机
    
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

