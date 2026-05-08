package com.undef.superahorro.data.network

import com.undef.superahorro.data.network.dto.DtoCompra
import com.undef.superahorro.data.network.dto.DtoSupermercado
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ServicioApi {
    @GET("supermercados")
    suspend fun obtenerSupermercados(): Response<List<DtoSupermercado>>

    @GET("compras")
    suspend fun obtenerCompras(): Response<List<DtoCompra>>

    @POST("compras")
    suspend fun crearCompra(@Body compra: DtoCompra): Response<DtoCompra>
}
