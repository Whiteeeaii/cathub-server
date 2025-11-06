package com.cathub.app.ui.profile

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.cathub.app.data.model.CreateCatRequest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 添加猫咪页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCatScreen(
    onNavigateBack: () -> Unit,
    onCatCreated: (Int) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("unknown") }
    var ageMonths by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("") }
    var activityAreas by remember { mutableStateOf("") }
    var personality by remember { mutableStateOf("") }
    var foodPreferences by remember { mutableStateOf("") }
    var feedingTips by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 相机权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
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
            selectedPhotos = selectedPhotos + photoUri
        }
    }

    // 从相册选择多张照片
    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedPhotos = selectedPhotos + uris
    }

    // 辅助函数：将 Uri 转换为 File
    fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.cacheDir, "upload_$timeStamp.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加猫咪") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 名字
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名字 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 性别
            Text("性别", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = sex == "male",
                    onClick = { sex = "male" },
                    label = { Text("公猫") }
                )
                FilterChip(
                    selected = sex == "female",
                    onClick = { sex = "female" },
                    label = { Text("母猫") }
                )
                FilterChip(
                    selected = sex == "unknown",
                    onClick = { sex = "unknown" },
                    label = { Text("未知") }
                )
            }
            
            // 年龄
            OutlinedTextField(
                value = ageMonths,
                onValueChange = { ageMonths = it.filter { c -> c.isDigit() } },
                label = { Text("年龄（月）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 花色
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                label = { Text("花色") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("例如：三花、狸花、黑白") }
            )
            
            // 活动区域
            OutlinedTextField(
                value = activityAreas,
                onValueChange = { activityAreas = it },
                label = { Text("活动区域") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("用逗号分隔，例如：小区东门,停车场") }
            )
            
            // 性格
            OutlinedTextField(
                value = personality,
                onValueChange = { personality = it },
                label = { Text("性格") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("用逗号分隔，例如：温顺,胆小") }
            )
            
            // 食物喜好
            OutlinedTextField(
                value = foodPreferences,
                onValueChange = { foodPreferences = it },
                label = { Text("食物喜好") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("用逗号分隔，例如：鸡胸肉,幼猫粮") }
            )
            
            // 投喂建议
            OutlinedTextField(
                value = feedingTips,
                onValueChange = { feedingTips = it },
                label = { Text("投喂建议") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("例如：避免乳制品；少量多餐") }
            )

            // 照片上传
            Text("照片（可选）", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 拍照按钮
                OutlinedButton(
                    onClick = {
                        if (hasCameraPermission) {
                            takePictureLauncher.launch(photoUri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("拍照")
                }

                // 从相册选择
                OutlinedButton(
                    onClick = { pickImagesLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("相册")
                }
            }

            // 显示已选择的照片
            if (selectedPhotos.isNotEmpty()) {
                Text("已选择 ${selectedPhotos.size} 张照片", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedPhotos.take(3).forEach { uri ->
                        Box(modifier = Modifier.size(80.dp)) {
                            Card {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "照片",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            IconButton(
                                onClick = { selectedPhotos = selectedPhotos - uri },
                                modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    if (selectedPhotos.size > 3) {
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text("+${selectedPhotos.size - 3}")
                        }
                    }
                }
            }

            // 错误提示
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // 保存按钮
            Button(
                onClick = {
                    if (name.isBlank()) {
                        return@Button
                    }

                    val request = CreateCatRequest(
                        name = name,
                        sex = sex,
                        age_months = ageMonths.toIntOrNull(),
                        pattern = pattern.ifBlank { null },
                        activity_areas = activityAreas.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        personality = personality.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        food_preferences = foodPreferences.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        feeding_tips = feedingTips.ifBlank { null }
                    )

                    // 创建猫咪
                    viewModel.createCat(request) { catId ->
                        // 如果有照片，上传照片
                        if (selectedPhotos.isNotEmpty()) {
                            scope.launch {
                                var uploadedCount = 0
                                var failedCount = 0

                                selectedPhotos.forEach { uri ->
                                    val file = uriToFile(uri)
                                    if (file != null) {
                                        viewModel.uploadPhoto(
                                            catId = catId,
                                            photoFile = file,
                                            onSuccess = {
                                                uploadedCount++
                                                // 所有照片上传完成
                                                if (uploadedCount + failedCount == selectedPhotos.size) {
                                                    onCatCreated(catId)
                                                }
                                            },
                                            onError = { error ->
                                                failedCount++
                                                // 所有照片上传完成（包括失败的）
                                                if (uploadedCount + failedCount == selectedPhotos.size) {
                                                    onCatCreated(catId)
                                                }
                                            }
                                        )
                                    } else {
                                        failedCount++
                                        if (uploadedCount + failedCount == selectedPhotos.size) {
                                            onCatCreated(catId)
                                        }
                                    }
                                }
                            }
                        } else {
                            // 没有照片，直接完成
                            onCatCreated(catId)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && name.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("保存")
                }
            }
        }
    }
}

