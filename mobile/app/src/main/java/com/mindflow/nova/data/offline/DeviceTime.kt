package com.mindflow.nova.data.offline

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

// Horas del teléfono para los intentos jugados sin conexión. La app soporta
// Android 7 (minSdk 24) y java.time recién existe desde Android 8, así que las
// horas viajan como epoch en milisegundos más el desfase del huso en minutos
// (positivo al este; Costa Rica = -360), igual que en el backend.

private const val MINUTE_MS = 60_000L
private const val DAY_MS = 24 * 60 * MINUTE_MS

// SimpleDateFormat no es seguro entre hilos: se crea uno por llamada.
private fun utcFormat(pattern: String) =
    SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

/** "2026-10-01T12:26:04.000-06:00": la hora local del dispositivo con su huso, como la pide el backend. */
fun isoWithOffset(epochMs: Long, tzOffsetMinutes: Int): String {
    val local = utcFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(epochMs + tzOffsetMinutes * MINUTE_MS)
    val sign = if (tzOffsetMinutes < 0) "-" else "+"
    val minutes = abs(tzOffsetMinutes)

    // Locale.US para que los dígitos no salgan en otro sistema de numeración.
    return local + sign + String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60)
}

/** Fecha local "YYYY-MM-DD" de un instante, en el huso dado. */
fun localDay(epochMs: Long, tzOffsetMinutes: Int): String =
    utcFormat("yyyy-MM-dd").format(epochMs + tzOffsetMinutes * MINUTE_MS)

/** El instante de una hora armada con [isoWithOffset] (o terminada en "Z"). */
fun epochFromIso(iso: String): Long {
    val local = utcFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(iso.substring(0, 23))!!.time
    val zone = iso.substring(23)

    if (zone == "Z") return local

    val sign = if (zone[0] == '-') -1 else 1
    val offsetMinutes = zone.substring(1, 3).toInt() * 60 + zone.substring(4, 6).toInt()

    return local - sign * offsetMinutes * MINUTE_MS
}

/** Número de día desde 1970 de una fecha "YYYY-MM-DD". */
internal fun dayNumber(day: String): Long = utcFormat("yyyy-MM-dd").parse(day)!!.time / DAY_MS

internal fun dayFromNumber(number: Long): String = utcFormat("yyyy-MM-dd").format(number * DAY_MS)
