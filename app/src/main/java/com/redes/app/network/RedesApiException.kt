package com.redes.app.network

class RedesApiException(
    message: String,
    val statusCode: Int? = null,
) : IllegalStateException(message)
