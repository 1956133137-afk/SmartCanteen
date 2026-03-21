package com.example.smartcanteen.presentation.devicesync

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

data class EntranceRecord(
    val studentId: String, // 工号
    val accessTime: String, // 访问时间
    val accessFlag: String // 访问标记
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSyncScreen(
    onBackClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val mockRecords = remember {
        listOf(
            EntranceRecord("234242", "2023-11-30 15:00:00", "1"),
            EntranceRecord("234243", "2023-11-30 15:02:15", "1"),
            EntranceRecord("234244", "2023-11-30 15:08:45", "0"), // 识别失败的记录
            EntranceRecord("234245", "2023-11-30 15:10:22", "1")
        )
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
                    Text(
                        text = "设备与考勤同步",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "正在同步考勤记录...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudUpload,
                            contentDescription = "上传同步",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 56.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    DeviceSummaryCard(
                        deviceNo = "B20231226",
                        schoolId = "22222222",
                        unsyncedCount = mockRecords.size
                    )
                }

                item {
                    Text(
                        text = "待同步本地出入记录",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                items(mockRecords) { record ->
                    EntranceRecordItem(record)
                }
            }
        }
    }
}

@Composable
private fun DeviceSummaryCard(deviceNo: String, schoolId: String, unsyncedCount: Int) {
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
                Icon(Icons.Rounded.Dns, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("当前设备: $deviceNo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Spacer(modifier = Modifier.height(8.dp))
                Text("学校编号: $schoolId", fontSize = 18.sp, color = Color(0xFF888888))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("待同步", fontSize = 18.sp, color = Color(0xFF888888))
                Spacer(modifier = Modifier.height(4.dp))
                Text("$unsyncedCount 条", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
private fun EntranceRecordItem(record: EntranceRecord) {
    val isSuccess = record.accessFlag == "1"
    val iconColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
    val bgColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val statusText = if (isSuccess) "通行成功" else "通行失败"

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
                Icon(
                    imageVector = if (isSuccess) Icons.Rounded.HowToReg else Icons.Rounded.PersonOff,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("学号 / 职工号: ${record.studentId}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Spacer(modifier = Modifier.height(6.dp))
                Text("通行时间: ${record.accessTime}", fontSize = 16.sp, color = Color(0xFF888888))
            }

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    color = iconColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=800px,height=1280px,dpi=160")
@Composable
fun DeviceSyncScreenPreview() {
    MaterialTheme {
        DeviceSyncScreen(onBackClick = {})
    }
}