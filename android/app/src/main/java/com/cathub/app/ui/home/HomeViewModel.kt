package com.cathub.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cathub.app.data.api.RetrofitClient
import com.cathub.app.data.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主页 ViewModel
 * 管理事件列表数据
 */
class HomeViewModel : ViewModel() {
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 加载事件列表
     */
    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val eventList = RetrofitClient.api.getEvents(limit = 10)
                _events.value = eventList
            } catch (e: Exception) {
                _error.value = "加载事件失败: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 刷新事件列表
     */
    fun refreshEvents() {
        loadEvents()
    }
}

