package com.example.smartcanteen.presentation.whitelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcanteen.presentation.main.BgColor
import com.example.smartcanteen.presentation.main.HeaderBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistSyncScreen(
    onBackClick: () -> Unit
) {
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
                        text = "白名单同步",
                        color = Color.White,
                        fontSize = 36.sp, // 调大字号
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(64.dp) // 调大按钮点击区域
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp) // 调大图标尺寸
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO: 执行同步逻辑 */ },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "同步刷新",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 24.dp) // 增加左右边距
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "白名单同步内容建设中...", fontSize = 20.sp, color = Color.Gray)
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
