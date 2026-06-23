package com.redes.app.ui.update

sealed class ForceUpdateState {
    data object Checking : ForceUpdateState()
    data object UpToDate : ForceUpdateState()
    data class UpdateRequired(
        val versionNominal: String,
        val mensaje: String,
    ) : ForceUpdateState()
}
