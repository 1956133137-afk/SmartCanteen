package com.example.smartcanteen.data.remote.model

import com.icbc.api.IcbcResponse
import com.alibaba.fastjson.annotation.JSONField

/**
 * 慧餐通白名单同步 - 响应类
 */
class IcscSyncFaceWhiteResponseV1 : IcbcResponse() {

    @JSONField(name = "wNLCount")
    var wNLCount: Int? = null

    @JSONField(name = "wLeftCount")
    var wLeftCount: Int? = null

    @JSONField(name = "wNLContent")
    var wNLContent: List<WhiteListContentBean>? = null

    // 👉 必须新增这个内部类，Fastjson 才能把数组里的 JSON 解析成对象，ViewModel 才能点出字段
    class WhiteListContentBean {
        @JSONField(name = "optType") var optType: String? = null
        @JSONField(name = "resetWhiteList") var resetWhiteList: String? = null
        @JSONField(name = "studentId") var studentId: String? = null
        @JSONField(name = "studentName") var studentName: String? = null
        @JSONField(name = "synId") var synId: String? = null
        @JSONField(name = "icCardNo") var icCardNo: String? = null
        @JSONField(name = "cardValid") var cardValid: String? = null
        @JSONField(name = "status") var status: String? = null
    }
}