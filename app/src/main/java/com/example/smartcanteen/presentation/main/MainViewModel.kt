package com.example.smartcanteen.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcanteen.data.repository.SmartCanteenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider
import javax.inject.Inject
import com.alibaba.fastjson.JSON

// 引入工行官方 SDK 核心类
import com.icbc.api.DefaultIcbcClient
import com.example.smartcanteen.data.remote.model.IcscSyncFaceWhiteRequestV1
import com.example.smartcanteen.data.remote.model.IcscSyncFaceWhiteResponseV1

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SmartCanteenRepository
) : ViewModel() {

    private val TAG = "SmartCanteen_SDK"

    init {
        // 确保 BouncyCastle 已经注册，解决 Android 环境下的 RSA2 签名兼容性问题
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
        Log.d(TAG, "✅ BouncyCastle 加密提供者注册成功")
    }

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    // --- ⚠️ 核心配置参数 ⚠️ ---
    private val myAppId = "11000000000000070107"
    
    // 请确保 myPrivateKey 是你在工行后台对应的【应用私钥】
    private val myPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCEBcRYHRLmUax4GKABuqfqJMblS0qoSZ4fsGhobjUnGpRpzsS7g8QNdioSUEOMH52XywDkRwB2YRSRZo/YDOtStqX8izNJIHM98/IkyPUate9TeIzFUF2yDPc87bskaQAbq03y2b2pmDOELF3FFXpHdZPkNnDJQcSCnQh2Ml27T+qISbcbOUsuk260UIdyUmi2s3fJ6t/QovcYaDrKZTkTNZjvtlcpf7sMPRp+jYNfHZakycaUGc+sHLAM0gczx3GFBuWUiB0m0lgowCHdtLbBvSnr7V1wJVQ+Q+c2opga5arE5qcEQfMDjIMIxlNUsMaWCcyEswRYf6XaJZgNLNAHAgMBAAECggEAINDWJxjC8+DlP8zPKN7ekgKmNaRrYD08QqA0s5CBQcEHlu1AVWPBPZ9bgcXVpGTPZUWeaR+E6+fhluqpYoiDjcG8rHNlQg4A1ytlyuLwmb0TSeozA3CgeXtjkQ9+buRylG4WKAEbdaZRV883V5+pD04YfqdRjZT9ZYHm8oo+ZipxCRJFtnqZxb76nxivNLSKHXCPrUBpf5l/nrxYFf4UfNLZCeYtA/jlG/LyrYX5Ar3fOIFbhUXR+rsaKOmA6Q8uyyIJcqyHXlnEJHAk/x609eDkQ/aM6xC10N0lSNACSocMHMFFVJmyO91m59xgCBG9sP1ybQ8XItQcc6AbL6oD0QKBgQDP8K7hFHRBicFZw/047O2VyfRdSRhq3hWX1T1C0rDYSysfgH0LDmNm5qRuSpx+cLUUpZiQSzSLRllZJW/nC+gtTFSxT1YLZLhwsjtXrwN6S7IbiqS4dbMXj8ZiRhfKJVHTsZW2OKEmiayaz/TgDYl+Y58+sDuoazIh1Ybjmu34hQKBgQCiiTh6bTpE0lnzBjY6mpT2WNsvSBbGSfnJnnErynlIEB8AIhMgYk0/rCYRXqUjWDhjeNmVejBNmRJfa9YDEugZyRr713kBxxy5omQUZqSZlHhWT4NHkst8PYvJTR2ZJd1okc0u72k/jKTuhQQ+T5SRq2ez+nkdukWTPAXYsGVSGwKBgFyG8qpBGcIX9CtnZIQyGT4v0Ua5Qd472EvpnQrhCcQVHqkz2XBUBLNDKiPRm3U/3bPEQ/DhJg5bTWK3n+WljLirYHxNmzISrWeoR3Oq8tc2W5iZxtY1uW1gkpwkKG3Q0WprePzOkha96EozAvm3zFirJ68TaFh26qXXh+A7jmypAoGAZ7BksG2YO6l6Kriuxcox98qN4eb6aHtu9/m1NvuPTbLDJCGY1BMCNvmKu5AGh7rTFlpVbK+ruwVJHEi+Ge3o1fJe3YUDtGei4qY/dGha34NBFcbZ+EIkwFi/Iaeplzb5vakuHaAsI5eE7Ik9QMch/6A6oy3KMHXRxvEGxcsaiDsCgYEAsQTwUfEeweYXecSzURipJMUOhA5SyejYQT8l9PkBO/cg7w7Z373Yog1ITlNnSs9msvVX5AaxZWst+la0pWh2c+2IXRLVem5I2TGt29KQwrCGdnn9/NhAbZbCZPcm0HrKnhLKplqQ/BNpjeCbb1UFbTSj7bjRr7MtRsbu1EdryOI="
    private val icbcGatewayPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMpjaWjngB4E3ATh+G1DVAmQnIpiPEFAEDqRfNGAVvvH35yDetqewKi0l7OEceTMN1C6NPym3zStvSoQayjYV+eIcZERkx31KhtFu9clZKgRTyPjdKMIth/wBtPKjL/5+PYalLdomM4ONthrPgnkN4x4R0+D4+EBpXo8gNiAFsNwIDAQAB"

    /**
     * 清洗密钥字符串
     */
    private fun cleanKey(key: String?): String {
        return key?.replace("-----BEGIN PRIVATE KEY-----", "")
            ?.replace("-----END PRIVATE KEY-----", "")
            ?.replace("-----BEGIN PUBLIC KEY-----", "")
            ?.replace("-----END PUBLIC KEY-----", "")
            ?.replace("\\s".toRegex(), "")
            ?.trim() ?: ""
    }

    fun testSyncWhitelist() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "--------------------------------------------------")
            Log.d(TAG, "🚀 [开始请求] 慧餐通白名单同步")
            _uiEvent.emit("🚀 正在发起同步请求...")
            
            val cleanedPrivateKey = cleanKey(myPrivateKey)
            val cleanedGatewayPublicKey = cleanKey(icbcGatewayPublicKey)
            
            Log.d(TAG, "[配置信息] AppID: $myAppId")

            try {
                // 1. 初始化客户端 - 使用 10 参数构造函数并显式传 null，避免 NPE 和空参数污染
                Log.d(TAG, "1️⃣ 正在初始化 DefaultIcbcClient (10参数 + null模式)...")
                val client = DefaultIcbcClient(
                    myAppId,                // 1. AppID
                    "RSA2",                 // 2. 签名类型
                    myPrivateKey,      // 3. 应用私钥
                    "UTF-8",                // 4. 字符集
                    "json",                 // 5. 格式
                    icbcGatewayPublicKey,// 6. 工行平台公钥
                    "AES",                   // 7. encryptType (设为 null)
                    "JXJMOSihsXJoBJ5CpN4Ttg==",                   // 8. encryptKey (设为 null)
                    "",                   // 9. caPath (设为 null，修复 NPE)
                    ""                    // 10. password (设为 null)
                )

                // 2. 构造请求
                val request = IcscSyncFaceWhiteRequestV1()
                request.setServiceUrl("https://gw.open.icbc.com.cn/api/icsc/synchronizeFaceWhite/V1")

                // 3. 构造业务参数
                Log.d(TAG, "3️⃣ 正在构造业务参数 (BizContent)...")
                val bizContent = IcscSyncFaceWhiteRequestV1.IcscSyncFaceWhiteRequestV1Biz().apply {
                    this.appId = myAppId
                    this.deviceNo = "SN123456789"
                    this.synNum = 20000
                    this.modifyStatus = "1"
                }
                request.setBizContent(bizContent)
                Log.d(TAG, "[请求明文] BizContent JSON: ${JSON.toJSONString(bizContent)}")

                // 4. 执行请求
                val msgId = System.currentTimeMillis().toString()
                Log.d(TAG, "4️⃣ 正在执行 SDK 请求 (msgId: $msgId)...")
                
                val response: IcscSyncFaceWhiteResponseV1 = client.execute(request, msgId)

                // 5. 处理响应
                Log.d(TAG, "5️⃣ 收到 SDK 响应内容: ${JSON.toJSONString(response)}")
                
                if (response.isSuccess) {
                    Log.d(TAG, "✅ [接口成功] 名单总数: ${response.wNLCount}")
                    _uiEvent.emit("✅ 同步成功！名单数: ${response.wNLCount ?: 0}")
                } else {
                    Log.e(TAG, "❌ [业务失败] 错误码: ${response.returnCode}")
                    Log.e(TAG, "❌ [业务失败] 错误说明: ${response.returnMsg}")
                    _uiEvent.emit("❌ 失败：${response.returnMsg} (${response.returnCode})")
                }

            } catch (e: Exception) {
                Log.e(TAG, "🚨 [系统异常] 发生不可预知的错误:", e)
                _uiEvent.emit("⚠️ 异常：${e.message}")
            }
            Log.d(TAG, "--------------------------------------------------")
        }
    }
}
