package com.undef.superahorro.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import java.util.Calendar


@Composable
/**
 * Campo de fecha con formato DD/MM/AAAA.
 * Las barras están fijas y valida que el día, mes y año sean coherentes.
 */
fun CampoFecha(
    valor: String,
    alCambiar: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String = "Fecha"
) {
    val anioActual = Calendar.getInstance().get(Calendar.YEAR)

    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo ->
            val soloNumeros = nuevo.filter { it.isDigit() }
            if (soloNumeros.length <= 8) {
                alCambiar(formatearFecha(soloNumeros, anioActual))
            }
        },
        label = { Text(etiqueta) },
        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
        placeholder = { Text("DD/MM/AAAA") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        isError = valor.length == 10 && !fechaValida(valor, anioActual),
        supportingText = {
            if (valor.length == 10 && !fechaValida(valor, anioActual)) {
                Text("Fecha inválida", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

/**
 * Campo de hora con formato HH:MM.
 * El ":" está fijo. Solo se ingresan los números.
 * Valida que horas (0-23) y minutos (0-59) sean válidos.
 */
@Composable
fun CampoHora(
    valor: String,
    alCambiar: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: String = "Hora"
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevo ->
            val soloNumeros = nuevo.filter { it.isDigit() }
            if (soloNumeros.length <= 4) {
                alCambiar(formatearHora(soloNumeros))
            }
        },
        label = { Text(etiqueta) },
        leadingIcon = { Icon(Icons.Outlined.Schedule, null) },
        placeholder = { Text("HH:MM") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        isError = valor.length == 5 && !horaValida(valor),
        supportingText = {
            if (valor.length == 5 && !horaValida(valor)) {
                Text("Hora inválida", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

// ── Formateo ─────────────────────────────────────────────────────────────────

private fun formatearFecha(numeros: String, anioActual: Int): String {
    val sb = StringBuilder()
    for (i in numeros.indices) {
        when (i) {
            0 -> {
                // Primer dígito del día: solo puede ser 0, 1, 2 o 3
                val d = numeros[i].digitToInt()
                sb.append(if (d > 3) "3" else numeros[i])
            }
            1 -> {
                // Segundo dígito del día: validar con el primero
                val dia = "${sb[0]}${numeros[i]}".toIntOrNull() ?: 0
                sb.append(if (dia < 1) "1" else if (dia > 31) "1" else numeros[i])
                sb.append("/")
            }
            2 -> {
                // Primer dígito del mes: solo puede ser 0 o 1
                val m = numeros[i].digitToInt()
                sb.append(if (m > 1) "1" else numeros[i])
            }
            3 -> {
                // Segundo dígito del mes: validar con el primero
                val mes = "${sb[3]}${numeros[i]}".toIntOrNull() ?: 0
                sb.append(if (mes < 1) "1" else if (mes > 12) "2" else numeros[i])
                sb.append("/")
            }
            4 -> {
                // Primer dígito del año: solo puede ser 1 o 2
                val a = numeros[i].digitToInt()
                sb.append(if (a < 1) "1" else if (a > 2) "2" else numeros[i])
            }
            5, 6, 7 -> sb.append(numeros[i])
        }
    }
    // Validar que el año completo no supere el actual
    if (sb.length == 10) {
        val anio = sb.substring(6, 10).toIntOrNull() ?: 0
        if (anio > anioActual) {
            return sb.substring(0, 6) + anioActual.toString()
        }
    }
    return sb.toString()
}

private fun formatearHora(numeros: String): String {
    val sb = StringBuilder()
    for (i in numeros.indices) {
        when (i) {
            0 -> {
                // Primer dígito de la hora: 0, 1 o 2
                val h = numeros[i].digitToInt()
                sb.append(if (h > 2) "2" else numeros[i])
            }
            1 -> {
                // Segundo dígito: si primera es 2, solo 0-3
                val hora = "${sb[0]}${numeros[i]}".toIntOrNull() ?: 0
                sb.append(if (hora > 23) "3" else numeros[i])
                sb.append(":")
            }
            2 -> {
                // Primer dígito de minutos: solo 0-5
                val m = numeros[i].digitToInt()
                sb.append(if (m > 5) "5" else numeros[i])
            }
            3 -> sb.append(numeros[i])
        }
    }
    return sb.toString()
}

// ── Validación ────────────────────────────────────────────────────────────────

fun fechaValida(fecha: String, anioActual: Int): Boolean {
    if (fecha.length != 10) return false
    val partes = fecha.split("/")
    if (partes.size != 3) return false
    val dia  = partes[0].toIntOrNull() ?: return false
    val mes  = partes[1].toIntOrNull() ?: return false
    val anio = partes[2].toIntOrNull() ?: return false
    if (dia < 1 || dia > 31) return false
    if (mes < 1 || mes > 12) return false
    if (anio < 1900 || anio > anioActual) return false
    // Días por mes
    val diasEnMes = diasDelMes(mes, anio)
    return dia <= diasEnMes
}

fun horaValida(hora: String): Boolean {
    if (hora.length != 5) return false
    val partes = hora.split(":")
    if (partes.size != 2) return false
    val horas   = partes[0].toIntOrNull() ?: return false
    val minutos = partes[1].toIntOrNull() ?: return false
    return horas in 0..23 && minutos in 0..59
}

private fun diasDelMes(mes: Int, anio: Int): Int = when (mes) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11            -> 30
    2 -> if (anio % 4 == 0 && (anio % 100 != 0 || anio % 400 == 0)) 29 else 28
    else -> 31
}
