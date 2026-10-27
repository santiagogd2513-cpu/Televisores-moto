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

## Publicar en Render (gratis)

1. Entra en <https://dashboard.render.com> e inicia sesión con tu cuenta de GitHub.
2. Pulsa **New → Blueprint**, elige el repositorio **Televisores-moto** y pulsa **Apply**. Render lee `render.yaml` y crea el servicio solo.
3. Cuando termine, tu página estará en `https://televisores-moto.onrender.com` (o una dirección parecida que muestra Render).

En el plan gratuito, el servidor se duerme tras unos minutos sin visitas (la primera carga tarda ~1 minuto) y su disco se borra al reiniciarse. Para que los enlaces **nunca expiren**, configura el almacenamiento permanente (siguiente sección).

## Enlaces permanentes con Backblaze B2 (gratis, 10 GB)

Si se configuran estas variables, los archivos se guardan en un bucket externo y el enlace apunta directo a él: no cambia nunca y funciona aunque el servidor se duerma o se reinicie. Sin ellas, los archivos se guardan en el disco del servidor.

1. Crea una cuenta gratis en <https://www.backblaze.com/sign-up/cloud-storage> (no pide tarjeta). Si te pide verificar tu correo, hazlo: es necesario para crear buckets públicos.
2. **Buckets → Create a Bucket**: ponle un nombre único (por ejemplo `televisores-moto-archivos`), elige **Public** y créalo. Anota el **Endpoint** que aparece en el bucket (por ejemplo `s3.us-east-005.backblazeb2.com`).
3. **Application Keys → Add a New Application Key**: dale acceso **Read and Write** solo a ese bucket. Anota el **keyID** y el **applicationKey** (este último solo se muestra una vez).
4. En Render, abre tu servicio → **Environment** → añade:

| Variable               | Valor                                            |
|------------------------|--------------------------------------------------|
| `S3_ENDPOINT`          | `https://` + el Endpoint (p. ej. `https://s3.us-east-005.backblazeb2.com`) |
| `S3_BUCKET`            | el nombre del bucket                             |
| `S3_ACCESS_KEY_ID`     | el keyID                                         |
| `S3_SECRET_ACCESS_KEY` | el applicationKey                                |

5. Guarda (**Save, rebuild, and deploy**). En los logs debe aparecer `Almacenamiento: bucket externo (enlaces permanentes)`.

Opcional: `S3_REGION` (se deduce del endpoint) y `S3_PUBLIC_URL` si quieres que los enlaces usen otra dirección pública (por defecto `S3_ENDPOINT/S3_BUCKET`). También funciona con Cloudflare R2 u otro servicio compatible con S3.

> Para que los enlaces funcionen fuera de tu computadora, el servidor debe estar publicado en internet (por ejemplo, en un VPS, Render, Railway, etc.).
