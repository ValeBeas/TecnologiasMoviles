package com.undef.superahorro.data.network

import com.undef.superahorro.data.network.dto.DtoDolar
import retrofit2.http.GET

/**
 * Endpoint de la API del dólar blue (bluelytics.com.ar).
 * Devuelve la cotización actual del dólar oficial y blue.
 */
interface ServicioDolar {
    @GET("v2/latest")
    suspend fun obtenerCotizacion(): DtoDolar
}
