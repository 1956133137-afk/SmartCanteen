package com.example.smartcanteen.core.network

import android.util.Log
import com.example.smartcanteen.core.utils.SmCryptoUtils
import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IcbcGatewayInterceptor(
    private val appId: String,
    private val privateKey: String,
    private val aesKey: String? = null
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1. 获取业务 JSON 并极致压缩 (去掉所有空格)
        val requestBody = originalRequest.body
        var bizContentJson = "{}"
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val rawJson = buffer.readUtf8()
            try {
                val jsonElement = JsonParser.parseString(rawJson)
                bizContentJson = jsonElement.toString()
            } catch (e: Exception) {
                bizContentJson = rawJson.trim()
            }
        }

        // 2. 执行 AES 加密 (如果配置了密钥)
        val finalBizContent: String
        val encryptType: String?
        if (!aesKey.isNullOrBlank()) {
            finalBizContent = SmCryptoUtils.encryptAES(bizContentJson, aesKey)
            encryptType = "AES"
        } else {
            finalBizContent = bizContentJson
            encryptType = null
        }

        // 3. 准备所有请求参数
        val currentTime = System.currentTimeMillis()
        val msgId = currentTime.toString()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(currentTime))
        val format = "json"
        val charset = "UTF-8"
        val signType = "RSA2"

        // 4. 【关键修改】将所有参数放入有序 Map (包括 sign_type，排除 sign)
        val paramsMap = sortedMapOf(
            "app_id" to appId,
            "msg_id" to msgId,
            "format" to format,
            "charset" to charset,
            "timestamp" to timestamp,
            "sign_type" to signType, // 重新加入签名计算
            "biz_content" to finalBizContent
        )
        
        if (encryptType != null) {
            paramsMap["encrypt_type"] = encryptType
        }

        // 5. 拼接原串
        val signContent = paramsMap.entries.joinToString("&") { "${it.key}=${it.value}" }
        Log.d("ICBC_DEBUG", ">>> 最终待加签原串: $signContent")
        
        // 6. 生成签名
        val sign = SmCryptoUtils.signRSA2(signContent, privateKey)
        Log.d("ICBC_DEBUG", ">>> 最终生成的签名: $sign")

        // 7. 构造最终表单
        val formBodyBuilder = FormBody.Builder()
        paramsMap.forEach { (key, value) ->
            formBodyBuilder.add(key, value)
        }
        formBodyBuilder.add("sign", sign)

        val newRequest = originalRequest.newBuilder()
            .post(formBodyBuilder.build())
            .build()

        return chain.proceed(newRequest)
    }
}
