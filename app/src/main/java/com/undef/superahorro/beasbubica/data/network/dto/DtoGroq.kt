package com.undef.superahorro.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTOs para la API de Groq (formato compatible con OpenAI: /chat/completions).
 * Se usan para mandar la imagen del ticket y recibir el JSON con los datos extraídos.
 */

// ---------- Request ----------

data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.1,
    @SerializedName("response_format") val responseFormat: GroqResponseFormat = GroqResponseFormat()
)

/** Fuerza a que el modelo responda un JSON válido (modo JSON de Groq). */
data class GroqResponseFormat(val type: String = "json_object")

data class GroqMessage(
    val role: String,
    val content: List<GroqContent>
)

/**
 * Cada parte del mensaje puede ser texto o una imagen.
 * Para texto se usa 'text'; para imagen se usa 'imageUrl' con un data URL en base64.
 */
data class GroqContent(
    val type: String,
    val text: String? = null,
    @SerializedName("image_url") val imageUrl: GroqImageUrl? = null
)

data class GroqImageUrl(val url: String)

// ---------- Response ----------

data class GroqResponse(val choices: List<GroqChoice> = emptyList())
data class GroqChoice(val message: GroqResponseMessage)
data class GroqResponseMessage(val content: String)

// ---------- JSON del ticket (lo que devuelve el modelo dentro de 'content') ----------

/**
 * Estructura que le pedimos al modelo que devuelva a partir del ticket.
 * Todos los campos son opcionales por si el ticket está borroso o incompleto.
 */
data class TicketExtraido(
    val supermercado: String? = null,
    val fecha: String? = null,   // formato dd/MM/yyyy
    val hora: String? = null,    // formato HH:mm
    val total: Double? = null,   // total final impreso (ya con el descuento aplicado)
    val descuento: Double? = null, // monto total de descuentos (0 si no hubo)
    val productos: List<ProductoExtraido>? = null
)

data class ProductoExtraido(
    val nombre: String? = null,
    val cantidad: Int? = null,
    val precio: Double? = null,   // costo unitario
    @SerializedName("codigo_barras") val codigoBarras: String? = null
)
