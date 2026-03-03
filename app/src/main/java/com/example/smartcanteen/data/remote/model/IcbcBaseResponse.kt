package com.example.smartcanteen.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * 工行网关标准响应外层包装
 */
data class IcbcBaseResponse<T>(
    @SerializedName("response_biz_content")
    val responseBizContent: T?,
    val sign: String? // 部分接口外层会带有工行网关返回的签名
)

/**
 * 业务结果基类，包含 return_code 和 return_msg
 */
open class BaseBizContent(
    @SerializedName("return_code")
    val returnCode: Int = -1,
    @SerializedName("return_msg")
    val returnMsg: String = ""
) {
    fun isSuccess() = returnCode == 0
}
