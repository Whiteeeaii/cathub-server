package com.cathub.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

/**
 * 档案详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    catId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val cat by viewModel.currentCat.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(catId) {
        viewModel.loadCat(catId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cat?.name ?: "档案详情") },
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
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "加载失败",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "未知错误",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                cat != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 照片画廊
                        if (cat!!.photos.isNotEmpty()) {
                            InfoSection(title = "照片") {
                                // 主照片
                                AsyncImage(
                                    model = cat!!.photos.first().path,
                                    contentDescription = cat!!.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                // 其他照片缩略图
                                if (cat!!.photos.size > 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(cat!!.photos.drop(1)) { photo ->
                                            AsyncImage(
                                                model = photo.path,
                                                contentDescription = "照片",
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 基本信息
                        InfoSection(title = "基本信息") {
                            InfoRow("名字", cat!!.name)
                            InfoRow("性别", when(cat!!.sex) {
                                "male" -> "公猫"
                                "female" -> "母猫"
                                else -> "未知"
                            })
                            cat!!.ageMonths?.let {
                                InfoRow("年龄", "$it 个月")
                            }
                            cat!!.pattern?.let {
                                InfoRow("花色", it)
                            }
                        }

                        // 最后一次出没
                        if (cat!!.lastSeenAt != null) {
                            InfoSection(title = "最后一次出没") {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                val date = Date(cat!!.lastSeenAt!!)
                                InfoRow("时间", dateFormat.format(date))
                                cat!!.lastSeenLocation?.let {
                                    InfoRow("地点", it)
                                }
                            }
                        }

                        // 活动区域
                        if (cat!!.activityAreas.isNotEmpty()) {
                            InfoSection(title = "活动区域") {
                                Text(
                                    text = cat!!.activityAreas.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // 性格
                        if (cat!!.personality.isNotEmpty()) {
                            InfoSection(title = "性格") {
                                Text(
                                    text = cat!!.personality.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // 食物喜好
                        if (cat!!.foodPreferences.isNotEmpty()) {
                            InfoSection(title = "食物喜好") {
                                Text(
                                    text = cat!!.foodPreferences.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // 投喂建议
                        cat!!.feedingTips?.let {
                            InfoSection(title = "投喂建议") {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // 备注
                        cat!!.notes?.let {
                            InfoSection(title = "备注") {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Divider()
        content()
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

