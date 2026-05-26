package com.redes.app.ui.common

fun String.toShortPersonName(): String {
    val parts = trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return ""
    if (parts.size == 1) return parts[0]
    return "${parts.first()} ${parts[1]}"
}

fun preferredPersonName(nombreCorto: String?, nombreCompleto: String): String {
    return nombreCorto?.trim().orEmpty().ifBlank { nombreCompleto.toShortPersonName() }
}
