# Monitoreo Parental

Aplicación Android para que los padres de familia monitoreen y controlen el consumo de videos cortos
(TikTok, Instagram y YouTube) de niños de educación primaria en Santa Marta.

| | |
|---|---|
| **Universidad** | Universidad de La Salle |
| **Programa** | Ingeniería de Software |
| **Materia** | Proyecto Integrador II |
| **Profesora** | Karla Yohana Sánchez Mojica |
| **Autor** | Sergio Alejandro Romero Vélez |
| **Versión** | 0.1.0 |

## Entregables

| Entregable | Descripción |
|---|---|
| 📄 [Documento del proyecto](docs/Proyecto%20Integrador.pdf) | Las tres fases: planteamiento, requisitos, diseño, implementación, documentación técnica y pruebas |
| 📘 [Manual de usuario](docs/Manual%20de%20usuario.pdf) | Instalación y uso de la aplicación, paso a paso, para el padre o la madre |
| 📱 [Descargar la aplicación (APK)](https://github.com/Romeleter/monitoreo-parental/releases/download/v0.1.0/MonitoreoParental-v0.1.0.apk) | Instalador para Android 8 o superior. También en [Releases](https://github.com/Romeleter/monitoreo-parental/releases) |
| 💻 Código fuente | Este repositorio |

## Qué hace

- Mide cuánto tiempo se usa cada aplicación de video corto en el teléfono del niño.
- Permite fijar un límite diario por aplicación, pausar su monitoreo o bloquearla al instante.
- Muestra un aviso de pausa a pantalla completa al llegar al límite.
- Presenta un resumen del día y el historial de los últimos 7 días.
- Protege la configuración con un PIN. Funciona sin internet y los datos se guardan solo en el teléfono.

## Tecnologías

- Kotlin 2.4 + Jetpack Compose con Material 3 (interfaz)
- Room / SQLite (datos locales, solo en el dispositivo)
- UsageStatsManager (tiempo de uso de cada app)
- WorkManager (actualización cada 15 minutos)
- Servicio en primer plano + ventana superpuesta (aviso de pausa)
- JUnit 4 (pruebas unitarias)
- Android 8.0 (API 26) como versión mínima

## Módulos

| Carpeta | Módulo | Historias de usuario |
|---|---|---|
| `configuracion/` | Configuración parental: vinculación, PIN y límites | HU-02, HU-05 |
| `deteccion/` | Detección de uso | HU-01 |
| `pausas/` | Aviso de pausa y bloqueo manual | HU-03, HU-06, HU-07 |
| `reportes/` | Resumen del día e historial | HU-01, HU-04 |
| `data/` | Base de datos (clases del diagrama de clases) | — |
| `ui/` | Navegación, tema y componentes comunes | — |

## Instalación en un teléfono Android

La aplicación se instala en el teléfono del niño o niña, que debe tener Android 8 o superior.

1. Desde el teléfono, abrir el enlace de descarga del
   [APK](https://github.com/Romeleter/monitoreo-parental/releases/download/v0.1.0/MonitoreoParental-v0.1.0.apk).
2. Abrir el archivo descargado. Si Android pide permiso para "instalar aplicaciones desconocidas", activarlo y volver.
3. Tocar "Instalar". Si Google Play Protect muestra una advertencia, elegir "Instalar de todas formas".
4. Abrir Monitoreo Parental y seguir el registro: datos del niño, PIN, permisos y límites.

El paso a paso completo, con capturas, está en el [manual de usuario](docs/Manual%20de%20usuario.pdf).

## Ejecución desde el código fuente

Para revisar o modificar el código:

1. Abrir esta carpeta en Android Studio.
2. Esperar a que termine "Gradle sync".
3. Conectar un teléfono Android con la depuración USB activada, o crear un emulador, y pulsar ▶ Run.

## Pruebas

El proyecto tiene 34 pruebas unitarias. Se ejecutan con clic derecho sobre `app/src/test` → Run Tests, o con:

```
gradlew testDebugUnitTest
```

Los resultados de las pruebas unitarias y de sistema están en la sección 6.3 del
[documento del proyecto](docs/Proyecto%20Integrador.pdf).
