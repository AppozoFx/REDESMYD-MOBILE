# ViewModels Android

Actualizado: 2026-06-18.

Estado: **Actualizar / mapa incremental**. Este documento registra ViewModels detectados por la revision incremental; falta deep dive metodo por metodo.

## Alcance Leido

Lectura focalizada por simbolos:

- `app\src\main\java\com\redes\app\ui\tecnico\TecnicoViewModel.kt`
- `app\src\main\java\com\redes\app\ui\supervisor\SupervisorViewModel.kt`
- `app\src\main\java\com\redes\app\ui\coordinador\CoordinadorViewModel.kt`
- `app\src\main\java\com\redes\app\ui\update\ForceUpdateViewModel.kt`
- `app\src\main\java\com\redes\app\ui\*\*UiState.kt`

## ViewModels Detectados

| ViewModel | Fuente | Estado documental | Responsabilidad visible |
| --- | --- | --- | --- |
| `AuthViewModel` | `ui\auth\AuthViewModel.kt` | Documentado indirecto | Login/logout Firebase; cubierto en `session-auth-bootstrap.md`. |
| `HomeViewModel` | `ui\home\HomeViewModel.kt` | Documentado indirecto | Bootstrap, cache, roles, comunicados y seleccion de rol. |
| `TecnicoViewModel` | `ui\tecnico\TecnicoViewModel.kt` | Revisar | Deep dive en `android/tecnico-alertas-cierre-ruta.md`; jornada tecnico, ordenes, mapa, stock, sustento de equipos, alertas, notificaciones y cierre de ruta. |
| `SupervisorViewModel` | `ui\supervisor\SupervisorViewModel.kt` | Revisar | Deep dive en `android/supervisor-shell-alertas.md`; home supervisor, jornada, cierre de ruta, refrigerio, mapa, cuadrillas, ordenes, supervision, garantias y alertas. |
| `CoordinadorViewModel` | `ui\coordinador\CoordinadorViewModel.kt` | Revisar | Deep dive en `android/coordinador-shell-repositorio.md`; resumen mensual, cuadrillas, mapas, stock, auditoria, predespacho, ventas, plantillas y sustento de auditoria. |
| `ForceUpdateViewModel` | `ui\update\ForceUpdateViewModel.kt` | Revisar | Deep dive en `android/force-update.md`; lee Firestore `app_config/android`, compara `BuildConfig.VERSION_CODE` con `versionMinima` y falla abierto ante excepcion. |

## Estados UI Detectados

| Estado | Fuente | Notas |
| --- | --- | --- |
| `TecnicoUiState` | `ui\tecnico\TecnicoUiState.kt` | Tabs tecnico, fecha Lima, mapa/ordenes/stock y estados de alerta. |
| `SupervisorUiState` | `ui\supervisor\SupervisorUiState.kt` | Tabs supervisor, jornada, mapa, cuadrillas, alertas y garantia guardada. |
| `CoordinadorUiState` | `ui\coordinador\CoordinadorUiState.kt` | Tabs/subtabs coordinador, fecha mensual/diaria, listas expandidas y seleccion de cuadrilla. |
| `ForceUpdateState` | `ui\update\ForceUpdateState.kt` | `Checking`, `UpToDate`, `UpdateRequired`. |

## Pendientes De Analisis

- `TecnicoViewModel`: quedan decisiones de contrato sobre cierre local antes de confirmacion remota, rechazo/reinicio de tracking y Firestore rules.
- `ForceUpdateViewModel`: decidir si fail-open debe mantenerse, si backend debe reforzar version minima y si se agregan retry/observabilidad.
- Revisar errores silenciosos de `CoordinadorViewModel` en mapa, auditoria, predespacho y mapa de cuadrillas.

## Siguiente Unidad Recomendada

`Modelos/DTOs tecnico-supervisor-coordinador contra backend REDES`.
