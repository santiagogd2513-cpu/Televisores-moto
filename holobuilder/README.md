# HoloBuilder

Mod de Minecraft **1.20.1** (Forge **47.4.5**) que añade un menú simple (tecla **C**) para construir y deshacer hologramas de **Forgematica**.

## Características

- Tecla **C** → abre el menú
- **Construir holograma**: intenta colocar los bloques del placement seleccionado de Forgematica
- **Deshacer holograma construido**: revierte los bloques colocados por el último build
- **Cerrar menú**
- Funciona en **supervivencia** consumiendo ítems del inventario del jugador
- En creativo no consume ítems

## Dependencias

- Minecraft 1.20.1
- Forge 47.4.5 (o compatible)
- **Forgematica** + **MaFgLib** (recomendado / necesario para leer el holograma real)

## Cómo usar

1. Instala Forgematica + MaFgLib + este mod.
2. Carga un schematic y crea/selecciona un placement en Forgematica.
3. Pulsa **C** y elige **Construir holograma**.
4. Si te equivocas, vuelve a pulsar **C** y elige **Deshacer**.

## Estado actual

- Estructura del mod, menú, keybind, consumo de inventario y sistema de deshacer: **listos**.
- Lectura real del placement de Forgematica: **pendiente de bridge por reflection** (próximo paso).

## Build

```bash
cd holobuilder
./gradlew build
```

El jar sale en `holobuilder/build/libs/`.

## GitHub Actions

Hay un workflow que compila el mod al hacer push a esta rama.
