package com.example.smartcanteen.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * 对应文档第4节：请求参数 (biz_content 内部)
 */
data class SyncWhitelistRequest(
    val appId: String,         // APP的编号
    val deviceNo: String,      // 设备序列号
    val synNum: Int,           // 本次同步个数，最大20000
    val modifyStatus: String   // 是否同步修改白名单状态 (0-否, 1-是)
)

/**
 * 对应文档第6节：响应参数 (包含 return_code 和业务字段)
 */
data class SyncWhitelistResponse(
    @SerializedName("return_code")
    val returnCode: Int,
    @SerializedName("return_msg")
    val returnMsg: String?,
    val wNLCount: Int = 0,     // 本次名单总数
    val wLeftCount: Int = 0,   // 剩余未同步名单数
    val wNLContent: List<WhiteListItem>? = null // 名单内容集合
) {
    fun isSuccess() = returnCode == 0
}

/**
 * 对应文档第6.1节：wNLContent参数说明
 */
data class WhiteListItem(
    val optType: String?,       // 操作方式 0-删除, 1-新增
    val resetWhiteList: String?,// 是否重置白名单
    val studentId: String?,     // 学生编号
    val studentName: String?,   // 学生姓名
    val synId: String?,         // 业务主键
    val icCardNo: String?,      // IC卡号
    val cardValid: String?,     // 卡状态
    val status: String?         // 学生状态
)
