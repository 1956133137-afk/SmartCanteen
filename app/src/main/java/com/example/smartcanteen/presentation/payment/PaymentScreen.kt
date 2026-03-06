package com.example.smartcanteen.presentation.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties // 👉 新增导入
import com.example.smartcanteen.presentation.main.BgColor
import com.example.smartcanteen.presentation.main.HeaderBackground
import com.example.smartcanteen.presentation.main.TextPrimary
import com.example.smartcanteen.presentation.main.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 支付状态枚举 ---
enum class PaymentUiState {
    IDLE,       // 空闲
    PROCESSING, // 支付中
    SUCCESS,    // 支付成功
    FAILED      // 支付失败
}

@Composable
fun PaymentScreen(
    balanceText: String = "0.00",
    paymentState: PaymentUiState = PaymentUiState.IDLE,
    onConfirmPay: (amountFen: Long, payMethod: String) -> Unit = { _, _ -> },
    onBalanceClick: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {},
    onResetPaymentState: () -> Unit = {}
) {
    var amountText by remember { mutableStateOf("0") }
    val amountFen = remember(amountText) { moneyTextToFenOrNull(amountText) ?: 0L }

    // 控制等待刷卡/扫码提示弹窗的显示
    var showPaymentPromptDialog by remember { mutableStateOf(false) }

    // 联动：当外部检测到刷卡/扫码开始处理时，自动关闭提示弹窗
    LaunchedEffect(paymentState) {
        if (paymentState != PaymentUiState.IDLE) {
            showPaymentPromptDialog = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // 1. 顶部渐变背景
        HeaderBackground(height = 330.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // 2. 头部标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 66.dp, vertical = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "慧餐通支付",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                // 余额显示区域
                Surface(
                    onClick = onBalanceClick,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.height(64.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "账户余额",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // 更多按钮与弹窗菜单
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        onClick = { showMenu = true },
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier.height(64.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "更多设置",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "更多",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 下拉菜单
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(Color.White)
                            .width(300.dp)
                    ) {
                        val menuItems = listOf(
                            Triple("白名单同步", Icons.Rounded.Assignment, Color(0xFF4DB6AC)),
                            Triple("人脸采集", Icons.Rounded.Face, Color(0xFFFF7043)),
                            Triple("退款", Icons.Rounded.CurrencyExchange, Color(0xFFFFA726)),
                            Triple("设备同步", Icons.Rounded.Sync, Color(0xFF3B82F6)),
                            Triple("其他", Icons.Rounded.Category, Color(0xFF999999))
                        )

                        menuItems.forEach { (title, icon, iconColor) ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(36.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = title,
                                        color = TextPrimary,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onMenuItemClick(title)
                                },
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
                            )
                            if (menuItems.last().first != title) {
                                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.8.dp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            // 3. 内容主体区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(BgColor)
                    .padding(top = 10.dp, bottom = 55.dp)
            ) {
                // 金额显示卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "消费金额",
                            fontSize = 18.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "¥",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = fenToMoneyText(amountFen),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 数字键盘容器
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    CashierKeyboardFill(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        amountFen = amountFen,
                        onKey = { key -> amountText = applyMoneyKey(amountText, key) },
                        onDelete = { amountText = deleteMoneyChar(amountText) },
                        onClear = { amountText = "0" },
                        onConfirm = {
                            if (amountFen > 0) {
                                showPaymentPromptDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    // ==========================================
    // 弹窗层 1：精美定制版 等待刷卡/扫码提示
    // ==========================================
    if (showPaymentPromptDialog) {
        Dialog(
            onDismissRequest = { }, // 👉 设为空，防止任何默认的关闭行为
            // 👉 新增属性：禁用点击外部关闭，禁用物理返回键关闭
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false
            )
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                modifier = Modifier
                    .width(460.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题与金额区
                    Text("请选择支付方式", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("收款金额", fontSize = 18.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("¥ ${fenToMoneyText(amountFen)}", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color(0xFF3B82F6))

                    Spacer(modifier = Modifier.height(40.dp))

                    // 刷卡/扫码 感应区视觉设计
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF0F5FF))
                            .border(1.5.dp, Color(0xFFD6E4FF), RoundedCornerShape(24.dp))
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CreditCard, contentDescription = "刷卡", tint = Color(0xFF3B82F6), modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.width(32.dp))
                                Icon(Icons.Rounded.QrCodeScanner, contentDescription = "扫码", tint = Color(0xFF3B82F6), modifier = Modifier.size(56.dp))
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("请在设备上刷卡或扫码", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // "或" 分割线
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Text(" 或 ", fontSize = 16.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE), thickness = 1.dp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 超大号 人脸识别按钮
                    Button(
                        onClick = {
                            showPaymentPromptDialog = false
                            onConfirmPay(amountFen, "FACE")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(Icons.Rounded.FaceRetouchingNatural, contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("人 脸 识 别", fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 底部取消按钮 (这是现在唯一能关闭弹窗的方法)
                    TextButton(
                        onClick = { showPaymentPromptDialog = false },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("取消收款", fontSize = 25.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // ==========================================
    // 弹窗层 2：支付结果回调状态弹窗
    // ==========================================
    when (paymentState) {
        PaymentUiState.PROCESSING -> {
            Dialog(
                onDismissRequest = { },
                // 👉 支付中同样严禁点击外部或返回键关闭
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                )
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
                        CircularProgressIndicator(color = Color(0xFF3B82F6), strokeWidth = 4.dp, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("等待设备读取/处理中...", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
        PaymentUiState.SUCCESS -> {
            AlertDialog(
                onDismissRequest = { },
                // 👉 必须点击“完成”才能关闭
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                ),
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(72.dp)) },
                title = { Text("支付成功", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Text("本次收款 ¥ ${fenToMoneyText(amountFen)}", fontSize = 22.sp, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetPaymentState()
                            amountText = "0" // 成功后重置输入金额
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("完成", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        PaymentUiState.FAILED -> {
            AlertDialog(
                onDismissRequest = { },
                // 👉 必须点击“我知道了”才能关闭
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                ),
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White,
                icon = { Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(72.dp)) },
                title = { Text("支付失败", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Text("请检查设备或重试", fontSize = 22.sp, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                },
                confirmButton = {
                    Button(
                        onClick = { onResetPaymentState() },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("我知道了", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
        PaymentUiState.IDLE -> { /* 空闲时不显示任何状态弹窗 */ }
    }
}

// --- 以下为键盘等辅助组件 (未修改) ---

@Composable
private fun CashierKeyboardFill(
    modifier: Modifier,
    amountFen: Long,
    onKey: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onConfirm: () -> Unit
) {
    val gap = 16.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        KeyRow(gap) {
            NumKeyBig("1", Modifier.weight(1f)) { onKey("1") }
            NumKeyBig("2", Modifier.weight(1f)) { onKey("2") }
            NumKeyBig("3", Modifier.weight(1f)) { onKey("3") }
            FuncKeyBig(
                text = "删除",
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = null, modifier = Modifier.size(32.dp)) },
                onClick = onDelete
            )
        }

        KeyRow(gap) {
            NumKeyBig("4", Modifier.weight(1f)) { onKey("4") }
            NumKeyBig("5", Modifier.weight(1f)) { onKey("5") }
            NumKeyBig("6", Modifier.weight(1f)) { onKey("6") }
            FuncKeyBig(
                text = "清空",
                modifier = Modifier.weight(1f),
                onClick = onClear
            )
        }

        KeyRow(gap) {
            NumKeyBig("7", Modifier.weight(1f)) { onKey("7") }
            NumKeyBig("8", Modifier.weight(1f)) { onKey("8") }
            NumKeyBig("9", Modifier.weight(1f)) { onKey("9") }
            NumKeyBig(".", Modifier.weight(1f)) { onKey(".") }
        }

        KeyRow(gap) {
            NumKeyBig("0", Modifier.weight(2f)) { onKey("0") }
            ConfirmKeyBig(
                enabled = amountFen > 0,
                amountFen = amountFen,
                modifier = Modifier.weight(2f),
                onClick = onConfirm
            )
        }
    }
}

@Composable
private fun ColumnScope.KeyRow(
    gap: Dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(gap),
        content = content
    )
}

@Composable
private fun NumKeyBig(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8F9FA),
        contentColor = TextPrimary,
        shadowElevation = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FuncKeyBig(
    text: String,
    modifier: Modifier,
    icon: (@Composable (() -> Unit))? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFE9F0FF),
        contentColor = Color(0xFF3B82F6)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ConfirmKeyBig(
    enabled: Boolean,
    amountFen: Long,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3B82F6),
            disabledContainerColor = Color(0xFFDDE2F1),
            contentColor = Color.White,
            disabledContentColor = Color(0xFF8E9199)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = if (enabled) "确认收款 ¥${fenToMoneyText(amountFen)}" else "请输入金额",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

private fun applyMoneyKey(current: String, key: String): String {
    val s = current.ifBlank { "0" }
    return when (key) {
        "." -> {
            if (s.contains(".")) s
            else if (!s.any { it.isDigit() } || s == "0") "0."
            else "$s."
        }
        else -> {
            if (key.length != 1 || !key[0].isDigit()) return s
            if (!s.contains(".") && s == "0") return key
            val dot = s.indexOf('.')
            if (dot >= 0) {
                val decimals = s.length - dot - 1
                if (decimals >= 2) return s
            }
            if (s.length >= 6) return s
            s + key
        }
    }
}

private fun deleteMoneyChar(current: String): String {
    if (current.isBlank() || current == "0") return "0"
    val newS = current.dropLast(1)
    return if (newS.isBlank() || newS == "-") "0" else newS
}

private fun moneyTextToFenOrNull(text: String): Long? {
    val t = text.trim()
    if (t.isEmpty()) return 0L
    if (t == ".") return null
    try {
        val parts = t.split(".")
        val yuan = parts[0].ifEmpty { "0" }.toLong()
        val fen = when (parts.size) {
            1 -> 0L
            2 -> parts[1].padEnd(2, '0').take(2).toLong()
            else -> return null
        }
        return yuan * 100 + fen
    } catch (_: Throwable) { return null }
}

private fun fenToMoneyText(fen: Long): String {
    val yuan = fen / 100
    val rest = fen % 100
    return "${yuan}.${rest.toString().padStart(2, '0')}"
}

// ==========================================
// 交互预览与业务逻辑演示
// ==========================================
@Preview(showBackground = true, device = "spec:width=800px,height=1280px,dpi=160")
@Composable
private fun PaymentScreenDemo() {
    MaterialTheme {
        var currentPaymentState by remember { mutableStateOf(PaymentUiState.IDLE) }
        val coroutineScope = rememberCoroutineScope()

        PaymentScreen(
            paymentState = currentPaymentState,
            onConfirmPay = { amountFen, method ->
                currentPaymentState = PaymentUiState.PROCESSING
                println("发起支付请求: 金额=$amountFen, 方式=$method")

                coroutineScope.launch {
                    delay(2000)
                    currentPaymentState = PaymentUiState.SUCCESS
                }
            },
            onResetPaymentState = {
                currentPaymentState = PaymentUiState.IDLE
            }
        )
    }
}