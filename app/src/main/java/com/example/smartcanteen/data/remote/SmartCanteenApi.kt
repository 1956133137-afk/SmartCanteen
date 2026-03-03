package com.example.smartcanteen.data.remote

import com.example.smartcanteen.data.remote.model.IcbcBaseResponse
import com.example.smartcanteen.data.remote.model.SyncWhitelistRequest
import com.example.smartcanteen.data.remote.model.SyncWhitelistResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SmartCanteenApi {

    /**
     * 1. 白名单同步接口
     * 文档：IcscSynchronizeFaceWhiteV1.md
     */
    @POST("api/icsc/synchronizeFaceWhite/V1")
    suspend fun syncWhitelist(
        @Body request: SyncWhitelistRequest
    ): IcbcBaseResponse<SyncWhitelistResponse>

    // 预留位置：后续在这里添加 下单、退款、设备回调 等其他接口...
}
