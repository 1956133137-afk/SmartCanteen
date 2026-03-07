package com.example.smartcanteen.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider
import javax.inject.Inject
import com.alibaba.fastjson.JSON
import java.util.*
import javax.net.ssl.*
import java.security.cert.X509Certificate

// 引入工行官方 SDK 核心类
import com.icbc.api.DefaultIcbcClient
import com.example.smartcanteen.data.remote.model.IcscSyncFaceWhiteRequestV1
import com.example.smartcanteen.data.remote.model.IcscSyncFaceWhiteResponseV1

// 引入等下要在 UI 页面定义的数据模型
import com.example.smartcanteen.presentation.whitelist.WhitelistRecord
import com.example.smartcanteen.presentation.whitelist.WhitelistSummary

// 👉 引入刚才新建的数据库 DAO 和实体类
import com.example.smartcanteen.data.local.WhitelistDao
import com.example.smartcanteen.data.local.WhitelistEntity

@HiltViewModel
class MainViewModel @Inject constructor(
    private val whitelistDao: WhitelistDao // 👉 通过 Hilt 注入数据库操作类，绝对不会报错
) : ViewModel() {

    private val TAG = "ICBC_SDK_DEBUG"

    // --- 用于驱动 UI 变化的 StateFlow ---
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _summaryData = MutableStateFlow<WhitelistSummary?>(null)
    val summaryData = _summaryData.asStateFlow()

    private val _records = MutableStateFlow<List<WhitelistRecord>>(emptyList())
    val records = _records.asStateFlow()
    // ------------------------------------------

    init {
        // 1. 注册 BouncyCastle 加密提供者
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())

        // 2. 【仅限开发测试】绕过 SSL 证书校验，排查是否为证书信任问题
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            // 同时也绕过域名校验
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
            Log.d(TAG, "SSL 证书校验已临时绕过（仅限测试）")
        } catch (e: Exception) {
            Log.e(TAG, "设置绕过 SSL 失败", e)
        }
    }

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    // ---  核心配置参数 ---
    private val myAppId = "11000000000000070107"
    private val myPrivateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDACZ1v1qdRRYwcFCQFf+oUXMre6CA1O5ZhKCXC4rWNMT0reovFTw3nv3tBNr0XRHU/hp77R5I+zRPEFH9b3lg9R3DlT698QNz6199p2DedXmCZwjAKWVq78OQMjHnonIFZd832KMCE2eLwChBLQH+UEm7iHAj5FxgYuafbFbWxbyAH2IgaEce2fupca+no33ndRdHhVwx/D+GMkv+ynqrFNT0I2EehYMHIw4AmaWe5mpT2HEJNOdgVX10Ol1DJpqIizMxIuExljVCvFcGiP6VeufoWV7K8jbGVtOq4g/E5faOTB7Kif/+vtwDmY6ZUrCvRM/Kler1rrDClFCqhNlZrAgMBAAECggEAR58uZyhFJfQM0eoXFzACYw7KoQEtBf2P0/OPxdQdByhOmpktaOzXkE/jjcp3Eqb3Hba9M5WZby+4SACnEWHnQg+ThQrHtc1RPYLmsciw0VICMEZy+WEjDIZG34FC9GTufypGGCFR0BqdX445Tn+jNVv8m/r9w7z/wTT47CZ4KdDVRoHqbSXURAwB7TlGv88BiQUI9ADFXnOJIf7gs8RQbAmlt6FMfh5p8Ud5wRqmp+le0LdsNB9dDNvlz+oNCgkg1qIdf4JjKSqOM4eOhsZiWH6SQ1kQYo97WBBoUVXRUuL/jb1aFxWW4zPp5hEdNEWKGWCLst8kJShwHcw6oodSnQKBgQDW6e4HAgcFUbDEeDh24TZjpy22Z7s78NO59Mzqgkp6OKTgcVar22STtYVxBOfvW5MUzHAfhsFmmtlQOnC+gsl/F7YUtr7RymW/l9IaIORmDUroomQ3p6s5syzGiUyYB8F6GfA1t7JrCDegbAiv0DxuPsBRPOBjtnz8JzUQ3a1x1QKBgQDkwBozmXCwXrz7bIcngRKT/kZLx1ingI1hqG/kzf4QRr8WOOiuyKQfdRD7BVPe7dIOkiTIGD9RlDyc6lsmLvt66JQvB8RBeOStwuhTBFBDuTec+4h8Vkx03N+aH+m0zJP3EQcGekSblbB50aWm/icEybVrXFRgWvbauxX46oCHPwKBgQCkBdHWo2N8WcaRjDd785KxJ8yppC2wJ7NP/1fNuzbgZQ7hBV9itoTifu2jPl1NvxRYEVeZmB1PE+u7YX7ex67FQvGCiZ7FOrXBLjv6GRR4FrkPJ7FZEKyL0wXfWLaOaYzuhZFhThvruE/MLefLVyBn+5iH5/BR6dsmDz5e6vEPKQKBgCCeJ95NtdzgVXBAhHEknYKO0nVBwql30jEntHTazqyBegPwL3Wk1IpLxhUVKGV0YeyD+Eyz8GtwiMgTPtYOAvv+qAqgv+JaG7mPPlOAHPXbNkvjLg4UvCg5yoSOomOOfFbRjb/ltVy+FoD4XPeX6/Zp0L2zV7C5p9N+s95fid4/AoGBALZ5yMblQVgxTfQxFkcGpF7E7b0AojkOfrf1wHyvr77sGn4X+BuSXiwvbRNpLvYyIl+QpaAhul7eCotQPPYT3hhd30Be4kmmPeIeBMyXq9lRNOd+xoJiCL9kQBKFczrko+ZnKkRYV5ga59vLi0USttxKWyBA2q73mEiUk18gbRfj"
    private val icbcGatewayPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMpjaWjngB4E3ATh+G1DVAmQnIpiPEFAEDqRfNGAVvvH35yDetqewKi0l7OEceTMN1C6NPym3zStvSoQayjYV+eIcZERkx31KhtFu9clZKgRTyPjdKMIth/wBtPKjL/5+PYalLdomM4ONthrPgnkN4x4R0+D4+EBpXo8gNiAFsNwIDAQAB"
    private val myEncryptKey = "22WlwH8dax4xlulvOoyAyA=="

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
            _isSyncing.value = true // 👉 开启加载动画
            Log.d(TAG, "-------------------- SDK 请求详情 --------------------")
            _uiEvent.emit(" 正在同步白名单...")

            val cleanedPrivateKey = cleanKey(myPrivateKey)
            val cleanedGatewayPublicKey = cleanKey(icbcGatewayPublicKey)

            try {
                // 1. 初始化 SDK 客户端
                val client = DefaultIcbcClient(
                    myAppId, "RSA2", cleanedPrivateKey, "UTF-8", "json",
                    cleanedGatewayPublicKey, "AES", myEncryptKey, "", ""
                )

                // 2. 构造请求
                val request = IcscSyncFaceWhiteRequestV1()
                val targetUrl = "https://gw.open.icbc.com.cn/api/icsc/synchronizeFaceWhite/V1"
                request.setServiceUrl(targetUrl)

                // 3. 构造业务参数
                val bizContent = IcscSyncFaceWhiteRequestV1.IcscSyncFaceWhiteRequestV1Biz().apply {
                    this.appId = myAppId
                    this.deviceNo = "SN123456789"
                    this.synNum = 100
                    this.modifyStatus = "1"
                }
                request.setBizContent(bizContent)
                request.setNeedEncrypt(true)

                // 4. 发起请求
                val msgId = UUID.randomUUID().toString().replace("-", "").take(20)
                val response: IcscSyncFaceWhiteResponseV1 = client.execute(request, msgId)

                // 5. 处理响应
                if (response.isSuccess) {
                    val count = response.wNLCount ?: 0
                    val recordsList = response.wNLContent ?: emptyList()

                    // ===============================================
                    // 👉 核心新增：将白名单信息保存到本地数据库，供其他地方使用
                    // ===============================================
                    try {
                        // 如果重置标志为1，清空本地表
                        val isResetAll = recordsList.firstOrNull()?.resetWhiteList == "1"
                        if (isResetAll) {
                            whitelistDao.clearAll()
                            Log.d(TAG, "检测到全量更新指令，已清空本地白名单库")
                        }

                        // 循环判断新增还是删除，操作本地库
                        recordsList.forEach { bean ->
                            val studentId = bean.studentId
                            val optType = bean.optType ?: "1" // 1: 新增, 0: 删除

                            if (!studentId.isNullOrEmpty()) {
                                if (optType == "1") {
                                    whitelistDao.insertOrUpdate(
                                        WhitelistEntity(
                                            studentId = studentId,
                                            studentName = bean.studentName ?: "未知",
                                            icCardNo = bean.icCardNo ?: "",
                                            cardValid = bean.cardValid ?: "0"
                                        )
                                    )
                                } else if (optType == "0") {
                                    whitelistDao.deleteByStudentId(studentId)
                                }
                            }
                        }
                        Log.d(TAG, "本地数据库更新完成")
                    } catch (e: Exception) {
                        Log.e(TAG, "本地数据库保存异常", e)
                    }
                    // ===============================================

                    // 6. 更新 UI
                    _summaryData.value = WhitelistSummary(
                        deviceNo = bizContent.deviceNo ?: "未知设备",
                        wNLCount = count,
                        wLeftCount = response.wLeftCount ?: 0
                    )

                    _records.value = recordsList.map { bean ->
                        WhitelistRecord(
                            studentId = bean.studentId ?: "",
                            studentName = bean.studentName ?: "未知姓名",
                            icCardNo = bean.icCardNo ?: "",
                            optType = bean.optType ?: "1",
                            cardValid = bean.cardValid ?: "0"
                        )
                    }

                    _uiEvent.emit(" 同步成功，数据已安全保存本地！数量: $count")
                } else {
                    Log.e(TAG, " [失败] 错误码: ${response.returnCode}, 消息: ${response.returnMsg}")
                    _uiEvent.emit(" 同步失败: ${response.returnMsg}")
                }

            } catch (e: Exception) {
                Log.e(TAG, " [详细异常堆栈] ", e)
                _uiEvent.emit(" 程序异常: ${e.message}")
            } finally {
                _isSyncing.value = false // 结束加载动画
            }
            Log.d(TAG, "-------------------- SDK 请求详情结束 --------------------")
        }
    }
}