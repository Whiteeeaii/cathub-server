package com.cathub.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * 位置信息数据类
 */
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val locationName: String
)

/**
 * 位置辅助类
 * 用于获取用户当前位置和地址信息
 */
class LocationHelper(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    
    /**
     * 检查是否有位置权限
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 获取当前位置信息
     * 返回 LocationInfo 或 null（如果获取失败）
     */
    suspend fun getCurrentLocation(): LocationInfo? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            val location = getCurrentLocationInternal()
            location?.let {
                val locationName = getLocationName(it.latitude, it.longitude)
                LocationInfo(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    locationName = locationName
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 获取当前位置（内部方法）
     */
    private suspend fun getCurrentLocationInternal(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            if (!hasLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            
            val cancellationTokenSource = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener {
                continuation.resume(null)
            }
            
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: SecurityException) {
            continuation.resume(null)
        }
    }
    
    /**
     * 根据经纬度获取地址名称
     */
    private fun getLocationName(latitude: Double, longitude: Double): String {
        return try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    // 优先使用详细地址
                    address.featureName?.let { append(it) }
                    address.thoroughfare?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                    address.subLocality?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                    address.locality?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                    
                    // 如果没有详细地址，使用城市和国家
                    if (isEmpty()) {
                        address.locality?.let { append(it) }
                        address.countryName?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                    }
                }.ifEmpty { "未知位置" }
            } else {
                "未知位置"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "未知位置"
        }
    }
}

