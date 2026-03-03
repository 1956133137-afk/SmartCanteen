package com.example.smartcanteen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 定义菜单项的数据结构
 */
data class MainMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,      // Compose 的矢量图标
    val iconBgColor: Color,     // 图标的背景颜色
    val route: String           // 点击后路由跳转的目标地址
)
