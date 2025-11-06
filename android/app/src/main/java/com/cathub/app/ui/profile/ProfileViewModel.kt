package com.cathub.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cathub.app.data.model.Cat
import com.cathub.app.data.model.CreateCatRequest
import com.cathub.app.data.repository.CatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 档案 ViewModel
 */
class ProfileViewModel : ViewModel() {
    
    private val repository = CatRepository()
    
    private val _cats = MutableStateFlow<List<Cat>>(emptyList())
    val cats: StateFlow<List<Cat>> = _cats.asStateFlow()
    
    private val _currentCat = MutableStateFlow<Cat?>(null)
    val currentCat: StateFlow<Cat?> = _currentCat.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 加载所有猫咪
     */
    fun loadCats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getCats()
                .onSuccess { cats ->
                    _cats.value = cats
                }
                .onFailure { e ->
                    _error.value = e.message ?: "加载失败"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 加载单个猫咪详情
     */
    fun loadCat(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getCat(id)
                .onSuccess { cat ->
                    _currentCat.value = cat
                }
                .onFailure { e ->
                    _error.value = e.message ?: "加载失败"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 创建猫咪档案
     */
    fun createCat(request: CreateCatRequest, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.createCat(request)
                .onSuccess { catId ->
                    onSuccess(catId)
                }
                .onFailure { e ->
                    _error.value = e.message ?: "创建失败"
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 更新猫咪档案
     */
    fun updateCat(id: Int, request: CreateCatRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.updateCat(id, request)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { e ->
                    _error.value = e.message ?: "更新失败"
                }
            
            _isLoading.value = false
        }
    }
}

