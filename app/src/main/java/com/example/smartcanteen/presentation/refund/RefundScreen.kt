package com.example.smartcanteen.presentation.refund

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smartcanteen.presentation.main.BgColor
import com.example.smartcanteen.presentation.main.HeaderBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 颜色常量 ---
private val TextPrimary = Color(0xFF333333)
private val TextSecondary = Color(0xFF888888)
private val BrandBlue = Color(0xFF3B82F6)

// --- 数据模型：退款结果 (基于 IcscRefundOrderBackV1 接口) ---
data class RefundResultData(
    val statusCode: Int,       // 0: 成功, 1: 失败, 2: 退款中
    val statusMsg: String,
    val refundId: String,      // 慧餐通退款单号
    val jftAccountNo: String,  // 融e聚子商户编号
    val merchineId: String     // 合作方下单编号
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundScreen(
    onBackClick: () -> Unit = {},
    // 外部传入的状态
    isProcessing: Boolean = false,
    refundResult: RefundResultData? = null,
    // 触发退款回调：传入订单号、以及重新验证的支付方式
    onConfirmRefund: (orderId: String, cardType: String) -> Unit = { _, _ -> },
    // 重置页面以便进行下一笔退款
    onResetState: () -> Unit = {}
) {
    // 表单状态
    var orderId by remember { mutableStateOf("") }

    // 控制安全验证弹窗
    var showVerifyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // 1. 顶部渐变波浪背景
        HeaderBackground(height = 200.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // 2. 头部标题栏
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
                    text = "交易撤销退款",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. 内容主体区域 (使用 LazyColumn 适配键盘弹出和长卡片)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 56.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (refundResult == null) {
                    // --- 状态 A：输入表单 ---
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFF3E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.CurrencyExchange, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(36.dp))
                                    }
                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column {
                                        Text("发起退款申请", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("请扫描或输入需要撤销的完整订单号", fontSize = 18.sp, color = TextSecondary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(40.dp))

                                // 订单号输入框 (对应接口的 orderId)
                                OutlinedTextField(
                                    value = orderId,
                                    onValueChange = { orderId = it },
                                    label = { Text("原交易订单号", fontSize = 18.sp) },
                                    placeholder = { Text("例如：A15166849854689") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandBlue),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                                )

                                Spacer(modifier = Modifier.height(48.dp))

                                // 发起退款按钮
                                Button(
                                    onClick = { showVerifyDialog = true },
                                    enabled = orderId.isNotBlank() && !isProcessing,
                                    modifier = Modifier.fillMaxWidth().height(72.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, disabledContainerColor = Color(0xFFDDE2F1))
                                ) {
                                    Text("发 起 退 款", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                                }
                            }
                        }
                    }
                } else {
                    // --- 状态 B：退款结果展示 (白名单卡片风格) ---
                    item {
                        RefundResultCard(resultData = refundResult)
                    }

                    item {
                        Button(
                            onClick = {
                                orderId = "" // 清空输入框
                                onResetState() // 还原为表单状态
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(2.dp, BrandBlue)
                        ) {
                            Text("继 续 处 理 下 一 笔", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 弹窗层 1：安全验证弹窗 (要求验证原支付介质)
    // ==========================================
    if (showVerifyDialog) {
        Dialog(onDismissRequest = { showVerifyDialog = false }) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                modifier = Modifier.width(460.dp).wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("安全验证", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("为保障资金安全，请让用户提供原支付介质进行身份核验", fontSize = 16.sp, color = TextSecondary, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(40.dp))

                    // 刷卡/扫码 感应区
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF0F5FF))
                            .border(1.5.dp, Color(0xFFD6E4FF), RoundedCornerShape(24.dp))
                            .clickable {
                                showVerifyDialog = false
                                onConfirmRefund(orderId, "0") // 模拟刷卡 (0: 实体卡, 20: 扫码)
                            }
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CreditCard, contentDescription = "刷卡", tint = BrandBlue, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.width(32.dp))
                                Icon(Icons.Rounded.QrCodeScanner, contentDescription = "扫码", tint = BrandBlue, modifier = Modifier.size(56.dp))
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("在此刷卡或扫码验证", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Text(" 或 ", fontSize = 16.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE), thickness = 1.dp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 人脸识别验证
                    Button(
                        onClick = {
                            showVerifyDialog = false
                            onConfirmRefund(orderId, "21") // 21: 人脸
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Rounded.FaceRetouchingNatural, contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("刷 脸 验 证", fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = { showVerifyDialog = false }) {
                        Text("取消退款", fontSize = 20.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // ==========================================
    // 弹窗层 2：正在处理防误触弹窗
    // ==========================================
    if (isProcessing) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                modifier = Modifier.width(360.dp)
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = BrandBlue, strokeWidth = 4.dp, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("正在向网关提交退款...", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

// --- 独立UI组件：精美的退款结果展示卡片 (类似白名单数据卡片风格) ---
@Composable
private fun RefundResultCard(resultData: RefundResultData) {
    // 0:退款成功, 1:退款失败, 2:退款处理中
    val (iconColor, bgColor, iconVector, titleText) = when (resultData.statusCode) {
        0 -> listOf(Color(0xFF4CAF50), Color(0xFFE8F5E9), Icons.Rounded.CheckCircle, "退款成功")
        1 -> listOf(Color(0xFFF44336), Color(0xFFFFEBEE), Icons.Rounded.Cancel, "退款失败")
        2 -> listOf(Color(0xFFFF9800), Color(0xFFFFF3E0), Icons.Rounded.HourglassEmpty, "退款处理中")
        else -> listOf(Color(0xFF999999), Color(0xFFF5F5F5), Icons.Rounded.HelpOutline, "未知状态")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(40.dp)) {
            // 头部：大圆角色块 + 状态
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(bgColor as Color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = iconVector as ImageVector, contentDescription = null, tint = iconColor as Color, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.width(28.dp))
                Column(modifier = Modifier.weight(1f)) {
//                    Text(text = titleText as String, fontSize = 32.sp, fontWeight = FontWeight.Black, color = iconColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = resultData.statusMsg.ifEmpty { "系统已处理本次请求" }, fontSize = 18.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(32.dp))

            // 详情展示 (接口返回的三个关键单号)
            DetailRow(label = "慧餐通退款单号", value = resultData.refundId.ifEmpty { "--" })
            Spacer(modifier = Modifier.height(24.dp))
            DetailRow(label = "子商户编号", value = resultData.jftAccountNo.ifEmpty { "--" })
            Spacer(modifier = Modifier.height(24.dp))
            DetailRow(label = "合作方订单号", value = resultData.merchineId.ifEmpty { "--" })
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 20.sp, color = TextSecondary)
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

// ==========================================
// 状态驱动演示 (可直接在 Android Studio 预览运行)
// ==========================================
@Preview(showBackground = true, device = "spec:width=800px,height=1280px,dpi=160")
@Composable
private fun RefundScreenDemo() {
    MaterialTheme {
        var isProcessing by remember { mutableStateOf(false) }
        var resultData by remember { mutableStateOf<RefundResultData?>(null) }
        val scope = rememberCoroutineScope()

        RefundScreen(
            isProcessing = isProcessing,
            refundResult = resultData,
            onConfirmRefund = { orderId, cardType ->
                // 1. 用户验证完毕，进入处理中状态
                isProcessing = true

                // 2. 模拟网络请求工行网关 (IcscRefundOrderBackV1)
                scope.launch {
                    delay(2000)
                    isProcessing = false

                    // 3. 模拟接口返回真实数据并展示结果卡片
                    resultData = RefundResultData(
                        statusCode = 0, // 0: 成功
                        statusMsg = "退款处理完成，金额已原路返回",
                        refundId = "DER2021082500271",
                        jftAccountNo = "JFT999888777",
                        merchineId = orderId
                    )
                }
            },
            onResetState = {
                // 点击继续下一笔时，重置状态回表单
                resultData = null
            }
        )
    }
}