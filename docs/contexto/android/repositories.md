# Repositorios Android Y Manejo De Resultados

Actualizado: 2026-06-22.

Estado: **Revisar**. Deep dive incremental de repositorios Android, ensamblaje DI y patrones de errores/resultados.

## Ensamblaje

`C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\di\AppContainer.kt` crea:

- `RedesApiClient` con `BuildConfig.API_BASE_URL`.
- `OkHttpClient` con `AuthTokenInterceptor`.
- Repositorios remotos: auth, session, presence, tecnico, alertas, coordinador, supervisor, almacen y tracking.
- `TrackingManager` se instancia con `Context` y arranca/detiene `LocationTrackingService`; no usa `TrackingRepository` directamente desde DI.

## Mapa De Repositorios

| Repositorio/clase | Metodos | Endpoint via `RedesApiClient` | Resultado | Observaciones |
| --- | --- | --- | --- | --- |
| `FirebaseAuthRepository` | `currentUser`, `signIn`, `signOut` | Firebase Auth SDK | `Flow<AuthUser?>`, `Result<AuthUser>` | `signIn` captura cualquier excepcion del SDK y expone `Result.failure`; no consulta backend REDES |
| `RemoteSessionRepository` | `fetchBootstrap`, `markComunicadoSeen` | `/bootstrap`, `/comunicados/{id}/seen` | `MobileBootstrap`, `Unit` | Guarda sesion en `SessionCacheDataSource`; no usa `/me` |
| `BackendPresenceRepository` | `markOnline`, `markOffline` | `/presencia` POST/DELETE | `Unit` | Ignora `PresencePayload`/`uid` local y delega identidad al token/backend |
| `TrackingRepository` | `postLocation` | `/tracking` POST | `Unit` | No retorna `Result`; `RedesApiClient.postTracking` encola llamada asincrona OkHttp |
| `RemoteAlertaRepository` | `postAlertaCerrarRuta`, `postRequiereAtencion` | `/alertas-app` POST | `alertaId` o `Unit` | Tambien escucha Firestore `alertas_app` y actualiza `notificaciones_tecnico` |
| `RemoteTecnicoRepository` | home, ordenes, detalle, stock, mapa, cuadrillas, inicio jornada, sustento | Rutas `/tecnico/*`, `/inicio-jornada` | Modelos `Tecnico*` | En sustento valida SN/cuadrilla local antes de subir multipart |
| `RemoteSupervisorRepository` | home, ordenes, detalle, supervision, garantia, mapa, cuadrillas, jornada | Rutas `/supervisor/*`; tambien expone `/inicio-jornada` | Modelos `Supervisor*` | `iniciarJornada` requiere revision porque backend de `/inicio-jornada` es tecnico |
| `RemoteCoordinadorRepository` | resumen, cuadrillas, mapa, stock, auditoria, predespacho, ventas, plantillas, sustento | Rutas `/coordinador/*` y `/tecnico/cuadrillas-mapa` | Modelos `Coordinador*` | Deep dive en `android/coordinador-shell-repositorio.md`; usa ruta tecnica compartida para cuadrillas en mapa |
| `RemoteAlmacenRepository` | stock, liquidacion, instalaciones, cuadrillas mapa | Rutas `/almacen/*` y `/tecnico/cuadrillas-mapa` | Modelos `Almacen*` y `CuadrillaMapa` | Revision incremental en `android/almacen-shell-repositorio.md`; usa header `X-Mobile-Role: ALMACEN` |

Nota de cierre corto: `LocationTrackingService.kt` fue verificado en `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\data\tracking\LocationTrackingService.kt`; no existe en `app\src\main\java\com\redes\app\service`.

## Manejo De Resultados

- `FirebaseAuthRepository.signIn` retorna `Result<AuthUser>`; `currentUser` es un `callbackFlow` que refleja `FirebaseAuth.AuthStateListener`.
- `RemoteSessionRepository.fetchBootstrap` y `markComunicadoSeen` fallan temprano con `BackendConfigurationException` si `apiClient.isConfigured` es false.
- `RemoteTecnicoRepository`, `RemoteSupervisorRepository` y `RemoteCoordinadorRepository` usan un helper `call { ... }` con `withContext(Dispatchers.IO)`, capturan `RedesApiException` y excepciones generales, y devuelven `Result<T>`.
- `RemoteAlmacenRepository` usa el mismo patron `call { ... }` con `withContext(Dispatchers.IO)` y devuelve `Result<T>` para stock, liquidacion, instalaciones y mapa.
- `RemoteAlertaRepository` captura excepciones generales en los posts, pero sus listeners Firestore no devuelven `Result`: ante error de snapshot de notificaciones emite lista vacia; en `listenAlertaEstado` simplemente ignora error/snapshot nulo.
- `BackendPresenceRepository.markOnline/markOffline` no retorna `Result`; los errores quedan para el consumidor (`MobilePresenceManager`).
- `TrackingRepository.postLocation` no es suspend y no propaga resultado; `RedesApiClient.postTracking` hace `enqueue`, por lo que fallas HTTP/red quedan fuera del contrato del repositorio.

## Validaciones Locales

- `RemoteTecnicoRepository.sustainStockEquipment` normaliza `sn` a uppercase, valida `SN_REQUIRED` y `CUADRILLA_REQUIRED`, lee bytes desde `ContentResolver`, deriva extension por MIME y llama multipart tecnico.
- `RemoteCoordinadorRepository.sustainEquipo` normaliza `sn` y `cuadrillaId`, lee bytes y deriva extension, pero no valida explicitamente blancos antes del multipart; si el `Uri` no abre lanza `PHOTO_READ_FAILED`.
- `RemoteSupervisorRepository.updateGarantia` pasa campos de garantia directamente al cliente API; las opciones/obligatoriedad se controlan en UI/backend, no en el repositorio.
- `RemoteSessionRepository` guarda cache solo despues de `fetchBootstrap` exitoso; `clearCache` y `saveSelectedRole` delegan directo a `SessionCacheDataSource`.

## Fallos Y Silencios Observados

- `Result.failure` preserva excepciones, pero varios ViewModels convierten errores a mensajes generales o actualizan solo flags parciales; revisar cada consumidor antes de cambiar UX.
- Los listeners de `RemoteAlertaRepository` no exponen errores al ViewModel, lo que puede ocultar problemas de Firestore rules o conexion.
- Presencia y tracking privilegian fire-and-forget; son sensibles a pruebas reales de red, permisos y ciclo de vida.
- La dependencia comun `CuadrillaMapa` vive en `data\tecnico`, aunque la consumen supervisor y coordinador.
- `RemoteSupervisorRepository.iniciarJornada()` sigue llamando `apiClient.postInicioJornada()`, endpoint documentado como tecnico en revisiones previas.

## Relacion Con Documentos De Profundidad

- Sesion/cache/bootstrap: `android/session-auth-bootstrap.md`.
- Tracking/presencia: `android/tracking.md`.
- Tecnico alertas/cierre: `android/tecnico-alertas-cierre-ruta.md`.
- Supervisor shell/alertas: `android/supervisor-shell-alertas.md`.
- Coordinador shell/repositorio: `android/coordinador-shell-repositorio.md`.
- Modelos/DTOs: `android/models.md`.

## Pendientes Especificos 2026-06-20

- Decidir si todos los repositorios remotos deben validar `apiClient.isConfigured` o centralizar ese fallo en `RedesApiClient`.
- Definir si tracking/presencia/Firestore listeners deben exponer errores observables hacia UI o telemetria.
- Validar `RemoteSupervisorRepository.iniciarJornada()` contra endpoint supervisor real o retirarlo si no se usa.
- Alinear validacion local de multipart entre tecnico y coordinador.
- Mover o duplicar `CuadrillaMapa` a modelo comun si se confirma uso multirrol estable.
- Agregar pruebas/fixtures de repositorios con respuestas de `RedesApiClient` o contratos fake antes de refactors.

## Manejo De Resultados Historico

- `RemoteSessionRepository` falla temprano si `apiClient.isConfigured` es false.
- `RemoteTecnicoRepository`, `RemoteSupervisorRepository` y `RemoteCoordinadorRepository` no verifican `isConfigured` antes de todos los llamados; dependen de errores del cliente.
- `TrackingRepository.postLocation` no es suspend y llama al cliente sin propagar resultado; `LocationTrackingService` captura excepciones y solo escribe log.

## Dependencias Hacia UI/ViewModel

Lectura representativa:

- `TecnicoViewModel` llama `tecnicoRepository.iniciarJornada`, `fetchHome`, `fetchOrders`, `fetchStock`, `fetchMap`, `fetchOrderDetail`, `sustainStockEquipment`, `fetchCuadrillasMapa`.
- `SupervisorViewModel` consume repositorio supervisor para ordenes, mapa, jornada, supervision y garantia.
- `CoordinadorViewModel` consume repositorio coordinador para resumen, cuadrillas, mapa, stock, auditoria, predespacho, ventas y plantillas; tambien recibe `TecnicoRepository` para flujos compartidos.
- `AlmacenViewModel` consume repositorio almacen para stock, liquidacion, instalaciones y mapa; se habilita solo con usuario autenticado y rol seleccionado `ALMACEN`.

No se documento UI completa en esta unidad.

## Actualizacion 2026-06-22 - Almacen

Se documento `AlmacenShellScreen`, `AlmacenViewModel`, `AlmacenUiState`, `RemoteAlmacenRepository`, `AlmacenModels.kt`, `AlmacenDtos.kt`, endpoints mobile de almacen y cableado en `MainActivity`, `AppNavHost` y `AppContainer` en `android/almacen-shell-repositorio.md`.

Pendientes especificos: validar mapa/permisos en dispositivo, revisar `isRefreshing`, crear fixtures JSON y decidir si el mapa de cuadrillas de almacen debe seguir reutilizando `/api/mobile/tecnico/cuadrillas-mapa`.

## Actualizacion 2026-06-17 - Coordinador

Se documento `RemoteCoordinadorRepository`, `CoordinadorRepository`, `CoordinadorModels.kt`, `CoordinadorDtos.kt`, `CoordinadorViewModel` y `CoordinadorShellScreen` en `android/coordinador-shell-repositorio.md`.

Pendientes especificos: validar backend REDES para `/api/mobile/coordinador/*`, decidir si el mapa de cuadrillas debe seguir usando `/api/mobile/tecnico/cuadrillas-mapa`, probar sustento fotografico en dispositivo real y revisar errores silenciosos en cargas secundarias.

## Actualizacion 2026-06-18 - Tecnico Alertas/Cierre

Se documento `TecnicoViewModel`, `TecnicoUiState`, `TecnicoShellScreen`, `NotificationsScreen`, `RemoteTecnicoRepository`, `RemoteAlertaRepository`, `TrackingManager`, `LocationTrackingService`, endpoints mobile de tecnico y backend web de respuesta de alertas en `android/tecnico-alertas-cierre-ruta.md`.

Hallazgo clave: el cierre tecnico no cierra ruta directamente en backend; Android detiene tracking local y crea una alerta `CERRAR_RUTA`. La escritura oficial `RUTA_CERRADA` ocurre cuando web acepta la alerta. Pendientes: rules Firestore para `alertas_app`/`notificaciones_tecnico`, manejo de rechazo, reinicio de tracking y validacion en dispositivo.
