# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

- **Primary: parents of primary-school children (about 5–12 years old) in Santa Marta, Colombia.** They want to know how much time their child spends on short-form video and to set limits on it. Many have little technical knowledge: per the CRC 2024 report cited in the project, only 13 % of Colombian children say their parents use parental-control tools. They use the app on the **child's phone**, in a PIN-protected "modo padre".
- **Secondary: the child.** The child sees only one screen, the full-screen pause notice. It must be understandable to a primary-school reader, with simple language and visual support.
- **Evaluator: the course professor** (Proyecto Integrador II, Ingeniería de Software). The app is presented as an academic demo.

## Product Purpose

An Android app that lets parents **monitor and limit their child's use of short-form video** (TikTok, Instagram, YouTube) and **interrupts automatic consumption** with a conscious pause when a daily limit is reached. It exists because research the project cites links short-video use to attention problems in children (Chiencharoenthanakij et al. 2025; Nguyen et al. 2025; Gong & Tao 2024), and parents in Colombia have few accessible tools focused on this specific format.

Success, for this semester: a working demo that covers the four modules in the project document (configuración parental, detección de uso, pausas de uso, reportes) and that the professor can follow on an emulator or a pilot phone.

## Positioning

- Focused **only on short-form video apps**, not general screen time.
- The pause is **conscious, not just a block**. The child must wait 5–10 s and press "Entendido". The parent sees when it was acknowledged and how many times it appeared.
- **Everything stays on the child's device.** There is no account, server, or second parent app.

## Operating Context

- **Single device.** The parent sets everything up on the child's phone. Setup is a 4-step flow: datos del niño, límites, permisos, éxito.
- **Two Android special permissions**, granted manually in system settings: "Acceso a datos de uso" (UsageStatsManager) and "Mostrar sobre otras apps" (overlay).
- **Monitoring:**
  - A foreground service checks the app on screen every 2 s.
  - The parent panel refreshes every 15 min (WorkManager) and when it is opened.
  - Usage counts from midnight and includes time used before the app was installed.
- **Parent screens:** Resumen de hoy, Límites, Últimos 7 días (historial), and Bloqueo manual. All of them are behind a 4-digit PIN that is asked again every time the app returns from the background.
- **Child screen:** the pause notice, drawn over the video app. It pauses the video's audio, sends the child to the home screen after "Entendido", and appears again every time the app is reopened that day.
- **Testing:** the app is tested on the Android Studio emulator; the project schedule also plans tests on a physical pilot phone.

## Capabilities and Constraints

- **Stack:** Kotlin + Jetpack Compose (Material 3), Room/SQLite, WorkManager, foreground service with a `specialUse` type. minSdk 26, target/compile SDK 37. Android only; iOS cannot measure other apps' usage this way.
- **Monitored apps:** TikTok, Instagram, and YouTube as **whole apps**. Reels and Shorts cannot be measured separately through UsageStatsManager.
- **Limits:** set per app, 1–300 minutes per day. Monitoring can be paused per app without losing the limit.
- **Manual block:** immediate, per app, toggled from the parent panel.
- **Records:**
  - daily minutes per app, and the limit in force that day;
  - pause notices: when shown, acknowledged, and closed;
  - time with the notice on screen is subtracted from usage.
- **Reports:**
  - today's summary;
  - a 7-day bar chart per app with the limit line;
  - days that reached the limit;
  - weekday vs. weekend averages and the share of days at the limit.
- **Known limitations, out of scope for the MVP:** the child could uninstall the app, revoke its permissions, or force-stop it. Closing these gaps needs device-admin or enterprise APIs.
- **Profile:** one child per device. Age is stored and displayed but does not change behavior; using it for suggested limits was considered and deliberately deferred.
- **Undecided:** a "tiempo extra solo por hoy" feature was discussed but not approved.

## Brand Commitments

- **Name:** "Monitoreo Parental" (final).
- **Language:** Spanish (Colombia).
- **Voice:**
  - Address the **parent with "usted"**, in a formal register. The current UI still uses "tú" and needs to be updated.
  - Address the **child with "tú"**: short, warm, simple sentences, as in "¡Hora de una pausa!".
- **Terminology:** requirements and stories are traced with the project document's IDs (HU-01…07, RF-01…30, CU-01…05). Code comments reference them.

## Evidence on Hand

- **Project document:** `D:\Cosas U\Proyecto Integrador\Act 4\Proyecto Integrador.pdf`. It contains the problem tree, user stories, prioritized requirements, use cases, UML activity and class diagrams, the original screen mockups (pantallas 1–6), and the bibliography.
- **National data:** the CRC (2024) "Infancia y Medios Audiovisuales" figures, cited in the document.
- **What is not available:** no user testing, testimonials, real-family data, or usage metrics exist. Do not fabricate any of them, and do not invent recommended minutes per age; the cited sources give none.

## Product Principles

1. **Clear to a non-technical parent.** Use plain words, no jargon, and show states with icons and labels, never color alone.
2. **Interrupt, don't punish.** The child-facing notice is calm, brief, and kind; its job is a moment of awareness.
3. **Honest numbers.** Show real usage, even above the limit, and never hide or round away data that the parent needs.
4. **Parent stays in control.** Every restriction is configurable and reversible from the PIN-protected parent mode.
5. **Private by design.** Data never leaves the device.

## Accessibility & Inclusion

- **The child's notice:** readable by early readers, with large type, a single action, and a visual cue.
- **Parent screens:** must stay usable with Android's large font sizes and must not rely on color alone for status.
- **Standard:** no formal standard (such as WCAG level) has been set.
