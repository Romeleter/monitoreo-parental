# Monitoreo Parental

Aplicación Android para que los padres de familia monitoreen y controlen el consumo de videos cortos
(TikTok, Instagram y YouTube) de niños de educación primaria en Santa Marta.

Proyecto Integrador II — Ingeniería de Software.

## Tecnologías

- Kotlin + Jetpack Compose (interfaz)
- Room / SQLite (datos locales, solo en el dispositivo)
- UsageStatsManager (tiempo de uso de cada app)
- WorkManager (actualización cada 15 minutos)
- Servicio en primer plano + ventana superpuesta (aviso de pausa)

## Módulos

| Carpeta | Módulo | Historias de usuario |
|---|---|---|
| `configuracion/` | Configuración parental: vinculación, PIN y límites | HU-02, HU-05 |
| `deteccion/` | Detección de uso | HU-01 |
| `pausas/` | Aviso de pausa y bloqueo manual | HU-03, HU-06, HU-07 |
| `reportes/` | Resumen del día e historial | HU-01, HU-04 |
| `data/` | Base de datos (clases del diagrama de clases) | — |

## Cómo ejecutarla

1. Abrir esta carpeta en Android Studio.
2. Esperar a que termine "Gradle sync".
3. Elegir un emulador o un celular Android conectado y pulsar ▶ Run.

Pruebas unitarias: clic derecho sobre `app/src/test` → Run Tests.
