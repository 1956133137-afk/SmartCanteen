package com.example.smartcanteen.presentation.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.presentation.main.BgColor
import com.example.smartcanteen.presentation.main.HeaderBackground
import com.example.smartcanteen.presentation.main.TextPrimary
import com.example.smartcanteen.presentation.main.TextSecondary

@Composable
fun PaymentScreen(
    balanceText: String = "0.00",
    onConfirmPay: (amountFen: Long) -> Unit = {},
    onBalanceClick: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {}
) {
    var amountText by remember { mutableStateOf("0") }

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
                                    showMenu = false // 点击任意项时，立即关闭更多弹窗
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

            // --- 间距下移 ---
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
                                text = fenToMoneyText(moneyTextToFenOrNull(amountText) ?: 0L),
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
                    val amountFen = remember(amountText) { moneyTextToFenOrNull(amountText) ?: 0L }

                    CashierKeyboardFill(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        amountFen = amountFen,
                        onKey = { key -> amountText = applyMoneyKey(amountText, key) },
                        onDelete = { amountText = deleteMoneyChar(amountText) },
                        onClear = { amountText = "0" },
                        onConfirm = { if (amountFen > 0) onConfirmPay(amountFen) }
                    )
                }
            }
        }
    }
}

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
            if (s.length >= 10) return s
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

@Preview(showBackground = true, device = "spec:width=800px,height=1280px,dpi=160")
@Composable
private fun PaymentScreenPreview() {
    MaterialTheme {
        PaymentScreen()
    }
}
