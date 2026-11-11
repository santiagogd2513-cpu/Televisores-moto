# Cómo compilar HoloBuilder

## Opción rápida (recomendada)

1. Descarga el **Forge MDK** de 1.20.1 (cualquier 47.x cercano):
   https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html

2. Extrae el MDK en una carpeta temporal.

3. Copia estos archivos del MDK a `holobuilder/`:
   - `gradlew`
   - `gradlew.bat`
   - `gradle/wrapper/gradle-wrapper.jar`

   (El `gradle-wrapper.properties` ya está en el repo con Gradle 8.1.1.)

4. En la carpeta `holobuilder`:

```bash
chmod +x gradlew
./gradlew build
```

El jar sale en:

```
holobuilder/build/libs/holobuilder-1.0.0.jar
```

## Si ya tienes Gradle instalado (8.1+)

```bash
cd holobuilder
gradle wrapper --gradle-version 8.1.1
./gradlew build
```

Eso genera `gradlew`, `gradlew.bat` y `gradle-wrapper.jar`.

## Requisitos

- **Java 17**
- Internet (ForgeGradle descarga Minecraft y dependencias la primera vez)

## GitHub Actions

Cuando existan `gradlew` + `gradle-wrapper.jar` en el repo, el workflow
`.github/workflows/holobuilder-build.yml` compilará solo al hacer push
a `ram-televisores` o `main`.

## Dependencias en juego

Para usar el mod necesitas también:

- Forgematica (1.20.1)
- MaFgLib (1.20.1)
