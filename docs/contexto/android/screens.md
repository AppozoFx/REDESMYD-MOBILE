# Pantallas Android

Actualizado: 2026-06-22.

Estado: **Pendiente / mapa incremental**. Este documento registra las pantallas detectadas en la revision incremental diaria; no representa deep dive visual ni validacion funcional en dispositivo.

## Alcance Leido

Lectura focalizada por simbolos y archivos modificados:

- `app\src\main\java\com\redes\app\ui\screens\SupervisorShellScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\SupervisorOrderDetailScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\CoordinadorShellScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\ForceUpdateScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\TecnicoShellScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\NotificationsScreen.kt`
- `app\src\main\java\com\redes\app\ui\screens\AlmacenShellScreen.kt`
- `app\src\main\java\com\redes\app\ui\navigation\AppDestination.kt`

## Pantallas Detectadas Por Navegacion

| Pantalla | Ruta/entrada | Estado documental | Notas |
| --- | --- | --- | --- |
| `LoginScreen` | `login` | Revisado | Login Firebase. Paleta navy `#0B142D/#1A2C58/#2F2850`, orbes animados en Canvas, borde con 4 estados (idle/input/loading/connected/error), logo pulse. Ver `CHANGELOG-CONTEXTO.md 2026-06-22`. |
| `AlmacenShellScreen` | `home` con rol `ALMACEN` | Revisado | Shell ALMACEN. 4 tabs: STOCK (KPIs + cards expandibles por cuadrilla), LIQUIDACIÓN (órdenes finalizadas por mes), INSTALACIONES (materiales por mes con acta/SNs), MAPA (cuadrillas, idéntico al del Coordinador). Ver `CHANGELOG-CONTEXTO.md 2026-06-22`. |
| `ComunicadosScreen` | `comunicados` | Pendiente | Gate de comunicados obligatorios. |
| `RoleSelectionScreen` | `role_selection` | Pendiente | Seleccion de rol despues de bootstrap. |
| `HomeScreen` | `home` generico | Pendiente | Fallback para roles sin shell mobile especializado. |
| `TecnicoShellScreen` | `home` con rol `TECNICO` | Revisar | Deep dive en `android/tecnico-alertas-cierre-ruta.md`; cubre inicio jornada, alertas, cierre de ruta, notificaciones, tracking y mapa de cuadrillas. |
| `TecnicoOrderDetailScreen` | `tecnico_order_detail/{orderId}` | Pendiente | Detalle de orden tecnico. |
| `SupervisorShellScreen` | `home` con rol `SUPERVISOR` | Revisar | Deep dive en `android/supervisor-shell-alertas.md`; detecta jornada, home, ordenes, mapa, garantias, alertas y permisos de ubicacion/notificaciones. |
| `SupervisorOrderDetailScreen` | `supervisor_order_detail/{orderId}` | Revisar | Deep dive en `android/supervisor-shell-alertas.md`; detalle supervisor con supervision y edicion de garantia. |
| `CoordinadorShellScreen` | `home` con rol `COORDINADOR` | Revisar | Deep dive en `android/coordinador-shell-repositorio.md`; inicio, cuadrillas lista/mapa, almacen, auditoria con foto, predespacho, ventas y plantillas. |
| `ProfileScreen` | `profile` | Pendiente | Perfil. |
| `SettingsScreen` | `settings` | Pendiente | Ajustes/cambio de rol. |
| `NotificationsScreen` | `notifications` | Revisar | Deep dive en `android/tecnico-alertas-cierre-ruta.md`; muestra alertas persistidas de gestion tecnico y comunicados del home. |
| `ForceUpdateScreen` | gate previo por estado de version | Revisar | Deep dive en `android/force-update.md`; bloqueo completo, BackHandler activo, Play Store/web fallback y mensaje desde Firestore. |
| `SplashScreen` | estado inicial/loading | Pendiente | Mientras auth/startup/update estan resolviendo. |

## Hallazgos Incrementales

- `SupervisorShellScreen` contiene componentes internos para KPIs, jornada, ordenes, mapa, popups, filtros, cuadrillas y estado de garantias.
- `SupervisorOrderDetailScreen` expone flujo de supervision y actualizacion de garantia.
- `CoordinadorShellScreen` concentra varias subpantallas en un solo shell: inicio, cuadrillas, mapa, stock, auditoria, predespacho, ventas y plantillas.
- Deep dive 2026-06-17 confirma que `CoordinadorShellScreen` alterna mapa de ordenes y mapa de cuadrillas, y que auditoria dispara sustento fotografico mediante el ViewModel.
- Deep dive 2026-06-18 confirma que `ForceUpdateScreen` bloquea la salida por back, abre Play Store con fallback web y solo aparece cuando `ForceUpdateState.UpdateRequired` corta el arbol normal en `MainActivity`.
- Deep dive 2026-06-18 confirma que `TecnicoShellScreen` muestra estado de ruta, boton de cierre aprobable, badge de notificaciones y accion de requiere atencion; `NotificationsScreen` separa alertas de gestion y comunicados.

## Insets / Edge-to-Edge (Android 15)

Aplicado 2026-06-21 por recomendaciones de Google Play Console.

`MainActivity` ya llama `enableEdgeToEdge()`. El tema base en `values/themes.xml` ahora declara `windowTranslucentStatus=false`, `windowTranslucentNavigation=false` y `fitsSystemWindows=false` para complementar ese llamado.

**Pantallas con Scaffold** (insets gestionados automáticamente por Material3):

- `TecnicoShellScreen`, `SupervisorShellScreen`, `CoordinadorShellScreen`
- `HomeScreen`, `ComunicadosScreen`
- `TecnicoOrderDetailScreen`, `SupervisorOrderDetailScreen`, `CoordinadorOrderDetailScreen`
- `NotificationsScreen`, `ProfileScreen`, `SettingsScreen`

**Pantallas sin Scaffold** (modifiers manuales en Column principal):

| Pantalla | Cambio |
|---|---|
| `LoginScreen` | `.statusBarsPadding().navigationBarsPadding()` añadido al Column interior |
| `RoleSelectionScreen` | `.statusBarsPadding().navigationBarsPadding()` reemplaza `padding(top=56.dp, bottom=36.dp)` hardcodeado |
| `ForceUpdateScreen` | `.statusBarsPadding().navigationBarsPadding()` añadido al Column interior |

## Pendiente De Deep Dive

- Leer composables principales y callbacks por pantalla para documentar entradas/salidas, side effects y dependencias de ViewModel.
- Validar en dispositivo permisos Android 13/14 para notificaciones, ubicacion y foreground service.
- Cruzar cada pantalla operativa con su ViewModel y repositorio correspondiente.
- Para force update: validar UX real de Play Store/web fallback y definir si necesita reintento, soporte o logout.

## Siguiente Unidad Recomendada

`Pantallas por rol y UI comun/tema`, incluyendo el shell `ALMACEN`.
