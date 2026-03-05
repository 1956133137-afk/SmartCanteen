package com.example.smartcanteen.data.remote.model

import com.icbc.api.IcbcRequest
import com.icbc.api.BizContent
import com.alibaba.fastjson.annotation.JSONField

/**
 * 慧餐通白名单同步 - 请求类
 */
class IcscSyncFaceWhiteRequestV1 : IcbcRequest<IcscSyncFaceWhiteResponseV1> {
    
    private var serviceUrl: String? = null
    private var bizContent: IcscSyncFaceWhiteRequestV1Biz? = null
    private var needEncrypt: Boolean = false

    override fun getMethod(): String = "POST"
    
    override fun getServiceUrl(): String? = serviceUrl
    
    override fun setServiceUrl(url: String?) { 
        this.serviceUrl = url 
    }
    
    override fun getResponseClass(): Class<IcscSyncFaceWhiteResponseV1> = IcscSyncFaceWhiteResponseV1::class.java
    
    override fun getBizContent(): BizContent? = bizContent

    override fun setBizContent(bizContent: BizContent?) {
        this.bizContent = bizContent as? IcscSyncFaceWhiteRequestV1Biz
    }

    // 修复：应该返回 needEncrypt 变量的值
    override fun isNeedEncrypt(): Boolean = needEncrypt

    // 提供 Setter 供外部调用
    fun setNeedEncrypt(needEncrypt: Boolean) {
        this.needEncrypt = needEncrypt
    }

    override fun getBizContentClass(): Class<out BizContent>? = IcscSyncFaceWhiteRequestV1Biz::class.java

    override fun getExtraParameters(): Map<String, String>? = null

    /**
     * 内部业务参数数据模型
     * 必须确保字段名与工行文档完全一致
     */
    class IcscSyncFaceWhiteRequestV1Biz : BizContent {
        @JSONField(name = "appId")
        var appId: String? = null
        
        @JSONField(name = "deviceNo")
        var deviceNo: String? = null
        
        @JSONField(name = "synNum")
        var synNum: Int? = null
        
        @JSONField(name = "modifyStatus")
        var modifyStatus: String? = null
    }
}
