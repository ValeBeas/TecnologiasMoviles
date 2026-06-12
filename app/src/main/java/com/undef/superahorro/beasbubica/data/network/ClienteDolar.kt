package com.undef.superahorro.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para la API del dólar (bluelytics.com.ar).
 * Se llama una vez al iniciar la app para obtener el tipo de cambio.
 */
object ClienteDolar {
    private const val URL_BASE = "https://api.bluelytics.com.ar/"

    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val servicio: ServicioDolar = Retrofit.Builder()
        .baseUrl(URL_BASE)
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ServicioDolar::class.java)
}
