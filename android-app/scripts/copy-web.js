// Kopiert die PWA aus dem Repo-Root nach www/ (wird von Capacitor in die App gepackt).
const fs = require('fs');
const path = require('path');
const root = path.join(__dirname, '..', '..');
const out = path.join(__dirname, '..', 'www');
fs.rmSync(out, { recursive: true, force: true });
fs.mkdirSync(out, { recursive: true });
for (const f of ['index.html', 'icon.svg', 'manifest.webmanifest']) {
  fs.copyFileSync(path.join(root, f), path.join(out, f));
}
console.log('Web-Dateien nach www/ kopiert');
