package com.example.smartcanteen.data.repository

import com.example.smartcanteen.data.remote.SmartCanteenApi
import com.example.smartcanteen.data.remote.model.SyncWhitelistRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartCanteenRepository @Inject constructor(
    private val api: SmartCanteenApi
) {
    // 封装白名单同步的网络请求
    suspend fun syncWhitelist(deviceNo: String, appId: String) =
        api.syncWhitelist(
            SyncWhitelistRequest(
                appId = appId,
                deviceNo = deviceNo,
                synNum = 20000,       // 默认最大两万条
                modifyStatus = "1"    // 1-表示同步修改状态
            )
        )
}
