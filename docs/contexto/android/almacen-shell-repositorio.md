# Almacen Shell, ViewModel Y Repositorio

Actualizado: 2026-06-22.

Estado: **Revisar**. Revision incremental diaria focalizada en la nueva unidad `ALMACEN`. No representa validacion funcional en dispositivo ni pruebas de contrato.

## Alcance Leido

REDES-MOBILE:

- `app/src/main/java/com/redes/app/ui/screens/AlmacenShellScreen.kt`
- `app/src/main/java/com/redes/app/ui/almacen/AlmacenViewModel.kt`
- `app/src/main/java/com/redes/app/ui/almacen/AlmacenUiState.kt`
- `app/src/main/java/com/redes/app/data/almacen/AlmacenRepository.kt`
- `app/src/main/java/com/redes/app/data/almacen/RemoteAlmacenRepository.kt`
- `app/src/main/java/com/redes/app/data/almacen/AlmacenModels.kt`
- `app/src/main/java/com/redes/app/network/dto/AlmacenDtos.kt`
- `app/src/main/java/com/redes/app/network/MobileEndpoints.kt`
- `app/src/main/java/com/redes/app/network/RedesApiClient.kt`
- `app/src/main/java/com/redes/app/di/AppContainer.kt`
- `app/src/main/java/com/redes/app/ui/navigation/AppNavHost.kt`
- `app/src/main/java/com/redes/app/MainActivity.kt`

REDES:

- `apps/web/src/app/api/mobile/almacen/stock/route.ts`
- `apps/web/src/app/api/mobile/almacen/liquidacion/route.ts`
- `apps/web/src/app/api/mobile/almacen/instalaciones/route.ts`

## Proposito

La unidad agrega un shell operativo para usuarios con rol seleccionado `ALMACEN`. El flujo entra desde `AppNavHost` cuando `homeUiState.selectedRole == "ALMACEN"` y usa `AlmacenViewModel` para cargar stock, liquidacion, instalaciones y mapa de cuadrillas.

`MainActivity` instancia `AlmacenViewModel` con `AuthRepository`, `SessionRepository` y `AlmacenRepository`, y conecta callbacks de tabs, refresh, cambio de mes, expansion de cards y descarte de errores.

## Navegacion Y UI

`AlmacenShellScreen` usa `Scaffold` con `AppTopBar` y `NavigationBar`.

| Tab | Fuente UI | Datos | Conducta |
| --- | --- | --- | --- |
| `STOCK` | `AlmacenStockTab` | `AlmacenStockCuadrilla` | KPIs globales por tipo de equipo y cards expandibles por cuadrilla con SNs. |
| `LIQUIDACION` | `AlmacenLiquidacionTab` | `AlmacenLiquidacionData` | Selector mensual, KPIs finalizadas/liquidadas/pendientes y listas separadas. |
| `INSTALACIONES` | `AlmacenInstalacionesTab` | `AlmacenInstalacion` | Selector mensual, KPIs completas/pendientes y cards expandibles con acta, SNs, precon y bobina. |
| `MAPA` | `AlmacenMapaTab` | `CuadrillaMapa` | Google Map con marcadores reutilizados desde el contrato tecnico/coordinador. |

El mapa solicita solo lectura del estado de permiso local (`ACCESS_FINE_LOCATION`) para habilitar `isMyLocationEnabled`; la solicitud de permiso no se observa dentro del shell de almacen.

## ViewModel

`AlmacenViewModel` combina `authRepository.currentUser` con `sessionRepository.selectedRole`.

- Si no hay usuario o el rol no es `ALMACEN`, resetea `AlmacenUiState()`.
- Si el rol queda habilitado y el stock esta vacio, dispara `refreshStock()`.
- `selectTab()` carga bajo demanda la primera vez que una lista esta vacia.
- `previousMonth()` y `nextMonth()` cambian `selectedYm` y limpian liquidacion/instalaciones antes de recargar segun tab activo.
- `refreshAll()` delega al refresh del tab seleccionado.

Riesgo observado: `refreshAll()` pone `isRefreshing=true`, llama a un metodo que lanza coroutine propia y despues lanza otra coroutine que puede volver `isRefreshing=false` antes de que termine la carga real. Los refresh internos tambien apagan `isRefreshing=false`, pero el indicador pull-to-refresh puede no representar exactamente el tiempo de red.

## Repositorio Y Endpoints

`RemoteAlmacenRepository` envuelve llamadas en `Result<T>` con `withContext(Dispatchers.IO)` y captura `RedesApiException` y excepciones generales.

| Metodo repositorio | Cliente API | Endpoint |
| --- | --- | --- |
| `fetchStock()` | `fetchAlmacenStock()` | `GET /api/mobile/almacen/stock` |
| `fetchLiquidacion(ym)` | `fetchAlmacenLiquidacion(ym)` | `GET /api/mobile/almacen/liquidacion?ym=YYYY-MM` |
| `fetchInstalaciones(ym)` | `fetchAlmacenInstalaciones(ym)` | `GET /api/mobile/almacen/instalaciones?ym=YYYY-MM` |
| `fetchCuadrillasMapa()` | `fetchAlmacenCuadrillasMapa()` | `GET /api/mobile/tecnico/cuadrillas-mapa` con header `X-Mobile-Role: ALMACEN` |

`AppContainer` agrega `almacenRepository` a la interfaz e implementacion, usando el mismo `RedesApiClient` autenticado con `AuthTokenInterceptor`.

## Contrato Backend

Los endpoints REDES de almacen autorizan acceso si el contexto mobile tiene rol `ALMACEN`, rol `ADMIN`, area `ALMACEN` o area `INSTALACIONES`.

- `stock/route.ts` lee cuadrillas habilitadas y equipos por cuadrilla para devolver `{ ok, cuadrillas }`.
- `liquidacion/route.ts` valida `ym`, consulta instalaciones finalizadas del mes y devuelve `{ ok, ym, items, kpi }`.
- `instalaciones/route.ts` acepta `ymd` o `ym`, consulta `instalaciones`, deriva SNs desde `equiposInstalados` con fallback a campos directos y devuelve `{ ok, ym, items }`.

Los DTOs Android parsean manualmente con `opt*`, por lo que campos faltantes degradan a vacios, ceros o listas vacias sin error visible.

## Diagrama

```mermaid
flowchart TD
  Role[Rol seleccionado ALMACEN] --> Nav[AppNavHost home]
  Nav --> Shell[AlmacenShellScreen]
  Shell --> VM[AlmacenViewModel]
  VM --> Repo[RemoteAlmacenRepository]
  Repo --> Client[RedesApiClient]
  Client --> Stock[/api/mobile/almacen/stock]
  Client --> Liq[/api/mobile/almacen/liquidacion]
  Client --> Inst[/api/mobile/almacen/instalaciones]
  Client --> Mapa[/api/mobile/tecnico/cuadrillas-mapa]
  Stock --> Dto[AlmacenDtos]
  Liq --> Dto
  Inst --> Dto
  Mapa --> Cuad[CuadrillaMapa tecnico]
  Dto --> State[AlmacenUiState]
  Cuad --> State
```

## Hallazgos Y Riesgos

- `ALMACEN_CUADRILLAS_MAPA` reutiliza `/api/mobile/tecnico/cuadrillas-mapa`, igual que otros roles que consumen `CuadrillaMapa`.
- `AlmacenDtos.kt` usa `opt*`; un cambio de contrato backend puede verse como datos vacios sin error.
- `AlmacenViewModel.refreshAll()` puede apagar `isRefreshing` antes de completar la coroutine real del tab.
- El shell de almacen muestra mapa con ubicacion propia si el permiso ya existe, pero no dispara solicitud de permiso como tecnico/supervisor.
- Textos fuente siguen mostrando mojibake en comentarios y labels, pendiente visual transversal.

## Pendientes

- Validar en dispositivo real el shell `ALMACEN`, especialmente mapa, permisos y estados de carga.
- Crear fixtures JSON para `stock`, `liquidacion` e `instalaciones`.
- Decidir si el mapa de cuadrillas debe tener endpoint propio de almacen o seguir reutilizando el tecnico.
- Revisar sincronizacion exacta de `isRefreshing` en `AlmacenViewModel`.
- Confirmar con Arturo si el segundo tab queda cerrado como `LIQUIDACION` o si la definicion funcional cambio.

## Siguiente Unidad Recomendada

Pantallas por rol y UI comun/tema, incorporando el nuevo shell de almacen al recorrido visual completo.
