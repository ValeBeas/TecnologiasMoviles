package com.undef.superahorro.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para la API de Groq (OCR/IA del ticket).
 * Timeouts largos porque la inferencia de visión puede tardar varios segundos.
 */
object ClienteGroq {
    private const val URL_BASE = "https://api.groq.com/"

    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val servicio: ServicioGroq = Retrofit.Builder()
        .baseUrl(URL_BASE)
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ServicioGroq::class.java)
}
