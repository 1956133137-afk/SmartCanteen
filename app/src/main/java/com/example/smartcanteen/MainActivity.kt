package com.example.smartcanteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.example.smartcanteen.presentation.main.MainScreen
import com.example.smartcanteen.presentation.payment.PaymentScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 开启沉浸式全屏，让蓝色头部延伸到状态栏
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                PaymentScreen()
            }
        }
    }
}
