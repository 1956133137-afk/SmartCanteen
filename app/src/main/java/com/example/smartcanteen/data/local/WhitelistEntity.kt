package com.example.smartcanteen.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whitelist_table")
data class WhitelistEntity(
    @PrimaryKey
    val studentId: String,       // 学号作为唯一主键
    val studentName: String,     // 姓名
    val icCardNo: String,        // IC卡号
    val cardValid: String        // 卡状态
)