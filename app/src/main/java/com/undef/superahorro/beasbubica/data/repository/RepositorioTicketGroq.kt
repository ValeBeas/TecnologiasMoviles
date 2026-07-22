package com.undef.superahorro.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import com.undef.superahorro.BuildConfig
import com.undef.superahorro.data.network.ClienteGroq
import com.undef.superahorro.data.network.dto.GroqContent
import com.undef.superahorro.data.network.dto.GroqImageUrl
import com.undef.superahorro.data.network.dto.GroqMessage
import com.undef.superahorro.data.network.dto.GroqRequest
import com.undef.superahorro.data.network.dto.TicketExtraido
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

/** La API key de Groq no está configurada (local.properties vacío). */
class ApiKeyFaltanteException : Exception("Falta la API key de Groq")

/** La API key de Groq es inválida (Groq respondió 401). */
class ApiKeyInvalidaException : Exception("API key de Groq inválida")

/** Se alcanzó el límite de uso de la API de Groq (respondió 429). */
class RateLimitException : Exception("Límite de la API de Groq alcanzado")

/**
 * Procesa la foto de un ticket usando la IA de visión de Groq.
 * Manda la imagen (base64) + un prompt y devuelve los datos estructurados del ticket.
 *
 * Es la operación de red POST real de la app (Retrofit → Groq).
 */
class RepositorioTicketGroq {

    private val gson = Gson()

    /**
     * Analiza el ticket y devuelve los datos extraídos.
     * @param bytesImagen bytes de la foto del ticket (JPEG/PNG).
     */
    suspend fun analizarTicket(bytesImagen: ByteArray): Result<TicketExtraido> = runCatching {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isBlank()) throw ApiKeyFaltanteException()

        // 1. Reducir la imagen (menos tokens = más rápido y evita el rate limit) → base64 → data URL
        val bytesReducidos = reducirImagen(bytesImagen)
        val base64 = Base64.encodeToString(bytesReducidos, Base64.NO_WRAP)
        val dataUrl = "data:image/jpeg;base64,$base64"

        // 2. Armar el request: prompt de texto + la imagen
        val request = GroqRequest(
            model = MODELO_VISION,
            messages = listOf(
                GroqMessage(
                    role = "user",
                    content = listOf(
                        GroqContent(type = "text", text = PROMPT),
                        GroqContent(type = "image_url", imageUrl = GroqImageUrl(dataUrl))
                    )
                )
            )
        )

        // 3. Llamar a Groq (POST) con la key en el header Authorization.
        //    401 = key inválida; 429 = se alcanzó el límite de uso.
        val respuesta = try {
            ClienteGroq.servicio.analizarTicket("Bearer $apiKey", request)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> throw ApiKeyInvalidaException()
                429 -> throw RateLimitException()
                else -> throw e
            }
        }

        // 4. El modelo responde un JSON (string) dentro de content → lo parseamos
        val jsonTicket = respuesta.choices.firstOrNull()?.message?.content
            ?: throw Exception("La IA no devolvió datos")

        gson.fromJson(jsonTicket, TicketExtraido::class.java)
    }

    /**
     * Baja la resolución y recomprime la imagen para mandar menos datos a la IA.
     * Menos tokens = más rápido y menos chance de pegarle al rate limit.
     * Si por algún motivo no se puede decodificar, devuelve los bytes originales.
     */
    private fun reducirImagen(bytes: ByteArray, ladoMaximo: Int = 1500, calidad: Int = 85): ByteArray {
        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val escala = ladoMaximo.toFloat() / maxOf(original.width, original.height)
        val bitmap = if (escala < 1f) {
            Bitmap.createScaledBitmap(
                original,
                (original.width * escala).toInt(),
                (original.height * escala).toInt(),
                true
            )
        } else original
        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
        return salida.toByteArray()
    }

    companion object {
        // Modelo de visión de Groq. Si Groq lo da de baja, cambiar solo esta constante.
        private const val MODELO_VISION = "qwen/qwen3.6-27b"

        // Instrucciones para el modelo. Le pedimos JSON estricto con el esquema del ticket.
        private val PROMPT = """
            Sos un asistente que lee tickets de supermercado argentinos.
            Analizá la imagen del ticket y devolvé SOLO un JSON con esta estructura exacta:
            {
              "supermercado": "nombre del super o comercio",
              "fecha": "dd/MM/yyyy",
              "hora": "HH:mm",
              "total": number,
              "descuento": number,
              "productos": [
                { "nombre": "descripción del producto", "cantidad": number, "precio": number, "codigo_barras": "opcional" }
              ]
            }
            Reglas:
            - "precio" es el costo UNITARIO de cada producto SIN descuento (no el subtotal).
            - "descuento" es el monto TOTAL de descuentos/promociones aplicados a la compra
              (sumá todos los descuentos si hay varios). Si NO hubo ningún descuento, poné 0.
            - "total" es el total FINAL impreso en el ticket (ya con el descuento restado).
            - Debe cumplirse: (suma de precio×cantidad de los productos) - descuento ≈ total.
            - Si no podés leer un dato, poné null (para cantidad usá 1 si no se distingue; para descuento usá 0).
            - Los números van sin símbolo de moneda ni separador de miles (usá punto decimal).
            - No agregues texto fuera del JSON.
        """.trimIndent()
    }
}
