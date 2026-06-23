# Contexto Tecnico - REDES-MOBILE

Estado: mantenimiento incremental activo. Fase 0 completada el 2026-06-13; ultimas unidades profundas: tracking/presencia, supervisor shell/alertas, coordinador shell/repositorio, force update, tecnico alertas/cierre de ruta, modelos/DTOs contra backend, repositorios/manejo de resultados y almacen shell/repositorio.

Esta carpeta contiene documentacion viva de REDES-MOBILE para orientar trabajo tecnico en la app Android. En esta fase solo se hizo un mapa superficial: estructura top-level, manifests Gradle/Android y nombres visibles de paquetes, pantallas, repositorios y clientes.

## Alcance De Fase 0

Fuentes revisadas:

- `C:\Proyectos\REDES-MOBILE\settings.gradle.kts`
- `C:\Proyectos\REDES-MOBILE\build.gradle.kts`
- `C:\Proyectos\REDES-MOBILE\app\build.gradle.kts`
- `C:\Proyectos\REDES-MOBILE\app\src\main\AndroidManifest.xml`
- estructura visible bajo `app\src\main\java\com\redes\app`

No se hizo deep dive de clases, metodos ni pantallas.

## Mapa Rapido

REDES-MOBILE es una app Android:

- Kotlin + Jetpack Compose.
- Firebase Auth, Firestore y Storage.
- OkHttp para API backend.
- DataStore Preferences.
- Google Play Services Location y Maps Compose.
- Servicio foreground de ubicacion: `data\tracking\LocationTrackingService`.
- Navegacion Compose bajo `ui\navigation`.
- Paquetes por rol: tecnico, supervisor, coordinador y almacen.

## Documentos De Contexto

- [INDEX.md](INDEX.md): mapa de unidades detectadas y estado documental.
- [PENDIENTES.md](PENDIENTES.md): backlog inicial de unidades por documentar.
- [CHANGELOG-CONTEXTO.md](CHANGELOG-CONTEXTO.md): cambios de contexto.
- [architecture/overview.md](architecture/overview.md): vision inicial de arquitectura.
- [architecture/diagrams.md](architecture/diagrams.md): diagramas Mermaid iniciales.
- [indexes/source-index.json](indexes/source-index.json): indice JSON incremental de unidades documentadas y pendientes.
- [android/force-update.md](android/force-update.md): deep dive del gate de actualizacion obligatoria, Firestore `app_config/android`, bloqueo UI y cruce con bootstrap.
- [android/coordinador-shell-repositorio.md](android/coordinador-shell-repositorio.md): deep dive de coordinador shell, ViewModel, repositorio, endpoints y DTOs.
- [android/tecnico-alertas-cierre-ruta.md](android/tecnico-alertas-cierre-ruta.md): deep dive de tecnico alertas, notificaciones, cierre de ruta, tracking y endpoints backend.
- [android/models.md](android/models.md): cruce de modelos/DTOs Android con respuestas de endpoints mobile REDES.
- [android/repositories.md](android/repositories.md): mapa de repositorios Android, DI, patrones `Result`, silencios y pendientes de manejo de errores.
- [android/almacen-shell-repositorio.md](android/almacen-shell-repositorio.md): revision incremental del shell `ALMACEN`, ViewModel, repositorio, DTOs y endpoints REDES asociados.

## Regla De Mantenimiento

RedesContext puede modificar Markdown, Mermaid e indices JSON dentro de `docs/contexto`. No debe modificar codigo fuente, Gradle, configs, credenciales, keystores, binarios ni outputs.

## Siguiente Unidad Recomendada

Siguiente unidad para deep dive: **Pantallas por rol y UI comun/tema**. Tracking/presencia, supervisor shell/alertas, coordinador shell/repositorio, force update, tecnico alertas/cierre de ruta, modelos/DTOs, repositorios y almacen ya quedaron documentados como unidades sensibles, pero requieren validacion en dispositivo real y decisiones de contrato.
