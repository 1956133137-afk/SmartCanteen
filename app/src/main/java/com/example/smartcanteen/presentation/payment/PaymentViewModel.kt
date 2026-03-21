package com.example.smartcanteen.presentation.payment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(PaymentUiState.IDLE)
        private set

    var amountFen by mutableLongStateOf(0L)
        private set

    fun resetState() {
        uiState = PaymentUiState.IDLE
    }

    fun handlePayment(amount: Long, method: String, authCode: String) {
        amountFen = amount
        uiState = PaymentUiState.PROCESSING
        
        viewModelScope.launch {
            try {
                delay(1500)
                uiState = PaymentUiState.SUCCESS
            } catch (e: Exception) {
                uiState = PaymentUiState.FAILED
            }
        }
    }
}
