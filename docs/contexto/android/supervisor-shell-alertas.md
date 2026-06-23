# Supervisor Shell, ViewModel y Alertas - REDES-MOBILE

Actualizado: 2026-06-16.

Estado: **Revisar**. Deep dive focalizado de la unidad recomendada: shell supervisor, detalle de orden, ViewModel, repositorio supervisor, DTOs, alertas locales y repositorio de alertas/notificaciones.

## Alcance

Fuentes leidas:

- `app/src/main/java/com/redes/app/ui/screens/SupervisorShellScreen.kt`
- `app/src/main/java/com/redes/app/ui/screens/SupervisorOrderDetailScreen.kt`
- `app/src/main/java/com/redes/app/ui/supervisor/SupervisorViewModel.kt`
- `app/src/main/java/com/redes/app/ui/supervisor/SupervisorUiState.kt`
- `app/src/main/java/com/redes/app/ui/supervisor/SupervisorNotificationHelper.kt`
- `app/src/main/java/com/redes/app/data/supervisor/SupervisorRepository.kt`
- `app/src/main/java/com/redes/app/data/supervisor/RemoteSupervisorRepository.kt`
- `app/src/main/java/com/redes/app/data/supervisor/SupervisorModels.kt`
- `app/src/main/java/com/redes/app/data/alertas/*`
- `app/src/main/java/com/redes/app/network/dto/SupervisorDtos.kt`
- `app/src/main/java/com/redes/app/network/RedesApiClient.kt`
- `app/src/main/java/com/redes/app/network/MobileEndpoints.kt`
- `app/src/main/java/com/redes/app/ui/navigation/AppNavHost.kt`
- `app/src/main/java/com/redes/app/ui/navigation/AppDestination.kt`

No se ejecuto build, app, emulador, pruebas en dispositivo, llamadas reales al backend, listeners Firestore reales ni cambios de codigo fuente.

## Resumen

El flujo supervisor se activa cuando `homeUiState.selectedRole == "SUPERVISOR"`. `AppNavHost` solicita permisos de ubicacion fina y, en Android 13+, notificaciones; luego renderiza `SupervisorShellScreen`.

La pantalla opera cuatro tabs:

- `INICIO`: perfil, jornada, KPIs, regiones y cuadrillas del dia.
- `ORDENES`: ordenes por fecha y filtro de garantias.
- `STOCK`: placeholder de pantalla, sin flujo de repositorio observado en esta unidad.
- `MAPA`: ordenes/garantias/cuadrillas en Google Maps.

El detalle vive en `SupervisorOrderDetailScreen` y permite:

- ver datos de cliente, orden, direccion y mapa externo.
- guardar supervision (`notas`, `observaciones`).
- editar campos de garantia si `detail.isGarantia`.

## Navegacion

`AppDestination` define:

- `home`
- `supervisor_order_detail/{orderId}`

En `AppNavHost`, al tocar una orden:

1. llama `onSupervisorOrderClick(orderId)`.
2. navega a `SupervisorOrderDetail.createRoute(orderId)`.
3. el detalle usa el mismo `SupervisorUiState.selectedOrderDetail`.

Riesgo: si la carga de detalle falla o queda stale, la pantalla de detalle depende del estado compartido del ViewModel y muestra `Orden no disponible`.

## ViewModel

`SupervisorViewModel` depende de:

- `AuthRepository`
- `SessionRepository`
- `SupervisorRepository`
- `TrackingManager`
- `SharedPreferences`
- `Context`

En `init`:

- crea canal local de notificaciones.
- combina usuario Firebase y rol seleccionado.
- habilita el estado solo si hay usuario y rol `SUPERVISOR`.
- cuando se habilita, ejecuta `refreshAll()` y `loadJornada()`.
- inicia loop cada 60 segundos para `checkTramoAlertas()`.

### Carga Inicial

`refreshAll()` lanza en paralelo:

- `fetchHome()`
- `fetchOrders(ymd, soloGarantias)`
- `fetchMapa(ymd, mapMode)`

Luego actualiza home, orders, mapa y errores. Si el modo es `CUADRILLAS`, tambien llama:

- `refreshAllMapItems()`
- `refreshCuadrillasMapa()`

Riesgo: `refreshAll()` no llama `checkGarantiasAlertas()` despues de cargar ordenes; las alertas de nuevas garantias se disparan en `refreshOrders()`, no necesariamente en la primera carga paralela.

### Ordenes Y Garantias

`refreshOrders()`:

- llama `fetchOrders(selectedYmd, showGarantias)`.
- actualiza `ordersData` y `orders`.
- ejecuta `checkGarantiasAlertas(items)`.
- ejecuta `checkTramoAlertas()`.

`checkGarantiasAlertas()`:

- detecta garantias nuevas por `id` no visto en `prevGarantias`.
- detecta cambios de estado por comparacion `id -> estado`.
- guarda `prevGarantias = garantias.associate { it.id to it.estado }`.

Riesgo: `prevGarantias` es memoria de proceso. Al recrearse el ViewModel, puede volver a notificar garantias ya vistas.

### Alertas De Tramo

`checkTramoAlertas()`:

- usa ordenes actuales del estado.
- filtra garantias.
- dispara alertas a las `07:45`, `11:45`, `15:45` para tramos `08:00`, `12:00`, `16:00`.
- evita duplicar por `lastTramoAlertKey = ymd_tramo`.

Riesgos:

- Si `orders` no esta fresco o la app estuvo dormida justo en el minuto exacto, puede no alertar.
- Solo cubre tres tramos fijos.
- La deduplicacion tambien vive solo en memoria.

## Jornada Y Tracking

`loadJornada()`:

- llama `fetchJornada(todayLimaYmd())`.
- si estado es `EN_RUTA` o `EN_REFRIGERIO`, llama `trackingManager.startIfNeeded()`.
- actualiza `estadoRuta` a `EN_CAMPO` o `RUTA_CERRADA`.

`iniciarRuta()`:

- obtiene ultima ubicacion con Fused Location.
- llama `postJornadaEvento("INICIO_RUTA", lat, lng)`.
- guarda `sup_inicio_ymd` en SharedPreferences.
- llama `trackingManager.startIfNeeded()`.

`confirmarCerrarRuta()`:

- llama `trackingManager.stop()` antes de pedir ubicacion y postear `FIN_RUTA`.
- postea `postJornadaEvento("FIN_RUTA", lat?, lng?)`.
- marca `estadoRuta = "RUTA_CERRADA"` si el backend responde OK.

Riesgo: si `FIN_RUTA` falla, el tracking ya fue detenido localmente. La unidad previa de tracking ya marcaba diferencia entre cierre tecnico y supervisor.

El repositorio tambien conserva `iniciarJornada()`, que llama `apiClient.postInicioJornada()`. Ese endpoint usa `X-Mobile-Role: TECNICO` y `MobileEndpoints.INICIO_JORNADA`; en este flujo supervisor no se observa uso desde el ViewModel actual, pero queda como deuda de contrato.

## Repositorio Y Endpoints

`RemoteSupervisorRepository` delega en `RedesApiClient`.

| Metodo | Endpoint | Header |
| --- | --- | --- |
| `fetchHome` | `GET /api/mobile/supervisor/home` | `X-Mobile-Role: SUPERVISOR` |
| `fetchOrders` | `GET /api/mobile/supervisor/ordenes?ymd=...&garantias=true` | `SUPERVISOR` |
| `fetchOrderDetail` | `GET /api/mobile/supervisor/ordenes/{id}` | `SUPERVISOR` |
| `saveSupervision` | `POST /api/mobile/supervisor/supervision` | `SUPERVISOR` |
| `updateGarantia` | `POST /api/mobile/supervisor/garantias/update` | `SUPERVISOR` |
| `fetchMapa` | `GET /api/mobile/supervisor/mapa?ymd=...&modo=...` | `SUPERVISOR` |
| `fetchCuadrillasMapa` | `GET /api/mobile/supervisor/cuadrillas-mapa` | `SUPERVISOR` |
| `fetchJornada` | `GET /api/mobile/supervisor/jornada?ymd=...` | `SUPERVISOR` |
| `postJornadaEvento` | `POST /api/mobile/supervisor/jornada` | `SUPERVISOR` |

DTOs:

- `SupervisorHomeData`: supervisor, tracking habilitado, regiones, cuadrillas, resumen por region y totales.
- `SupervisorOrdersData`: fecha, updateInfo e items.
- `SupervisorOrderDetail`: datos de orden, cliente, garantia y supervision.
- `SupervisorMapItem`: coordenadas de ordenes en mapa.
- `JornadaData`: jornada y configuracion de oficina.

## Alertas Locales

`SupervisorNotificationHelper`:

- canal `redes_supervisor_alertas`.
- importancia alta.
- usa `NotificationCompat`.
- ignora si notificaciones estan deshabilitadas.
- captura `SecurityException` para Android 13+ sin permiso.

`SupervisorUiState` mantiene:

- `alertas: List<SupervisorAlertItem>`
- `alertCount`
- `showAlertas`

`showAlertas()` marca todas como leidas solo en memoria local.

Riesgo: no hay persistencia ni sincronizacion con backend para estas alertas de supervisor. Al reiniciar proceso se pierden alertas, y al recrear ViewModel pueden reaparecer.

## Data Alertas

`RemoteAlertaRepository` no es especifico de supervisor, pero entra en esta unidad porque maneja alertas/notificaciones mobile:

- `postAlertaCerrarRuta()` -> `apiClient.postAlertaApp("CERRAR_RUTA")`.
- `postRequiereAtencion()` -> `apiClient.postAlertaApp("REQUIERE_ATENCION")`.
- `listenAlertaEstado(alertaId)` escucha `alertas_app/{alertaId}` directo desde Firestore.
- `listenNotificaciones(cuadrillaId)` escucha `notificaciones_tecnico/{cuadrillaId}/items`.
- `markNotificacionesLeidas` actualiza `leido` directo en Firestore.

Riesgos cruzados con REDES:

- `postAlertaApp` usa `X-Mobile-Role: TECNICO`; si se reutiliza desde supervisor/coordinador, el contrato seria incorrecto.
- La unidad Firebase rules de REDES marco que `alertas_app` no tenia regla explicita para cliente directo.
- `notificaciones_tecnico` queda acoplado a `cuadrillaId`, mas natural para tecnico que para supervisor.

## UI Observada

`SupervisorShellScreen`:

- `AppTopBar` muestra `notifCount = uiState.alertCount`.
- bottom nav con Inicio, Ordenes, Stock y Mapa.
- dialogo de cierre de ruta.
- modal de cuadrillas del dia.
- dialogo de alertas de garantias.

`SupervisorOrderDetailScreen`:

- detalle cliente/orden/direccion.
- acciones de telefono, WhatsApp y Google Maps.
- edicion de garantia con opciones fijas:
  - Responsable: `Cuadrilla`, `Cliente`, `Externo`.
  - Caso: cambio de ONT/MESH/FONO/BOX/conector/roseta, recableado, reubicacion.
  - Imputado: `REDES M&D`, `WIN`.
- supervision con notas y observaciones.

Riesgo: las opciones de garantia estan hardcodeadas en Android; si backend/web usa catalogos o valores distintos, puede haber divergencia.

## Pendientes

- Validar en dispositivo Android 13/14 permisos de ubicacion y notificaciones para supervisor.
- Decidir si alertas de garantia/tramo deben persistirse o sincronizarse, no solo vivir en memoria del ViewModel.
- Revisar si `refreshAll()` debe ejecutar `checkGarantiasAlertas()` en la primera carga.
- Revisar alertas de tramo: minuto exacto, tramos fijos y comportamiento cuando app esta en background/doze.
- Resolver o eliminar `SupervisorRepository.iniciarJornada()` porque apunta al endpoint tecnico con header `TECNICO`.
- Confirmar que cerrar ruta supervisor no debe detener tracking antes de que backend acepte `FIN_RUTA`.
- Alinear opciones hardcodeadas de garantia con backend/web.
- Revisar `RemoteAlertaRepository` frente a Firestore rules: `alertas_app` y `notificaciones_tecnico`.
- Definir si `NotificationsScreen` debe mostrar alertas supervisor o solo notificaciones tecnico/home; el shell supervisor usa dialogo local propio.
- Revisar mojibake en textos de UI antes de entrega visual final.
