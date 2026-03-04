package com.example.smartcanteen.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * 工行网关标准响应包装
 */
data class IcbcBaseResponse<T>(
    @SerializedName("response_biz_content")
    val responseBizContent: T?,
    val sign: String?,
    
    // 【关键修复】接收网关外层直接抛出的错误码和信息（避免被 response_biz_content 结构吞掉）
    @SerializedName("return_code")
    val gatewayReturnCode: Int? = null,
    @SerializedName("return_msg")
    val gatewayReturnMsg: String? = null
)

/**
 * 业务结果基类
 */
open class BaseBizContent(
    @SerializedName("return_code")
    val returnCode: Int = -1,
    @SerializedName("return_msg")
    val returnMsg: String = ""
) {
    fun isSuccess() = returnCode == 0
}
