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
    var wNLContent: List<Any>? = null
}
