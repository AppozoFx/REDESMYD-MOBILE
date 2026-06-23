# Changelog de Contexto - REDES-MOBILE

## 2026-06-22 - Revision incremental diaria: unidad ALMACEN

- Revisados `git status` y `git diff --name-only` de REDES y REDES-MOBILE.
- REDES no mostro cambios pendientes de codigo fuente; se verifico existencia de endpoints `api/mobile/almacen/stock`, `liquidacion` e `instalaciones`.
- REDES-MOBILE mantiene cambios amplios de fuente; se documento la novedad funcional `ALMACEN` sin tocar codigo.
- Creado `android/almacen-shell-repositorio.md` con shell, tabs, `AlmacenViewModel`, repositorio, DTOs, endpoints, diagrama Mermaid y pendientes.
- Actualizados `README.md`, `INDEX.md`, `PENDIENTES.md`, `android/screens.md`, `android/repositories.md`, `android/models.md` e `indexes/source-index.json`.
- Hallazgos: mapa de almacen reutiliza `/api/mobile/tecnico/cuadrillas-mapa`, DTOs degradan con `opt*`, `refreshAll()` puede apagar `isRefreshing` antes de la carga real y falta validar permisos/mapa en dispositivo.
- Siguiente unidad recomendada: pantallas por rol y UI comun/tema.
- No se modifico codigo fuente, Gradle, configs, Firebase rules, package files, lockfiles, credenciales ni binarios.

## 2026-06-22 - Feature: Rol ALMACEN — shell screen + stack completo

- **5 tabs implementados (4 definidos + mapa)**: STOCK, LIQUIDACIÓN, INSTALACIONES, MAPA. El 2.° tab quedó pendiente de definición por el usuario.
- **Backend (REDES web)** — 3 endpoints nuevos:
  - `GET /api/mobile/almacen/stock` → stock de equipos por cuadrilla (todas las cuadrillas habilitadas). Auth: role ALMACEN, área ALMACEN o INSTALACIONES, o isAdmin.
  - `GET /api/mobile/almacen/liquidacion?ym=YYYY-MM` → órdenes finalizadas con estado liquidado/pendiente y KPIs (finalizadas/liquidadas/pendientes).
  - `GET /api/mobile/almacen/instalaciones?ym=YYYY-MM` → instalaciones con materiales (acta, precon, bobina, SNs ONT/MESH/BOX) y estado `ok`/`pendiente`.
- **Android — capa de datos nueva**:
  - `data/almacen/AlmacenModels.kt`: `AlmacenStockCuadrilla`, `AlmacenEquipo`, `AlmacenLiquidacionData`, `AlmacenLiquidacionItem`, `AlmacenLiquidacionKpi`, `AlmacenInstalacion`.
  - `network/dto/AlmacenDtos.kt`: parsers `toAlmacenStockList`, `toAlmacenLiquidacionData`, `toAlmacenInstalacionesList`.
  - `data/almacen/AlmacenRepository.kt` (interfaz) + `RemoteAlmacenRepository.kt`.
  - `network/MobileEndpoints.kt`: constantes `ALMACEN_STOCK`, `ALMACEN_LIQUIDACION`, `ALMACEN_INSTALACIONES`, `ALMACEN_CUADRILLAS_MAPA`.
  - `network/RedesApiClient.kt`: métodos `fetchAlmacenStock`, `fetchAlmacenLiquidacion`, `fetchAlmacenInstalaciones`, `fetchAlmacenCuadrillasMapa`.
- **Android — capa de UI nueva**:
  - `ui/almacen/AlmacenUiState.kt`: enum `AlmacenTab` (STOCK/LIQUIDACION/INSTALACIONES/MAPA), `AlmacenUiState`.
  - `ui/almacen/AlmacenViewModel.kt` + `AlmacenViewModelFactory`.
  - `ui/screens/AlmacenShellScreen.kt`: shell completo con 4 tabs.
    - STOCK: KPIs globales + cards expandibles por cuadrilla con conteo ONT/MESH/FONO/BOX y lista de SNs.
    - LIQUIDACIÓN: selector de mes, KPIs, lista de órdenes separada en pendientes/liquidadas.
    - INSTALACIONES: selector de mes, KPIs ok/pendientes, cards expandibles con acta/SNs/precon/bobina.
    - MAPA: mapa de cuadrillas idéntico al del Coordinador (reutiliza `CuadrillaMarkerIcon`, `MapCuadrillaPopup`, endpoint `/api/mobile/tecnico/cuadrillas-mapa`).
- **Cableado**:
  - `di/AppContainer.kt`: `almacenRepository` agregado a interfaz e implementación.
  - `ui/navigation/AppNavHost.kt`: case `ALMACEN` en el bloque del `Home`.
  - `MainActivity.kt`: `almacenViewModel` instanciado y callbacks conectados.

## 2026-06-22 - Feature: LoginScreen — orbes animados + estado connected + logo pulse

- **Objetivo**: igualar visualmente `LoginScreen.kt` con el login web de REDES (misma paleta, misma máquina de estados del borde, orbes flotantes).
- **Orbes animados (4)**: fondo con `Canvas` + `Brush.radialGradient` + `infiniteTransition`. Cada orbe tiene animación de alpha (pulse) y drift x/y independientes, con duraciones distintas (3800–6200ms) para movimiento orgánico. Sustituyen los dos `Box` estáticos de `radialGradient` que existían antes.
- **Estado `connected`** (éxito verde): `LaunchedEffect(uiState.currentUser)` activa `isSuccess = true` cuando el usuario queda autenticado. El borde de la card cambia a `RedesConnected` (`#22C55E`) sólido, el botón se vuelve verde con texto "✓ Acceso concedido", el halo del logo también cambia a verde.
- **Logo pulse**: `logoGlowAlpha` y `logoScale` animados — el halo exterior del logo late durante `isSubmitting` (período 800ms) y más suavemente en idle (2000ms); la escala va de 1f a 1.09f vía `graphicsLayer`.
- **`Color.kt`**: agregados `RedesBrand = Color(0xFF30518C)` (primario web) y `RedesConnected = Color(0xFF22C55E)` (verde éxito).
- No se modificaron otros archivos Kotlin, Gradle, Firebase rules, recursos de strings ni AndroidManifest.

## 2026-06-21 - Revision incremental diaria: cruce force update con rules REDES

- Revisados `git status` y `git diff --name-only` de REDES y REDES-MOBILE.
- REDES agrego en fuente regla Firestore para `app_config/{docId}` con `allow get: if true`, necesaria para que Android lea `app_config/android` antes de login.
- Actualizados `android/force-update.md`, `PENDIENTES.md` e `indexes/source-index.json` para cambiar el pendiente de "regla faltante" por "validar deploy/emulador".
- REDES-MOBILE mantiene cambios amplios ya detectados en roles, navegacion, network, modelos, repositorios, recursos y carpetas nuevas; no se abrio un nuevo deep dive porque la unidad recomendada sigue siendo pantallas por rol y UI comun/tema.
- No se modifico codigo fuente, Gradle, configs, package files, lockfiles, credenciales ni binarios.

## 2026-06-20 - Revision incremental diaria: repositorios y manejo de resultados

- Revisados `git status` y `git diff --name-only` de REDES y REDES-MOBILE.
- REDES no mostro cambios de codigo fuente.
- REDES-MOBILE mantiene cambios funcionales ya detectados en roles, navegacion, network, modelos, recursos y carpetas nuevas; se priorizo la unidad recomendada de repositorios por rol restantes y manejo de errores/resultados.
- Actualizado `android/repositories.md` con `AppContainer`, auth, session, presence, tracking, alertas, tecnico, supervisor y coordinador.
- Actualizados `README.md`, `INDEX.md`, `PENDIENTES.md` e `indexes/source-index.json`.
- Hallazgos: session valida `apiClient.isConfigured`, roles usan `Result<T>` con helper `call`, tracking/presencia no propagan `Result`, listeners Firestore de alertas/notificaciones silencian errores hacia UI, y el inicio de jornada supervisor sigue apuntando al endpoint tecnico.
- Siguiente unidad recomendada: pantallas por rol y UI comun/tema.
- No se modifico codigo fuente, Gradle, configs, Firebase rules, package files, lockfiles, credenciales ni binarios.

## 2026-06-19 - Revision incremental diaria: modelos/DTOs contra backend

- Revisados `git status` y `git diff --name-only` de REDES y REDES-MOBILE.
- REDES no mostro cambios de codigo fuente.
- REDES-MOBILE mantiene cambios funcionales ya detectados en roles, navegacion, network, modelos y recursos; se priorizo la unidad recomendada de modelos/DTOs.
- Creado `android/models.md` con cruce de `MobileSessionDto`, `MobileBootstrapDto`, `TecnicoDtos`, `SupervisorDtos`, `CoordinadorDtos` y modelos de dominio contra endpoints REDES `api/mobile`.
- Actualizados `README.md`, `INDEX.md`, `PENDIENTES.md` e `indexes/source-index.json`.
- Hallazgos: parseo JSON manual con `opt*` degrada cambios de contrato a valores vacios/cero; items de mapas sin coordenadas se descartan; ramas vacias de endpoints coordinador pueden omitir metadata esperada.
- Siguiente unidad recomendada: repositorios por rol restantes y manejo de errores/resultados.
- No se modifico codigo fuente, Gradle, configs, Firebase rules, package files, lockfiles, credenciales ni binarios.

## 2026-06-18 - Feature: cuadrillas/lista — equipos por orden y navegacion a detalle

**Objetivo:** mostrar cantidades MESH/FONO/BOX en cada card de orden del coordinador y permitir tocar para ver el detalle de la orden.

**Backend REDES (agregados previamente a este changelog):**
- `cuadrillas/route.ts`: agrega `cantMesh/cantFono/cantBox` por item de orden.
- Nuevo endpoint `GET /api/mobile/coordinador/ordenes/[id]` con detalle completo de orden.

**Android:**
- `CoordinadorOrdenItem` (`CoordinadorModels.kt`): nuevos campos `cantMesh`, `cantFono`, `cantBox` (default 0).
- `CoordinadorOrdenDetail` (`CoordinadorModels.kt`): nuevo data class para detalle de orden del coordinador.
- `CoordinadorDtos.kt`: `toOrdenItemList()` parsea cantMesh/cantFono/cantBox; nuevo `toCoordinadorOrdenDetail()`.
- `MobileEndpoints.kt`: funcion `coordinadorOrdenDetail(id)`.
- `RedesApiClient.kt`: `fetchCoordinadorOrdenDetail(id)`.
- `CoordinadorRepository.kt`: `fetchOrdenDetail(id)` en interfaz.
- `RemoteCoordinadorRepository.kt`: implementacion de `fetchOrdenDetail`.
- `CoordinadorUiState.kt`: `selectedOrdenDetail: CoordinadorOrdenDetail?`, `isOrdenDetailLoading: Boolean`.
- `CoordinadorViewModel.kt`: `loadOrdenDetail(id)` y `clearOrdenDetail()`.
- `CoordinadorShellScreen.kt`: `OrdenItemRow` ahora muestra chips MESH/FONO/BOX (solo si > 0) y es clickeable; `onOrdenClick` propagado por toda la cadena (`CoordCuadrillaExpandableCard` → `CoordCuadrillasLista` → `CoordCuadrillasTab` → `CoordinadorShellScreen`).
- Nuevo `CoordinadorOrderDetailScreen.kt`: pantalla de solo lectura con scaffold propio, muestra cliente, documento, telefono (clickeable para marcar), tipo, servicio, fecha, region, cuadrilla, equipos y direccion con acceso a Google Maps.
- `AppDestination.kt`: nuevo `CoordinadorOrderDetail`.
- `AppNavHost.kt`: importa `CoordinadorOrderDetailScreen`, agrega `onCoordinadorOrdenClick`/`onCoordinadorOrdenDetailBack` en firma y nuevo `composable(CoordinadorOrderDetail.route)`.
- `MainActivity.kt`: `onCoordinadorOrdenClick = coordinadorViewModel::loadOrdenDetail`, `onCoordinadorOrdenDetailBack = coordinadorViewModel::clearOrdenDetail`.

## 2026-06-18 - Fix: advertencias Google Play Console (fragment, edge-to-edge, APIs deprecadas)

**Problema 1 — androidx.fragment:fragment versión desactualizada (1.1.0)**
- Firebase Auth y GMS traen como dependencia transitiva `fragment:1.1.0`, que Google Play Console marca como obsoleta.
- Fix: agregado `androidx.fragment:fragment-ktx:1.8.6` como dependencia explícita en `libs.versions.toml` + `app/build.gradle.kts`. Gradle resuelve la versión más alta, forzando 1.8.6 en lugar de 1.1.0.

**Problema 2 — Es posible que la pantalla de borde a borde no se muestre para todos los usuarios**
- `enableEdgeToEdge()` ya estaba llamado en `MainActivity.kt` — eso es suficiente y cubre todos los niveles de API.
- Se intentó agregar `values-v35/themes.xml` con `android:windowOptInEdgeToEdge = true` pero causó build failure porque el atributo no estaba disponible en el SDK instalado (error AAPT2: `android:attr/windowOptInEdgeToEdge not found`).
- Fix final: el directorio `values-v35` fue eliminado. La llamada runtime a `enableEdgeToEdge()` en `MainActivity.onCreate()` resuelve el requisito de borde a borde para todas las versiones.

**Problema 3 — APIs deprecadas (`setStatusBarColor`, `setNavigationBarColor`, `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`)**
- Todas las llamadas vienen de código interno de librerías terceras: `FragmentActivity.onStop` (fragment), Firebase Auth internals y GMS Location internals.
- Fix parcial: el upgrade de `fragment-ktx:1.8.6` elimina el trace de `FragmentActivity.onStop`.
- Pendiente: actualizar Firebase BOM y `play-services-location` a versiones más recientes cuando estén disponibles; las versiones actuales (34.13.0 y 21.3.0) pueden ya incluir el fix en sus GMS internals.

**Archivos modificados:**
- `gradle/libs.versions.toml` — nueva versión `fragment = "1.8.6"` y entrada `androidx-fragment-ktx`
- `app/build.gradle.kts` — `implementation(libs.androidx.fragment.ktx)` agregado

## 2026-06-18 - Fix updatedByName en predespacho (sin cambios Android)

- El campo "Guardado por" en el tab Predespacho mostraba el UID de Firebase en lugar del nombre del usuario.
- El bug era en REDES web: `PredespachoClient.tsx` no enviaba `userName` al guardar, y el servidor guardaba el UID como fallback en `updatedByName`.
- Fix aplicado en REDES web (`dashboard/route.ts` + `PredespachoClient.tsx`). No hay cambios en Android.
- El endpoint mobile `GET /api/mobile/coordinador/predespacho` lee `updatedByName` directo del documento Firestore, por lo que REDES-MOBILE beneficia del fix automaticamente en todos los guardados nuevos.

## 2026-06-18 - Implementacion predespacho coordinador (web + mobile)

- Actualizado `android/coordinador-shell-repositorio.md` con subseccion "Predespacho — cambios 2026-06-18".
- **Backend REDES** (`api/mobile/coordinador/predespacho/route.ts`): reescrito para leer coleccion activa `instalaciones_predespacho` en lugar de legacy `instalaciones_predespacho_rows`. Batches de 30 por `cuadrillaId in [...]`, filtro en memoria `startYmd <= ymd <= endYmd`, agregacion por cuadrillaId sumando `final.ONT/MESH/FONO/BOX`, extraccion de `precon/bobinaResi/rolloCondo` solo de docs `ALL` o `SHARED`.
- **`CoordinadorPredespachoRow`** (`data/coordinador/CoordinadorModels.kt`): agregados `precon50/100/150/200: Int = 0`.
- **`CoordinadorDtos.kt`**: `toPredespachoRowList()` parsea objeto anidado `precon.PRECON_50/100/150/200`.
- **`CoordinadorShellScreen.kt`**: `CoordPredespachoContent` muestra chips principales con `horizontalScroll` y fila condicional de extras (PRE 50/100/150/200, Bobina Resi, Rollo Condo).
- **Web `PredespachoClient.tsx`**: coordinador auto-confirma modo, ve solo cuadrillas con predespacho guardado, sin filtros Estado/Modelo/Despacho/Lote, sin chip "Ver omitidas"/boton cambiar modo/chip IA, sin Panel de recursos.
- Actualizados docs Obsidian: `Transferencias instalaciones predespacho.md` y `06-REDES-MOBILE.md`.
- No se modifico Gradle, configs, Firebase rules, package files, lockfiles, credenciales ni binarios.

## 2026-06-18 - Deep dive tecnico alertas/notificaciones y cierre de ruta

- Creado `android/tecnico-alertas-cierre-ruta.md` con `TecnicoViewModel`, `TecnicoUiState`, `TecnicoShellScreen`, `NotificationsScreen`, `data/tecnico`, `data/alertas`, tracking, DTO/network y backend REDES relacionado.
- Actualizados `README.md`, `INDEX.md`, `PENDIENTES.md`, `android/screens.md`, `android/viewmodels.md`, `android/repositories.md` e `indexes/source-index.json`.
- Hallazgos: el cierre tecnico detiene tracking local antes de confirmar la alerta remota; la ruta se marca oficialmente `RUTA_CERRADA` solo si web acepta la alerta; `notificaciones_tecnico` funciona como inbox persistido por cuadrilla.
- Riesgos destacados: Firestore rules para listeners/updates directos, manejo de rechazo, posible doble fuente de verdad visual, errores silenciosos en listeners, permisos foreground service y diferencia con cierre supervisor.
- No se modifico codigo fuente, Gradle, configs, Firebase rules, package files, lockfiles, credenciales ni binarios.

## 2026-06-18 - Deep dive force update

- Creado `android/force-update.md` con `ForceUpdateViewModel`, `ForceUpdateState`, `ForceUpdateScreen`, `MainActivity`, splash, navegacion, sesion/bootstrap/cache y cruce REDES.
- Actualizados `README.md`, `INDEX.md`, `PENDIENTES.md` e `indexes/source-index.json`.
- Hallazgos: Android lee Firestore directo `app_config/android`, compara `BuildConfig.VERSION_CODE` con `versionMinima`, bloquea con pantalla completa si requiere update y falla abierto ante excepciones.
- Cruce backend: `/api/mobile/bootstrap` y `core/auth/mobileBootstrap.ts` no devuelven ni validan version minima; `MobileBootstrapDto` tampoco espera campos de version.
- Riesgos destacados: fail-open, ausencia de segunda barrera backend, rules Firestore para `app_config/android`, falta de retry/observabilidad y mojibake en textos fuente.
- No se modifico codigo fuente, Gradle, configs, Firebase rules, package files, lockfiles, credenciales ni binarios.

## 2026-06-17 - Deep dive coordinador shell/ViewModel y repositorio

- Revisados `git status` y `git diff --name-only` de REDES y REDES-MOBILE.
- REDES no mostro cambios de codigo fuente; solo cambios documentales bajo `docs/contexto`.
- Creado `android/coordinador-shell-repositorio.md` con `CoordinadorShellScreen`, `CoordinadorViewModel`, `CoordinadorUiState`, `data/coordinador`, DTOs y endpoints coordinador.
- Actualizados `README.md`, `INDEX.md`, `PENDIENTES.md`, `android/screens.md`, `android/viewmodels.md`, `android/repositories.md` e `indexes/source-index.json`.
- Hallazgos: el coordinador cubre inicio mensual, cuadrillas por dia, mapa de ordenes/cuadrillas, almacen, auditoria con foto, predespacho, ventas y plantillas; `refreshAll()` carga resumen/cuadrillas/mapa en paralelo.
- Riesgos destacados: mapa de cuadrillas usa endpoint tecnico compartido, falta validar backend `/api/mobile/coordinador/*`, sustento con foto requiere prueba real de URI/permisos, y varias cargas secundarias fallan sin exponer error.
- No se modifico codigo fuente, Gradle, configs, binarios ni credenciales.

## 2026-06-16 - Deep dive supervisor shell/ViewModel y alertas

- Creado `android/supervisor-shell-alertas.md` con `SupervisorShellScreen`, `SupervisorOrderDetailScreen`, `SupervisorViewModel`, `SupervisorUiState`, `SupervisorNotificationHelper`, `data/supervisor`, `data/alertas`, DTOs y endpoints supervisor.
- Actualizados `INDEX.md`, `PENDIENTES.md`, `android/screens.md`, `android/viewmodels.md` e `indexes/source-index.json`; siguiente unidad recomendada: `Coordinador shell/ViewModel y repositorio`.
- Hallazgos: el shell supervisor cubre inicio, ordenes, stock placeholder y mapa; el ViewModel carga home/orders/mapa en paralelo, maneja jornada/tracking, supervision, garantia y alertas locales.
- Riesgos destacados: alertas supervisor viven solo en memoria, `refreshAll()` no dispara `checkGarantiasAlertas()`, tramo depende de minuto exacto, cierre supervisor detiene tracking antes de confirmar backend, y `SupervisorRepository.iniciarJornada()` queda apuntando al endpoint tecnico.
- No se ejecuto build, app, emulador, pruebas en dispositivo, backend, listeners Firestore reales ni cambios de codigo fuente.

## 2026-06-16 - Revision incremental diaria: pantallas y ViewModels

- Revisados `git status` y `git diff --name-only` de REDES y REDES-MOBILE.
- REDES no mostro cambios de codigo fuente; solo cambios documentales bajo `docs/contexto`.
- REDES-MOBILE mantiene cambios amplios en fuente y carpetas nuevas por rol: supervisor, coordinador, alertas, tracking y update.
- Creados `android/screens.md` y `android/viewmodels.md` como mapas incrementales para documentos planificados que no existian.
- Actualizados `INDEX.md`, `PENDIENTES.md` e `indexes/source-index.json`.
- No se modifico codigo fuente, Gradle, configs, binarios ni credenciales.

## 2026-06-15 - Revision incremental diaria: mantenimiento de pendientes

- Revisados `git status` y `git diff` del proyecto mobile; persisten cambios relevantes en navegacion, red, repositorios, tracking, roles supervisor/coordinador y force update.
- Actualizados `INDEX.md`, `PENDIENTES.md` e `indexes/source-index.json` para reflejar revision diaria sin abrir un nuevo deep dive.
- No se modificaron documentos de flujo porque `android/navigation.md`, `android/network.md`, `android/repositories.md` y `android/tracking.md` ya cubren el contrato principal detectado; se mantienen pendientes supervisor/coordinador/force update.
- No se modifico codigo fuente, Gradle, configs, binarios ni credenciales.

## 2026-06-14 - Deep dive tracking/presencia

- Creado `android/tracking.md` con flujo de permisos, `TrackingManager`, `LocationTrackingService`, `TrackingRepository`, presencia por lifecycle y contratos backend `/api/mobile/tracking` y `/api/mobile/presencia`.
- Documentada relacion con `AppNavHost`, `REDESApplication`, `MainActivity.handleLogout`, `TecnicoViewModel.cerrarRuta` y `SupervisorViewModel.confirmarCerrarRuta`.
- Actualizados `INDEX.md`, `PENDIENTES.md`, `architecture/diagrams.md` e `indexes/source-index.json`; tracking/presencia queda en estado `Revisar`.
- Riesgos principales: header fijo `X-Mobile-Role: TECNICO` en tracking, permiso suprimido dentro del service, diferencia entre cierre tecnico/supervisor, silencios si `API_BASE_URL` no esta configurado y falta prueba en dispositivo real.
- No se modifico codigo fuente, Gradle, configs, binarios ni credenciales.

## 2026-06-14 - Revision incremental diaria: navegacion por rol

- Detectados cambios relevantes en navegacion, pantallas, repositorios y ViewModels para roles `TECNICO`, `SUPERVISOR` y `COORDINADOR`, ademas de tracking y force update.
- Creado `android/navigation.md` con destinos, resolucion de entrada, shells por rol, permisos Android, dependencias DI y diagrama Mermaid.
- Actualizados `INDEX.md`, `PENDIENTES.md` e `indexes/source-index.json`; navegacion marcada como `Revisar`.
- Pendientes nuevos: tracking/presencia, supervisor shell/ViewModel, coordinador shell/ViewModel, force update y roles backend sin shell mobile.
- No se modifico codigo fuente, Gradle, configs, outputs binarios ni credenciales.

## 2026-06-14 - Cierre corto recuperacion sesion/bootstrap/auth

- Validada desde REDES con `Get-Content -LiteralPath` la ruta `apps/web/src/app/api/mobile/comunicados/[id]/seen/route.ts`, que habia fallado por lectura PowerShell con corchetes.
- Actualizados `android/session-auth-bootstrap.md` y `android/network.md` con la conducta confirmada del endpoint de visto de comunicados y su relacion con el refresh de bootstrap.
- La unidad `Sesion/bootstrap/auth REDES-MOBILE + RBAC/accessContext REDES` se mantiene en `Revisar` por decisiones humanas pendientes sobre roles web sin shell Android, cache stale, mensajes de acceso, permisos y force update.
- No se modifico codigo fuente, Gradle, configs ni credenciales.

## 2026-06-14 - Deep dive sesion/bootstrap/auth + RBAC REDES

- Creado `android/session-auth-bootstrap.md` con flujo Firebase Auth -> ID token -> `/api/mobile/bootstrap` -> cache local -> comunicados -> seleccion de rol -> home/shell.
- Actualizado `android/network.md` con nota de que bootstrap es el flujo efectivo y `/api/mobile/me` no tiene consumidor directo encontrado.
- Actualizado `architecture/diagrams.md` con secuencia de bootstrap y decision de entrada mobile.
- Actualizados `INDEX.md`, `PENDIENTES.md` e `indexes/source-index.json`; unidad marcada como `Revisar`.
- Hallazgos principales: roles web sin shell mobile, `defaultRole` con prioridad web, cache stale si falla bootstrap, errores de acceso mal diferenciados, force update fail-open y permisos recibidos sin gating mobile observado.
- No se modifico codigo fuente, Gradle, configs ni credenciales.

## 2026-06-13 - Cierre corto API mobile + Network/API

- Validada la ruta real de `LocationTrackingService.kt`: `app/src/main/java/com/redes/app/data/tracking/LocationTrackingService.kt`.
- Actualizados `android/network.md` y `android/repositories.md` con la evidencia puntual de tracking/presencia.
- La unidad se mantiene en `Revisar` porque quedan inconsistencias de contrato que requieren decision humana.
- No se modifico codigo fuente, Gradle, configs ni credenciales.

## 2026-06-13 - Deep dive API mobile REDES + Network/API REDES-MOBILE

- Creado `android/network.md` con contrato del `RedesApiClient`, endpoints, DTOs, auth token y errores.
- Creado `android/repositories.md` solo para consumidores directos del network layer.
- Actualizado `architecture/diagrams.md` con flujo Network/API y tracking background.
- Actualizados `INDEX.md` y `PENDIENTES.md`: unidad marcada como `Revisar`; repositorios directos marcados como cobertura parcial.
- Se registro existencia de configuracion local de API base URL sin copiar valores.
- No se modifico codigo fuente, Gradle, configs ni credenciales.

## 2026-06-13 - Fase 0 validacion y cierre

- Validada la existencia de documentos base de Fase 0: `README.md`, `INDEX.md`, `PENDIENTES.md`, `CHANGELOG-CONTEXTO.md`, `architecture/overview.md`, `architecture/diagrams.md` e `indexes/source-index.json`.
- Revisados diagramas Mermaid iniciales como mapas de alto nivel, sin deep dive de funciones.
- Actualizados `INDEX.md` y `PENDIENTES.md` para dejar como primera unidad conjunta: `API mobile REDES + Network/API REDES-MOBILE`.
- Confirmada existencia superficial de `app/src/main/java/com/redes/app/network` y de `apps/web/src/app/api/mobile` en REDES.

Notas:

- No se hizo Fase 1 ni Fase 2.
- No se modifico codigo fuente, Gradle ni configs.
- No se abrieron secretos locales ni keystores.

## 2026-06-13 - Fase 0 mapa simple inicial

- Actualizado `README.md` con alcance de Fase 0 y mapa rapido.
- Creado `INDEX.md` con unidades documentales detectadas.
- Actualizado `PENDIENTES.md` con backlog inicial.
- Creado `architecture/overview.md`.
- Creado `architecture/diagrams.md` con Mermaid de arquitectura Android y relacion con REDES.
- Creado `indexes/source-index.json` como indice superficial.

Notas:

- No se hizo deep dive archivo por archivo.
- No se modifico codigo fuente, Gradle ni configs.
- No se abrieron secretos locales ni keystores.
