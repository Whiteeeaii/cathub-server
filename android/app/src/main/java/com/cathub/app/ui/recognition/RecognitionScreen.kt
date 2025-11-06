package com.cathub.app.ui.recognition

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.cathub.app.data.api.RetrofitClient
import com.cathub.app.data.model.Cat
import com.cathub.app.utils.LocationHelper
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 猫咪识别页面
 * 进入页面后自动启动相机拍照识别
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToAddCat: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper(context) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var recognizedCats by remember { mutableStateOf<List<Cat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasLocationPermission by remember {
        mutableStateOf(locationHelper.hasLocationPermission())
    }

    // 相机权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // 位置权限请求
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    // 拍照
    val photoFile = remember {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        File(context.cacheDir, "cat_photo_$timeStamp.jpg")
    }

    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoUri
            // 请求位置权限（如果还没有）
            if (!hasLocationPermission) {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            // 自动识别
            scope.launch {
                recognizeCat(
                    photoFile,
                    locationHelper,
                    onLoading = { isLoading = it },
                    onSuccess = { recognizedCats = it },
                    onError = { errorMessage = it }
                )
            }
        } else {
            // 用户取消拍照，返回上一页
            onNavigateBack()
        }
    }

    // 进入页面时自动启动相机
    LaunchedEffect(Unit) {
        if (hasCameraPermission) {
            takePictureLauncher.launch(photoUri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 监听相机权限变化，获得权限后立即启动相机
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission && selectedImageUri == null) {
            takePictureLauncher.launch(photoUri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("猫咪识别") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 说明文字
            if (selectedImageUri == null && !isLoading) {
                Text(
                    text = "正在启动相机...",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 重新拍照按钮（仅在已有照片时显示）
            if (selectedImageUri != null) {
                Button(
                    onClick = {
                        // 清空当前结果
                        selectedImageUri = null
                        recognizedCats = emptyList()
                        errorMessage = null
                        // 重新启动相机
                        takePictureLauncher.launch(photoUri)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重新拍照")
                }
            }

            // 显示选中的图片
            selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "选中的照片",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 加载状态
            if (isLoading) {
                CircularProgressIndicator()
                Text("正在识别...")
            }

            // 错误信息
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // 识别结果
            if (recognizedCats.isNotEmpty()) {
                Text(
                    text = "识别结果",
                    style = MaterialTheme.typography.titleMedium
                )

                recognizedCats.forEach { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigateToProfile(cat.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "性别: ${when(cat.sex) {
                                    "male" -> "公"
                                    "female" -> "母"
                                    else -> "未知"
                                }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            cat.pattern?.let {
                                Text(
                                    text = "花纹: $it",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            } else if (!isLoading && selectedImageUri != null && errorMessage == null) {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("未找到匹配的猫咪")
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onNavigateToAddCat) {
                            Text("添加新猫咪档案")
                        }
                    }
                }
            }
        }
    }
}

// 识别猫咪的函数
private suspend fun recognizeCat(
    imageFile: File,
    locationHelper: LocationHelper,
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<Cat>) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        // 获取位置信息
        val locationInfo = locationHelper.getCurrentLocation()

        // 调用识别 API
        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("photo", imageFile.name, requestBody)

        // 准备位置参数
        val locationPart = locationInfo?.locationName?.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudePart = locationInfo?.latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudePart = locationInfo?.longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = RetrofitClient.api.recognizeCat(part, locationPart, latitudePart, longitudePart)

        // 将 CatMatch 转换为 Cat
        val cats = response.matches.map { match ->
            Cat(
                id = match.id,
                name = match.name,
                sex = match.sex,
                ageMonths = match.age_months,
                pattern = match.pattern,
                activityAreas = match.activity_areas,
                personality = match.personality,
                foodPreferences = match.food_preferences,
                feedingTips = match.feeding_tips,
                notes = match.notes,
                photos = match.photos,
                embeddings = emptyList(), // CatMatch 的 embeddings 是 List<String>，Cat 需要 List<Embedding>
                createdAt = match.created_at,
                updatedAt = match.updated_at
            )
        }

        if (cats.isEmpty()) {
            onError("未找到匹配的猫咪")
        } else {
            onError("找到 ${cats.size} 只相似的猫咪（相似度 ${response.matches.firstOrNull()?.similarity?.toInt() ?: 0}%）")
        }
        onSuccess(cats)
    } catch (e: Exception) {
        onError("识别失败: ${e.message}")
        onSuccess(emptyList())
    } finally {
        onLoading(false)
    }
}

