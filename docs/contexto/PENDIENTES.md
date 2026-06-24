# Pendientes de Contexto - REDES-MOBILE

Actualizado: 2026-06-23.

Siguiente unidad recomendada: **Pantallas por rol y UI comun/tema**.

## Backlog Inicial

| Prioridad | Estado | Tipo | Fuente | Motivo | Accion |
| --- | --- | --- | --- | --- | --- |
| Alta | Revisar | API mobile + Network/API Android | `C:\Proyectos\REDES\apps\web\src\app\api\mobile` + `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app\network` | Define contrato entre backend REDES y Android, incluyendo tokens, endpoints, DTOs y errores | Validar inconsistencias detectadas y mantener docs creados |
| Alta | Revisar | Sesion/bootstrap | `data\session`, `data\auth`, `data\local\SessionCacheDataSource.kt`, `ui\auth`, `ui\home`, `ui\update` | Determina login, bootstrap mobile, roles y cruce con force update | Validar decisiones sobre roles web sin shell mobile, cache stale, mensajes de acceso y contrato de update |
| Alta | Revisar | Force update | `ui\update`, `ui\screens\ForceUpdateScreen.kt`, `MainActivity.kt`, REDES `/api/mobile/bootstrap` | Gate de version minima antes de login y bootstrap | Resolver decisiones de fail-open, refuerzo backend, UX de reintento/Play Store y observabilidad; la lectura de `app_config` ya tiene regla fuente en REDES |
| Alta | Revisar | Navegacion | `ui\navigation`, `di\AppContainer.kt`, `AndroidManifest.xml` | Define destinos y pantallas disponibles por rol | Validar roles web sin shell mobile y profundizar shells por rol |
| Alta | Revisar | Tracking | `data\tracking`, `data\presence`, `LocationTrackingService` | Usa permisos sensibles de ubicacion y foreground service | Validar riesgos documentados en `android\tracking.md` |
| Alta | Revisar | Repositorios por rol y transversales | `data`, `di\AppContainer.kt` | Consumen endpoints mobile, Firebase Auth, Firestore listeners, presencia y tracking | Deep dive creado en `android\repositories.md`; revisar validacion uniforme, errores observables, endpoint supervisor de inicio jornada, multipart y modelo comun |
| Alta | Revisar | Supervisor shell/ViewModel y alertas | `ui\screens\SupervisorShellScreen.kt`, `ui\supervisor`, `data\supervisor`, `data\alertas`, `network\dto\SupervisorDtos.kt` | Shell operativo supervisor: jornada, tracking, ordenes, mapa, garantias y alertas | Validar permisos Android, alertas en memoria, cierre de tracking, endpoint tecnico heredado y Firestore rules |
| Alta | Revisar | Coordinador shell/ViewModel y repositorio | `ui\screens\CoordinadorShellScreen.kt`, `ui\coordinador`, `data\coordinador`, `network\dto\CoordinadorDtos.kt` | Shell operativo coordinador: inicio, cuadrillas, mapa, almacen, auditoria, predespacho, ventas y plantillas | Validar endpoint mapa compartido, backend coordinador, sustento con foto y errores silenciosos |
| Alta | Revisar | Almacen shell/ViewModel y repositorio | `ui\screens\AlmacenShellScreen.kt`, `ui\almacen`, `data\almacen`, `network\dto\AlmacenDtos.kt`, REDES `api\mobile\almacen` | Nuevo shell operativo para rol `ALMACEN`: stock, liquidacion, instalaciones y mapa | Validar permisos de mapa, sincronizacion de refresh, fixtures JSON y si el mapa debe tener endpoint propio |
| Alta | Revisar | Tecnico alertas/notificaciones y cierre de ruta | `ui\tecnico`, `TecnicoShellScreen.kt`, `NotificationsScreen.kt`, `data\tecnico`, `data\alertas`, `data\tracking`, `network`, REDES alertas/inicio/tracking | Flujo tecnico sensible: inicio jornada, cierre aprobable, tracking, Firestore listeners y notificaciones | Validar cierre local antes de confirmacion, rechazo/reinicio tracking, rules Firestore, notificaciones de rechazo y prueba real en dispositivo |
| Media | Pendiente | Pantallas por rol | `ui\screens`, `ui\tecnico`, `ui\supervisor`, `ui\coordinador` | UI principal de campo/supervision/coordinacion | Siguiente unidad recomendada despues de repositorios |
| Media | Revisar | Modelos/DTOs | `network\dto`, `data\*\*Models.kt` + REDES `api\mobile` | Estructuras compartidas con backend | Deep dive creado en `android\models.md`; revisar defaults silenciosos, metadata en respuestas vacias, fixtures de contrato y modelo comun para `CuadrillaMapa` |
| Media | Pendiente | DI/AppContainer | `di\AppContainer.kt`, `REDESApplication.kt`, `MainActivity.kt` | Ensamblaje de dependencias y ciclo inicial | Documentar con sesion/bootstrap |
| Baja | Pendiente | UI comun y tema | `ui\components`, `ui\common`, `ui\theme` | Componentes transversales | Documentar despues de flujos funcionales |

## Nuevos Pendientes Detectados En Fase 0

- Revisar `local.properties` solo como existencia sensible, sin abrir valores.
- Confirmar como se usa `API_BASE_URL` por variantes debug/staging/release.
- Validar en dispositivo real permisos de ubicacion/notificaciones con flujos de tracking/presencia.
- Cruzar pantallas `Tecnico`, `Supervisor` y `Coordinador` con endpoints de REDES.

## Pendientes Detectados En Revision Incremental 2026-06-19

- Revisar si los DTOs deben seguir degradando silenciosamente con `opt*` o fallar fuerte en campos obligatorios de endpoints criticos.
- Crear fixtures/pruebas de contrato JSON para `/api/mobile/bootstrap`, tecnico, supervisor y coordinador antes de cambios grandes de backend.
- Alinear respuestas vacias de endpoints coordinador para devolver metadata esperada (`ymd`, `ym`, `updateInfo`) cuando corresponda.
- Decidir si `CuadrillaMapa` debe moverse desde `data\tecnico` a un modelo comun usado por tecnico/supervisor/coordinador.
- Mantener como siguiente unidad `Repositorios por rol restantes y manejo de errores/resultados`.

## Pendientes Detectados En Revision Incremental 2026-06-20

- Decidir si todos los repositorios remotos deben validar `apiClient.isConfigured` o dejar el fallo centralizado en `RedesApiClient`.
- Definir si errores de tracking, presencia y listeners Firestore deben subir a UI, logs persistentes o telemetria.
- Revisar `RemoteSupervisorRepository.iniciarJornada()` porque sigue delegando en el endpoint tecnico `/api/mobile/inicio-jornada`.
- Alinear validacion local de multipart: tecnico valida SN/cuadrilla antes de leer/subir; coordinador normaliza pero no bloquea blancos explicitamente.
- Evaluar mover `CuadrillaMapa` desde `data\tecnico` a un modelo comun multirrol.
- Mantener como siguiente unidad `Pantallas por rol y UI comun/tema`.

## Pendientes Detectados En Revision Incremental 2026-06-21

- REDES agrego en fuente regla Firestore `app_config/{docId}` con `allow get: if true`, necesaria para que `ForceUpdateViewModel` lea `app_config/android` antes del login.
- Validar deploy/emulador de reglas para confirmar que la regla publicada coincide con `firebase/firestore.rules`.
- Mantener pendientes de force update: fail-open ante error real de red, ausencia de refuerzo backend en `/api/mobile/bootstrap`, contrato operativo de version minima, reintento/soporte y observabilidad.
- Mantener como siguiente unidad `Pantallas por rol y UI comun/tema`.

## Pendientes Detectados En Revision Incremental 2026-06-22

- Validar en dispositivo real el shell `ALMACEN`: tabs, navegacion, mapa y permisos de ubicacion.
- Crear fixtures JSON para `/api/mobile/almacen/stock`, `/api/mobile/almacen/liquidacion` y `/api/mobile/almacen/instalaciones`.
- Revisar si `AlmacenViewModel.refreshAll()` apaga `isRefreshing` antes de completar la carga real.
- Decidir si `ALMACEN_CUADRILLAS_MAPA` debe seguir usando `/api/mobile/tecnico/cuadrillas-mapa` o tener endpoint propio.
- Confirmar si el segundo tab del shell queda como `LIQUIDACION` o si falta definicion funcional.
- Mantener como siguiente unidad `Pantallas por rol y UI comun/tema`, ahora incluyendo `AlmacenShellScreen`.

## Pendientes Detectados En Revision Incremental 2026-06-14

- Revisar diferencia entre cierre tecnico (`stopAndCloseRoute`) y cierre supervisor (`stop`) para evitar reinicio de tracking el mismo dia.
- Revisar header fijo `X-Mobile-Role: TECNICO` en `postTracking`, aunque el backend hoy decide por roles del token.
- Profundizar `ui\screens\SupervisorShellScreen.kt`, `ui\supervisor\SupervisorViewModel.kt` y `data\supervisor`.
- Profundizar `ui\screens\CoordinadorShellScreen.kt`, `ui\coordinador\CoordinadorViewModel.kt` y `data\coordinador`.
- Revisar `ui\update\ForceUpdateViewModel.kt` y `ForceUpdateScreen.kt` junto con la decision de force update fail-open.
- Revisar si roles backend distintos de `TECNICO`, `SUPERVISOR` y `COORDINADOR` deben caer a `HomeScreen` o bloquearse con mensaje explicito.
- Ignorar outputs/binaries detectados en `app\release` y configuracion IDE `.idea` salvo que Arturo pida revisar empaquetado.

## Pendientes Detectados En Revision Incremental 2026-06-15

- Mantener como siguiente unidad `SupervisorShellScreen`, `SupervisorViewModel`, `SupervisorNotificationHelper`, `data\supervisor` y `network\dto\SupervisorDtos.kt`.
- Mantener pendiente posterior `CoordinadorShellScreen`, `CoordinadorViewModel`, `data\coordinador` y `network\dto\CoordinadorDtos.kt`.
- Revisar `ForceUpdateViewModel` y `ForceUpdateScreen` como unidad separada por lectura directa de Firestore y decision fail-open.
- Ignorar `app\release`, `.idea` y outputs/configuracion IDE salvo solicitud explicita de Arturo; no son contexto funcional prioritario.

## Pendientes Detectados En Revision Incremental 2026-06-16

- Profundizar `CoordinadorShellScreen`, `CoordinadorViewModel`, `data\coordinador` y `network\dto\CoordinadorDtos.kt`.
- Actualizar `TecnicoViewModel` por alertas/notificaciones y relacion con cierre de ruta/tracking.
- Revisar `ForceUpdateViewModel`, `ForceUpdateState` y `ForceUpdateScreen` como unidad separada por lectura directa de Firestore y bloqueo de version.
- Completar deep dive de `android\screens.md` y `android\viewmodels.md`; hoy quedan como mapas incrementales, no cobertura completa.

## Pendientes Detectados En Revision Incremental 2026-06-17

- Revisar `ForceUpdateViewModel`, `ForceUpdateState` y `ForceUpdateScreen` como siguiente unidad por lectura directa de Firestore, bloqueo de version y comportamiento fail-open.
- Actualizar `TecnicoViewModel`, `TecnicoShellScreen`, `NotificationsScreen`, `data\tecnico` y `data\alertas` por alertas/notificaciones y relacion con cierre de ruta/tracking.
- Profundizar modelos/DTOs contra backend REDES, especialmente coordinador, supervisor y tecnico.
- Mantener en revision los cambios de Gradle, manifest, recursos y configs solo como contexto de build/permisos; no modificarlos desde RedesContext.

## Pendientes Detectados En Revision Incremental 2026-06-23

- REDES-MOBILE incremento `app/build.gradle.kts` a `versionCode 11` / `versionName 1.0.10`; validar publicacion o rollout en Play Console si corresponde.
- Ignorar `app\release\app-release.aab` como output binario generado salvo solicitud explicita de revision de empaquetado.
- Mantener como siguiente unidad `Pantallas por rol y UI comun/tema`.

## Pendientes Detectados En Deep Dive Force Update

- Confirmar si el fail-open ante error de Firestore debe mantenerse o cambiar a bloqueo/reintento.
- Decidir si `/api/mobile/bootstrap` debe validar o devolver metadata de version minima como segunda barrera backend.
- Validar en entorno desplegado que la regla fuente nueva permite lectura cliente de `app_config/android`.
- Definir responsable operativo y contrato exacto de `versionMinima`, `versionNominalMinima` y `mensaje`.
- Evaluar UX para reintento, soporte/logout y error cuando Play Store o navegador no abren.
- Agregar observabilidad si se necesita medir usuarios bloqueados o usuarios que entran por fail-open.
- Revisar mojibake en textos fuente de force update antes de entrega visual final.

## Pendientes Detectados En Deep Dive Tecnico Alertas/Notificaciones Y Cierre De Ruta

- Confirmar si `TecnicoViewModel.cerrarRuta()` debe detener tracking antes o despues de que `/api/mobile/alertas-app` confirme la alerta.
- Definir que pasa con tracking local si la alerta de cierre queda `RECHAZADA`; hoy `ruta_cerrada_ymd` ya evita reinicio automatico diario.
- Validar Firestore rules para `alertas_app` y `notificaciones_tecnico`, incluyendo update de `leido=true` por Android.
- Decidir si rechazos de cierre o atencion deben crear notificaciones al tecnico.
- Confirmar si la UI debe refrescar `inicio-jornada`/home despues de `ACEPTADA` o basta con mapear `alertaEstado` a `RUTA_CERRADA`.
- Validar en dispositivo real permisos de ubicacion/notificaciones, foreground service, background/doze y reintentos.
- Revisar mojibake visible en textos fuente de tecnico/notificaciones antes de entrega visual final.

## Pendientes Detectados En Deep Dive Coordinador Shell/ViewModel Y Repositorio

- Revisar si `COORDINADOR_CUADRILLAS_MAPA` debe mantener ruta `/api/mobile/tecnico/cuadrillas-mapa` o migrar a endpoint coordinador propio.
- Validar backend REDES para endpoints `/api/mobile/coordinador/*`; esta unidad leyo el lado Android.
- Validar en dispositivo real flujo de foto de auditoria: permisos, picker/camara, lectura de URI, tamaño de archivo y mensajes de error.
- Revisar manejo de errores silenciosos en `refreshMapaItems`, `refreshCuadrillasMapa`, `refreshAuditoria` y `refreshPredespacho`.
- Revisar textos con mojibake visibles en fuente antes de entrega visual final.
- Cruzar `CoordinadorDtos.kt` con respuestas backend para evitar degradacion silenciosa por `opt*`.

## Pendientes Detectados En Deep Dive Supervisor Shell/ViewModel Y Alertas

- Validar en dispositivo Android 13/14 permisos de ubicacion y notificaciones para supervisor.
- Decidir si alertas de garantia/tramo deben persistirse o sincronizarse; hoy viven solo en memoria del `SupervisorViewModel`.
- Revisar si `refreshAll()` debe ejecutar `checkGarantiasAlertas()` en la primera carga.
- Revisar alertas de tramo: minuto exacto, tramos fijos y comportamiento cuando app esta en background/doze.
- Resolver o eliminar `SupervisorRepository.iniciarJornada()`, porque apunta a `/api/mobile/inicio-jornada` con header `TECNICO`.
- Confirmar que cerrar ruta supervisor no debe detener tracking antes de que backend acepte `FIN_RUTA`.
- Alinear opciones hardcodeadas de garantia con backend/web.
- Revisar `RemoteAlertaRepository` frente a Firestore rules: `alertas_app` y `notificaciones_tecnico`.
- Definir si `NotificationsScreen` debe mostrar alertas supervisor o solo notificaciones tecnico/home; el shell supervisor usa dialogo local propio.
- Revisar mojibake en textos de UI antes de entrega visual final.

## Pendientes Detectados En Deep Dive API Mobile

- Revisar `RemoteSupervisorRepository.iniciarJornada`: el backend de `/api/mobile/inicio-jornada` exige tecnico/admin con cuadrilla.
- Confirmar si `COORDINADOR_CUADRILLAS_MAPA` debe seguir usando `/api/mobile/tecnico/cuadrillas-mapa` o si se requiere endpoint propio.
- Actualizar mensajes de 404 en `RedesApiClient.buildErrorMessage`, porque mencionan endpoints ya existentes.
- Revisar si `fetchCurrentSession` y `/api/mobile/me` siguen siendo necesarios en el flujo actual.
- Sesion/bootstrap/auth ya fue documentado el 2026-06-14; queda en `Revisar` por las decisiones listadas abajo.

## Pendientes Detectados En Deep Dive Sesion/Bootstrap/Auth

- Decidir si Android debe filtrar roles a `TECNICO`, `SUPERVISOR` y `COORDINADOR` o soportar roles web como `TI`, `RRHH`, `GERENCIA`, `JEFATURA`, `ALMACEN`, `GESTOR`, `SEGURIDAD` y `ADMIN`.
- Revisar prioridad de `defaultRole` para mobile: backend reutiliza prioridad web y puede autoseleccionar un rol sin shell Android.
- Revisar UX de usuario Firebase valido pero `usuarios_access` inexistente o inhabilitado: hoy Android lo muestra como token rechazado.
- Definir si cache local debe permitir entrada a shells operativos cuando falla bootstrap fresco.
- Confirmar si `/api/mobile/me` y `RedesApiClient.fetchCurrentSession` son legado o fallback requerido.
- Evaluar si force update debe reforzarse desde backend, porque Android lee Firestore directo y falla abierto ante error.
- Revisar si `permissions` debe usarse para gating mobile o solo mostrarse en `HomeScreen`.

## No Revisado En Profundidad

- Metodos internos de ViewModels/repositorios.
- Grafo exacto de navegacion.
- Verificacion funcional en dispositivo real de tracking foreground, permisos Android 13/14 y restricciones de bateria.
- DTOs y mapeos de respuesta.
- Configuracion sensible local, keystore y archivos binarios.
