const express = require('express');
const multer = require('multer');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const fileStore = require('./storage');

const PORT = process.env.PORT || 3000;
const UPLOAD_DIR = path.join(__dirname, 'uploads');
const MAX_SIZE_MB = Number(process.env.MAX_SIZE_MB) || 500;

// Extensiones permitidas y su tipo MIME
const ALLOWED = {
  '.mp3': 'audio/mpeg',
  '.mp4': 'video/mp4',
  '.ogg': 'audio/ogg',
  '.wav': 'audio/wav',
};

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const app = express();
app.set('trust proxy', true);

// Limpia el nombre original para usarlo en la URL
function slugify(name) {
  return name
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[^a-zA-Z0-9-_]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 60)
    .toLowerCase() || 'archivo';
}

const storage = multer.diskStorage({
  destination: UPLOAD_DIR,
  filename: (req, file, cb) => {
    // multer entrega el nombre en latin1; lo pasamos a UTF-8 para conservar acentos
    file.originalname = Buffer.from(file.originalname, 'latin1').toString('utf8');
    const ext = path.extname(file.originalname).toLowerCase();
    const base = slugify(path.basename(file.originalname, ext));
    const id = crypto.randomBytes(4).toString('hex');
    cb(null, `${base}-${id}${ext}`);
  },
});

const upload = multer({
  storage,
  limits: { fileSize: MAX_SIZE_MB * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    if (ALLOWED[ext]) return cb(null, true);
    cb(new Error(`Formato no permitido: ${ext || 'sin extensión'}. Solo .mp3, .mp4, .ogg y .wav`));
  },
});

function fileInfo(req, name) {
  const stat = fs.statSync(path.join(UPLOAD_DIR, name));
  return {
    name,
    size: stat.size,
    type: ALLOWED[path.extname(name).toLowerCase()],
    uploadedAt: stat.mtime,
    url: localUrl(req, name),
  };
}

function localUrl(req, name) {
  return `${req.protocol}://${req.get('host')}/files/${encodeURIComponent(name)}`;
}

app.use(express.static(path.join(__dirname, 'public')));

// Enlace directo a los archivos (admite Range para reproducir/adelantar)
app.use('/files', express.static(UPLOAD_DIR, {
  setHeaders: (res, filePath) => {
    const type = ALLOWED[path.extname(filePath).toLowerCase()];
    if (type) res.setHeader('Content-Type', type);
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Expose-Headers', 'Content-Length');
    // Evita que proxies/CDN recompriman o modifiquen el archivo: se entrega byte a byte
    res.setHeader('Cache-Control', 'public, max-age=31536000, no-transform');
  },
}));

app.post('/api/upload', (req, res) => {
  upload.single('file')(req, res, async (err) => {
    if (err) {
      const msg = err.code === 'LIMIT_FILE_SIZE'
        ? `El archivo supera el límite de ${MAX_SIZE_MB} MB`
        : err.message;
      return res.status(400).json({ error: msg });
    }
    if (!req.file) return res.status(400).json({ error: 'No se recibió ningún archivo' });

    const name = req.file.filename;
    const type = ALLOWED[path.extname(name).toLowerCase()];
    const expected = Number(req.body.size);

    // El archivo recibido debe pesar exactamente lo mismo que el original
    const received = req.file.size;
    if (Number.isFinite(expected) && received !== expected) {
      fs.rmSync(req.file.path, { force: true });
      return res.status(400).json({ error: `El archivo recibido (${received} bytes) no pesa lo mismo que el original (${expected} bytes). Vuelve a subirlo.` });
    }

    let saved;
    try {
      saved = await fileStore.save(req.file.path, name, type);
    } catch (e) {
      console.error('Error al guardar en el almacenamiento:', e);
      return res.status(500).json({ error: 'No se pudo guardar el archivo en el almacenamiento. Vuelve a intentarlo.' });
    }
    if (Number.isFinite(expected) && saved !== expected) {
      return res.status(500).json({ error: `El archivo guardado (${saved} bytes) no pesa lo mismo que el original (${expected} bytes). Vuelve a subirlo.` });
    }

    res.json({
      name,
      size: saved,
      type,
      uploadedAt: new Date(),
      url: fileStore.enabled ? fileStore.publicUrl(name) : localUrl(req, name),
      permanent: fileStore.enabled,
    });
  });
});

app.get('/api/files', async (req, res) => {
  try {
    const files = fileStore.enabled
      ? await fileStore.list(ALLOWED)
      : fs.readdirSync(UPLOAD_DIR)
        .filter((name) => ALLOWED[path.extname(name).toLowerCase()])
        .map((name) => fileInfo(req, name));
    files.forEach((f) => { f.permanent = fileStore.enabled; });
    res.json(files.sort((a, b) => new Date(b.uploadedAt) - new Date(a.uploadedAt)));
  } catch (e) {
    console.error('Error al listar archivos:', e);
    res.status(500).json({ error: 'No se pudo cargar la lista de archivos' });
  }
});

app.listen(PORT, () => {
  console.log(`Servidor listo en http://localhost:${PORT}`);
  console.log(fileStore.enabled
    ? 'Almacenamiento: bucket externo (enlaces permanentes)'
    : 'Almacenamiento: disco local (en Render gratis los archivos se borran al reiniciar)');
});
