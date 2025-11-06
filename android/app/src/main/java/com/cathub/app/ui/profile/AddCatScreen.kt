package com.cathub.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cathub.app.data.model.CreateCatRequest

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
    var name by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("unknown") }
    var ageMonths by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("") }
    var activityAreas by remember { mutableStateOf("") }
    var personality by remember { mutableStateOf("") }
    var foodPreferences by remember { mutableStateOf("") }
    var feedingTips by remember { mutableStateOf("") }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
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
                    
                    viewModel.createCat(request) { catId ->
                        onCatCreated(catId)
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

