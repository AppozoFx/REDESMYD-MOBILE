Crea un nuevo módulo de pantalla en REDES-MOBILE siguiendo el patrón real del proyecto.

El argumento es el nombre del módulo en PascalCase, por ejemplo: `Garantias` o `StockDetalle`.

## Instrucciones

Preguntá al usuario qué rol usa esta pantalla (TECNICO, SUPERVISOR, COORDINADOR) si no está en $ARGUMENTS.

Luego creá los siguientes archivos:

### 1. `ui/[modulo]/[Modulo]UiState.kt`
Paquete: `com.redes.app.ui.[modulo en lowercase]`

```kotlin
package com.redes.app.ui.[modulo]

data class [Modulo]UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // agregar campos de datos según la pantalla
)
```

### 2. `ui/[modulo]/[Modulo]ViewModel.kt`
Paquete: `com.redes.app.ui.[modulo en lowercase]`

```kotlin
package com.redes.app.ui.[modulo]

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.redes.app.data.[modulo].[Modulo]Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class [Modulo]ViewModel(
    private val [modulo]Repository: [Modulo]Repository,
) : ViewModel() {

    private val _uiState = MutableStateFlow([Modulo]UiState())
    val uiState: StateFlow<[Modulo]UiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = [modulo]Repository.fetch[Modulo]()
            result.fold(
                onSuccess = { data ->
                    _uiState.update { it.copy(isLoading = false /*, agregar datos */ ) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            )
        }
    }

    companion object {
        fun factory([modulo]Repository: [Modulo]Repository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    [Modulo]ViewModel([modulo]Repository) as T
            }
    }
}
```

## Reglas que SIEMPRE debes respetar
- El paquete de `ui/[modulo]` debe coincidir exactamente: `com.redes.app.ui.[modulo]`
- Siempre `_uiState` privado y `uiState` público como `StateFlow` (nunca `LiveData`)
- El ViewModel recibe repositorios por constructor — no los instancia internamente
- Usar `.update { it.copy(...) }` para mutar el estado
- Errores van en `errorMessage: String?` — nunca mostrar stack trace en el estado
- El `companion object factory` es necesario para inyectar desde AppContainer sin Hilt
- No poner lógica de negocio en el UiState — es solo estado de presentación
- Si el módulo necesita un Repository nuevo, recordarle al usuario que debe crearlo con `/mobile-repo` y registrarlo en AppContainer
