package com.redes.app.data.common

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun <T> Task<T>.awaitResult(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            val exception = task.exception
            when {
                task.isSuccessful -> continuation.resume(task.result)
                exception != null -> continuation.resumeWith(Result.failure(exception))
                else -> continuation.resumeWith(Result.failure(IllegalStateException("Task cancelada.")))
            }
        }
    }
}
