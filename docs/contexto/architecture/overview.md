# Arquitectura Inicial - REDES-MOBILE

Estado: Fase 0, mapa superficial.

## Vision General

REDES-MOBILE es una aplicacion Android Kotlin/Compose que consume el backend REDES y usa Firebase para autenticacion/datos/almacenamiento.

Fuente principal: `C:\Proyectos\REDES-MOBILE\app\src\main\java\com\redes\app`.

## Areas Detectadas

### Entrada y ensamblaje

- `MainActivity.kt`
- `REDESApplication.kt`
- `di\AppContainer.kt`

### Network

- `network\RedesApiClient.kt`
- `network\MobileEndpoints.kt`
- `network\AuthTokenInterceptor.kt`
- `network\FirebaseIdTokenProvider.kt`
- `network\TokenProvider.kt`
- `network\RedesApiException.kt`
- DTOs bajo `network\dto`.

### Datos

Paquetes visibles:

- `data\auth`
- `data\session`
- `data\tecnico`
- `data\supervisor`
- `data\coordinador`
- `data\alertas`
- `data\presence`
- `data\tracking`
- `data\local`
- `data\common`

### UI y navegacion

Paquetes visibles:

- `ui\navigation`
- `ui\screens`
- `ui\auth`
- `ui\home`
- `ui\tecnico`
- `ui\supervisor`
- `ui\coordinador`
- `ui\update`
- `ui\components`
- `ui\common`
- `ui\theme`

Pantallas visibles:

- Login, Splash, Home, RoleSelection, Settings, Profile, Notifications, Comunicados, ForceUpdate.
- Shells y detalle de ordenes para Tecnico, Supervisor y Coordinador.

### Permisos y servicios Android

`AndroidManifest.xml` declara:

- Internet.
- Ubicacion fina y gruesa.
- Foreground service y foreground service location.
- Notificaciones.
- `LocationTrackingService` no exportado.
- `FileProvider`.

## Riesgos y Unknowns

- Falta confirmar el grafo de navegacion real y las condiciones por rol.
- Falta leer como se obtiene y refresca el token Firebase.
- Falta cruzar DTOs y endpoints con `REDES\apps\web\src\app\api\mobile`.
- Tracking requiere revision cuidadosa por permisos sensibles y ciclo foreground.
- No se debe abrir ni documentar valores de `local.properties` o keystore.

## Primera Unidad Para Deep Dive

`network` + `data\session` + `REDES\apps\web\src\app\api\mobile`.

Motivo: es el contrato de arranque, autenticacion, roles y comunicacion backend-mobile.
