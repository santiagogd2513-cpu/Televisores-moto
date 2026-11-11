# HoloBuilder

Mod de Minecraft **1.20.1** (Forge **47.4.5**) que añade un menú simple (tecla **C**) para construir y deshacer hologramas de **Forgematica**.

## Características

- Tecla **C** → abre el menú
- **Construir holograma**: lee el placement seleccionado de Forgematica y coloca los bloques con el `BlockState` correcto
- **Deshacer holograma construido**: revierte los bloques del último build
- **Cerrar menú**
- Funciona en **supervivencia** consumiendo ítems del inventario
- En creativo no consume ítems
- Soft-dependency de Forgematica (el mod carga aunque no esté instalado; solo avisa)

## Dependencias

- Minecraft 1.20.1
- Forge 47.4.5 (o compatible)
- **Forgematica** + **MaFgLib** (necesario para leer el holograma real)

## Cómo usar

1. Instala Forgematica + MaFgLib + este mod.
2. Carga un schematic y crea/selecciona un placement en Forgematica (`M` → Schematic Placements).
3. Pulsa **C** y elige **Construir holograma**.
4. Si te equivocas, **C** → **Deshacer**.

## Bridge con Forgematica

Se usa reflection sobre:

- `fi.dy.masa.litematica.data.DataManager`
- `SchematicPlacementManager.getSelectedSchematicPlacement()`
- Contenedores de regiones del schematic para obtener cada `BlockState`

Así se respetan orientaciones, half slabs, stairs, etc. en la medida en que el container de Litematica los expone.

**Nota:** placements con rotación/espejo complejos pueden necesitar ajuste fino del offset de coordenadas. La primera versión prioriza placements sin rotar.

## Limitaciones actuales

- `setBlock` es client-side: en singleplayer / mundo integrado funciona; en multiplayer dedicado puede no persistir sin lógica de servidor o paquetes.
- Schematics enormes (>200k bloques por región) se saltan por seguridad.
- Falta el `gradlew` del MDK de Forge para que GitHub Actions compile el jar automáticamente.

## Build local

Necesitas el wrapper de Gradle del Forge MDK 1.20.1-47.4.5 dentro de `holobuilder/`, luego:

```bash
cd holobuilder
./gradlew build
```

El jar sale en `holobuilder/build/libs/`.
