package com.cathub.app.data.repository

import com.cathub.app.data.api.RetrofitClient
import com.cathub.app.data.model.Cat
import com.cathub.app.data.model.CreateCatRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * 猫咪档案仓库
 */
class CatRepository {
    
    private val api = RetrofitClient.api
    
    /**
     * 获取所有猫咪列表
     */
    suspend fun getCats(): Result<List<Cat>> = withContext(Dispatchers.IO) {
        try {
            val cats = api.getCats()
            Result.success(cats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取单个猫咪详情
     */
    suspend fun getCat(id: Int): Result<Cat> = withContext(Dispatchers.IO) {
        try {
            val cat = api.getCat(id)
            Result.success(cat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建猫咪档案
     */
    suspend fun createCat(request: CreateCatRequest): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val response = api.createCat(request)
            Result.success(response.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新猫咪档案
     */
    suspend fun updateCat(id: Int, request: CreateCatRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.updateCat(id, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 上传猫咪照片
     */
    suspend fun uploadPhoto(catId: Int, photoFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("photo", photoFile.name, requestBody)
            val response = api.uploadCatPhoto(catId, part)
            Result.success(response.path)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

