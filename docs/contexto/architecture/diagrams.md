# Diagramas Iniciales - REDES-MOBILE

Estado: Fase 0, alto nivel.

## Arquitectura Android

```mermaid
flowchart TD
  Main[MainActivity] --> Nav[AppNavHost]
  App[REDESApplication] --> DI[AppContainer]
  Nav --> Screens[ui/screens + shells por rol]
  Screens --> ViewModels[ViewModels por flujo]
  ViewModels --> Repositories[data repositories]
  Repositories --> ApiClient[RedesApiClient]
  Repositories --> FirebaseAuth[Firebase Auth]
  Repositories --> Firestore[Firestore]
  Repositories --> DataStore[DataStore]
  ApiClient --> Backend[REDES /api/mobile]
  Tracking[LocationTrackingService] --> TrackingRepo[TrackingRepository]
  TrackingRepo --> Backend
  TrackingRepo --> Location[Play Services Location]
  Screens --> Maps[Maps Compose]
```

## Relacion Con REDES

```mermaid
sequenceDiagram
  participant App as REDES-MOBILE
  participant Auth as Firebase Auth
  participant API as REDES /api/mobile
  participant Store as Firestore/Storage

  App->>Auth: autentica usuario y obtiene ID token
  App->>API: requests con token via OkHttp
  API->>Store: valida/lee/escribe datos operativos
  Store-->>API: datos de sesion, ordenes, tracking, comunicados
  API-->>App: DTOs mobile por rol
```

## Unidades Iniciales

```mermaid
flowchart TB
  Root[REDES-MOBILE] --> AppModule[app]
  AppModule --> Manifest[AndroidManifest.xml]
  AppModule --> MainSrc[com/redes/app]
  MainSrc --> DI[di]
  MainSrc --> Network[network]
  MainSrc --> Data[data]
  MainSrc --> UI[ui]
  Data --> Session[session]
  Data --> RoleRepos[tecnico/supervisor/coordinador]
  Data --> Tracking[tracking/presence]
  UI --> Navigation[navigation]
  UI --> Screens[screens]
  UI --> RoleUI[tecnico/supervisor/coordinador]
```

## Network Layer Hacia API Mobile

Estado: deep dive 2026-06-13, unidad en **Revisar**.

```mermaid
flowchart LR
  subgraph App[REDES-MOBILE]
    VM[ViewModels]
    Session[RemoteSessionRepository]
    Tecnico[RemoteTecnicoRepository]
    Supervisor[RemoteSupervisorRepository]
    Coord[RemoteCoordinadorRepository]
    Tracking[LocationTrackingService]
    Presence[BackendPresenceRepository]
    Api[RedesApiClient]
    Interceptor[AuthTokenInterceptor]
    Token[FirebaseIdTokenProvider]
  end

  subgraph REDES[REDES Backend]
    MobileApi[/apps/web/src/app/api/mobile/]
    Auth[getMobileAuthContext]
    RoleHelpers[mobileTecnico/mobileSupervisor/mobileCoordinador]
    Firebase[(Firestore/Storage)]
  end

  VM --> Session
  VM --> Tecnico
  VM --> Supervisor
  VM --> Coord
  VM --> Presence
  Tracking --> Api
  Session --> Api
  Tecnico --> Api
  Supervisor --> Api
  Coord --> Api
  Presence --> Api
  Api --> Interceptor
  Interceptor --> Token
  Api --> MobileApi
  MobileApi --> Auth
  Auth --> RoleHelpers
  RoleHelpers --> Firebase
  MobileApi --> Firebase
```

## Tracking Background

Estado: deep dive 2026-06-14, unidad en **Revisar**.

```mermaid
sequenceDiagram
  participant UI as Pantalla por rol
  participant Manager as TrackingManager
  participant Service as LocationTrackingService
  participant Repo as TrackingRepository
  participant API as /api/mobile/tracking
  participant Store as Firestore

  UI->>Manager: startIfNeeded()
  Manager->>Service: ACTION_START foreground service
  Service->>Service: filtra movimiento menor a 30 m
  Service->>Repo: postLocation(lat,lng,accuracy,speed)
  Repo->>API: POST JSON con Bearer token
  API->>Store: supervisores/{uid}/tracking o cuadrillas/{id}/tracking
  API-->>Repo: { ok: true }
```

## Presencia Mobile

Estado: deep dive 2026-06-14, unidad en **Revisar**.

```mermaid
sequenceDiagram
  participant App as REDESApplication
  participant Lifecycle as ProcessLifecycleOwner
  participant Manager as MobilePresenceManager
  participant Repo as BackendPresenceRepository
  participant API as /api/mobile/presencia
  participant Store as usuarios_presencia/{uid}

  App->>Lifecycle: registra observer global
  Lifecycle->>Manager: onStart()
  loop cada 60 s
    Manager->>Repo: markOnline(payload local)
    Repo->>API: POST /presencia
    API->>Store: online=true, roles/areas backend
  end
  Lifecycle->>Manager: onStop() o onSignedOut()
  Manager->>Repo: markOffline(uid)
  Repo->>API: DELETE /presencia
  API->>Store: online=false
```

## Sesion, Bootstrap Y Roles

Estado: deep dive 2026-06-14, unidad en **Revisar**.

```mermaid
sequenceDiagram
  participant UI as Login/AppNavHost
  participant AuthVM as AuthViewModel
  participant Firebase as Firebase Auth
  participant HomeVM as HomeViewModel
  participant Provider as FirebaseIdTokenProvider
  participant API as REDES /api/mobile/bootstrap
  participant Backend as Auth + AccessContext
  participant Cache as DataStore

  UI->>AuthVM: signIn(email,password)
  AuthVM->>Firebase: signInWithEmailAndPassword
  Firebase-->>AuthVM: currentUser(uid,email)
  AuthVM-->>HomeVM: auth state
  HomeVM->>Provider: getIdToken()
  Provider-->>HomeVM: Firebase ID token
  HomeVM->>API: GET /api/mobile/bootstrap
  API->>Backend: verify token + usuarios_access + roles
  Backend-->>API: session, permissions, comunicados, gates
  API-->>HomeVM: MobileBootstrap
  HomeVM->>Cache: saveSession + saveSelectedRole si aplica
  HomeVM-->>UI: Comunicados, RoleSelection, shell por rol o Home generico
```

## Decision De Entrada Mobile

```mermaid
flowchart TD
  Start[MainActivity] --> Update{Force update}
  Update -->|Checking| Splash[Splash]
  Update -->|UpdateRequired| Force[ForceUpdateScreen]
  Update -->|UpToDate| Auth{Firebase user}
  Auth -->|No| Login[LoginScreen]
  Auth -->|Si| Bootstrap[/api/mobile/bootstrap]
  Bootstrap -->|OK| Gate{Comunicados obligatorios?}
  Bootstrap -->|Error con cache| Cache[Sesion cacheada + error]
  Bootstrap -->|Error sin cache| HomeError[Home generico con error]
  Gate -->|Si| Comunicados[ComunicadosScreen]
  Gate -->|No| Roles{Varios roles sin selectedRole?}
  Roles -->|Si| Selector[RoleSelectionScreen]
  Roles -->|No| Selected{selectedRole}
  Selected -->|TECNICO| Tecnico[TecnicoShellScreen]
  Selected -->|SUPERVISOR| Supervisor[SupervisorShellScreen]
  Selected -->|COORDINADOR| Coordinador[CoordinadorShellScreen]
  Selected -->|Otro/null| Home[HomeScreen generico]
```
