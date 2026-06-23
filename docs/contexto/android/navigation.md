# Navegacion Y Destinos Por Rol

Actualizado: 2026-06-14.

Estado: **Revisar**. Esta unidad fue priorizada por la revision incremental diaria y documentada con lectura focalizada de navegacion, DI y rutas nuevas por rol.

## Alcance

Fuentes leidas:

- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\ui\navigation\AppDestination.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\ui\navigation\AppNavHost.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\di\AppContainer.kt`
- `C:\Proyectos\REDES-MOBILE\app\src\main\AndroidManifest.xml`

Fuentes detectadas como dependencias directas, no documentadas archivo por archivo:

- `app\src\main\java\com\redes\app\ui\screens\CoordinadorShellScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\SupervisorShellScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\SupervisorOrderDetailScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\ForceUpdateScreen.kt`
- `app\src\main\java\com\redes\app\ui\coordinador`
- `app\src\main\java\com\redes\app\ui\supervisor`
- `app\src\main\java\com\redes\app\ui\update`

## Destinos

`AppDestination.kt` define destinos internos:

| Destino | Route | Uso |
| --- | --- | --- |
| `Login` | `login` | Entrada cuando no hay usuario autenticado. |
| `Comunicados` | `comunicados` | Gate previo al home si hay comunicados obligatorios pendientes. |
| `RoleSelection` | `role_selection` | Seleccion de rol cuando corresponde. |
| `Home` | `home` | Shell principal; despacha por `homeUiState.selectedRole`. |
| `Profile` | `profile` | Perfil del usuario. |
| `Settings` | `settings` | Ajustes y cambio de rol. |
| `Notifications` | `notifications` | Notificaciones/alertas. |
| `TecnicoOrderDetail` | `tecnico_order_detail/{orderId}` | Detalle de orden para tecnico. |
| `SupervisorOrderDetail` | `supervisor_order_detail/{orderId}` | Detalle de orden para supervisor. |

## Resolucion De Entrada

`AppNavHost` no muestra el grafo mientras `uiState.isAuthResolved` o `homeUiState.isStartupReady` no esten listos; devuelve `SplashScreen`.

Despues calcula `targetRoute`:

1. Sin `currentUser`: `Login`.
2. Con comunicados obligatorios: `Comunicados`.
3. Si requiere seleccion de rol: `RoleSelection`.
4. En otro caso: `Home`.

El cambio de `targetRoute` navega limpiando el stack hasta el inicio del grafo.

## Shells Por Rol

Dentro de `Home`, el rol seleccionado decide la experiencia:

| Rol | Shell | Efectos |
| --- | --- | --- |
| `TECNICO` | `TecnicoShellScreen` | Solicita ubicacion fina; en Android 13+ tambien notificaciones; inicia tracking tecnico si hay permiso. |
| `SUPERVISOR` | `SupervisorShellScreen` | Solicita ubicacion fina y notificaciones; inicia tracking supervisor; expone jornada, mapa, garantias y alertas. |
| `COORDINADOR` | `CoordinadorShellScreen` | Expone inicio, cuadrillas, almacen y gestion; no inicia tracking desde esta lectura. |
| Otro rol | `HomeScreen` | Pantalla generica con perfil, alertas, ajustes, refresh y cambio de rol. |

Riesgo ya detectado en la unidad de sesion/bootstrap: el backend puede devolver roles web sin shell especializado. Esos roles entran a `HomeScreen`, no a un flujo operativo especifico.

## Navegacion De Detalle

- Tecnico: `onOrderClick` carga detalle en ViewModel y navega a `tecnico_order_detail/{orderId}`.
- Supervisor: `onOrderClick` carga detalle en ViewModel y navega a `supervisor_order_detail/{orderId}`.
- Al volver, se invocan callbacks de limpieza (`onTecnicoDetailBack`, `onSupervisorDetailBack`) antes de `popBackStack`.

## Permisos Y Servicios

`AndroidManifest.xml` declara:

- `INTERNET`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_LOCATION`
- `POST_NOTIFICATIONS`

Tambien registra:

- `MainActivity`
- `LocationTrackingService` en `.data.tracking.LocationTrackingService`
- `FileProvider` con `@xml/file_paths`
- metadata `com.google.android.geo.API_KEY` desde `${MAPS_API_KEY}`

## Dependencias Ensambladas

`AppContainer.kt` registra repositorios y managers usados por los shells:

- Auth/sesion: `FirebaseAuthRepository`, `RemoteSessionRepository`, `SessionCacheDataSource`.
- Network: `RedesApiClient` con `BuildConfig.API_BASE_URL`, `AuthTokenInterceptor` y `FirebaseIdTokenProvider`.
- Presencia/tracking: `MobilePresenceManager`, `BackendPresenceRepository`, `TrackingRepository`, `TrackingManager`.
- Roles: `RemoteTecnicoRepository`, `RemoteSupervisorRepository`, `RemoteCoordinadorRepository`.
- Alertas: `RemoteAlertaRepository`.

## Flujo

```mermaid
flowchart TD
  Splash[SplashScreen] --> AuthResolved{Auth y startup listos}
  AuthResolved -->|No| Splash
  AuthResolved -->|Si| User{Usuario Firebase}
  User -->|No| Login
  User -->|Si| Comunicados{Comunicados obligatorios}
  Comunicados -->|Si| ComunicadosScreen
  Comunicados -->|No| RoleSelect{Requiere rol}
  RoleSelect -->|Si| RoleSelectionScreen
  RoleSelect -->|No| HomeRoute[Home route]
  HomeRoute -->|TECNICO| TecnicoShell
  HomeRoute -->|SUPERVISOR| SupervisorShell
  HomeRoute -->|COORDINADOR| CoordinadorShell
  HomeRoute -->|Otro| HomeScreen
  TecnicoShell --> TecnicoDetail[tecnico_order_detail/{orderId}]
  SupervisorShell --> SupervisorDetail[supervisor_order_detail/{orderId}]
```

## Nuevos Pendientes Detectados

- Documentar en profundidad `SupervisorShellScreen` y `SupervisorViewModel`, porque agregan jornada, mapa, alertas y edicion de garantias.
- Documentar en profundidad `CoordinadorShellScreen` y `CoordinadorViewModel`, porque agregan almacen, auditoria, predespacho, ventas y plantillas.
- Documentar `ForceUpdateScreen` y `ForceUpdateViewModel` junto con la decision pendiente de force update fail-open.
- Revisar si roles distintos de `TECNICO`, `SUPERVISOR` y `COORDINADOR` deben llegar a `HomeScreen` o bloquearse/explicarse.
- Documentar `LocationTrackingService` como siguiente unidad sensible por permisos.

## Siguiente Unidad Recomendada

`Tracking/presencia REDES-MOBILE`, porque la navegacion inicia tracking para tecnico y supervisor, y el manifest declara foreground service de ubicacion.
