package com.example.smartcanteen.presentation.main

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// --- 1. 数据模型与静态数据 ---
data class MainMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color
)

val BgColor = Color(0xFFF4F6F9)
val TextPrimary = Color(0xFF333333)
val TextSecondary = Color(0xFF999999)

val menuItems = listOf(
    MainMenuItem("设备信息同步", "同步设备数据", Icons.Rounded.Settings, Color(0xFFFF8A65)),
    MainMenuItem("白名单同步", "同步白名单", Icons.Rounded.Assignment, Color(0xFF4DB6AC)),
    MainMenuItem("下单接口", "刷脸 / 刷卡 / 扫码", Icons.Rounded.ShoppingCart, Color(0xFF26A69A)),
    MainMenuItem("退款接口", "交易撤销退款", Icons.Rounded.CurrencyExchange, Color(0xFFFFA726)),
    MainMenuItem("查询余额和记录", "余额和消费记录", Icons.Rounded.Search, Color(0xFF7E57C2)),
    MainMenuItem("人脸采集接口", "第三方提供路径", Icons.Rounded.Face, Color(0xFFFF7043))
)

// --- 2. 有状态的容器层 (绑定 ViewModel 和 事件处理) ---
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // 监听 ViewModel 弹出的 Toast 事件
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 调用无状态的纯 UI 层，把点击事件传进去
    MainScreenContent(
        onMenuClick = { title ->
            if (title == "白名单同步") {
                viewModel.testSyncWhitelist()
            } else {
                Toast.makeText(context, "点击了：$title", Toast.LENGTH_SHORT).show()
            }
        },
        onMoreClick = {
            Toast.makeText(context, "点击了更多设置", Toast.LENGTH_SHORT).show()
        }
    )
}

// --- 3. 无状态的纯 UI 层 (完美支持 Preview 预览) ---
@Composable
fun MainScreenContent(
    onMenuClick: (String) -> Unit, // 菜单点击回调
    onMoreClick: () -> Unit        // 右上角三个点点击回调
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // 1. 蓝色渐变与波浪底纹 - 针对大屏高度
        HeaderBackground(height = 200.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // 头部：标题 + 三个点
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp, end = 56.dp, top = 30.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "慧餐通智慧管理系统",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "更多设置",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // 大圆角底板与列表
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(BgColor)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 56.dp, end = 56.dp, top = 10.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(menuItems) { item ->
                        MenuItemCard(
                            item = item,
                            onClick = { onMenuClick(item.title) } // 触发上层传下来的回调
                        )
                    }
                }
            }
        }
    }
}

// --- 4. 局部组件保持不变 ---
@Composable
fun HeaderBackground(height: Dp) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
    )
    Canvas(
        modifier = Modifier.fillMaxWidth().height(height)
    ) {
        drawRect(brush = gradientBrush)
        val path1 = Path().apply {
            moveTo(0f, size.height * 0.45f)
            cubicTo(size.width * 0.3f, size.height * 0.35f, size.width * 0.7f, size.height * 0.65f, size.width, size.height * 0.45f)
            lineTo(size.width, 0f); lineTo(0f, 0f); close()
        }
        drawPath(path = path1, color = Color.White.copy(alpha = 0.12f))
        val path2 = Path().apply {
            moveTo(0f, size.height * 0.65f)
            cubicTo(size.width * 0.4f, size.height * 0.85f, size.width * 0.8f, size.height * 0.35f, size.width, size.height * 0.55f)
            lineTo(size.width, 0f); lineTo(0f, 0f); close()
        }
        drawPath(path = path2, color = Color.White.copy(alpha = 0.08f))
    }
}

@Composable
fun MenuItemCard(item: MainMenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(item.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 20.sp,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = "进入",
                tint = Color(0xFFC8C8C8),
                modifier = Modifier.size(44.dp)
            )
        }
    }
}

// --- 5. 预览层 (只预览无状态的 MainScreenContent) ---
@Preview(showBackground = true, device = "spec:width=800px,height=1232px,dpi=160")
@Composable
fun MainScreenPreview() {
    MaterialTheme {
        MainScreenContent(
            onMenuClick = {}, 
            onMoreClick = {}  
        )
    }
}
