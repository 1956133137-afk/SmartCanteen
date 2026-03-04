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
     * 方案一：使用之前封装的 Retrofit
     */
    fun testSyncWhitelist() {
        viewModelScope.launch {
            _uiEvent.emit("🚀 正在同步白名单...")
            try {
                val response = repository.syncWhitelist(
                    deviceNo = "SN123456789", // 建议使用纯英文数字测试
                    appId = "11000000000000006202"
                )
                
                val bizContent = response.responseBizContent
                
                // 【关键修复】判断逻辑：优先识别业务成功，否则识别网关错误或业务错误
                if (bizContent != null && bizContent.isSuccess()) {
                    _uiEvent.emit("✅ 同步成功！获取到 ${bizContent.wNLCount} 条白名单。")
                } else {
                    // 兼容处理：获取外层网关错误或内层业务错误
                    val errorMsg = response.gatewayReturnMsg 
                        ?: bizContent?.returnMsg 
                        ?: "错误码: ${response.gatewayReturnCode ?: "未知"}"
                    _uiEvent.emit("❌ 同步失败：$errorMsg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiEvent.emit("⚠️ 网络请求异常：${e.message}")
            }
        }
    }
}
