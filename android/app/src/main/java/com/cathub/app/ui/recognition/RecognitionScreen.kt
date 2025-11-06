package com.cathub.app.ui.recognition

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 识别页面（占位符）
 * TODO: 实现 CameraX + ML 模型推理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToAddCat: () -> Unit
) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "识别功能开发中",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "将集成 CameraX + TensorFlow Lite",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(onClick = onNavigateToAddCat) {
                    Text("暂时先添加猫咪档案")
                }
            }
        }
    }
}

