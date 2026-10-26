# Televisores-moto

Página web en modo oscuro para subir archivos de audio y video (**.mp3, .mp4, .ogg, .wav**). Al subir un archivo, la página genera un **enlace directo** al archivo, listo para copiar, abrir o reproducir.

## Cómo usar

Requiere [Node.js](https://nodejs.org/) 18 o superior.

```bash
npm install
npm start
```

Luego abre <http://localhost:3000> en el navegador.

- Arrastra los archivos a la zona de subida o haz clic para elegirlos (se pueden subir varios a la vez).
- Cada archivo muestra una barra de progreso y, al terminar, su enlace directo con botones **Copiar** y **Abrir**, además de un reproductor.
- Los enlaces tienen la forma `http://tu-servidor/files/nombre-del-archivo-a1b2c3d4.mp4` y admiten reproducción por rangos (se puede adelantar el video/audio).

## Configuración

| Variable      | Por defecto | Descripción                          |
|---------------|-------------|--------------------------------------|
| `PORT`        | `3000`      | Puerto del servidor                  |
| `MAX_SIZE_MB` | `500`       | Tamaño máximo por archivo (en MB)    |

Los archivos se guardan en la carpeta `uploads/`.

> Para que los enlaces funcionen fuera de tu computadora, el servidor debe estar publicado en internet (por ejemplo, en un VPS, Render, Railway, etc.).
