package com.undef.superahorro.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configura el cliente HTTP para conectarse a la API.
 * Tiene un interceptor que muestra las peticiones y respuestas en el Logcat para debug.
 */
object ClienteRetrofit {
    private const val URL_BASE = "https://api.superahorro.com/v1/"

    // OkHttp con interceptor de logs y timeouts razonables
    private val clienteHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Instancia única de Retrofit — usa Gson para serializar JSON
    private val retrofit = Retrofit.Builder()
        .baseUrl(URL_BASE)
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val servicio: ServicioApi = retrofit.create(ServicioApi::class.java)
}
