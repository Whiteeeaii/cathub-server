package com.cathub.app.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cathub.app.data.api.RetrofitClient
import com.cathub.app.data.model.Cat
import com.cathub.app.data.model.CreateHealthReportRequest
import kotlinx.coroutines.launch

/**
 * 健康上报页面
 * 支持上报猫咪的受伤、生病、绝育等状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cats by remember { mutableStateOf<List<Cat>>(emptyList()) }
    var selectedCat by remember { mutableStateOf<Cat?>(null) }
    var selectedType by remember { mutableStateOf("injury") }
    var selectedSeverity by remember { mutableStateOf("medium") }
    var note by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showCatDropdown by remember { mutableStateOf(false) }

    // 加载猫咪列表
    LaunchedEffect(Unit) {
        try {
            cats = RetrofitClient.api.getCats()
        } catch (e: Exception) {
            errorMessage = "加载猫咪列表失败: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("状态上报") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 选择猫咪
            ExposedDropdownMenuBox(
                expanded = showCatDropdown,
                onExpandedChange = { showCatDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCat?.name ?: "请选择猫咪",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("猫咪") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCatDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = showCatDropdown,
                    onDismissRequest = { showCatDropdown = false }
                ) {
                    cats.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCat = cat
                                showCatDropdown = false
                            }
                        )
                    }
                }
            }

            // 上报类型
            Text("上报类型", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == "injury",
                    onClick = { selectedType = "injury" },
                    label = { Text("受伤") },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedType == "injury",
                        borderWidth = if (selectedType == "injury") 2.dp else 1.dp
                    )
                )
                FilterChip(
                    selected = selectedType == "sick",
                    onClick = { selectedType = "sick" },
                    label = { Text("生病") },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedType == "sick",
                        borderWidth = if (selectedType == "sick") 2.dp else 1.dp
                    )
                )
                FilterChip(
                    selected = selectedType == "other",
                    onClick = { selectedType = "other" },
                    label = { Text("其他") },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedType == "other",
                        borderWidth = if (selectedType == "other") 2.dp else 1.dp
                    )
                )
            }

            // 严重程度（仅在受伤或生病时显示）
            if (selectedType == "injury" || selectedType == "sick") {
                Text("严重程度", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedSeverity == "low",
                        onClick = { selectedSeverity = "low" },
                        label = { Text("轻微") },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedSeverity == "low",
                            borderWidth = if (selectedSeverity == "low") 2.dp else 1.dp
                        )
                    )
                    FilterChip(
                        selected = selectedSeverity == "medium",
                        onClick = { selectedSeverity = "medium" },
                        label = { Text("中等") },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedSeverity == "medium",
                            borderWidth = if (selectedSeverity == "medium") 2.dp else 1.dp
                        )
                    )
                    FilterChip(
                        selected = selectedSeverity == "high",
                        onClick = { selectedSeverity = "high" },
                        label = { Text("严重") },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedSeverity == "high",
                            borderWidth = if (selectedSeverity == "high") 2.dp else 1.dp
                        )
                    )
                }
            }

            // 详细说明
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("详细说明") },
                placeholder = { Text("请描述具体情况...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            // 上报人
            OutlinedTextField(
                value = reporterName,
                onValueChange = { reporterName = it },
                label = { Text("上报人（可选）") },
                placeholder = { Text("您的昵称") },
                modifier = Modifier.fillMaxWidth()
            )

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

            // 成功信息
            successMessage?.let { success ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = success,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 提交按钮
            Button(
                onClick = {
                    if (selectedCat == null) {
                        errorMessage = "请选择猫咪"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        successMessage = null

                        try {
                            val request = CreateHealthReportRequest(
                                cat_id = selectedCat!!.id,
                                type = selectedType,
                                severity = if (selectedType == "injury" || selectedType == "sick") selectedSeverity else null,
                                note = note.ifBlank { null },
                                reporter = reporterName.ifBlank { "anonymous" }
                            )

                            RetrofitClient.api.createHealthReport(request)
                            successMessage = "上报成功！感谢您的关心。"

                            // 清空表单
                            selectedCat = null
                            selectedType = "injury"
                            selectedSeverity = "medium"
                            note = ""
                            reporterName = ""
                        } catch (e: Exception) {
                            errorMessage = "上报失败: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && selectedCat != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "提交中..." else "提交上报")
            }
        }
    }
}

