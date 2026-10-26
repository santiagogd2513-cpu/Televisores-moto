const express = require('express');
const multer = require('multer');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

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
    url: `${req.protocol}://${req.get('host')}/files/${encodeURIComponent(name)}`,
  };
}

app.use(express.static(path.join(__dirname, 'public')));

// Enlace directo a los archivos (admite Range para reproducir/adelantar)
app.use('/files', express.static(UPLOAD_DIR, {
  setHeaders: (res, filePath) => {
    const type = ALLOWED[path.extname(filePath).toLowerCase()];
    if (type) res.setHeader('Content-Type', type);
    res.setHeader('Access-Control-Allow-Origin', '*');
  },
}));

app.post('/api/upload', (req, res) => {
  upload.single('file')(req, res, (err) => {
    if (err) {
      const msg = err.code === 'LIMIT_FILE_SIZE'
        ? `El archivo supera el límite de ${MAX_SIZE_MB} MB`
        : err.message;
      return res.status(400).json({ error: msg });
    }
    if (!req.file) return res.status(400).json({ error: 'No se recibió ningún archivo' });
    res.json(fileInfo(req, req.file.filename));
  });
});

app.get('/api/files', (req, res) => {
  const files = fs.readdirSync(UPLOAD_DIR)
    .filter((name) => ALLOWED[path.extname(name).toLowerCase()])
    .map((name) => fileInfo(req, name))
    .sort((a, b) => b.uploadedAt - a.uploadedAt);
  res.json(files);
});

app.listen(PORT, () => {
  console.log(`Servidor listo en http://localhost:${PORT}`);
});
