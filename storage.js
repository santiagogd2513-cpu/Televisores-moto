const fs = require('fs');
const path = require('path');
const { S3Client, HeadObjectCommand, ListObjectsV2Command } = require('@aws-sdk/client-s3');
const { Upload } = require('@aws-sdk/lib-storage');

// Almacenamiento permanente en un bucket compatible con S3 (Backblaze B2, Cloudflare R2, etc.).
// Si no hay bucket configurado, los archivos se quedan en el disco del servidor.
const {
  S3_ENDPOINT,
  S3_REGION,
  S3_BUCKET,
  S3_ACCESS_KEY_ID,
  S3_SECRET_ACCESS_KEY,
  S3_PUBLIC_URL,
} = process.env;

const enabled = Boolean(S3_ENDPOINT && S3_BUCKET && S3_ACCESS_KEY_ID && S3_SECRET_ACCESS_KEY);

const client = enabled && new S3Client({
  endpoint: S3_ENDPOINT,
  region: S3_REGION || S3_ENDPOINT.match(/s3\.([^.]+)\./)?.[1] || 'auto',
  credentials: { accessKeyId: S3_ACCESS_KEY_ID, secretAccessKey: S3_SECRET_ACCESS_KEY },
  forcePathStyle: true,
  // Backblaze B2 no admite las sumas de verificación que el SDK añade por defecto
  requestChecksumCalculation: 'WHEN_REQUIRED',
  responseChecksumValidation: 'WHEN_REQUIRED',
});

// Dirección pública del bucket, p. ej. https://s3.us-east-005.backblazeb2.com/mi-bucket
const publicBase = (S3_PUBLIC_URL || (enabled && `${S3_ENDPOINT}/${S3_BUCKET}`) || '').replace(/\/+$/, '');

function publicUrl(name) {
  return `${publicBase}/${encodeURIComponent(name)}`;
}

// Sube el archivo al bucket, borra la copia temporal y devuelve el peso guardado en el bucket
async function save(localPath, name, contentType) {
  if (!enabled) return fs.statSync(localPath).size;
  try {
    await new Upload({
      client,
      params: {
        Bucket: S3_BUCKET,
        Key: name,
        Body: fs.createReadStream(localPath),
        ContentType: contentType,
        CacheControl: 'public, max-age=31536000, no-transform',
      },
    }).done();
    const head = await client.send(new HeadObjectCommand({ Bucket: S3_BUCKET, Key: name }));
    return head.ContentLength;
  } finally {
    fs.rmSync(localPath, { force: true });
  }
}

async function list(allowed) {
  const files = [];
  let token;
  do {
    const page = await client.send(new ListObjectsV2Command({ Bucket: S3_BUCKET, ContinuationToken: token }));
    for (const obj of page.Contents || []) {
      const type = allowed[path.extname(obj.Key).toLowerCase()];
      if (type) files.push({ name: obj.Key, size: obj.Size, type, uploadedAt: obj.LastModified, url: publicUrl(obj.Key) });
    }
    token = page.IsTruncated ? page.NextContinuationToken : undefined;
  } while (token);
  return files;
}

module.exports = { enabled, save, list, publicUrl };
