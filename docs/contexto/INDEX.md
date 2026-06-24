# Indice de Contexto - REDES-MOBILE

Actualizado: 2026-06-23.

Estado actual: revision incremental diaria 2026-06-23. REDES no muestra cambios pendientes en git. REDES-MOBILE muestra solo incremento de version en `app/build.gradle.kts` (`versionCode 11`, `versionName 1.0.10`) y un AAB generado en `app/release`. No se detectaron rutas, pantallas, servicios, repositorios, modelos ni funciones nuevas.

## Estado General

| Area | Estado | Fuente | Documento | Prioridad | Notas |
| --- | --- | --- | --- | --- | --- |
| Arquitectura inicial | Documentado | `C:\Proyectos\REDES-MOBILE` | `architecture/overview.md` | Alta | Fase 0 superficial |
| Diagramas iniciales | Documentado | `C:\Proyectos\REDES-MOBILE` | `architecture/diagrams.md` | Alta | Mermaid de alto nivel |
| API mobile REDES + Network/API REDES-MOBILE | Revisar | `C:\Proyectos\REDES\apps\web\src\app\api\mobile` + `app\src\main\java\com\redes\app\network` | `C:\Proyectos\REDES\docs\contexto\web\api-routes.md` + `android/network.md` | Alta | Deep dive documentado; requiere validar inconsistencias puntuales del contrato |
| Sesion/bootstrap/auth | Revisar | `data\session`, `data\auth`, `data\local\SessionCacheDataSource.kt`, `ui\auth`, `ui\home`, `ui\update` | `android/session-auth-bootstrap.md` | Alta | Deep dive completado; revisar roles web sin shell mobile, cache stale, `/api/mobile/me` legado y decisiones de force update |
| Force update | Revisar | `ui\update`, `ui\screens\ForceUpdateScreen.kt`, `MainActivity.kt`, cruce con bootstrap REDES | `android/force-update.md` | Alta | Deep dive completado; `app_config/android` ya tiene regla fuente en REDES, queda validar deploy, fail-open y refuerzo backend |
| Navegacion | Revisar | `ui\navigation`, `di\AppContainer.kt`, `AndroidManifest.xml` | `android/navigation.md` | Alta | Documentado despacho por rol, permisos y destinos; requiere validar roles web sin shell mobile |
| Pantallas | Parcial | `ui\screens` y paquetes por rol | `android/screens.md` | Media | Supervisor, coordinador, force update y tecnico alertas/cierre tienen deep dive por unidad; falta deep dive visual/funcional completo |
| ViewModels | Parcial | `ui\*\*ViewModel.kt` | `android/viewmodels.md` | Media | Supervisor, coordinador, force update y tecnico alertas/cierre documentados; falta resto de metodos no cubiertos por unidades |
| Tecnico alertas/notificaciones y cierre de ruta | Revisar | `ui\tecnico`, `ui\screens\TecnicoShellScreen.kt`, `ui\screens\NotificationsScreen.kt`, `data\tecnico`, `data\alertas`, `data\tracking`, `network` + REDES alertas/inicio/tracking | `android/tecnico-alertas-cierre-ruta.md` | Alta | Deep dive documentado; revisar cierre local antes de confirmacion remota, Firestore rules, rechazo/reinicio de tracking, inbox persistido y validacion en dispositivo |
| Supervisor shell/ViewModel y alertas | Revisar | `ui\screens\SupervisorShellScreen.kt`, `ui\supervisor`, `data\supervisor`, `data\alertas`, `network\dto\SupervisorDtos.kt` | `android/supervisor-shell-alertas.md` | Alta | Deep dive documentado; revisar alertas en memoria, permisos Android, cierre de tracking, endpoint tecnico heredado y rules Firestore |
| Coordinador shell/ViewModel y repositorio | Revisar | `ui\screens\CoordinadorShellScreen.kt`, `ui\coordinador`, `data\coordinador`, `network\dto\CoordinadorDtos.kt` | `android/coordinador-shell-repositorio.md` | Alta | Deep dive documentado + cambios 2026-06-18: predespacho (Firestore, precon) y cuadrillas/lista (chips MESH/FONO/BOX, tap a detalle, CoordinadorOrderDetailScreen, nuevo endpoint backend) |
| Almacen shell/ViewModel y repositorio | Revisar | `ui\screens\AlmacenShellScreen.kt`, `ui\almacen`, `data\almacen`, `network\dto\AlmacenDtos.kt`, REDES `api\mobile\almacen` | `android/almacen-shell-repositorio.md` | Alta | Revision incremental 2026-06-22: rol `ALMACEN`, tabs stock/liquidacion/instalaciones/mapa, repositorio remoto, DTOs y endpoints backend. Validar refresh, permisos mapa y contrato JSON. |
| Repositorios | Revisar | `data`, `di\AppContainer.kt` | `android/repositories.md` | Alta | Deep dive 2026-06-20: auth/session/presence/tracking/alertas/tecnico/supervisor/coordinador, patrones `Result`, validaciones locales y silencios de errores |
| Modelos/DTOs | Revisar | `network\dto`, `data\*\*Models.kt` + REDES `api\mobile` | `android/models.md` | Media | Deep dive 2026-06-19: parseadores JSON manuales, defaults `opt*`, contratos tecnico/supervisor/coordinador y riesgos de degradacion silenciosa |
| Tracking/presencia | Revisar | `data\tracking`, `data\presence`, `AndroidManifest.xml` + REDES `/api/mobile/tracking` y `/api/mobile/presencia` | `android/tracking.md` | Alta | Documentado ciclo foreground, presencia lifecycle, contratos backend y riesgos de permisos/header |
| Indice de fuente | Documentado | estructura superficial | `indexes/source-index.json` | Media | No es cobertura completa |

## Orden Propuesto De Documentacion

1. Pantallas por rol y UI comun/tema.
2. Validacion en dispositivo de permisos/tracking/notificaciones.
3. Contratos/pruebas de repositorios y DTOs.

## Revision Incremental 2026-06-23

- REDES: `git status` y `git diff --name-only` sin cambios pendientes.
- REDES-MOBILE: cambio acotado en `app/build.gradle.kts` para publicar `versionCode 11` / `versionName 1.0.10`; `app/release/app-release.aab` tratado como output binario generado y excluido de documentacion funcional.
- Actualizados `INDEX.md`, `PENDIENTES.md`, `CHANGELOG-CONTEXTO.md` e `indexes\source-index.json`.
- No se detectaron nuevas carpetas, rutas, pantallas, servicios, repositorios, modelos ni funciones con impacto de contexto.
- Siguiente unidad recomendada: `Pantallas por rol y UI comun/tema`.

## Revision Incremental 2026-06-22

- REDES: `git status` y `git diff --name-only` sin cambios pendientes de codigo fuente.
- REDES-MOBILE: cambios amplios en fuente; se priorizo la novedad funcional `ALMACEN`.
- Creado `android\almacen-shell-repositorio.md` con shell, tabs, ViewModel, repositorio, endpoints, DTOs, diagrama Mermaid y pendientes.
- Actualizados `README.md`, `INDEX.md`, `PENDIENTES.md`, `android\screens.md`, `android\repositories.md`, `android\models.md`, `CHANGELOG-CONTEXTO.md` e `indexes\source-index.json`.
- Siguiente unidad recomendada: `Pantallas por rol y UI comun/tema`.

## Deep Dive Repositorios 2026-06-20

- Actualizado `android\repositories.md` con `AppContainer`, `FirebaseAuthRepository`, `RemoteSessionRepository`, `BackendPresenceRepository`, `TrackingRepository`, `RemoteAlertaRepository`, `RemoteTecnicoRepository`, `RemoteSupervisorRepository` y `RemoteCoordinadorRepository`.
- Hallazgos: los repositorios de roles devuelven `Result<T>` con helper `call`, session valida `apiClient.isConfigured`, tracking/presencia operan fire-and-forget o sin `Result`, y listeners Firestore de alertas/notificaciones no exponen errores observables a UI.
- La unidad queda en `Revisar` por decisiones de contrato: validacion uniforme de `API_BASE_URL`, exposicion de errores de tracking/presencia/listeners, endpoint supervisor de inicio jornada, validacion local de multipart y modelo comun para `CuadrillaMapa`.
- Siguiente unidad recomendada: `Pantallas por rol y UI comun/tema`.

## Deep Dive Modelos/DTOs 2026-06-19

- Creado `android\models.md` con cruce entre `network\dto`, `data\tecnico`, `data\supervisor`, `data\coordinador` y endpoints REDES `api\mobile`.
- Hallazgos: los DTOs parsean JSON manualmente con `opt*`, por lo que cambios backend pueden degradar a valores vacios/cero sin error visible; los mapas descartan items sin coordenadas; varias ramas vacias de endpoints coordinador no devuelven toda la metadata esperada.
- La unidad queda en `Revisar` por decisiones de contrato: fixtures/pruebas JSON, falla fuerte versus degradacion, modelo comun para `CuadrillaMapa`, metadata en respuestas vacias y revision de mojibake.
- Siguiente unidad recomendada: `Repositorios por rol restantes y manejo de errores/resultados`.

## Deep Dive Tecnico Alertas/Notificaciones Y Cierre De Ruta 2026-06-18

- Creado `android\tecnico-alertas-cierre-ruta.md` con flujo de inicio jornada, solicitud de cierre, listener de alerta, inbox tecnico, endpoints backend y relacion con tracking.
- Hallazgos: el cierre tecnico detiene tracking local antes de confirmar la alerta remota; el cierre oficial `RUTA_CERRADA` se escribe solo cuando web acepta la alerta; `notificaciones_tecnico` se lee/escribe directo desde Android.
- La unidad queda en `Revisar` por decisiones humanas: reactivar tracking si rechazo, validar Firestore rules, definir notificaciones de rechazo, confirmar si UI debe refrescar estado oficial y probar permisos/foreground service.
- Siguiente unidad recomendada: `Modelos/DTOs tecnico-supervisor-coordinador contra backend REDES`.

## Deep Dive Force Update 2026-06-18

- Creado `android\force-update.md` con flujo desde `MainActivity` hasta `ForceUpdateScreen`, consulta Firestore `app_config/android`, estados UI, bloqueo, fail-open y cruce con sesion/bootstrap.
- REDES backend revisado para `/api/mobile/bootstrap` y `core\auth\mobileBootstrap.ts`; no se observo refuerzo de `versionMinima` ni campos de version en el bootstrap mobile.
- La unidad queda en `Revisar` por decisiones humanas: fail-open, refuerzo backend, validacion de deploy de rules, UX de reintento/Play Store y observabilidad.
- Siguiente unidad recomendada: `Tecnico alertas/notificaciones y cierre de ruta`.

## Revision Incremental 2026-06-17

- REDES: `git status` muestra solo cambios dentro de `docs/contexto`; no se detectaron cambios de codigo fuente para actualizar contexto funcional.
- REDES-MOBILE: persisten cambios de codigo y carpetas nuevas por rol; se priorizo la unidad ya recomendada `Coordinador shell/ViewModel y repositorio`.
- Se agrego `android\coordinador-shell-repositorio.md` con shell, tabs/subtabs, `CoordinadorViewModel`, `CoordinadorUiState`, repositorio, endpoints, DTOs, flujo Mermaid y pendientes.
- Se actualizaron `README.md`, `INDEX.md`, `PENDIENTES.md`, `android\screens.md`, `android\viewmodels.md`, `android\repositories.md`, `CHANGELOG-CONTEXTO.md` e `indexes\source-index.json`.
- Siguiente unidad recomendada: `Force update`.

## Revision Incremental 2026-06-16

- REDES: `git status` muestra solo cambios dentro de `docs/contexto`; no se detectaron cambios de codigo fuente para actualizar contexto funcional.
- REDES-MOBILE: persisten cambios de codigo y carpetas nuevas en `data\alertas`, `data\coordinador`, `data\supervisor`, `data\tracking`, `ui\coordinador`, `ui\supervisor` y `ui\update`.
- Se agregaron `android\screens.md` y `android\viewmodels.md` como mapas preliminares porque las rutas ya estaban planificadas pero no existian.
- La unidad `Supervisor shell/ViewModel y alertas` quedo documentada; la siguiente prioridad alta pasa a coordinador y force update.

## Evidencia De Manifests

- `settings.gradle.kts` declara proyecto `REDES` con modulo `:app`.
- `app/build.gradle.kts` declara namespace `com.redes.app`, applicationId `com.redesmyd.mobile`, minSdk 26, target/compile SDK 36, version `1.0.10` (`versionCode 11`).
- Dependencias: Compose, Navigation Compose, Lifecycle ViewModel, DataStore, OkHttp, Firebase Auth/Firestore/Storage, Play Services Location y Maps Compose.
- `AndroidManifest.xml` declara permisos de internet, ubicacion fina/gruesa, foreground service location y notificaciones.
- Manifest registra `REDESApplication`, `MainActivity`, `LocationTrackingService` y `FileProvider`.

## Cobertura

Cobertura actual: mapa inicial. No representa documentacion completa ni validacion funcional.
