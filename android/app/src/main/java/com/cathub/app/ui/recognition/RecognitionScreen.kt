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
import androidx.compose.material.icons.filled.PhotoLibrary
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
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 猫咪识别页面
 * 支持拍照或从相册选择照片进行识别
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
            selectedImageUri = photoUri
            // 自动识别
            scope.launch {
                recognizeCat(photoFile,
                    onLoading = { isLoading = it },
                    onSuccess = { recognizedCats = it },
                    onError = { errorMessage = it }
                )
            }
        }
    }

    // 从相册选择
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // 将 URI 转换为文件并识别
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val tempFile = File(context.cacheDir, "temp_cat_photo.jpg")
                    tempFile.outputStream().use { output ->
                        inputStream?.copyTo(output)
                    }
                    recognizeCat(tempFile,
                        onLoading = { isLoading = it },
                        onSuccess = { recognizedCats = it },
                        onError = { errorMessage = it }
                    )
                } catch (e: Exception) {
                    errorMessage = "读取图片失败: ${e.message}"
                }
            }
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
            Text(
                text = "拍照或选择照片识别猫咪",
                style = MaterialTheme.typography.titleMedium
            )

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 拍照按钮
                Button(
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("拍照")
                }

                // 从相册选择
                OutlinedButton(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("相册")
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
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<Cat>) -> Unit,
    onError: (String) -> Unit
) {
    onLoading(true)
    try {
        // TODO: 实际应该调用识别 API
        // 目前简化为返回所有猫咪列表
        val cats = RetrofitClient.api.getCats()
        onSuccess(cats)
        onError("提示: 当前为简化版本，显示所有猫咪。完整版本将使用图像识别技术。")
    } catch (e: Exception) {
        onError("识别失败: ${e.message}")
        onSuccess(emptyList())
    } finally {
        onLoading(false)
    }
}

