package com.undef.superahorro.data.network

import com.undef.superahorro.data.network.dto.GroqRequest
import com.undef.superahorro.data.network.dto.GroqResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Endpoint de Groq (compatible con OpenAI).
 * Manda la imagen del ticket + el prompt y devuelve la respuesta del modelo.
 * Es la operación de red POST real de la app.
 */
interface ServicioGroq {
    @POST("openai/v1/chat/completions")
    suspend fun analizarTicket(
        @Header("Authorization") autorizacion: String,
        @Body cuerpo: GroqRequest
    ): GroqResponse
}
