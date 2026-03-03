package com.example.smartcanteen.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcanteen.data.repository.SmartCanteenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// 注意：这些 import 需要在您导入工行 SDK 后才会正常生效
// import com.icbc.api.DefaultIcbcClient
// import com.icbc.api.request.IcscSyncFaceWhiteRequestV1

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SmartCanteenRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    /**
     * 方案一：使用之前封装的 Retrofit (目前正在解决签名问题)
     */
    fun testSyncWhitelist() {
        viewModelScope.launch {
            _uiEvent.emit("🚀 正在请求 (Retrofit 方式)...")
            try {
                val response = repository.syncWhitelist(
                    deviceNo = "SN666888",
                    appId = "11000000000000006202"
                )
                val bizContent = response.responseBizContent
                if (bizContent != null && bizContent.isSuccess()) {
                    _uiEvent.emit("✅ 同步成功！获取到 ${bizContent.wNLCount} 条数据。")
                } else {
                    _uiEvent.emit("❌ 失败：${bizContent?.returnMsg ?: "未知错误"}")
                }
            } catch (e: Exception) {
                _uiEvent.emit("⚠️ 异常：${e.message}")
            }
        }
    }

    /**
     * 方案二：使用工行官方 SDK 方式 (推荐，更稳定)
     * 注意：使用前请确保 app/libs 中已放入工行 SDK 相关的 jar 包
     */
    fun testSyncWhitelistSdk() {
        viewModelScope.launch(Dispatchers.IO) { // 网络操作必须在 IO 线程
            _uiEvent.emit("🚀 正在通过官方 SDK 同步...")
            try {
                /*
                // ！！！ 取消下面代码的注释需要先配置好 SDK 依赖 ！！！
                val client = DefaultIcbcClient(
                    "10000000000000212516", "RSA2", 
                    "您的私钥", "UTF-8", "json", 
                    "网关公钥", "AES", "5xGJdh7qb+B95SUoxDlatg==", "", ""
                )
                
                val request = IcscSyncFaceWhiteRequestV1()
                request.serviceUrl = "https://gw.open.icbc.com.cn/api/icsc/synchronizeFaceWhite/V1"
                
                val bizContent = IcscSyncFaceWhiteRequestV1.IcscSyncFaceWhiteRequestV1Biz().apply {
                    appId = "110000000"
                    deviceNo = "SN666888"
                    synNum = 20000
                    modifyStatus = "1"
                }
                request.bizContent = bizContent

                val response = client.execute(request, UUID.randomUUID().toString().replace("-", ""))
                
                if (response.isSuccess) {
                    _uiEvent.emit("✅ SDK 同步成功！")
                } else {
                    _uiEvent.emit("❌ SDK 同 msg: ${response.returnMsg}")
                }
                */
            } catch (e: Exception) {
                _uiEvent.emit("⚠️ SDK 调用异常: ${e.message}")
            }
        }
    }
}
