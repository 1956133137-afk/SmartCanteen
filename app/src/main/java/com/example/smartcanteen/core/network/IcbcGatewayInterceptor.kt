package com.example.smartcanteen.core.network

import android.util.Log
import com.example.smartcanteen.core.utils.SmCryptoUtils
import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.text.SimpleDateFormat
import java.util.*

/**
 * 工行网关拦截器 - 深度适配版
 * 修复了工行特有的: 1. 路径参与加签 2. sign_type参与加签 3. 东八区时间戳
 */
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

        // 3. 准备公共参数 (保持强制东八区时间)
        val currentTime = System.currentTimeMillis()
        val msgId = currentTime.toString()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        sdf.timeZone = TimeZone.getTimeZone("GMT+8")
        val timestamp = sdf.format(Date(currentTime))
        val format = "json"
        val charset = "UTF-8"
        val signType = "RSA2"

        // 4. 使用 TreeMap 保证绝对按 key 字母升序排序！
        // 根据工行规范：除 sign 以外的所有参数(包括 sign_type)均需参与加签
        val paramsMap = java.util.TreeMap<String, String>()
        paramsMap["app_id"] = appId
        paramsMap["msg_id"] = msgId
        paramsMap["format"] = format
        paramsMap["charset"] = charset
        paramsMap["timestamp"] = timestamp
        paramsMap["sign_type"] = signType
        paramsMap["biz_content"] = finalBizContent
        if (encryptType != null) {
            paramsMap["encrypt_type"] = encryptType
        }

        // 5. 【最关键修正】工行要求：加签原串必须以 "URI路径?" 开头
        val apiPath = originalRequest.url.encodedPath // 获取类似 "/api/icsc/..." 的路径
        val signContent = apiPath + "?" + paramsMap.entries.joinToString("&") { "${it.key}=${it.value}" }
        
        Log.d("ICBC_DEBUG", ">>> 待加签原串: $signContent")

        // 6. 生成签名
        val sign = SmCryptoUtils.signRSA2(signContent, privateKey)
        Log.d("ICBC_DEBUG", ">>> 生成的签名: $sign")

        // 7. 构造最终表单
        val formBodyBuilder = FormBody.Builder()
        paramsMap.forEach { (key, value) ->
            formBodyBuilder.add(key, value)
        }
        formBodyBuilder.add("sign", sign)

        val finalRequest = originalRequest.newBuilder()
            .post(formBodyBuilder.build())
            .build()

        return chain.proceed(finalRequest)
    }
}
