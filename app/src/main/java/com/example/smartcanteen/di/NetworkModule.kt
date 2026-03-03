package com.example.smartcanteen.di

import com.example.smartcanteen.core.network.IcbcGatewayInterceptor
import com.example.smartcanteen.data.remote.SmartCanteenApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val APP_ID = "11000000000000006202"
    private const val PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCEBcRYHRLmUax4GKABuqfqJMblS0qoSZ4fsGhobjUnGpRpzsS7g8QNdioSUEOMH52XywDkRwB2YRSRZo/YDOtStqX8izNJIHM98/IkyPUate9TeIzFUF2yDPc87bskaQAbq03y2b2pmDOELF3FFXpHdZPkNnDJQcSCnQh2Ml27T+qISbcbOUsuk260UIdyUmi2s3fJ6t/QovcYaDrKZTkTNZjvtlcpf7sMPRp+jYNfHZakycaUGc+sHLAM0gczx3GFBuWUiB0m0lgowCHdtLbBvSnr7V1wJVQ+Q+c2opga5arE5qcEQfMDjIMIxlNUsMaWCcyEswRYf6XaJZgNLNAHAgMBAAECggEAINDWJxjC8+DlP8zPKN7ekgKmNaRrYD08QqA0s5CBQcEHlu1AVWPBPZ9bgcXVpGTPZUWeaR+E6+fhluqpYoiDjcG8rHNlQg4A1ytlyuLwmb0TSeozA3CgeXtjkQ9+buRylG4WKAEbdaZRV883V5+pD04YfqdRjZT9ZYHm8oo+ZipxCRJFtnqZxb76nxivNLSKHXCPrUBpf5l/nrxYFf4UfNLZCeYtA/jlG/LyrYX5Ar3fOIFbhUXR+rsaKOmA6Q8uyyIJcqyHXlnEJHAk/x609eDkQ/aM6xC10N0lSNACSocMHMFFVJmyO91m59xgCBG9sP1ybQ8XItQcc6AbL6oD0QKBgQDP8K7hFHRBicFZw/047O2VyfRdSRhq3hWX1T1C0rDYSysfgH0LDmNm5qRuSpx+cLUUpZiQSzSLRllZJW/nC+gtTFSxT1YLZLhwsjtXrwN6S7IbiqS4dbMXj8ZiRhfKJVHTsZW2OKEmiayaz/TgDYl+Y58+sDuoazIh1Ybjmu34hQKBgQCiiTh6bTpE0lnzBjY6mpT2WNsvSBbGSfnJnnErynlIEB8AIhMgYk0/rCYRXqUjWDhjeNmVejBNmRJfa9YDEugZyRr713kBxxy5omQUZqSZlHhWT4NHkst8PYvJTR2ZJd1okc0u72k/jKTuhQQ+T5SRq2ez+nkdukWTPAXYsGVSGwKBgFyG8qpBGcIX9CtnZIQyGT4v0Ua5Qd472EvpnQrhCcQVHqkz2XBUBLNDKiPRm3U/3bPEQ/DhJg5bTWK3n+WljLirYHxNmzISrWeoR3Oq8tc2W5iZxtY1uW1gkpwkKG3Q0WprePzOkha96EozAvm3zFirJ68TaFh26qXXh+A7jmypAoGAZ7BksG2YO6l6Kriuxcox98qN4eb6aHtu9/m1NvuPTbLDJCGY1BMCNvmKu5AGh7rTFlpVbK+ruwVJHEi+Ge3o1fJe3YUDtGei4qY/dGha34NBFcbZ+EIkwFi/Iaeplzb5vakuHaAsI5eE7Ik9QMch/6A6oy3KMHXRxvEGxcsaiDsCgYEAsQTwUfEeweYXecSzURipJMUOhA5SyejYQT8l9PkBO/cg7w7Z373Yog1ITlNnSs9msvVX5AaxZWst+la0pWh2c+2IXRLVem5I2TGt29KQwrCGdnn9/NhAbZbCZPcm0HrKnhLKplqQ/BNpjeCbb1UFbTSj7bjRr7MtRsbu1EdryOI=\n"
    private const val AES_KEY = "JXJMOSihsXJoBJ5CpN4Ttg=="
    
    private const val BASE_URL = "https://gw.open.icbc.com.cn/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            // 注入带 AES 支持的拦截器
            .addInterceptor(IcbcGatewayInterceptor(APP_ID, PRIVATE_KEY, AES_KEY))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSmartCanteenApi(retrofit: Retrofit): SmartCanteenApi {
        return retrofit.create(SmartCanteenApi::class.java)
    }
}
