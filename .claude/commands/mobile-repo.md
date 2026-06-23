Crea un nuevo Repository en REDES-MOBILE siguiendo el patrón real del proyecto y lo registra en AppContainer.

El argumento es el nombre del módulo en PascalCase, por ejemplo: `Garantias` o `StockDetalle`.

## Instrucciones

Preguntá al usuario qué operaciones necesita el repositorio (fetch, post, etc.) si no están claras.

Luego creá los siguientes archivos y modificá AppContainer:

### 1. `data/[modulo]/[Modulo]Models.kt`
Paquete: `com.redes.app.data.[modulo en lowercase]`

```kotlin
package com.redes.app.data.[modulo]

data class [Modulo]Data(
    // campos que devuelve el backend
)
```

### 2. `data/[modulo]/[Modulo]Repository.kt`
Interfaz del repositorio:

```kotlin
package com.redes.app.data.[modulo]

interface [Modulo]Repository {
    suspend fun fetch[Modulo](): Result<[Modulo]Data>
    // agregar métodos según las operaciones necesarias
}
```

### 3. `data/[modulo]/Remote[Modulo]Repository.kt`
Implementación real que usa RedesApiClient:

```kotlin
package com.redes.app.data.[modulo]

import com.redes.app.network.RedesApiClient
import com.redes.app.network.RedesApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Remote[Modulo]Repository(
    private val apiClient: RedesApiClient,
) : [Modulo]Repository {

    override suspend fun fetch[Modulo](): Result<[Modulo]Data> = call {
        apiClient.fetch[Modulo]()  // agregar este método a RedesApiClient
    }

    private suspend inline fun <T> call(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(withContext(Dispatchers.IO) { block() })
        } catch (exception: RedesApiException) {
            Result.failure(exception)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
```

### 4. Registrar en `di/AppContainer.kt`

Agregar a la **interfaz** `AppContainer`:
```kotlin
val [modulo]Repository: [Modulo]Repository
```

Agregar a la **clase** `DefaultAppContainer`:
```kotlin
override val [modulo]Repository: [Modulo]Repository by lazy {
    Remote[Modulo]Repository(apiClient)
}
```

Agregar el import correspondiente en la cabecera del archivo.

### 5. Agregar método a `network/RedesApiClient.kt`

El método nuevo que llama Remote[Modulo]Repository necesita existir en RedesApiClient.
Seguir el patrón de los métodos existentes: construir Request con OkHttp, ejecutar, parsear JSON de respuesta.

## Reglas que SIEMPRE debes respetar
- El paquete de `data/[modulo]` debe coincidir: `com.redes.app.data.[modulo]`
- Remote[Modulo]Repository siempre usa el helper `call { }` con `withContext(Dispatchers.IO)`
- Nunca hacer llamadas de red en el hilo principal
- Remote[Modulo]Repository recibe `RedesApiClient` por constructor — no lo instancia
- En AppContainer usar `by lazy { }` para toda instanciación
- Si el endpoint del backend aún no existe, recordarle al usuario que debe crearlo con `/redes-mobile-endpoint` en el proyecto REDES
- Los modelos (`[Modulo]Models.kt`) solo contienen data classes — sin lógica
