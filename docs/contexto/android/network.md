# Network/API REDES-MOBILE

Actualizado: 2026-06-14.

Estado de la unidad: **Revisar**. Esta lectura documenta el cliente Android contra `C:\Proyectos\REDES\apps\web\src\app\api\mobile` y sus consumidores directos en REDES-MOBILE.

## Archivos Leidos

Network:

- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\network\RedesApiClient.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\network\MobileEndpoints.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\network\AuthTokenInterceptor.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\network\FirebaseIdTokenProvider.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\network\TokenProvider.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\network\RedesApiException.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\network\dto\*.kt`

Dependencias directas:

- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\di\AppContainer.kt`
- `data/session/RemoteSessionRepository.kt`
- `data/tecnico/RemoteTecnicoRepository.kt`
- `data/supervisor/RemoteSupervisorRepository.kt`
- `data/coordinador/RemoteCoordinadorRepository.kt`
- `data/tracking/TrackingRepository.kt`, `TrackingManager.kt`, `LocationTrackingService.kt`
- `data/presence/BackendPresenceRepository.kt`
- `data/alertas/RemoteAlertaRepository.kt`
- Modelos `TecnicoModels.kt`, `SupervisorModels.kt`, `CoordinadorModels.kt`.

## Configuracion Del Cliente

- `DefaultAppContainer` instancia `RedesApiClient(baseUrl = BuildConfig.API_BASE_URL)` y un `OkHttpClient` con `AuthTokenInterceptor`.
- `AuthTokenInterceptor` pide `TokenProvider.getIdToken()` y agrega `Authorization: Bearer <token>` si el request no trae `Authorization`.
- `fetchBootstrap(idToken)` y `fetchCurrentSession(idToken)` agregan el token manualmente. El resto depende del interceptor.
- El token sale de Firebase Auth con `FirebaseIdTokenProvider`.
- `local.properties` existe y contiene configuracion local de API base URL. No se copiaron valores.
- `RedesApiClient.isConfigured` solo valida base URL no vacia; algunos metodos hacen return silencioso si no esta configurado (`presence`, `tracking`, `markComunicadoSeen`), otros lanzan `RedesApiException`.

Nota 2026-06-14: el flujo real de sesion queda documentado en `android/session-auth-bootstrap.md`. `fetchCurrentSession` y `/api/mobile/me` siguen existiendo, pero no se encontro consumidor directo; `fetchBootstrap` es la ruta efectiva para sesion, roles, permisos, comunicados y `defaultRole`.

## Manejo De Errores

`RedesApiClient.executeJson` y `executeWithoutBody`:

- Rechazan respuestas no 2xx con `RedesApiException(message, statusCode)`.
- 401 -> "El backend rechazo el token de Firebase."
- 403 -> "El backend no permitio el acceso del usuario."
- 404 -> mensaje historico de endpoint inexistente para bootstrap, me y tecnico stock; otros 404 dicen endpoint solicitado no existe.
- Otros status -> intenta leer `error` del JSON de backend.
- IOException local contra `127.0.0.1`/`localhost` con conexion rechazada -> sugiere `adb reverse`.
- Respuestas no JSON o JSON invalido tambien se transforman en `RedesApiException`.

## Endpoints Android

| Metodo Android | Endpoint | Metodo HTTP | DTO/parser | Repositorio directo | Fuente Android |
| --- | --- | --- | --- | --- | --- |
| `fetchBootstrap(idToken)` | `/api/mobile/bootstrap` | GET | `MobileBootstrapDto` | `RemoteSessionRepository.fetchBootstrap` | `network\RedesApiClient.kt` |
| `fetchCurrentSession(idToken)` | `/api/mobile/me` | GET | `MobileSessionDto` | No se encontro uso directo | `network\RedesApiClient.kt` |
| `markComunicadoSeen(id)` | `/api/mobile/comunicados/{id}/seen` | POST | Sin body de dominio; backend validado con `-LiteralPath` por ruta `[id]`, usa `getMobileAuthContext` y `markMobileComunicadoSeen` | `RemoteSessionRepository.markComunicadoSeen` | `network\RedesApiClient.kt` |
| `markPresenceOnline` | `/api/mobile/presencia` | POST | Sin body de dominio | `BackendPresenceRepository.markOnline` | `network\RedesApiClient.kt` |
| `markPresenceOffline` | `/api/mobile/presencia` | DELETE | Sin body de dominio | `BackendPresenceRepository.markOffline` | `network\RedesApiClient.kt` |
| `postTracking` | `/api/mobile/tracking` | POST | Sin body de dominio | `LocationTrackingService` -> `TrackingRepository` | `data\tracking\LocationTrackingService.kt` |
| `postInicioJornada` | `/api/mobile/inicio-jornada` | POST | `estadoRuta: String` | `RemoteTecnicoRepository.iniciarJornada`; tambien expuesto en supervisor | `network\RedesApiClient.kt` |
| `postAlertaApp` | `/api/mobile/alertas-app` | POST | `alertaId: String` | `RemoteAlertaRepository` | `network\RedesApiClient.kt` |
| `fetchTecnicoHome` | `/api/mobile/tecnico/home` | GET | `toTecnicoHomeData` | `RemoteTecnicoRepository.fetchHome` | `network\dto\TecnicoDtos.kt` |
| `fetchTecnicoOrders` | `/api/mobile/tecnico/ordenes?ymd=` | GET | `toTecnicoOrdersData` | `RemoteTecnicoRepository.fetchOrders` | `network\dto\TecnicoDtos.kt` |
| `fetchTecnicoOrderDetail` | `/api/mobile/tecnico/ordenes/{id}` | GET | `toTecnicoOrderDetail` | `RemoteTecnicoRepository.fetchOrderDetail` | `network\dto\TecnicoDtos.kt` |
| `fetchTecnicoStock` | `/api/mobile/tecnico/stock` | GET | `toTecnicoStockData` | `RemoteTecnicoRepository.fetchStock` | `network\dto\TecnicoDtos.kt` |
| `sustainTecnicoStockEquipment` | `/api/mobile/tecnico/stock` | POST multipart | `toTecnicoStockEquipment` | `RemoteTecnicoRepository.sustainStockEquipment` | `network\RedesApiClient.kt` |
| `fetchTecnicoMap` | `/api/mobile/tecnico/mapa?ymd=` | GET | `toTecnicoMapData` | `RemoteTecnicoRepository.fetchMap` | `network\dto\TecnicoDtos.kt` |
| `fetchTecnicoCuadrillasMapa` | `/api/mobile/tecnico/cuadrillas-mapa` | GET | `toCuadrillasMapa` | `RemoteTecnicoRepository.fetchCuadrillasMapa` | `network\dto\TecnicoDtos.kt` |
| `fetchCoordinadorResumen` | `/api/mobile/coordinador/inicio?ym=` | GET | `toCoordinadorResumen` | `RemoteCoordinadorRepository.fetchResumen` | `network\dto\CoordinadorDtos.kt` |
| `fetchCoordinadorCuadrillas` | `/api/mobile/coordinador/cuadrillas?ymd=` | GET | `toCoordinadorCuadrillaData` | `RemoteCoordinadorRepository.fetchCuadrillas` | `network\dto\CoordinadorDtos.kt` |
| `fetchCoordinadorCuadrillasMapa` | `/api/mobile/tecnico/cuadrillas-mapa` | GET | `toCuadrillasMapa` | `RemoteCoordinadorRepository.fetchCuadrillasMapa` | `network\MobileEndpoints.kt` |
| `fetchCoordinadorMapa` | `/api/mobile/coordinador/mapa?ymd=` | GET | `toCoordinadorMapItems` | `RemoteCoordinadorRepository.fetchMapa` | `network\dto\CoordinadorDtos.kt` |
| `fetchCoordinadorStock` | `/api/mobile/coordinador/stock` | GET | `toCoordinadorStockList` | `RemoteCoordinadorRepository.fetchStock` | `network\dto\CoordinadorDtos.kt` |
| `fetchCoordinadorAuditoria` | `/api/mobile/coordinador/auditoria` | GET | `toCoordinadorAuditoriaList` | `RemoteCoordinadorRepository.fetchAuditoria` | `network\dto\CoordinadorDtos.kt` |
| `sustainCoordinadorEquipo` | `/api/mobile/coordinador/auditoria/sustentar` | POST multipart | `toCoordinadorEquipoAuditoria` | `RemoteCoordinadorRepository.sustainEquipo` | `network\RedesApiClient.kt` |
| `fetchCoordinadorPredespacho` | `/api/mobile/coordinador/predespacho?ymd=` | GET | `toCoordinadorPredespacho` | `RemoteCoordinadorRepository.fetchPredespacho` | `network\dto\CoordinadorDtos.kt` |
| `fetchCoordinadorVentas` | `/api/mobile/coordinador/ventas?year=&month=` | GET | `toCoordinadorVentaList` | `RemoteCoordinadorRepository.fetchVentas` | `network\dto\CoordinadorDtos.kt` |
| `fetchCoordinadorPlantillas` | `/api/mobile/coordinador/plantillas?ym=` | GET | `toCoordinadorPlantillasList` | `RemoteCoordinadorRepository.fetchPlantillas` | `network\dto\CoordinadorDtos.kt` |
| `fetchSupervisorHome` | `/api/mobile/supervisor/home` | GET | `toSupervisorHomeData` | `RemoteSupervisorRepository.fetchHome` | `network\dto\SupervisorDtos.kt` |
| `fetchSupervisorOrders` | `/api/mobile/supervisor/ordenes?ymd=&garantias=` | GET | `toSupervisorOrdersData` | `RemoteSupervisorRepository.fetchOrders` | `network\dto\SupervisorDtos.kt` |
| `fetchSupervisorOrderDetail` | `/api/mobile/supervisor/ordenes/{id}` | GET | `toSupervisorOrderDetail` | `RemoteSupervisorRepository.fetchOrderDetail` | `network\dto\SupervisorDtos.kt` |
| `postSupervisorSupervision` | `/api/mobile/supervisor/supervision` | POST | Sin body de dominio | `RemoteSupervisorRepository.saveSupervision` | `network\RedesApiClient.kt` |
| `postSupervisorGarantiaUpdate` | `/api/mobile/supervisor/garantias/update` | POST | Sin body de dominio | `RemoteSupervisorRepository.updateGarantia` | `network\RedesApiClient.kt` |
| `fetchSupervisorMapa` | `/api/mobile/supervisor/mapa?ymd=&modo=` | GET | `toSupervisorMapItems` | `RemoteSupervisorRepository.fetchMapa` | `network\dto\SupervisorDtos.kt` |
| `fetchSupervisorCuadrillasMapa` | `/api/mobile/supervisor/cuadrillas-mapa` | GET | `toCuadrillasMapa` | `RemoteSupervisorRepository.fetchCuadrillasMapa` | `network\dto\TecnicoDtos.kt` |
| `fetchSupervisorJornada` | `/api/mobile/supervisor/jornada?ymd=` | GET | `toJornadaData` | `RemoteSupervisorRepository.fetchJornada` | `network\dto\SupervisorDtos.kt` |
| `postSupervisorJornadaEvento` | `/api/mobile/supervisor/jornada` | POST | `toSupervisorJornada` | `RemoteSupervisorRepository.postJornadaEvento` | `network\dto\SupervisorDtos.kt` |

## Flujos Principales

### Bootstrap

1. `RemoteSessionRepository.fetchBootstrap` obtiene Firebase ID token via `TokenProvider`.
2. Llama `RedesApiClient.fetchBootstrap(idToken)`.
3. Backend `/api/mobile/bootstrap` valida token y arma sesion/comunicados.
4. Android parsea `MobileBootstrapDto`, guarda `bootstrap.session` en `SessionCacheDataSource` y deja `selectedRole` en cache separada.

### Tracking

1. UI por rol llama `TrackingManager.startIfNeeded`.
2. `LocationTrackingService` existe en `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\data\tracking\LocationTrackingService.kt`; la ruta `app\src\main\java\com\redes\app\service\LocationTrackingService.kt` no existe.
3. `LocationTrackingService` corre como foreground service y escucha ubicacion con Play Services Location.
4. Si el usuario se movio al menos 30 m, llama `TrackingRepository.postLocation`.
5. `RedesApiClient.postTracking` envia `lat`, `lng`, `accuracy`, `speed` a `/api/mobile/tracking`.
6. Backend decide si escribe en `supervisores/{uid}/tracking` o `cuadrillas/{id}/tracking` segun roles del token, no segun `X-Mobile-Role`.

### Evidencia De Auditoria

- Tecnico: `RemoteTecnicoRepository.sustainStockEquipment` lee bytes desde `Uri`, arma multipart y llama `/api/mobile/tecnico/stock`.
- Coordinador: `RemoteCoordinadorRepository.sustainEquipo` hace lo mismo contra `/api/mobile/coordinador/auditoria/sustentar`.
- Backend sube a Storage y actualiza `equipos/{SN}.auditoria`.

## Campos DTO Sensibles Al Contrato

- `MobileSessionDto` espera `uid`, `nombre`, `nombreCorto`, `email`, `roles`, `areas`, `permissions`, `estadoAcceso`, `isAdmin`.
- `MobileBootstrapDto` espera objeto `session`, array `comunicados`, flags `requiresComunicadosGate`, `roleSelectionRequired` y `defaultRole`.
- `TecnicoDtos` filtra items de mapa/cuadrillas si `lat` o `lng` son nulos.
- `SupervisorDtos.toSupervisorMapItems` tambien descarta items sin `lat` o `lng`.
- `CoordinadorDtos.toCoordinadorPredespacho` solo marca `tienePredespacho = true` si el backend envia `tienePredespacho` y hay `rows`.
- Los DTOs usan `opt*`; campos faltantes no suelen romper parseo, pero pueden degradar UI con valores vacios o cero.

## Inconsistencias Para Revisar

1. `SupervisorRepository.iniciarJornada` y `RemoteSupervisorRepository.iniciarJornada` existen, pero llaman `/api/mobile/inicio-jornada`, que backend valida como tecnico. No se encontro uso UI directo de supervisor para ese metodo.
2. `COORDINADOR_CUADRILLAS_MAPA` apunta a `/api/mobile/tecnico/cuadrillas-mapa`. Backend soporta coordinador ahi, pero el nombre de ruta es tecnico.
3. `fetchCurrentSession` existe para `/api/mobile/me`, pero el flujo real usa bootstrap. Puede ser legado o fallback no conectado.
4. `postTracking` envia siempre header `X-Mobile-Role: TECNICO`; backend no lo usa y decide por token. Si en el futuro backend usa ese header, supervisor tracking podria romperse.
5. Mensajes 404 del cliente siguen diciendo que endpoints no existen para endpoints que ya existen.
6. `local.properties` contiene configuracion local sensible o no versionada; no documentar valores.
