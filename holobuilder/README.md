# HoloBuilder

Mod de Minecraft **1.20.1** (Forge **47.4.5**).
Menú con la tecla **C** para construir y deshacer hologramas de **Forgematica**.

## Características

- Tecla **C** → menú
- **Construir holograma** (lee el placement de Forgematica)
- **Deshacer holograma construido**
- **Cerrar menú**
- Supervivencia: consume ítems del inventario
- Creativo: no consume

## Dependencias en juego

- Forge 47.4.5 (1.20.1)
- Forgematica + MaFgLib

## Compilar

Lee **[SETUP.md](SETUP.md)**.

Resumen:

1. Copia `gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.jar` desde el MDK de Forge 1.20.1.
2. `./gradlew build`
3. Jar en `build/libs/`

## Estructura

```
holobuilder/
  src/main/java/.../HoloBuilder.java
  src/main/java/.../client/gui/HoloMenuScreen.java
  src/main/java/.../builder/BuildManager.java
  src/main/java/.../compat/ForgematicaBridge.java
  gradle.properties
  build.gradle
  SETUP.md
```
