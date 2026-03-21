package com.example.smartcanteen.presentation.balance

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import com.example.smartcanteen.presentation.main.BgColor
import com.example.smartcanteen.presentation.main.HeaderBackground
import com.yannuo.library.interfaces.QrCodeListener
import com.yannuo.library.interfaces.SerialPortListener
import com.yannuo.library.qrCodeHelper.QrCodeHelper
import com.yannuo.library.serialPortHelper.SerialPortHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch

private val TextPrimary = Color(0xFF333333)
private val TextSecondary = Color(0xFF888888)
private val BrandBlue = Color(0xFF3B82F6)

data class BalanceResult(
    val deptName: String, // 部门名称
    val rgAccName: String, // 员工名称
    val eBalance: String, // 电子余额
    val pBalance: String, // 部门余额
    val sBalance: String // 个人余额
)

data class TransactionLog(
    val orderId: String, // 订单编号
    val dwTranAmt: String, // 金额
    val transDate: String, // 交易时间
    val orderStatus: String // 订单状态
)

@Composable
fun BalanceQueryScreen(
    balanceData: BalanceResult? = null,
    logList: List<TransactionLog> = emptyList(),
    isQuerying: Boolean = false,
    onBackClick: () -> Unit = {},
    onQueryClick: (cardType: String) -> Unit = {},
    onRefundClick: (orderId: String) -> Unit = {}
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCardType by remember { mutableStateOf("0") }

    var showPromptDialog by remember { mutableStateOf(false) }


    if (showPromptDialog) {
        DisposableEffect(Unit) {
            val qrHelper = QrCodeHelper.getInstance()
            val serialHelper = SerialPortHelper()
            serialHelper.setSerialPort(1)

            val handlerThread = android.os.HandlerThread("HardwareThread").apply { start() }
            val hardwareDispatcher = android.os.Handler(handlerThread.looper).asCoroutineDispatcher()

            coroutineScope.launch(hardwareDispatcher) {
                try {
                    when (selectedCardType) {
                        "20" -> {
                            qrHelper.setUSBorCOM("/dev/ttyS0", 4800, true)
                            qrHelper.open(context, object : QrCodeListener {
                                override fun onQrCodeSuccess() { Log.d("Hardware", "扫码枪打开成功") }
                                override fun onQrCodeReceived(scanResult: String) {
                                    coroutineScope.launch(Dispatchers.Main) {
                                        onQueryClick(scanResult) // 执行查询
                                        Log.d("Hardware", "扫码成功: ${scanResult}")
                                    }
                                }
                                override fun onQrCodeFailure(errMsg: String) {}
                                override fun onQrCodeException(excMsg: String) {}
                            })
                        }
                        "0" -> {
                            serialHelper.openSerialPort("/dev/ttyS2", 9600, object :
                                SerialPortListener {
                                override fun onSerialPortSuccess() {
                                    serialHelper.startSerialPort()
                                    Log.d("Hardware", "读卡器就绪")
                                }
                                override fun onSerialPortReceived(dataStr: String, data: ByteArray, size: Int) {
                                    coroutineScope.launch(Dispatchers.Main) {
//                                        showPromptDialog = false
                                        onQueryClick(dataStr) // 执行查询
                                        Log.d("Hardware", "读卡成功: ${dataStr}")
                                    }
                                }
                                override fun onSerialPortException(msg: String) {
                                    Log.e("Hardware", "读卡异常: ${msg}")
                                }
                                override fun onSerialPortFailure(msg: String) {
                                    Log.e("Hardware", "读卡失败: ${msg}")
                                }
                            })
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Hardware", "初始化异常: ${e.message}")
                }
            }

            onDispose {
                qrHelper.close()
                serialHelper.releaseSerialPort()
                handlerThread.quitSafely()
            }
        }
    }



    

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        HeaderBackground(height = 200.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, end = 56.dp, top = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "余额与消费查询",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(BgColor)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 56.dp, end = 56.dp, top = 24.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        QueryActionCard(
                            selectedCardType = selectedCardType,
                            isQuerying = isQuerying,
                            onTypeChange = { selectedCardType = it },
                            onQueryClick = {
                                showPromptDialog = true
                                onQueryClick(selectedCardType)
                            }
                        )
                    }

                    if (balanceData != null) {
                        item {
                            BalanceResultCard(data = balanceData)
                        }

                        item {
                            Text(
                                text = "最近10笔消费记录",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 16.dp, start = 8.dp)
                            )
                        }

                        if (logList.isEmpty()) {
                            item { EmptyListHint(message = "暂无消费记录") }
                        } else {
                            items(logList) { log ->
                                TransactionRecordItem(
                                    log = log,
                                    onRefundClick = { onRefundClick(log.orderId) }
                                )
                            }
                        }
                    } else {
                        item {
                            EmptyStateHint(isQuerying = isQuerying)
                        }
                    }
                }
            }
        }
    }

    if (showPromptDialog) {
        val (dialogTitle, dialogIcon) = when (selectedCardType) {
            "0" -> Pair("请刷卡...", Icons.Rounded.CreditCard)
            "20" -> Pair("请扫码...", Icons.Rounded.QrCode)
            "21" -> Pair("请看摄像头...", Icons.Rounded.FaceRetouchingNatural)
            else -> Pair("等待操作...", Icons.Rounded.Info)
        }

        AlertDialog(
            onDismissRequest = { showPromptDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            icon = {
                Icon(
                    imageVector = dialogIcon,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(56.dp)
                )
            },
            title = {
                Text(text = dialogTitle, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandBlue, strokeWidth = 4.dp, modifier = Modifier.size(48.dp))
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPromptDialog = false }) {
                    Text("取消", fontSize = 18.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun EmptyStateHint(isQuerying: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isQuerying) {
                CircularProgressIndicator(color = BrandBlue, strokeWidth = 4.dp, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(32.dp))
                Text("正在查询账户与消费信息...", fontSize = 22.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(BgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("暂无数据", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("请在上方选择查询方式并点击查询按钮", fontSize = 20.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun EmptyListHint(message: String) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
        Text(text = message, fontSize = 20.sp, color = TextSecondary)
    }
}

@Composable
private fun QueryActionCard(
    selectedCardType: String,
    isQuerying: Boolean,
    onTypeChange: (String) -> Unit,
    onQueryClick: () -> Unit
) {
    val typeOptions = listOf(
        Pair("0", "实体卡片"),
        Pair("20", "二维码"),
        Pair("21", "人脸识别")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text("请选择查询方式", fontSize = 24.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                typeOptions.forEach { (code, text) ->
                    val isSelected = selectedCardType == code
                    Surface(
                        onClick = { if (!isQuerying) onTypeChange(code) },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) BrandBlue.copy(alpha = 0.1f) else BgColor,
                        border = if (isSelected) BorderStroke(2.dp, BrandBlue) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = text,
                                fontSize = 20.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BrandBlue else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onQueryClick,
                enabled = !isQuerying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    disabledContainerColor = BrandBlue.copy(alpha = 0.5f)
                )
            ) {
                if (isQuerying) {
                    Text("查 询 中 ...", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text("立 即 查 询", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun BalanceResultCard(data: BalanceResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(BrandBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.width(32.dp))
                Column {
                    Text(text = data.rgAccName, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = data.deptName, fontSize = 20.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text("e账户余额 (元)", fontSize = 20.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("¥", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 6.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(data.eBalance, fontSize = 64.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("个人现金余额", fontSize = 20.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("¥ ${data.pBalance}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("单位现金余额", fontSize = 20.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("¥ ${data.sBalance}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun TransactionRecordItem(
    log: TransactionLog,
    onRefundClick: () -> Unit
) {
    val iconBgColor = when (log.orderStatus) {
        "0" -> Color(0xFF4DB6AC)
        "1" -> Color(0xFFFF8A65)
        "3", "4" -> Color(0xFF999999)
        else -> Color(0xFF999999)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("单号: ${log.orderId}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(log.transDate, fontSize = 20.sp, color = TextSecondary)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("¥ ${log.dwTranAmt}", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                val statusText = when (log.orderStatus) {
                    "0" -> "交易成功"
                    "1" -> "交易失败"
                    "3" -> "退款中"
                    "4" -> "退款成功"
                    else -> "状态异常"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = statusText, fontSize = 18.sp, color = TextSecondary, fontWeight = FontWeight.Medium)

                    if (log.orderStatus == "0") {
                        Spacer(modifier = Modifier.width(16.dp))
                        Surface(
                            onClick = onRefundClick,
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent,
                            border = BorderStroke(2.dp, Color(0xFFFFA726))
                        ) {
                            Text(
                                text = "退款",
                                color = Color(0xFFFFA726),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=800px,height=1280px,dpi=160")
@Composable
fun BalanceQueryScreenPreview_Empty() {
    MaterialTheme {
        BalanceQueryScreen(balanceData = null, isQuerying = false)
    }
}

@Preview(showBackground = true, device = "spec:width=800px,height=1280px,dpi=160")
@Composable
fun BalanceQueryScreenPreview_Success() {
    MaterialTheme {
        BalanceQueryScreen(
            balanceData = BalanceResult("研发部", "张三丰", "1288.50", "800.00", "488.50"),
            logList = listOf(
                TransactionLog("A15166849854689", "15.00", "2023-12-06 12:30:12", "0")
            )
        )
    }
}