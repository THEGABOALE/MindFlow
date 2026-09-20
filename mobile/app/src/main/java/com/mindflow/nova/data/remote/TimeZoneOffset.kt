package com.mindflow.nova.data.remote

import java.util.TimeZone

/** Minutos de diferencia con UTC del huso actual del dispositivo, positivo al este (Costa Rica = -360). */
fun currentTzOffsetMinutes(): Int =
    TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60_000
