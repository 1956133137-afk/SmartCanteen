package com.example.smartcanteen.presentation.whitelist

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartcanteen.presentation.main.BgColor
import com.example.smartcanteen.presentation.main.HeaderBackground
import com.example.smartcanteen.presentation.main.MainViewModel

// 👉 数据模型定义在这里，供 MainViewModel 引用
data class WhitelistSummary(
    val deviceNo: String,
    val wNLCount: Int,
    val wLeftCount: Int
)

data class WhitelistRecord(
    val studentId: String,
    val studentName: String,
    val icCardNo: String,
    val optType: String,
    val cardValid: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistSyncScreen(
    onBackClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 👉 监听 ViewModel 中暴露的真实数据状态
    val isSyncing by viewModel.isSyncing.collectAsState()
    val summaryData by viewModel.summaryData.collectAsState()
    val records by viewModel.records.collectAsState()

    // 监听 ViewModel 弹出的 Toast 事件
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        HeaderBackground(height = 90.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text("白名单同步", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(64.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回", tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.testSyncWhitelist() },
                        enabled = !isSyncing,
                        modifier = Modifier.size(64.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                        } else {
                            // 👉 已经修改为云端下载图标
                            Icon(
                                imageVector = Icons.Rounded.CloudDownload,
                                contentDescription = "下载同步",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // 👉 根据数据状态进行条件渲染
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 56.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (summaryData != null) {
                    item { WhitelistSummaryCard(summary = summaryData!!) }

                    item {
                        Text("本次同步名单明细", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                    }

                    if (records.isEmpty()) {
                        item { EmptyListHint("本次拉取暂无人员数据变动") }
                    } else {
                        items(records) { record ->
                            WhitelistRecordItem(record)
                        }
                    }
                } else {
                    item { EmptyStateHint(isSyncing = isSyncing) }
                }
            }
        }
    }
}

// --- 组件：空状态提示卡片 ---
@Composable
private fun EmptyStateHint(isSyncing: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().height(400.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isSyncing) {
                CircularProgressIndicator(color = Color(0xFF3B82F6), strokeWidth = 4.dp, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(32.dp))
                Text("正在从云端拉取白名单...", fontSize = 22.sp, color = Color(0xFF888888), fontWeight = FontWeight.Medium)
            } else {
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFF4F6F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("暂无同步数据", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Spacer(modifier = Modifier.height(12.dp))
                Text("请点击右上角按钮，从云端拉取最新名单", fontSize = 20.sp, color = Color(0xFF888888))
            }
        }
    }
}

@Composable
private fun EmptyListHint(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
        Text(text = message, fontSize = 20.sp, color = Color(0xFF888888))
    }
}

@Composable
private fun WhitelistSummaryCard(summary: WhitelistSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFE9F0FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AssignmentInd, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("当前设备: ${summary.deviceNo}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Spacer(modifier = Modifier.height(8.dp))
                val leftText = if (summary.wLeftCount == 0) "已是最新，无剩余记录" else "云端还有 ${summary.wLeftCount} 条待拉取"
                Text(leftText, fontSize = 18.sp, color = if (summary.wLeftCount == 0) Color(0xFF4CAF50) else Color(0xFFFF9800))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("本次拉取", fontSize = 18.sp, color = Color(0xFF888888))
                Spacer(modifier = Modifier.height(4.dp))
                Text("${summary.wNLCount} 人", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
private fun WhitelistRecordItem(record: WhitelistRecord) {
    val isAdd = record.optType == "1"
    val iconColor = if (isAdd) Color(0xFF4CAF50) else Color(0xFFF44336)
    val bgColor = if (isAdd) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val statusText = if (isAdd) "新增" else "删除"
    val iconVector = if (isAdd) Icons.Rounded.PersonAdd else Icons.Rounded.PersonRemove

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(record.studentName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("学号: ${record.studentId}", fontSize = 16.sp, color = Color(0xFF888888), modifier = Modifier.padding(bottom = 2.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("IC卡号: ${if (record.icCardNo.isEmpty()) "未绑定" else record.icCardNo}", fontSize = 16.sp, color = Color(0xFF888888))
            }
            Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
                Text(text = statusText, fontSize = 16.sp, color = iconColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=800px,height=1280px,dpi=160")
@Composable
private fun WhitelistSyncScreenPreview() {
    MaterialTheme {
        WhitelistSyncScreen(onBackClick = {})
    }
}