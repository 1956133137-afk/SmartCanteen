package com.example.smartcanteen.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcanteen.data.repository.SmartCanteenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SmartCanteenRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    /**
     * 回归 Retrofit 方式：移除对不可用 JAR 包类的依赖，使用纯手写拦截器逻辑
     */
    fun testSyncWhitelist() {
        viewModelScope.launch {
            _uiEvent.emit("🚀 正在发起同步请求...")
            try {
                // ！！！ 关键：确保 appId 为 20 位长号，deviceNo 为纯英文数字 ！！！
                val response = repository.syncWhitelist(
                    deviceNo = "SN123456789",
                    appId = "11000000000000070107"
                )
                
                val bizContent = response.responseBizContent
                if (bizContent != null && bizContent.isSuccess()) {
                    _uiEvent.emit("✅ 同步成功！共 ${bizContent.wNLCount} 条数据")
                } else {
                    val errorMsg = response.gatewayReturnMsg 
                        ?: bizContent?.returnMsg 
                        ?: "错误码: ${response.gatewayReturnCode ?: "未知"}"
                    _uiEvent.emit("❌ 失败：$errorMsg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvent.emit("⚠️ 网络异常：${e.message}")
            }
        }
    }
}
