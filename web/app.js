/* ==========================================================================
   杨梅树窝 (YANGMEI TREE) — 1:1 Android-Parity Interactive Web Engine
   ========================================================================== */
(function () {
  'use strict';

  const D = window.YANGMEI_DATA;
  const STORAGE_KEY = 'yangmei_tree_web_state_v2';

  // ---------------- Security: Strict HTML Escaping ----------------
  function esc(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function formatCompact(n) {
    if (n >= 10000) return (n / 10000).toFixed(1) + 'w';
    if (n >= 1000) return (n / 1000).toFixed(1) + 'k';
    return String(n);
  }

  // ---------------- SVG Icons (Material Symbols Style) ----------------
  const ICONS = {
    sparkle: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l1.9 5.3L19 10l-5.1 1.7L12 17l-1.9-5.3L5 10l5.1-1.7L12 3z"/><path d="M19 16l.8 2.2L22 19l-2.2.8L19 22l-.8-2.2L16 19l2.2-.8L19 16z"/></svg>',
    schedule: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2" stroke-linecap="round"/></svg>',
    tag: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 9h16M4 15h16M10 3L8 21M16 3l-2 18"/></svg>',
    store: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"><path d="M3 9l2-5h14l2 5M5 9v10h14V9M9 19v-6h6v6"/></svg>',
    handshake: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M11 17l-3-3-4-1 4-5 3 2 4-3 5 4-4 5-2-2"/><path d="M12 14l4 4"/></svg>',
    heart: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><path d="M12 20.5S4 15.2 4 9.5A4.5 4.5 0 0 1 12 6.8 4.5 4.5 0 0 1 20 9.5c0 5.7-8 11-8 11z"/></svg>',
    heartFilled: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>',
    eye: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M2 12s3.8-6.5 10-6.5S22 12 22 12s-3.8 6.5-10 6.5S2 12 2 12z"/><circle cx="12" cy="12" r="2.8"/></svg>',
    comment: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"><path d="M20 4H4a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2h3v3l4.5-3H20a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z"/></svg>',
    bookmark: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><path d="M6 4h12v17l-6-4-6 4V4z"/></svg>',
    bookmarkFilled: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 4h12v17l-6-4-6 4V4z"/></svg>',
    share: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="18" cy="5" r="2.5"/><circle cx="6" cy="12" r="2.5"/><circle cx="18" cy="19" r="2.5"/><path d="M8.2 10.8l7.5-4.5M8.2 13.2l7.5 4.5"/></svg>',
    search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4.2-4.2"/></svg>',
    fire: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><path d="M12 22c4.4 0 7-3 7-7 0-3.2-1.8-5.7-3.6-7.6-.4 1.6-1.4 2.6-2.4 2.9.6-3-.6-6.2-3.4-8.3 0 3.3-2.1 5.2-3.5 7C4.8 10.7 5 13 5 15c0 4 2.6 7 7 7z"/></svg>',
    arrowRight: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg>',
    arrowBack: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M11 6l-6 6 6 6"/></svg>',
    chevronRight: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l6 6-6 6"/></svg>',
    plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M12 5v14M5 12h14"/></svg>',
    home: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><path d="M3 10.5L12 3l9 7.5V20a1 1 0 0 1-1 1h-5.5v-6.5h-5V21H4a1 1 0 0 1-1-1v-9.5z"/></svg>',
    homeFilled: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 3L3 10.5V20a1 1 0 0 0 1 1h5.5v-6.5h5V21H20a1 1 0 0 0 1-1v-9.5L12 3z"/></svg>',
    explore: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><circle cx="12" cy="12" r="9"/><polygon points="15.8,8.2 13.7,13.7 8.2,15.8 10.3,10.3" stroke-linejoin="round"/></svg>',
    exploreFilled: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm2.2 12.2l-6.4 2 2-6.4 6.4-2-2 6.4z"/></svg>',
    mail: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 7l9 6 9-6"/></svg>',
    mailFilled: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 4H4a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>',
    person: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-6.5 8-6.5s8 2.5 8 6.5" stroke-linecap="round"/></svg>',
    personFilled: '<svg viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="8" r="4.2"/><path d="M4 20c0-4 3.6-6.5 8-6.5s8 2.5 8 6.5H4z"/></svg>',
    settings: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.6 1.6 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.6 1.6 0 0 0-1.8-.3 1.6 1.6 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.6 1.6 0 0 0-1-1.5 1.6 1.6 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.6 1.6 0 0 0 .3-1.8 1.6 1.6 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.6 1.6 0 0 0 1.5-1 1.6 1.6 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.6 1.6 0 0 0 1.8.3h.1a1.6 1.6 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.6 1.6 0 0 0 1 1.5 1.6 1.6 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.6 1.6 0 0 0-.3 1.8v.1a1.6 1.6 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.6 1.6 0 0 0-1.5 1z"/></svg>',
    edit: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>',
    moon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/></svg>',
    sun: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>',
    bell: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>',
    loc: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 21s-6.5-5.5-6.5-11a6.5 6.5 0 1 1 13 0c0 5.5-6.5 11-6.5 11z"/><circle cx="12" cy="10" r="2.2"/></svg>',
    mood: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M8.5 14.5s1.3 1.8 3.5 1.8 3.5-1.8 3.5-1.8" stroke-linecap="round"/><circle cx="9" cy="10" r=".8" fill="currentColor"/><circle cx="15" cy="10" r=".8" fill="currentColor"/></svg>',
    book: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H12v17H6.5A2.5 2.5 0 0 1 4 17.5v-12zM20 5.5A2.5 2.5 0 0 0 17.5 3H12v17h5.5a2.5 2.5 0 0 0 2.5-2.5v-12z"/></svg>',
    stories: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><path d="M4 19V5a2 2 0 0 1 2-2h11v16H6a2 2 0 0 0-2 2z"/><path d="M8 7h5M8 11h5"/></svg>',
    bag: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><path d="M6 8h12l1 12H5L6 8z"/><path d="M9 8V6a3 3 0 0 1 6 0v2"/></svg>',
    work: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><rect x="3" y="7" width="18" height="13" rx="2"/><path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/></svg>',
    find: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><path d="M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9l-6-6z"/><circle cx="11.5" cy="13.5" r="2.5"/><path d="M13.5 15.5L16 18"/></svg>',
    museum: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><path d="M3 10l9-6 9 6M5 10v9M10 10v9M14 10v9M19 10v9M3 19h18"/></svg>',
    palette: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><path d="M12 3a9 9 0 1 0 0 18h1.5a2.5 2.5 0 0 0 0-5h-.5a2 2 0 0 1 0-4h4a4 4 0 0 0 4-4c0-2.8-4-5-9-5z"/></svg>',
    spa: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"><path d="M12 20c5 0 8-4 8-10-4 0-7 2-8 5-1-3-4-5-8-5 0 6 3 10 8 10z"/><path d="M12 15c1.5-3 2-6 0-11-2 5-1.5 8 0 11z"/></svg>',
    info: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M12 16v-4M12 8h.01" stroke-linecap="round"/></svg>',
    refresh: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M21 12a9 9 0 1 1-2.6-6.4L21 8"/><path d="M21 3v5h-5"/></svg>',
    close: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6L6 18M6 6l12 12"/></svg>'
  };

  function catIcon(key) {
    const info = D.CATEGORIES[key];
    return ICONS[(info && info.icon) || 'sparkle'] || ICONS.sparkle;
  }

  // ---------------- Exact YangmeiTransparentLogo Processor (YangmeiBrandMark.kt) ----------------
  const logoCache = { dark: null, light: null, ready: false };

  function lerpRgb(a, b, t) {
    const ct = Math.max(0, Math.min(1, t));
    const ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
    const br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
    return [
      Math.round(ar + (br - ar) * ct),
      Math.round(ag + (bg - ag) * ct),
      Math.round(ab + (bb - ab) * ct)
    ];
  }

  function sampleIridescent(nx, ny, onDark) {
    const stops = onDark
      ? [0x818CF8, 0xC084FC, 0xF472B6, 0x22D3EE, 0x34D399, 0x818CF8]
      : [0x4F46E5, 0x9333EA, 0xDB2777, 0x0891B2, 0x0EA5E9, 0x4F46E5];
    let u = nx * 0.62 + ny * 0.58 + Math.sin((nx - ny) * Math.PI) * 0.08;
    u = Math.max(0, Math.min(0.9999, u));
    const scaled = u * (stops.length - 1);
    const idx = Math.min(stops.length - 2, Math.floor(scaled));
    return lerpRgb(stops[idx], stops[idx + 1], scaled - idx);
  }

  function buildHollowLogos() {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = function () {
      try {
        const workSize = 240;
        const c = document.createElement('canvas');
        c.width = workSize;
        c.height = workSize;
        const ctx = c.getContext('2d', { willReadFrequently: true });
        ctx.drawImage(img, 0, 0, workSize, workSize);
        const raw = ctx.getImageData(0, 0, workSize, workSize).data;

        const mask = new Uint8Array(workSize * workSize);
        let minX = workSize, minY = workSize, maxX = 0, maxY = 0;
        for (let y = 0; y < workSize; y++) {
          for (let x = 0; x < workSize; x++) {
            const p = (y * workSize + x) * 4;
            const maxC = Math.max(raw[p], raw[p + 1], raw[p + 2]);
            let a = 0;
            if (maxC >= 104) a = 255;
            else if (maxC > 50) a = Math.round(((maxC - 50) * 255) / 54);
            mask[y * workSize + x] = a;
            if (a > 36) {
              if (x < minX) minX = x;
              if (x > maxX) maxX = x;
              if (y < minY) minY = y;
              if (y > maxY) maxY = y;
            }
          }
        }
        if (minX >= maxX || minY >= maxY) {
          minX = 0; minY = 0; maxX = workSize - 1; maxY = workSize - 1;
        }
        const boxW = maxX - minX + 1;
        const boxH = maxY - minY + 1;
        const side = Math.min(workSize, Math.round(Math.max(boxW, boxH) * 1.08));
        const cx = Math.floor((minX + maxX) / 2);
        const cy = Math.floor((minY + maxY) / 2);
        const cropX = Math.max(0, Math.min(workSize - side, cx - Math.floor(side / 2)));
        const cropY = Math.max(0, Math.min(workSize - side, cy - Math.floor(side / 2)));
        const r = Math.max(2, Math.min(4, Math.floor(side * 0.016)));
        const r2 = r * r;

        function renderVariant(onDark) {
          const outC = document.createElement('canvas');
          outC.width = side;
          outC.height = side;
          const octx = outC.getContext('2d');
          const imgData = octx.createImageData(side, side);
          const d = imgData.data;
          for (let oy = 0; oy < side; oy++) {
            const sy = cropY + oy;
            const ny = oy / Math.max(1, side - 1);
            for (let ox = 0; ox < side; ox++) {
              const sx = cropX + ox;
              const centerMask = mask[sy * workSize + sx];
              if (centerMask <= 10) continue;
              let minN = 255;
              for (let dy = -r; dy <= r && minN > 0; dy++) {
                const nRow = Math.max(0, Math.min(workSize - 1, sy + dy)) * workSize;
                for (let dx = -r; dx <= r; dx++) {
                  if (dx * dx + dy * dy <= r2) {
                    const nxIdx = Math.max(0, Math.min(workSize - 1, sx + dx));
                    const v = mask[nRow + nxIdx];
                    if (v < minN) {
                      minN = v;
                      if (minN === 0) break;
                    }
                  }
                }
              }
              const rawAlpha = Math.max(0, Math.min(255, centerMask - Math.floor(minN * 0.94)));
              const alpha = rawAlpha > 14 ? Math.min(255, Math.floor(rawAlpha * 1.32)) : 0;
              if (alpha > 0) {
                const nx = ox / Math.max(1, side - 1);
                const rgb = sampleIridescent(nx, ny, onDark);
                const idx = (oy * side + ox) * 4;
                d[idx] = rgb[0];
                d[idx + 1] = rgb[1];
                d[idx + 2] = rgb[2];
                d[idx + 3] = alpha;
              }
            }
          }
          octx.putImageData(imgData, 0, 0);
          return outC.toDataURL('image/png');
        }

        logoCache.dark = renderVariant(true);
        logoCache.light = renderVariant(false);
        logoCache.ready = true;
        refreshLogoImages();
      } catch (e) {
        // Fallback SVG logo already displayed
      }
    };
    img.src = D.IMAGES.logo;
  }

  function fallbackSvgLogo(onDark) {
    const c1 = onDark ? '#818CF8' : '#4F46E5';
    const c2 = onDark ? '#F472B6' : '#DB2777';
    const c3 = onDark ? '#22D3EE' : '#0891B2';
    return 'data:image/svg+xml;utf8,' + encodeURIComponent(
      `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="${c1}"/><stop offset="50%" stop-color="${c2}"/><stop offset="100%" stop-color="${c3}"/></linearGradient></defs><circle cx="50" cy="48" r="34" fill="none" stroke="url(#g)" stroke-width="3.5"/><path d="M50 82V50M50 54L33 38M50 58L67 42" fill="none" stroke="url(#g)" stroke-width="3.5" stroke-linecap="round"/><circle cx="33" cy="36" r="6" fill="none" stroke="url(#g)" stroke-width="3"/><circle cx="67" cy="40" r="6" fill="none" stroke="url(#g)" stroke-width="3"/><circle cx="50" cy="27" r="7" fill="none" stroke="url(#g)" stroke-width="3"/></svg>`
    );
  }

  function getLogoSrc(onDark) {
    if (logoCache.ready) return onDark ? logoCache.dark : logoCache.light;
    return fallbackSvgLogo(onDark);
  }

  function refreshLogoImages() {
    document.querySelectorAll('img[data-yangmei-logo]').forEach(function (el) {
      const dark = el.getAttribute('data-yangmei-logo') === 'dark';
      el.src = getLogoSrc(dark);
    });
  }

  // ---------------- Exact Procedural AnonymousAvatar (FluidVisuals.kt) ----------------
  const avatarCache = new Map();

  function avatarHash(input) {
    let x = (input ^ 0x27D4EB2D) | 0;
    x = Math.imul(x ^ (x >>> 15), 0x85EBCA6B | 0);
    x = Math.imul(x ^ (x >>> 13), 0xC2B2AE35 | 0);
    return (x ^ (x >>> 16)) | 0;
  }

  function getAvatarDataUrl(seed) {
    if (avatarCache.has(seed)) return avatarCache.get(seed);
    const s = 120;
    const c = document.createElement('canvas');
    c.width = s;
    c.height = s;
    const ctx = c.getContext('2d');

    const h1 = Math.abs(avatarHash(seed));
    const h2 = Math.abs(avatarHash(seed * 31 + 97));
    const h3 = Math.abs(avatarHash(seed * 131 - 53));
    const pal = D.AVATAR_PALETTES[h1 % D.AVATAR_PALETTES.length];
    const motif = h2 % 10;
    const variant = h3 % 4;

    const grad = variant % 2 === 0
      ? ctx.createLinearGradient(0, 0, s, s)
      : ctx.createLinearGradient(s, 0, 0, s);
    grad.addColorStop(0, pal.bgStart);
    grad.addColorStop(1, pal.bgEnd);
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, s, s);

    const w = s, h = s;
    function circle(color, r, cx, cy) {
      ctx.beginPath();
      ctx.arc(cx, cy, r, 0, Math.PI * 2);
      ctx.fillStyle = color;
      ctx.fill();
    }

    if (motif === 0) {
      // Bauhaus Minimalist Portrait with beret
      circle(pal.primary, w * 0.28, w * 0.5, h * 0.56);
      ctx.save();
      ctx.beginPath();
      ctx.ellipse(w * 0.5, h * 0.37, w * 0.32, h * 0.21, 0, Math.PI * 1.08, Math.PI * 2.0);
      ctx.lineTo(w * 0.5, h * 0.37);
      ctx.closePath();
      ctx.fillStyle = pal.accent;
      ctx.fill();
      ctx.restore();
      circle('#FFFFFF', w * 0.05, w * 0.41, h * 0.54);
      circle('#FFFFFF', w * 0.05, w * 0.59, h * 0.54);
      ctx.beginPath();
      ctx.moveTo(w * 0.43, h * 0.67);
      ctx.lineTo(w * 0.57, h * 0.67);
      ctx.strokeStyle = pal.bgStart;
      ctx.lineWidth = w * 0.045;
      ctx.lineCap = 'round';
      ctx.stroke();
    } else if (motif === 1) {
      // Studio Cat
      ctx.fillStyle = pal.primary;
      ctx.beginPath();
      ctx.moveTo(w * 0.22, h * 0.46); ctx.lineTo(w * 0.28, h * 0.16); ctx.lineTo(w * 0.46, h * 0.34); ctx.closePath(); ctx.fill();
      ctx.beginPath();
      ctx.moveTo(w * 0.78, h * 0.46); ctx.lineTo(w * 0.72, h * 0.16); ctx.lineTo(w * 0.54, h * 0.34); ctx.closePath(); ctx.fill();
      circle(pal.primary, w * 0.30, w * 0.5, h * 0.58);
      circle('#FFFFFF', w * 0.055, w * 0.39, h * 0.54);
      circle('#FFFFFF', w * 0.055, w * 0.61, h * 0.54);
      circle(pal.accent, w * 0.045, w * 0.5, h * 0.63);
    } else if (motif === 2) {
      // Orbiting Planet
      circle(pal.primary, w * 0.26, w * 0.48, h * 0.52);
      ctx.beginPath();
      ctx.ellipse(w * 0.5, h * 0.51, w * 0.42, h * 0.13, 0, 0, Math.PI * 2);
      ctx.strokeStyle = pal.accent;
      ctx.lineWidth = w * 0.06;
      ctx.stroke();
      circle(pal.secondary, w * 0.08, w * 0.78, h * 0.24);
      circle('#FFFFFF', w * 0.04, w * 0.22, h * 0.24);
    } else if (motif === 3) {
      // Modernist Archway
      ctx.beginPath();
      ctx.moveTo(w * 0.22, h);
      ctx.lineTo(w * 0.22, h * 0.46);
      ctx.bezierCurveTo(w * 0.22, h * 0.16, w * 0.78, h * 0.16, w * 0.78, h * 0.46);
      ctx.lineTo(w * 0.78, h);
      ctx.closePath();
      ctx.fillStyle = pal.primary;
      ctx.fill();
      circle(pal.accent, w * 0.16, w * 0.5, h * 0.48);
      ctx.fillStyle = pal.secondary;
      ctx.fillRect(w * 0.38, h * 0.72, w * 0.24, h * 0.28);
    } else if (motif === 4) {
      // Huajiadi Bird
      ctx.beginPath();
      ctx.moveTo(w * 0.18, h * 0.68);
      ctx.bezierCurveTo(w * 0.22, h * 0.26, w * 0.66, h * 0.20, w * 0.78, h * 0.52);
      ctx.lineTo(w * 0.58, h * 0.82);
      ctx.closePath();
      ctx.fillStyle = pal.primary;
      ctx.fill();
      ctx.beginPath();
      ctx.moveTo(w * 0.74, h * 0.40); ctx.lineTo(w * 0.92, h * 0.47); ctx.lineTo(w * 0.74, h * 0.54); ctx.closePath();
      ctx.fillStyle = pal.accent;
      ctx.fill();
      circle('#FFFFFF', w * 0.055, w * 0.62, h * 0.43);
      circle(pal.secondary, w * 0.12, w * 0.42, h * 0.58);
    } else if (motif === 5) {
      // Ceramic Vase & Leaf
      ctx.beginPath();
      ctx.moveTo(w * 0.36, h * 0.42);
      ctx.lineTo(w * 0.64, h * 0.42);
      ctx.bezierCurveTo(w * 0.80, h * 0.60, w * 0.74, h * 0.88, w * 0.62, h * 0.90);
      ctx.lineTo(w * 0.38, h * 0.90);
      ctx.bezierCurveTo(w * 0.26, h * 0.88, w * 0.20, h * 0.60, w * 0.36, h * 0.42);
      ctx.closePath();
      ctx.fillStyle = pal.primary;
      ctx.fill();
      ctx.beginPath();
      ctx.moveTo(w * 0.5, h * 0.42); ctx.lineTo(w * 0.5, h * 0.16);
      ctx.strokeStyle = pal.secondary; ctx.lineWidth = w * 0.05; ctx.lineCap = 'round'; ctx.stroke();
      ctx.beginPath(); ctx.ellipse(w * 0.63, h * 0.22, w * 0.13, h * 0.08, 0, 0, Math.PI * 2); ctx.fillStyle = pal.accent; ctx.fill();
      ctx.beginPath(); ctx.ellipse(w * 0.36, h * 0.27, w * 0.12, h * 0.07, 0, 0, Math.PI * 2); ctx.fillStyle = pal.secondary; ctx.fill();
    } else if (motif === 6) {
      // Painter's Palette
      circle(pal.primary, w * 0.34, w * 0.5, h * 0.5);
      circle(pal.bgStart, w * 0.09, w * 0.66, h * 0.66);
      circle(pal.accent, w * 0.08, w * 0.36, h * 0.38);
      circle('#FFFFFF', w * 0.075, w * 0.58, h * 0.34);
      circle(pal.secondary, w * 0.08, w * 0.34, h * 0.62);
    } else if (motif === 7) {
      // Art Totem Mask
      ctx.beginPath();
      ctx.moveTo(w * 0.5, h * 0.14); ctx.lineTo(w * 0.84, h * 0.5);
      ctx.lineTo(w * 0.5, h * 0.86); ctx.lineTo(w * 0.16, h * 0.5); ctx.closePath();
      ctx.fillStyle = pal.primary; ctx.fill();
      circle(pal.secondary, w * 0.18, w * 0.5, h * 0.5);
      circle(pal.accent, w * 0.08, w * 0.5, h * 0.5);
      circle('#FFFFFF', w * 0.045, w * 0.5, h * 0.27);
    } else if (motif === 8) {
      // Cozy Bear
      circle(pal.secondary, w * 0.14, w * 0.28, h * 0.28);
      circle(pal.secondary, w * 0.14, w * 0.72, h * 0.28);
      circle(pal.primary, w * 0.30, w * 0.5, h * 0.56);
      circle('#FFFFFF', w * 0.05, w * 0.40, h * 0.52);
      circle('#FFFFFF', w * 0.05, w * 0.60, h * 0.52);
      ctx.globalAlpha = 0.85;
      circle(pal.accent, w * 0.06, w * 0.32, h * 0.62);
      circle(pal.accent, w * 0.06, w * 0.68, h * 0.62);
      ctx.globalAlpha = 1;
    } else {
      // Topographic Hills & Prism
      ctx.beginPath();
      ctx.moveTo(0, h * 0.64);
      ctx.bezierCurveTo(w * 0.32, h * 0.34, w * 0.68, h * 0.82, w, h * 0.50);
      ctx.lineTo(w, h); ctx.lineTo(0, h); ctx.closePath();
      ctx.fillStyle = pal.secondary; ctx.fill();
      ctx.beginPath();
      ctx.moveTo(0, h * 0.78);
      ctx.bezierCurveTo(w * 0.38, h * 0.56, w * 0.70, h * 0.94, w, h * 0.68);
      ctx.lineTo(w, h); ctx.lineTo(0, h); ctx.closePath();
      ctx.fillStyle = pal.primary; ctx.fill();
      ctx.beginPath();
      ctx.moveTo(w * 0.52, h * 0.14); ctx.lineTo(w * 0.70, h * 0.34);
      ctx.lineTo(w * 0.52, h * 0.48); ctx.lineTo(w * 0.34, h * 0.34); ctx.closePath();
      ctx.fillStyle = pal.accent; ctx.fill();
    }

    const url = c.toDataURL('image/png');
    avatarCache.set(seed, url);
    return url;
  }

  function avatarHtml(seed, sizeClass) {
    return `<img class="avatar-box ${sizeClass || 'avatar-24'}" src="${getAvatarDataUrl(seed)}" alt="匿名头像" />`;
  }

  // ---------------- Application State ----------------
  function clone(obj) {
    return JSON.parse(JSON.stringify(obj));
  }

  function defaultState() {
    return {
      persona: {
        id: 'u_me',
        nickname: '美术馆北窗光',
        avatarSeed: 1091,
        identityType: '在读学生',
        isVerified: true
      },
      darkTheme: false,
      homeTab: 'RECOMMENDED',
      msgFilter: 'ALL',
      posts: clone(D.INITIAL_POSTS),
      courses: clone(D.INITIAL_COURSES),
      experiences: clone(D.INITIAL_EXPERIENCES),
      hotTopics: clone(D.INITIAL_HOT_TOPICS),
      notifications: [],
      drafts: [],
      settings: {
        notifyComments: true,
        notifyLikes: true,
        allowAnonymousNotes: true,
        blockedUsers: [],
        blockedKeywords: []
      }
    };
  }

  function loadState() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return defaultState();
      const parsed = JSON.parse(raw);
      return Object.assign(defaultState(), parsed);
    } catch (e) {
      return defaultState();
    }
  }

  let state = loadState();
  function saveState() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch (e) { /* ignore quota */ }
  }

  // Runtime Navigation & Transient UI State
  const nav = {
    activeTab: 'home', // home | discover | messages | profile
    stack: [],         // [{ name, params }]
    expandedPosts: new Set(),
    courseFilters: { query: '', campus: '全部校区', college: '全部学院', type: '全部类型' },
    expCategory: '全部',
    categorySubFilter: '全部',
    searchQuery: '',
    searchScope: 'ALL', // ALL | POSTS | COURSES | EXPERIENCES
    aiStats: { status: 'idle', queryKey: '', report: null },
    aiTimer: null,
    modal: null,       // null | { type, data }
    marqueeRaf: null,
    marqueePaused: false
  };

  function applyTheme() {
    document.documentElement.classList.toggle('dark', !!state.darkTheme);
    const metaTheme = document.querySelector('meta[name="theme-color"]');
    if (metaTheme) metaTheme.setAttribute('content', state.darkTheme ? '#07070C' : '#E8ECF2');
  }

  let toastTimer = null;
  function showToast(msg) {
    const el = document.getElementById('toast');
    if (!el) return;
    el.textContent = msg;
    el.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('show'), 2200);
  }

  function visiblePosts() {
    const blockedU = new Set(state.settings.blockedUsers || []);
    const blockedK = (state.settings.blockedKeywords || []).filter(Boolean);
    return state.posts.filter(p => {
      if (blockedU.has(p.author.nickname)) return false;
      const text = (p.title || '') + ' ' + (p.body || '');
      return !blockedK.some(kw => text.includes(kw));
    });
  }

  function unreadCount() {
    return state.notifications.filter(n => n.isUnread).length;
  }

  // ---------------- Reusable Card Renderers ----------------
  function renderEditorialPostCard(post) {
    const cat = D.CATEGORIES[post.category] || D.CATEGORIES.EXHIBITION;
    const expanded = nav.expandedPosts.has(post.id);
    const displayText = (post.body && post.body.trim()) ? post.body : (post.title || '');
    const isLong = displayText.length > 40 || displayText.includes('\n');
    const media = post.mediaList || [];

    let bodyHtml = '';
    const priceHtml = post.price ? `<div class="post-price">${esc(post.price)}</div>` : '';
    const excerptHtml = expanded
      ? `<div><p class="post-excerpt">${esc(displayText).replace(/\n/g, '<br>')}</p>${isLong ? `<button class="post-fold-btn" data-action="toggleFold" data-id="${esc(post.id)}">收起</button>` : ''}</div>`
      : `<div class="post-excerpt-wrap"><p class="post-excerpt clamped">${esc(displayText.replace(/\n/g, ' '))}</p>${isLong ? `<button class="post-fold-btn" data-action="toggleFold" data-id="${esc(post.id)}">展开</button>` : ''}</div>`;

    if (media.length === 1) {
      bodyHtml = `
        <div class="post-content-row">
          <div class="post-text-col">${priceHtml}${excerptHtml}</div>
          <img class="post-thumb-single" src="${esc(media[0].src)}" alt="帖子图片" loading="lazy" />
        </div>`;
    } else {
      const thumbsHtml = media.length > 1
        ? `<div class="post-thumbs-row">${media.slice(0, 3).map(m => `<img class="post-thumb-multi" src="${esc(m.src)}" alt="帖子图片" loading="lazy" />`).join('')}</div>`
        : '';
      bodyHtml = `${priceHtml}${excerptHtml}${thumbsHtml}`;
    }

    const hasMeta = (post.topicTags && post.topicTags.length) || post.location || post.moodTag;
    const metaHtml = hasMeta ? `
      <div class="post-meta-strip">
        ${post.moodTag ? `<span class="compact-pill-tag mood">${ICONS.mood}<span>${esc(post.moodTag)}</span></span>` : ''}
        ${post.location ? `<span class="compact-pill-tag loc">${ICONS.loc}<span>${esc(post.location)}</span></span>` : ''}
        ${(post.topicTags || []).map(tag => `<button class="inline-hash-tag" data-action="openTopic" data-tag="${esc(tag)}">${esc(tag)}</button>`).join('')}
      </div>` : '';

    return `
      <article class="post-card" data-open-post="${esc(post.id)}">
        <div class="post-card-body-wrap">
          <div class="post-compact-header">
            <div class="post-author-row">
              ${avatarHtml(post.author.avatarSeed, 'avatar-24')}
              <div class="post-author-meta">
                <span class="post-author-name">${esc(post.author.nickname)}</span>
                <span class="post-time">${esc(post.createdAt)}</span>
              </div>
            </div>
            <span class="cat-pill ${esc(cat.accent)}">${catIcon(post.category)}<span>${esc(cat.titleCn)}</span></span>
          </div>
          ${bodyHtml}
          ${metaHtml}
        </div>
        <div class="hairline"></div>
        <div class="post-footer">
          <div class="post-footer-left">
            <span class="footer-stat">${ICONS.eye}<span>${formatCompact(post.viewCount || 0)}</span></span>
            <button class="footer-stat ${post.isLiked ? 'liked' : ''}" data-action="likePost" data-id="${esc(post.id)}">
              ${post.isLiked ? ICONS.heartFilled : ICONS.heart}<span>${post.likeCount}</span>
            </button>
            <button class="footer-stat" data-action="openPost" data-id="${esc(post.id)}">
              ${ICONS.comment}<span>${(post.comments || []).length}</span>
            </button>
            <button class="footer-stat ${post.isBookmarked ? 'bookmarked' : ''}" data-action="bookmarkPost" data-id="${esc(post.id)}">
              ${post.isBookmarked ? ICONS.bookmarkFilled : ICONS.bookmark}<span>${post.bookmarkCount}</span>
            </button>
          </div>
          <button class="footer-share-btn" data-action="sharePost" data-id="${esc(post.id)}" aria-label="分享">${ICONS.share}</button>
        </div>
      </article>
    `;
  }

  // ---------------- Screen 1: HOME (首页) ----------------
  const HOME_TABS = [
    { key: 'RECOMMENDED', label: '推荐', icon: ICONS.sparkle },
    { key: 'LATEST', label: '最新', icon: ICONS.schedule },
    { key: 'TOPICS', label: '话题', icon: ICONS.tag },
    { key: 'MARKETPLACE', label: '闲置交换', icon: ICONS.store },
    { key: 'MUTUAL_AID', label: '校园互助', icon: ICONS.handshake },
    { key: 'RELATIONSHIP', label: '恋爱情感', icon: ICONS.heart }
  ];

  function renderHomeScreen() {
    const all = visiblePosts();
    let list = all;
    if (state.homeTab === 'RECOMMENDED') {
      list = [...all].sort((a, b) => (b.likeCount + (b.comments.length * 3)) - (a.likeCount + (a.comments.length * 3)));
    } else if (state.homeTab === 'TOPICS') {
      list = all.filter(p => p.topicTags && p.topicTags.length > 0);
    } else if (state.homeTab === 'MARKETPLACE') {
      list = all.filter(p => p.category === 'MARKETPLACE');
    } else if (state.homeTab === 'MUTUAL_AID') {
      list = all.filter(p => p.category === 'MUTUAL_AID');
    } else if (state.homeTab === 'RELATIONSHIP') {
      list = all.filter(p => p.category === 'RELATIONSHIP');
    }

    const tabsHtml = HOME_TABS.map(t => `
      <button class="aurora-tab-pill ${state.homeTab === t.key ? 'active' : ''}" data-action="homeTab" data-tab="${t.key}">
        ${t.icon}<span>${t.label}</span>
      </button>
    `).join('');

    const feedHtml = list.length
      ? list.map(renderEditorialPostCard).join('')
      : `<div class="empty-state">
          <div class="empty-state-title">当前分区暂时还没有内容</div>
          <div class="empty-state-sub">下拉可刷新最新内容，或点击右下角 + 号发布第一条帖子</div>
        </div>`;

    return `
      <div class="screen-container">
        <div class="header-block">
          <div class="home-header-row">
            <div class="brand-lockup">
              <div class="brand-logo-sm">
                <img data-yangmei-logo="${state.darkTheme ? 'dark' : 'light'}" src="${getLogoSrc(state.darkTheme)}" alt="杨梅树窝 Logo" />
              </div>
              <h1 class="grad-flow-title">杨梅树窝</h1>
            </div>
            <button class="ai-search-pill" data-action="openSearch">
              ${ICONS.sparkle}<span>AI 搜索</span>
            </button>
          </div>
          <div class="h-scroll-row" id="home-tabs-row">${tabsHtml}</div>
        </div>
        <div class="scroll-area" id="home-scroll">
          <div class="ptr-indicator" id="ptr-indicator">✦ 正在刷新树窝最新内容...</div>
          ${feedHtml}
        </div>
      </div>
    `;
  }

  // ---------------- Screen 2: DISCOVER (发现) ----------------
  function renderDiscoverScreen() {
    const ranked = [...state.hotTopics].sort((a, b) => b.discussionCount - a.discussionCount);
    // Double array for seamless leftward marquee loop
    const marqueeList = ranked.concat(ranked);

    function rankCls(r) {
      return r === 1 ? 'rank-1' : r === 2 ? 'rank-2' : r === 3 ? 'rank-3' : 'rank-other';
    }
    function heatBadge(ch) {
      if (!ch) return '';
      const cls = ch === '沸' ? 'heat-fei' : ch === '热' ? 'heat-re' : ch === '新' ? 'heat-xin' : 'heat-jian';
      return `<span class="heat-char ${cls}">${esc(ch)}</span>`;
    }

    const marqueeCards = marqueeList.map((topic, idx) => {
      const rank = (idx % ranked.length) + 1;
      const num = rank < 10 ? '0' + rank : String(rank);
      return `
        <div class="gb-card clickable topic-scroll-card" data-action="openTopic" data-tag="${esc(topic.tag)}">
          <div class="gb-card-inner fluid-aurora-light">
            <div class="topic-scroll-inner">
              <div class="topic-top-row">
                <div style="display:flex;align-items:center;gap:0.375rem;">
                  <span class="rank-badge ${rankCls(rank)}">NO.${num}</span>
                  ${heatBadge(topic.heatStatus)}
                </div>
                <span class="topic-heat-text">${formatCompact(topic.discussionCount)} 热度</span>
              </div>
              <h3 class="topic-tag-name">${esc(topic.tag)}</h3>
              <p class="topic-desc-line">${esc(topic.description)}</p>
            </div>
          </div>
        </div>
      `;
    }).join('');

    const tintStyles = [
      { bg: 'var(--tint-acid-bg)', fg: 'var(--tint-acid-fg)' },
      { bg: 'var(--tint-mint-bg)', fg: 'var(--tint-mint-fg)' },
      { bg: 'var(--tint-slate-bg)', fg: 'var(--tint-slate-fg)' },
      { bg: 'var(--tint-rose-bg)', fg: 'var(--tint-rose-fg)' }
    ];

    const tilesHtml = D.CAMPUS_LIFE_TILES.map(tile => {
      const ts = tintStyles[tile.tintIndex % 4];
      return `
        <button class="discover-tile" data-action="openDiscoverTile" data-key="${tile.key}">
          <div class="discover-tile-top">
            <span class="discover-tile-title">${esc(tile.titleCn)}</span>
            <span class="discover-tile-icon" style="background:${ts.bg};color:${ts.fg};">${ICONS[tile.icon]}</span>
          </div>
          <span class="discover-tile-desc">${esc(tile.desc)}</span>
        </button>
      `;
    }).join('');

    return `
      <div class="screen-container">
        <div class="discover-header">
          <h1 class="progressive-title">发现</h1>
          <div class="gb-card r14 clickable" data-action="openSearch">
            <div class="gb-card-inner">
              <div class="discover-search-inner">
                <span style="color:var(--ink-secondary);display:flex;width:1.125rem;height:1.125rem;">${ICONS.search}</span>
                <span class="discover-search-placeholder">AI 搜索选课、经验、闲置或提问...</span>
                <span class="ai-badge-solid">${ICONS.sparkle}<span>AI搜索</span></span>
              </div>
            </div>
          </div>
        </div>
        <div class="scroll-area">
          <div class="section-header-row" data-action="openHotTopics" style="cursor:pointer;">
            <div class="section-title-group">
              <span style="color:var(--vermilion);display:flex;width:1.25rem;height:1.25rem;">${ICONS.fire}</span>
              <h2 class="section-title">热门话题榜</h2>
            </div>
            <span class="section-link">完整热搜榜 <span style="display:flex;width:0.875rem;height:0.875rem;">${ICONS.arrowRight}</span></span>
          </div>
          <div class="marquee-viewport" id="hot-topics-marquee">
            <div class="marquee-track">${marqueeCards}</div>
          </div>
          <div class="campus-life-head">
            <h2 class="section-title">校园生活</h2>
            <p class="campus-life-sub">选课评价、学长学姐说、二手交易、实习与互助</p>
          </div>
          <div class="discover-grid">${tilesHtml}</div>
        </div>
      </div>
    `;
  }

  // ---------------- Screen 3: MESSAGES (消息) ----------------
  const MSG_FILTERS = [
    { key: 'ALL', label: '全部' },
    { key: 'COMMENTS', label: '评论与回复' },
    { key: 'REACTIONS', label: '点赞与收藏' },
    { key: 'NOTES', label: '匿名留言' },
    { key: 'SYSTEM', label: '系统与审核' }
  ];

  function renderMessagesScreen() {
    const unread = unreadCount();
    const list = state.notifications.filter(n => {
      if (state.msgFilter === 'ALL') return true;
      return n.group === state.msgFilter;
    });

    const filterHtml = MSG_FILTERS.map(f => {
      const cnt = state.notifications.filter(n => n.isUnread && (f.key === 'ALL' || n.group === f.key)).length;
      return `
        <button class="aurora-tab-pill ${state.msgFilter === f.key ? 'active' : ''}" data-action="msgFilter" data-key="${f.key}">
          <span>${f.label}</span>
          ${cnt > 0 ? `<span class="red-badge red-badge-inline">${cnt > 99 ? '99+' : cnt}</span>` : ''}
        </button>
      `;
    }).join('');

    const contentHtml = list.length
      ? list.map(item => `
          <div class="post-card" style="padding:0.875rem 1rem;" data-action="readNotif" data-id="${esc(item.id)}">
            <div style="display:flex;gap:0.75rem;align-items:flex-start;">
              ${avatarHtml(item.senderSeed || 1013, 'avatar-40')}
              <div style="flex:1;min-width:0;">
                <div style="display:flex;justify-content:space-between;align-items:center;gap:0.5rem;">
                  <strong style="font-size:0.875rem;">${esc(item.senderName)}</strong>
                  <span style="font-size:0.75rem;color:var(--ink-muted);">${esc(item.createdAt)}</span>
                </div>
                <p style="margin:0.25rem 0 0;font-size:0.875rem;line-height:1.45;">${esc(item.summary)}</p>
              </div>
            </div>
          </div>
        `).join('')
      : `<div class="empty-state">
          <div style="width:2.125rem;height:2.125rem;color:var(--ink-muted);">${ICONS.bell}</div>
          <div class="empty-state-title" style="color:var(--ink-secondary);">暂无消息</div>
        </div>`;

    return `
      <div class="screen-container">
        <div class="header-block" style="padding-left:1.25rem;padding-right:1.25rem;padding-bottom:0.375rem;">
          <div style="display:flex;align-items:center;justify-content:space-between;padding:0.5rem 0 0.625rem;">
            <div style="display:flex;align-items:center;gap:0.5rem;">
              <h1 class="progressive-title">消息</h1>
              ${unread > 0 ? `<span class="red-badge red-badge-inline">${unread}</span>` : ''}
            </div>
            ${unread > 0 ? `<button style="font-size:0.75rem;color:var(--ink-secondary);" data-action="markAllRead">全部已读</button>` : ''}
          </div>
        </div>
        <div class="h-scroll-row" style="padding-left:1.25rem;padding-right:1.25rem;">${filterHtml}</div>
        <div class="scroll-area">${contentHtml}</div>
      </div>
    `;
  }

  // ---------------- Screen 4: PROFILE (我的) ----------------
  function renderProfileScreen() {
    const p = state.persona;
    const myPosts = state.posts.filter(x => x.isMine).length;
    const myComments = state.posts.reduce((acc, post) => acc + (post.comments || []).filter(c => c.author.id === p.id).length, 0);
    const myBookmarks = state.posts.filter(x => x.isBookmarked).length +
      state.courses.filter(x => x.isBookmarked).length +
      state.experiences.filter(x => x.isBookmarked).length;
    const myDrafts = (state.drafts || []).length;
    const myTrades = state.posts.filter(x => x.isMine && x.category === 'MARKETPLACE').length;
    const myHelp = state.posts.filter(x => x.isMine && (x.category === 'LOST_FOUND' || x.category === 'MUTUAL_AID')).length;

    const menuItems = [
      { key: 'MY_POSTS', title: '我的帖子', val: String(myPosts) },
      { key: 'MY_COMMENTS', title: '我的评论', val: String(myComments) },
      { key: 'MY_BOOKMARKS', title: '我的收藏', val: String(myBookmarks) },
      { key: 'MY_DRAFTS', title: '我的草稿', val: String(myDrafts) },
      { key: 'MY_TRADES', title: '我的交易', val: String(myTrades) },
      { key: 'MY_HELP', title: '我的求助', val: String(myHelp) },
      { key: 'VERIFY', title: '认证信息', val: p.isVerified ? '已认证' : '未认证' },
      { key: 'SETTINGS', title: '设置', val: '' }
    ];

    return `
      <div class="screen-container">
        <div class="scroll-area">
          <div class="profile-hero">
            <div class="profile-top-bar">
              <h1 class="progressive-title">我的</h1>
              <button class="icon-btn-48" data-action="openSettings" aria-label="设置">${ICONS.settings}</button>
            </div>

            <div class="profile-id-row">
              <div class="avatar-edit-trigger" data-action="openPersonaEditor">
                ${avatarHtml(p.avatarSeed, 'avatar-66')}
                <span class="avatar-edit-pen">${ICONS.edit}</span>
              </div>
              <div class="profile-id-info">
                <button class="profile-nick-btn" data-action="openPersonaEditor">
                  <span class="profile-nick-text">${esc(p.nickname)}</span>
                  <span style="width:1.0625rem;height:1.0625rem;color:var(--active-tint);display:flex;flex-shrink:0;">${ICONS.edit}</span>
                </button>
                <div class="verified-badge">
                  <span class="status-dot ${p.isVerified ? 'verified' : ''}"></span>
                  <span>${p.isVerified ? '已认证 · ' + esc(p.identityType) : '未认证'}</span>
                </div>
              </div>
            </div>

            <div class="profile-actions-row">
              <span class="profile-anon-hint">社区内仅展示匿名形象</span>
              <div class="profile-btn-group">
                <button class="btn-grad-sm" data-action="openPersonaEditor">${ICONS.edit}<span>改昵称/形象</span></button>
                <button class="btn-aurora-outline-sm" data-action="randomizePersona">随机换一套</button>
              </div>
            </div>
          </div>

          <div class="profile-section-pad">
            <div class="gb-card clickable" data-action="openVerification">
              <div class="gb-card-inner fluid-aurora-dark">
                <div class="verify-card-inner">
                  <div style="flex:1;min-width:0;">
                    <div class="verify-card-title">
                      <span class="status-dot ${p.isVerified ? 'verified' : ''}" style="width:0.4375rem;height:0.4375rem;"></span>
                      <span>校园身份认证 · ${p.isVerified ? '已认证' : '未认证'}</span>
                    </div>
                    <p class="verify-card-sub">
                      ${p.isVerified
                        ? '已通过校园核验，实名资料严格隔离不公开'
                        : '认证中央美术学院在读或校友身份，开启发帖、评论与点赞'}
                    </p>
                  </div>
                  <span style="width:1.25rem;height:1.25rem;display:flex;flex-shrink:0;">${ICONS.chevronRight}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="profile-section-pad">
            <div class="gb-card">
              <div class="gb-card-inner">
                <div class="stats-row-grid">
                  <button class="stat-col-btn" data-action="openCollection" data-key="MY_POSTS"><span class="stat-num">${myPosts}</span><span class="stat-lbl">帖子</span></button>
                  <button class="stat-col-btn" data-action="openCollection" data-key="MY_BOOKMARKS"><span class="stat-num">${myBookmarks}</span><span class="stat-lbl">收藏</span></button>
                  <button class="stat-col-btn" data-action="openCollection" data-key="MY_DRAFTS"><span class="stat-num">${myDrafts}</span><span class="stat-lbl">草稿</span></button>
                  <button class="stat-col-btn" data-action="openCollection" data-key="MY_COMMENTS"><span class="stat-num">${myComments}</span><span class="stat-lbl">评论</span></button>
                </div>
              </div>
            </div>
          </div>

          <div class="profile-section-pad">
            <div class="gb-card">
              <div class="gb-card-inner">
                <button class="menu-row" data-action="toggleDarkTheme">
                  <div style="display:flex;align-items:center;gap:0.625rem;min-width:0;">
                    <span style="width:1.25rem;height:1.25rem;color:var(--active-tint);display:flex;flex-shrink:0;">
                      ${state.darkTheme ? ICONS.moon : ICONS.sun}
                    </span>
                    <div>
                      <div class="menu-row-title">深色模式</div>
                      <div class="menu-row-sub">${state.darkTheme ? '已开启 · 开屏同款极光夜幕' : '切换为开屏同款深邃极光背景'}</div>
                    </div>
                  </div>
                  <div class="m3-switch ${state.darkTheme ? 'on' : ''}"><div class="m3-switch-thumb"></div></div>
                </button>
                <div class="hairline"></div>
                ${menuItems.map((m, i) => `
                  <button class="menu-row" data-action="profileMenu" data-key="${m.key}">
                    <span class="menu-row-title">${m.title}</span>
                    <span class="menu-row-right">
                      ${m.val ? `<span>${esc(m.val)}</span>` : ''}
                      <span style="width:1.125rem;height:1.125rem;display:flex;color:var(--ink-muted);">${ICONS.chevronRight}</span>
                    </span>
                  </button>
                  ${i < menuItems.length - 1 ? '<div class="hairline"></div>' : ''}
                `).join('')}
              </div>
            </div>
          </div>

          <div class="copyright-footer">
            <div class="copyright-footer-main">杨梅树窝 v${D.COPYRIGHT.version} · 创建者：${D.COPYRIGHT.creator}</div>
            <div class="copyright-footer-sub">${D.COPYRIGHT.line}</div>
          </div>
        </div>
      </div>
    `;
  }

  // ---------------- Sub-Screen: COURSE ARCHIVE (选课评价 — 1:1 CourseArchiveScreen.kt) ----------------
  function getCourseConsensus(c) {
    const reviews = c.reviews || [];
    if (!reviews.length) {
      return { attendance: null, exam: null, tags: [c.suitableFor || '待补充评价'] };
    }
    const countMode = (arr) => {
      const m = new Map();
      arr.filter(Boolean).forEach(x => m.set(x, (m.get(x) || 0) + 1));
      let best = arr[0] || '', max = 0;
      m.forEach((v, k) => { if (v > max) { max = v; best = k; } });
      return best;
    };
    const att = countMode(reviews.map(r => r.attendanceMode));
    const exam = countMode(reviews.map(r => r.examFormat));
    const tagSet = Array.from(new Set(reviews.map(r => r.suitableTag).filter(Boolean))).slice(0, 2);
    return { attendance: att, exam: exam, tags: tagSet.length ? tagSet : [c.suitableFor] };
  }

  function renderCourseArchiveScreen() {
    const f = nav.courseFilters;
    const campuses = ['全部校区', ...Object.keys(D.CAFA_CAMPUS_COLLEGES)];
    const types = ['全部类型', ...D.COURSE_TYPES];
    const colleges = f.campus === '全部校区'
      ? ['全部学院', ...D.CAFA_COLLEGES]
      : ['全部学院', ...(D.CAFA_CAMPUS_COLLEGES[f.campus] || [])];

    const filtered = state.courses.filter(c => {
      const q = (f.query || '').trim().toLowerCase();
      const matchQ = !q ||
        c.name.toLowerCase().includes(q) ||
        c.instructor.toLowerCase().includes(q) ||
        c.college.toLowerCase().includes(q) ||
        c.campus.toLowerCase().includes(q) ||
        (c.major || '').toLowerCase().includes(q) ||
        (c.summary || '').toLowerCase().includes(q);
      const matchCampus = f.campus === '全部校区' || c.campus === f.campus;
      const matchCollege = f.college === '全部学院' || c.college === f.college;
      const matchType = f.type === '全部类型' || c.courseType === f.type;
      return matchQ && matchCampus && matchCollege && matchType;
    });

    const groupsMap = new Map();
    D.COURSE_CATEGORIES.forEach(cat => groupsMap.set(cat, []));
    filtered.forEach(c => {
      const k = c.major || '其他方向';
      if (!groupsMap.has(k)) groupsMap.set(k, []);
      groupsMap.get(k).push(c);
    });

    const showNotice = f.type === '全部类型' || f.type === '跨院选修';

    let listHtml = '';
    groupsMap.forEach((items, catName) => {
      if (!items.length) return;
      listHtml += `
        <div class="course-group-header">
          <div class="course-group-title"><span class="course-group-dot"></span><span>${esc(catName)}</span></div>
          <span style="font-size:0.75rem;color:var(--ink-secondary);">${items.length} 门</span>
        </div>
      `;
      items.forEach(c => {
        const hasReviews = (c.reviews || []).length > 0 || c.recommendScore > 0;
        const typeCls = c.courseType === '体育选修' ? 'pe' : c.courseType === '公共选修' ? 'pub' : 'cross';
        const cons = getCourseConsensus(c);
        listHtml += `
          <div class="course-card-surface" data-action="openCourseDetail" data-id="${esc(c.id)}" style="cursor:pointer;">
            <div style="display:flex;justify-content:space-between;align-items:center;gap:0.5rem;">
              <div style="display:flex;align-items:center;gap:0.4375rem;min-width:0;flex:1;">
                <span class="course-type-badge ${typeCls}">${esc(c.courseType)}</span>
                <span class="course-meta-line" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(c.campus)} · ${esc(c.college)}</span>
              </div>
              <button class="icon-btn-48" style="width:2.25rem;height:2.25rem;color:${c.isBookmarked ? 'var(--active-tint)' : 'var(--ink-muted)'};" data-action="bookmarkCourse" data-id="${esc(c.id)}" aria-label="收藏课程">
                ${c.isBookmarked ? ICONS.bookmarkFilled : ICONS.bookmark}
              </button>
            </div>
            <div style="display:flex;flex-direction:column;gap:0.22rem;">
              <h3 class="course-name">${esc(c.name)}</h3>
              <div class="course-meta-line">授课教师：${esc(c.instructor)} · ${esc(c.major)}</div>
              ${c.summary ? `<p class="course-summary" style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">${esc(c.summary)}</p>` : ''}
            </div>
            <div class="course-metrics-bar">
              <div><div class="metric-val highlight">${hasReviews ? c.recommendScore.toFixed(1) : '--'}</div><div class="metric-lbl">综合推荐</div></div>
              <div><div class="metric-val">${hasReviews ? c.difficultyScore.toFixed(1) : '--'}</div><div class="metric-lbl">给分友好</div></div>
              <div><div class="metric-val">${hasReviews ? c.workloadScore.toFixed(1) : '--'}</div><div class="metric-lbl">作业强度</div></div>
              <div><div class="metric-val">${(c.reviews || []).length} 份</div><div class="metric-lbl">参与共建</div></div>
            </div>
            <div class="consensus-row">
              ${cons.attendance ? `<span class="consensus-badge">考勤：${esc(cons.attendance)}</span>` : ''}
              ${cons.exam ? `<span class="consensus-badge">考核：${esc(cons.exam)}</span>` : ''}
              ${cons.tags.map(t => `<span class="consensus-badge ${t === '慎选避雷' ? 'warning' : ''}">${esc(t)}</span>`).join('')}
            </div>
          </div>
        `;
      });
    });

    if (!filtered.length) {
      listHtml = `<div class="empty-state"><div class="empty-state-title">没有找到符合条件的课程</div><div class="empty-state-sub">试试切换校区/类型筛选，或点击右上角「共建课程」补充新课</div></div>`;
    }

    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack" aria-label="返回">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">选课评价</h1>
          </div>
          <button class="btn-grad-sm" style="margin-right:0.5rem;" data-action="openCreateCourse">${ICONS.plus}<span>共建课程</span></button>
        </div>
        <div class="scroll-area sub-scroll">
          <div class="filter-surface">
            <div style="font-size:0.75rem;color:var(--ink-secondary);line-height:1.45;">
              由央美本院、燕郊、上海三校区同学评分回答共同计算结果，欢迎补充新课或参与打分。
            </div>
            <input class="field-input" id="course-search-input" type="search" placeholder="搜索课程名、授课教师或学院..." value="${esc(f.query)}" />
            <div class="h-scroll-row" style="padding:0;">
              ${campuses.map(cp => `<button class="mono-chip ${f.campus === cp ? 'inverted' : ''}" data-action="courseCampus" data-val="${esc(cp)}">${esc(cp)}</button>`).join('')}
            </div>
            <div class="h-scroll-row" style="padding:0;">
              ${types.map(tp => `<button class="mono-chip ${f.type === tp ? 'inverted' : ''}" data-action="courseType" data-val="${esc(tp)}">${esc(tp)}</button>`).join('')}
            </div>
            ${showNotice ? `<div class="notice-box"><span style="width:1rem;height:1rem;color:var(--indigo);flex-shrink:0;margin-top:0.1rem;">${ICONS.info}</span><span>${esc(D.CROSS_COLLEGE_NOTICE)}</span></div>` : ''}
            <div class="college-filter-head">
              <span style="font-size:0.75rem;color:var(--ink-secondary);">按学院筛选（当前：${esc(f.college)}）</span>
              <button class="more-college-pill" data-action="openAllColleges">更多学院 (18) ⌄</button>
            </div>
            <div class="h-scroll-row" style="padding:0;">
              ${colleges.slice(0, 5).map(cl => `<button class="mono-chip ${f.college === cl ? 'inverted' : ''}" data-action="courseCollege" data-val="${esc(cl)}">${esc(cl)}</button>`).join('')}
              <button class="more-college-pill" data-action="openAllColleges">更多 ⌄</button>
            </div>
          </div>
          ${listHtml}
        </div>
      </div>
    `;
  }

  function renderCourseDetailScreen(courseId) {
    const c = state.courses.find(x => x.id === courseId);
    if (!c) return '';
    const hasReviews = (c.reviews || []).length > 0 || c.recommendScore > 0;
    const cons = getCourseConsensus(c);
    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <div style="min-width:0;flex:1;">
              <div style="font-size:0.6875rem;color:var(--ink-secondary);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(c.campus)} · ${esc(c.college)}</div>
              <h1 class="sub-topbar-title" style="font-size:1rem;">${esc(c.name)}</h1>
            </div>
          </div>
          <button class="icon-btn-48" data-action="bookmarkCourse" data-id="${esc(c.id)}" style="color:${c.isBookmarked ? 'var(--active-tint)' : 'var(--ink-secondary)'};">
            ${c.isBookmarked ? ICONS.bookmarkFilled : ICONS.bookmark}
          </button>
        </div>
        <div class="scroll-area sub-scroll" style="padding:0.875rem;">
          <div class="course-card-surface" style="margin:0 0 0.875rem;">
            <div class="h-scroll-row" style="padding:0;">
              <span class="mono-chip inverted">${esc(c.courseType)}</span>
              <span class="mono-chip">${esc(c.campus)}</span>
              <span class="mono-chip">${esc(c.college)}</span>
            </div>
            <h2 style="margin:0.125rem 0 0;font-size:1.25rem;line-height:1.3;">${esc(c.name)}</h2>
            <div style="font-size:0.8125rem;color:var(--ink-secondary);">授课教师：${esc(c.instructor)} · ${esc(c.major)}</div>
            <p style="margin:0;font-size:0.875rem;line-height:1.55;">${esc(c.summary)}</p>
            <div class="course-metrics-bar">
              <div><div class="metric-val highlight">${hasReviews ? c.recommendScore.toFixed(1) : '--'}</div><div class="metric-lbl">综合推荐</div></div>
              <div><div class="metric-val">${hasReviews ? c.difficultyScore.toFixed(1) : '--'}</div><div class="metric-lbl">给分友好</div></div>
              <div><div class="metric-val">${hasReviews ? c.workloadScore.toFixed(1) : '--'}</div><div class="metric-lbl">作业强度</div></div>
              <div><div class="metric-val">${(c.reviews || []).length} 份</div><div class="metric-lbl">参与共建</div></div>
            </div>
            <div class="consensus-row">
              ${cons.attendance ? `<span class="consensus-badge">考勤：${esc(cons.attendance)}</span>` : ''}
              ${cons.exam ? `<span class="consensus-badge">考核：${esc(cons.exam)}</span>` : ''}
              ${cons.tags.map(t => `<span class="consensus-badge ${t === '慎选避雷' ? 'warning' : ''}">${esc(t)}</span>`).join('')}
            </div>
          </div>

          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.625rem;padding:0 0.125rem;">
            <h3 style="margin:0;font-size:1rem;">同学评价 (${(c.reviews || []).length})</h3>
            <button class="btn-grad-sm" data-action="openAddReview" data-id="${esc(c.id)}">${ICONS.edit}<span>写评价</span></button>
          </div>

          ${(c.reviews || []).length ? c.reviews.map(r => `
            <div class="post-card" style="margin:0 0 0.625rem;padding:0.875rem 1rem;">
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.45rem;">
                <div style="display:flex;align-items:center;gap:0.5rem;">
                  ${avatarHtml(r.author.avatarSeed, 'avatar-24')}
                  <strong style="font-size:0.8125rem;">${esc(r.author.nickname)}</strong>
                  <span class="consensus-badge ${r.suitableTag === '慎选避雷' ? 'warning' : ''}">${esc(r.suitableTag)}</span>
                </div>
                <span style="font-size:0.75rem;color:var(--ink-muted);">${esc(r.createdAt)}</span>
              </div>
              <p style="margin:0 0 0.5rem;font-size:0.875rem;line-height:1.55;">${esc(r.content)}</p>
              <div style="font-size:0.75rem;color:var(--ink-secondary);display:flex;flex-wrap:wrap;gap:0.625rem;">
                <span>推荐 ${r.recommend}/5</span>
                <span>给分 ${r.difficulty}/5</span>
                <span>作业 ${r.workload}/5</span>
                <span>${esc(r.attendanceMode)}</span>
                <span>${esc(r.examFormat)}</span>
              </div>
            </div>
          `).join('') : `<div class="empty-state"><div class="empty-state-title">暂无同学实测评价</div><div class="empty-state-sub">如果你上过这门课，欢迎点击右上角「写评价」补充体验</div></div>`}
        </div>
      </div>
    `;
  }

  // ---------------- Sub-Screen: SENIOR EXPERIENCE (学长学姐说 — 1:1 SeniorExperienceScreen.kt) ----------------
  function renderSeniorScreen() {
    const cats = ['全部', '留学', '毕业作品', '考研', '就业', '工作室'];
    const list = state.experiences.filter(e => nav.expCategory === '全部' || e.category === nav.expCategory);
    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">学长学姐说</h1>
          </div>
        </div>
        <div class="scroll-area sub-scroll">
          <div class="editorial-hero-banner fluid-aurora-light">
            <div style="font-size:1.1875rem;font-weight:700;color:var(--ink);">把走过的路，留给后来的人。</div>
            <div style="font-size:0.75rem;color:var(--ink-secondary);">考研、留学、毕设与工作室真实问答 · 回答需标注哪一届</div>
          </div>
          <div class="h-scroll-row" style="background:var(--surface);border-bottom:0.6px solid var(--outline-soft);padding:0.75rem 1.25rem;">
            ${cats.map(cat => `<button class="mono-chip ${nav.expCategory === cat ? 'inverted' : ''}" data-action="expCat" data-val="${cat}">${cat}</button>`).join('')}
          </div>
          <div style="padding:0.75rem 0.875rem;">
            ${list.map(e => `
              <div class="course-card-surface" style="margin:0 0 0.75rem;cursor:pointer;" data-action="openExpDetail" data-id="${esc(e.id)}">
                <div style="display:flex;justify-content:space-between;align-items:center;">
                  <span class="course-type-badge pub">${esc(e.category)} · ${esc(e.year)}</span>
                  <span style="font-size:0.75rem;color:var(--ink-muted);">约 ${e.readMinutes} 分钟阅读</span>
                </div>
                <h3 style="margin:0;font-size:1rem;line-height:1.4;color:var(--ink);">${esc(e.title)}</h3>
                <p style="margin:0;font-size:0.8125rem;line-height:1.5;color:var(--ink-secondary);">${esc(e.summary)}</p>
                ${e.image ? `<img src="${esc(e.image)}" alt="经验配图" style="width:100%;height:9.5rem;object-fit:cover;border-radius:0.625rem;" loading="lazy" />` : ''}
                <div style="display:flex;justify-content:space-between;align-items:center;font-size:0.75rem;color:var(--ink-secondary);">
                  <div style="display:flex;align-items:center;gap:0.4375rem;">
                    ${avatarHtml(e.author.avatarSeed, 'avatar-24')}
                    <span>${esc(e.author.nickname)} · ${esc(e.college)}</span>
                  </div>
                  <span>♥ ${e.likeCount} · ★ ${e.bookmarkCount}</span>
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      </div>
    `;
  }

  function renderExpDetailScreen(expId) {
    const e = state.experiences.find(x => x.id === expId);
    if (!e) return '';
    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">经验详情</h1>
          </div>
          <button class="icon-btn-48" data-action="bookmarkExp" data-id="${esc(e.id)}" style="color:${e.isBookmarked ? 'var(--active-tint)' : 'var(--ink-secondary)'};">
            ${e.isBookmarked ? ICONS.bookmarkFilled : ICONS.bookmark}
          </button>
        </div>
        <div class="scroll-area sub-scroll" style="padding:1.125rem;">
          <span class="course-type-badge pub">${esc(e.category)} · ${esc(e.college)} · ${esc(e.year)}</span>
          <h2 style="margin:0.625rem 0;font-size:1.25rem;line-height:1.38;">${esc(e.title)}</h2>
          <div style="display:flex;align-items:center;gap:0.5rem;margin-bottom:0.875rem;font-size:0.8125rem;color:var(--ink-secondary);">
            ${avatarHtml(e.author.avatarSeed, 'avatar-32')}
            <span>${esc(e.author.nickname)} · ${esc(e.major)}</span>
          </div>
          ${e.image ? `<img class="detail-media-img" style="margin-bottom:0.875rem;" src="${esc(e.image)}" alt="经验图片" />` : ''}
          <div style="font-size:0.9375rem;line-height:1.72;white-space:pre-line;color:var(--ink);">${esc(e.body)}</div>
          <div class="chip-wrap" style="margin-top:1rem;">
            ${(e.tags || []).map(t => `<span class="mono-chip">#${esc(t)}</span>`).join('')}
          </div>
          <div class="hairline" style="margin:1.25rem 0;"></div>
          <h3 style="margin:0 0 0.625rem;font-size:0.9375rem;">留言交流 (${(e.comments || []).length})</h3>
          ${(e.comments || []).map(c => `
            <div class="comment-card" style="padding-left:0;padding-right:0;">
              ${avatarHtml(c.author.avatarSeed, 'avatar-32')}
              <div style="flex:1;">
                <div style="display:flex;align-items:center;gap:0.375rem;">
                  <strong style="font-size:0.8125rem;">${esc(c.author.nickname)}</strong>
                  ${c.cohortLabel ? `<span class="mono-chip" style="padding:0.0625rem 0.375rem;">${esc(c.cohortLabel)}</span>` : ''}
                  <span style="font-size:0.6875rem;color:var(--ink-muted);margin-left:auto;">${esc(c.createdAt)}</span>
                </div>
                <p style="margin:0.3125rem 0 0;font-size:0.875rem;line-height:1.5;">${esc(c.content)}</p>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  }

  // ---------------- Sub-Screen: POST DETAIL (帖子详情 — 1:1 PostDetailScreen.kt) ----------------
  function renderPostDetailScreen(postId) {
    const post = state.posts.find(p => p.id === postId);
    if (!post) return '';
    const cat = D.CATEGORIES[post.category] || D.CATEGORIES.EXHIBITION;
    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <div style="display:flex;align-items:center;gap:0.4375rem;min-width:0;">
              <span style="width:1.125rem;height:1.125rem;display:flex;color:var(--ink-secondary);flex-shrink:0;">${catIcon(post.category)}</span>
              <h1 class="sub-topbar-title" style="font-size:1rem;">${esc(cat.titleCn)}</h1>
            </div>
          </div>
          <button class="icon-btn-48" data-action="sharePost" data-id="${esc(post.id)}" aria-label="分享">${ICONS.share}</button>
        </div>
        <div class="scroll-area" style="padding-bottom:1.5rem;">
          <div style="background:var(--surface);padding:1.125rem 1.25rem;">
            <div class="post-compact-header" style="margin-bottom:0.875rem;">
              <div class="post-author-row">
                ${avatarHtml(post.author.avatarSeed, 'avatar-40')}
                <div>
                  <div style="display:flex;align-items:center;gap:0.375rem;">
                    <span class="post-author-name" style="font-size:0.9375rem;">${esc(post.author.nickname)}</span>
                    <span class="verified-badge" style="padding:0.1rem 0.375rem;font-size:0.625rem;">
                      <span class="status-dot verified"></span><span>央美认证</span>
                    </span>
                  </div>
                  <div class="post-time" style="margin-top:0.125rem;">${esc(post.createdAt)} · ${formatCompact(post.viewCount)} 浏览</div>
                </div>
              </div>
              <span class="cat-pill ${esc(cat.accent)}">${catIcon(post.category)}<span>${esc(cat.titleCn)}</span></span>
            </div>
            ${post.moodTag ? `<div style="margin-bottom:0.625rem;"><span class="mono-chip">树洞情绪 · ${esc(post.moodTag)}</span></div>` : ''}
            ${post.price ? `<div class="post-price" style="font-size:1.125rem;margin-bottom:0.375rem;">${esc(post.price)}</div>` : ''}
            ${post.title ? `<h2 style="margin:0 0 0.5rem;font-size:1.125rem;line-height:1.4;">${esc(post.title)}</h2>` : ''}
            <div style="font-size:0.9375rem;line-height:1.68;white-space:pre-line;color:var(--ink);">${esc(post.body)}</div>
            ${(post.mediaList || []).length ? `<div style="display:flex;flex-direction:column;gap:0.625rem;margin-top:0.875rem;">${post.mediaList.map(m => `<img class="detail-media-img" src="${esc(m.src)}" alt="帖子图片" />`).join('')}</div>` : ''}
            <div class="post-meta-strip" style="margin-top:0.875rem;">
              ${post.location ? `<span class="compact-pill-tag loc">${ICONS.loc}<span>${esc(post.location)}</span></span>` : ''}
              ${(post.topicTags || []).map(tag => `<button class="inline-hash-tag" data-action="openTopic" data-tag="${esc(tag)}">${esc(tag)}</button>`).join('')}
            </div>
            <div style="display:flex;gap:0.75rem;margin-top:1rem;padding-top:0.75rem;border-top:0.6px solid var(--outline-soft);">
              <button class="mono-chip ${post.isLiked ? 'inverted' : ''}" data-action="likePost" data-id="${esc(post.id)}">
                ${post.isLiked ? '已点赞' : '点赞'} · ${post.likeCount}
              </button>
              <button class="mono-chip ${post.isBookmarked ? 'inverted' : ''}" data-action="bookmarkPost" data-id="${esc(post.id)}">
                ${post.isBookmarked ? '已收藏' : '收藏'} · ${post.bookmarkCount}
              </button>
            </div>
          </div>

          <div style="padding:0.875rem 1.25rem 0.375rem;font-size:0.875rem;font-weight:700;color:var(--ink);">
            全部评论 (${(post.comments || []).length})
          </div>
          <div style="background:var(--surface);">
            ${(post.comments || []).length ? post.comments.map(c => {
              const isOp = c.author.id === post.author.id;
              return `
                <div class="comment-card">
                  ${avatarHtml(c.author.avatarSeed, 'avatar-32')}
                  <div style="flex:1;min-width:0;">
                    <div style="display:flex;align-items:center;gap:0.375rem;">
                      <strong style="font-size:0.8125rem;">${esc(c.author.nickname)}</strong>
                      ${isOp ? '<span class="op-badge">楼主</span>' : ''}
                      <span style="font-size:0.6875rem;color:var(--ink-muted);margin-left:auto;">${esc(c.createdAt)}</span>
                    </div>
                    <p style="margin:0.3125rem 0 0;font-size:0.875rem;line-height:1.5;color:var(--ink);">
                      ${c.replyToNickname ? `<span style="color:var(--active-tint);">回复 ${esc(c.replyToNickname)}：</span>` : ''}${esc(c.content)}
                    </p>
                  </div>
                </div>
              `;
            }).join('') : `<div class="empty-state" style="padding:2.5rem 1rem;"><div class="empty-state-sub">还没有评论，来说第一句吧</div></div>`}
          </div>
        </div>
        <form class="comment-composer-bar" id="comment-form" data-post-id="${esc(post.id)}">
          <input class="field-input" id="comment-input" placeholder="匿名写下你的回应..." autocomplete="off" />
          <button type="submit" class="btn-grad-sm" style="min-height:2.75rem;padding:0 1rem;">发送</button>
        </form>
      </div>
    `;
  }

  // ---------------- Sub-Screen: CATEGORY FEED (分区信息流) ----------------
  function renderCategoryFeedScreen(catKey) {
    const cat = D.CATEGORIES[catKey] || D.CATEGORIES.MARKETPLACE;
    const subMap = {
      MARKETPLACE: ['全部', '画材', '教材', '电子产品', '家具', '生活用品', '服饰'],
      OPPORTUNITY: ['全部', '兼职招募', '实习招聘', '项目合作', '展览协作', '模特招募'],
      LOST_FOUND: ['全部', '失物寻找', '拾物招领'],
      MUTUAL_AID: ['全部', '拼车买材料', '寻找搭子', '代取跑腿', '学习互助']
    };
    const subs = subMap[catKey] || [];
    const list = visiblePosts().filter(p => {
      if (p.category !== catKey) return false;
      if (nav.categorySubFilter === '全部') return true;
      const text = (p.title || '') + ' ' + (p.body || '') + ' ' + (p.topicTags || []).join(' ');
      return text.includes(nav.categorySubFilter);
    });

    return `
      <div class="screen-container">
        <div class="sub-topbar transparent">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">${esc(cat.titleCn)}</h1>
          </div>
        </div>
        <div class="scroll-area sub-scroll">
          <div class="editorial-hero-banner fluid-aurora-light">
            <div style="font-size:1rem;font-weight:700;color:var(--ink);">${esc(cat.desc)}</div>
            <div style="font-size:0.75rem;color:var(--ink-secondary);">共 ${list.length} 条内容</div>
          </div>
          ${subs.length ? `<div class="h-scroll-row" style="background:var(--surface);border-bottom:0.6px solid var(--outline-soft);padding:0.625rem 1.25rem;">
            ${subs.map(s => `<button class="mono-chip ${nav.categorySubFilter === s ? 'inverted' : ''}" data-action="catSubFilter" data-val="${esc(s)}">${esc(s)}</button>`).join('')}
          </div>` : ''}
          ${list.length ? list.map(renderEditorialPostCard).join('') : `
            <div class="empty-state">
              <div class="empty-state-title">暂无内容</div>
              <button class="btn-grad-sm" style="padding:0.625rem 1.25rem;font-size:0.875rem;" data-action="openPublishInCat" data-cat="${catKey}">发布第一条内容</button>
            </div>
          `}
        </div>
      </div>
    `;
  }

  // ---------------- Shared AI Campus Stats Engine + Weibo HotSearchBoardCard (1:1 Android) ----------------
  function getAllLiveTagCounts() {
    const map = new Map();
    visiblePosts().forEach(p => {
      (p.topicTags || []).forEach(t => {
        const clean = t.trim();
        if (clean) map.set(clean, (map.get(clean) || 0) + 1);
      });
    });
    return Array.from(map.entries()).sort((a, b) => b[1] - a[1]);
  }

  function buildAiAnalysisReport(query) {
    const q = (query || '').trim();
    const ql = q.toLowerCase();
    const posts = visiblePosts();
    const courses = state.courses;
    const exps = state.experiences;

    if (!q) {
      const liveTags = getAllLiveTagCounts();
      const topCourses = [...courses].filter(c => c.recommendScore >= 4.5).slice(0, 3).map(c => c.name);
      const top5 = liveTags.slice(0, 5).map(([tag, cnt], idx) => ({
        keyword: tag.replace(/^#/, ''),
        mentions: cnt + (state.hotTopics[idx] ? Math.round(state.hotTopics[idx].discussionCount / 180) : 3),
        trend: idx === 0 ? '飙升' : idx === 1 ? '热门' : idx === 2 ? '新锐' : '稳定'
      }));
      return {
        headline: '央美全站热点与选课口碑洞察',
        summary: `当前社区共聚合 ${posts.length} 篇动态、${courses.length} 门三校区选课档案与 ${exps.length} 篇学长学姐实战经验。高分推荐课程集中在「${topCourses.join('」「')}」等方向，晚间画室与毕业季讨论热度持续上升。`,
        topKeywords: top5,
        distributions: [
          { label: '选课评价与跨院推荐', percent: 38 },
          { label: '校园展览与创作互助', percent: 29 },
          { label: '留学作品集与毕设经验', percent: 19 },
          { label: '闲置画材与宿舍日常', percent: 14 }
        ],
        recommendedTags: ['#高分选修课', '#艺术创作', '#毕业作品', '#校园展览', '#闲置画材']
      };
    }

    const mCourses = courses.filter(c =>
      (c.name + c.instructor + c.college + c.major + c.summary + c.courseType + c.suitableFor).toLowerCase().includes(ql) ||
      ((ql.includes('给分') || ql.includes('水课') || ql.includes('选修')) && c.recommendScore >= 4.4)
    );
    const mPosts = posts.filter(p => ((p.title || '') + (p.body || '') + (p.topicTags || []).join(' ') + (p.location || '')).toLowerCase().includes(ql));
    const mExps = exps.filter(e => (e.title + e.summary + e.body + e.category + (e.tags || []).join(' ')).toLowerCase().includes(ql));

    const total = mCourses.length + mPosts.length + mExps.length;
    let headline = `关于「${q}」的 AI 校园情报汇总`;
    let summary = '';

    if (ql.includes('选修') || ql.includes('给分') || ql.includes('水课')) {
      const top = [...courses].filter(c => c.difficultyScore >= 4.5).sort((a, b) => b.recommendScore - a.recommendScore).slice(0, 4);
      headline = '央美高给分 / 热门选修课口碑排行';
      summary = `结合三校区同学实测：${top.map(c => `《${c.name}》（${c.instructor}，推荐 ${c.recommendScore.toFixed(1)}）`).join('、')}普遍评价老师友好、作业轻松且给分可观；同时建议留意标注「慎选避雷」的课程评价。`;
    } else if (ql.includes('留学') || ql.includes('作品集')) {
      headline = '央美留学作品集与申请经验提炼';
      summary = `学长学姐实战建议：皇艺/伦艺等名校更看重完整调研过程与材料实验，不必把所有作品做成同一种画风；建议提前 10 个月规划语言与排版节奏。`;
    } else if (ql.includes('食堂') || ql.includes('望京')) {
      headline = '望京本院周边生活与食堂讨论';
      summary = `花家地南街与望京本院相关讨论集中在工作室晚归拼车、画材采购与课间简餐推荐，建议在「校园互助」板块直接找同校区搭子。`;
    } else if (total > 0) {
      summary = `已为您检索全站内容，共发现 ${mCourses.length} 门相关课程、${mExps.length} 篇经验攻略与 ${mPosts.length} 条校园帖子。点击下方结果或推荐标签可直达详情。`;
    } else {
      summary = `暂未找到直接包含「${q}」的历史记录，建议尝试拆短关键词（如「跨院选修」「画材」「毕设」），或直接发帖向央美同学提问。`;
    }

    const kwList = [];
    mCourses.slice(0, 3).forEach((c, i) => kwList.push({ keyword: c.name, mentions: Math.max(3, (c.reviews || []).length + 4 - i), trend: i === 0 ? '飙升' : '热门' }));
    mExps.slice(0, 2).forEach(e => kwList.push({ keyword: e.category + ' · ' + e.year, mentions: Math.max(2, e.likeCount), trend: '新锐' }));
    if (kwList.length < 3) {
      state.hotTopics.slice(0, 4 - kwList.length).forEach(t => kwList.push({ keyword: t.tag.replace(/^#/, ''), mentions: Math.round(t.discussionCount / 200), trend: '稳定' }));
    }

    const cPct = total ? Math.round((mCourses.length / total) * 100) : 40;
    const pPct = total ? Math.round((mPosts.length / total) * 100) : 35;
    const ePct = Math.max(5, 100 - cPct - pPct);

    return {
      headline,
      summary,
      topKeywords: kwList.slice(0, 5),
      distributions: [
        { label: '选课评价相关度', percent: Math.max(12, cPct) },
        { label: '校园帖子讨论度', percent: Math.max(12, pPct) },
        { label: '学长学姐经验沉淀', percent: Math.max(10, ePct) }
      ],
      recommendedTags: [
        '#' + q.slice(0, 8),
        '#跨院选修',
        '#高分推荐',
        '#校园互助'
      ].filter((v, i, a) => a.indexOf(v) === i)
    };
  }

  function triggerAiStats(query) {
    const cleanQ = (query || '').trim();
    if (nav.aiTimer) clearTimeout(nav.aiTimer);
    nav.aiStats = { status: 'loading', queryKey: cleanQ, report: null };
    render();
    nav.aiTimer = setTimeout(() => {
      nav.aiStats = {
        status: 'ready',
        queryKey: cleanQ,
        report: buildAiAnalysisReport(cleanQ)
      };
      render();
    }, 420);
  }

  function renderAiCampusStatsCard(query, matchedCount, totalTagCount) {
    const cleanQ = (query || '').trim();
    const isSameKey = nav.aiStats.queryKey === cleanQ;
    const st = isSameKey ? nav.aiStats.status : 'idle';
    const report = isSameKey ? nav.aiStats.report : null;

    const title = cleanQ ? `AI 搜索「${cleanQ}」` : 'AI 智能搜索 · 校园标签统计';
    const subtitle = cleanQ
      ? `共匹配到 ${matchedCount} 条记录 · 点击生成 AI 解答与分析`
      : `已聚合 ${totalTagCount} 个校园与自定义标签 · 支持自然语言 AI 搜索`;

    const trendClass = (tr) => tr === '飙升' ? 'surge' : tr === '热门' ? 'hot' : tr === '新锐' ? 'fresh' : 'steady';
    const rankBg = (i) => i === 0 ? '#E53935' : i === 1 ? '#EA580C' : i === 2 ? '#D97706' : '#6366F1';

    let bodyHtml = '';
    if (st === 'loading') {
      bodyHtml = `
        <div class="ai-stats-loading">
          <div class="ai-spinner"></div>
          <span>${cleanQ ? `正在检索全站帖子、选课与经验，生成「${esc(cleanQ)}」的 AI 分析...` : '正在分析全站校园与自定义标签统计...'}</span>
        </div>
      `;
    } else if (st === 'ready' && report) {
      bodyHtml = `
        <div class="ai-summary-box">
          <div class="ai-summary-headline">${esc(report.headline)}</div>
          <div class="ai-summary-body">${esc(report.summary)}</div>
        </div>
        <div class="ai-subhead">高频关键词与标签统计</div>
        <div class="ai-kw-list">
          ${report.topKeywords.map((kw, i) => `
            <button class="ai-kw-row" data-action="quickAiPrompt" data-q="${esc(kw.keyword)}">
              <span class="ai-kw-rank" style="background:${rankBg(i)};">${i + 1}</span>
              <span class="ai-kw-label">${esc(kw.keyword)}</span>
              <span class="ai-trend-pill ${trendClass(kw.trend)}">${esc(kw.trend)}</span>
              <span class="ai-kw-mentions">${kw.mentions} 次提及</span>
            </button>
          `).join('')}
        </div>
        <div class="ai-subhead">主题与情绪分布</div>
        <div style="display:flex;flex-direction:column;gap:0.5rem;">
          ${report.distributions.map(d => `
            <div class="ai-dist-row">
              <div class="ai-dist-head">
                <span style="color:var(--ink);font-weight:500;">${esc(d.label)}</span>
                <span style="color:var(--active-tint);font-weight:700;">${d.percent}%</span>
              </div>
              <div class="ai-dist-track"><div class="ai-dist-fill" style="width:${Math.min(100, d.percent)}%;"></div></div>
            </div>
          `).join('')}
        </div>
        <div class="ai-subhead">AI 推荐探索标签</div>
        <div class="chip-wrap">
          ${report.recommendedTags.map(t => `<button class="search-chip-pill prompt" data-action="quickAiPrompt" data-q="${esc(t.replace(/^#/, ''))}">${esc(t)}</button>`).join('')}
        </div>
      `;
    } else {
      bodyHtml = `
        <p class="ai-stats-idle-text">
          ${cleanQ
            ? `点击右侧「AI 搜索」基于央美帖子、选课评价、学长学姐说为您智能汇总「${esc(cleanQ)}」的真实口碑与建议。`
            : '输入任意央美问题或关键词即可触发 AI 智能搜索，也可点击右侧「AI 搜索」一键汇总全站热点、选课口碑与自定义标签分布。'}
        </p>
      `;
    }

    return `
      <div class="ai-stats-card">
        <div class="ai-stats-inner">
          <div class="ai-stats-header">
            <div class="ai-stats-title-row">
              <span class="ai-stats-icon-box">${ICONS.sparkle}</span>
              <div style="min-width:0;flex:1;">
                <h3 class="ai-stats-title">${esc(title)}</h3>
                <p class="ai-stats-sub">${esc(subtitle)}</p>
              </div>
            </div>
            <button class="ai-stats-cta ${st === 'ready' ? 'outline' : ''}" data-action="runAiSearch">
              ${st === 'ready' ? ICONS.refresh : ICONS.sparkle}
              <span>${st === 'ready' ? '重新生成' : 'AI 搜索'}</span>
            </button>
          </div>
          ${bodyHtml}
        </div>
      </div>
    `;
  }

  function renderHotSearchBoardCard(rankedTopics) {
    const badgeColor = (ch) => {
      if (ch === '沸') return '#E53935';
      if (ch === '热') return '#EA580C';
      if (ch === '新') return '#D946EF';
      if (ch === '荐') return '#6366F1';
      return '';
    };
    return `
      <div class="hot-board-card">
        ${rankedTopics.map((topic, idx) => {
          const rank = idx + 1;
          const rCls = rank === 1 ? 'r1' : rank === 2 ? 'r2' : rank === 3 ? 'r3' : '';
          const ch = (topic.heatStatus || '').trim().slice(0, 1);
          const bg = badgeColor(ch);
          return `
            <button class="hot-board-row" data-action="openTopic" data-tag="${esc(topic.tag)}">
              <span class="hot-board-rank ${rCls}">${rank}</span>
              <span class="hot-board-main">
                <span class="hot-board-title ${rank <= 3 ? 'top3' : ''}">${esc(topic.tag.replace(/^#/, ''))}</span>
                <span class="hot-board-count">${formatCompact(topic.discussionCount)}</span>
              </span>
              ${bg ? `<span class="hot-board-badge" style="background:${bg};">${esc(ch)}</span>` : ''}
            </button>
            ${idx < rankedTopics.length - 1 ? '<div class="hairline" style="margin:0 0.875rem;width:auto;"></div>' : ''}
          `;
        }).join('')}
      </div>
    `;
  }

  // ---------------- Sub-Screen: AI SEARCH (搜索 — 1:1 SearchScreen.kt) ----------------
  function renderSearchScreen() {
    const rawQ = nav.searchQuery || '';
    const q = rawQ.trim();
    const ql = q.toLowerCase();
    const posts = visiblePosts();
    const rankedTopics = [...state.hotTopics].sort((a, b) => b.discussionCount - a.discussionCount);
    const liveTags = getAllLiveTagCounts();
    const totalTagCount = liveTags.length;

    const matchedPosts = q ? posts.filter(p => ((p.title || '') + ' ' + (p.body || '') + ' ' + (p.topicTags || []).join(' ') + ' ' + (p.location || '')).toLowerCase().includes(ql)) : [];
    const matchedCourses = q ? state.courses.filter(c => (c.name + ' ' + c.instructor + ' ' + c.college + ' ' + c.major + ' ' + c.summary).toLowerCase().includes(ql)) : [];
    const matchedExps = q ? state.experiences.filter(e => (e.title + ' ' + e.summary + ' ' + e.body + ' ' + e.category).toLowerCase().includes(ql)) : [];
    const totalMatches = matchedPosts.length + matchedCourses.length + matchedExps.length;

    const scopes = [
      { key: 'ALL', label: '全部' },
      { key: 'POSTS', label: '帖子' },
      { key: 'COURSES', label: '选课评价' },
      { key: 'EXPERIENCES', label: '学长学姐说' }
    ];
    const quickPrompts = ['哪些选修课给分高', '水课推荐', '望京食堂吃什么', '留学作品集经验', '闲置画材'];

    let contentHtml = '';
    if (!q) {
      contentHtml = `
        <div style="display:flex;flex-direction:column;gap:0.75rem;padding-top:0.25rem;">
          ${liveTags.length ? `
            <div class="h-scroll-row" style="padding:0 0.875rem;">
              ${liveTags.slice(0, 12).map(([tag, cnt]) => `
                <button class="search-chip-pill" data-action="quickAiPrompt" data-q="${esc(tag.replace(/^#/, ''))}">
                  <span>${esc(tag)}</span>
                  <span style="color:var(--ink-muted);font-size:0.6875rem;">· ${cnt}</span>
                </button>
              `).join('')}
            </div>
          ` : ''}

          <div class="h-scroll-row" style="padding:0 0.875rem;">
            ${quickPrompts.map(prompt => `
              <button class="search-chip-pill prompt" data-action="quickAiPrompt" data-q="${esc(prompt)}">
                ${ICONS.sparkle}<span>${esc(prompt)}</span>
              </button>
            `).join('')}
          </div>

          ${renderAiCampusStatsCard('', posts.length, totalTagCount)}

          <div class="hot-board-head">
            <div style="display:flex;align-items:center;gap:0.375rem;">
              <span style="width:1.125rem;height:1.125rem;color:var(--vermilion);display:flex;">${ICONS.fire}</span>
              <span style="font-size:1rem;font-weight:700;color:var(--ink);">央美热搜榜</span>
            </div>
            <span style="font-size:0.75rem;color:var(--ink-secondary);">实时更新</span>
          </div>

          ${renderHotSearchBoardCard(rankedTopics)}
        </div>
      `;
    } else {
      const showCourses = (nav.searchScope === 'ALL' || nav.searchScope === 'COURSES') && matchedCourses.length > 0;
      const showExps = (nav.searchScope === 'ALL' || nav.searchScope === 'EXPERIENCES') && matchedExps.length > 0;
      const showPosts = (nav.searchScope === 'ALL' || nav.searchScope === 'POSTS') && matchedPosts.length > 0;

      contentHtml = `
        <div style="display:flex;flex-direction:column;gap:0.75rem;padding-top:0.25rem;">
          ${renderAiCampusStatsCard(q, totalMatches, totalTagCount)}

          ${showCourses ? `
            <div class="search-group-sec-title">选课评价 (${matchedCourses.length})</div>
            <div class="search-result-card">
              ${matchedCourses.map((c, idx) => `
                <button class="search-result-row" data-action="openCourseDetail" data-id="${esc(c.id)}">
                  <div style="min-width:0;flex:1;">
                    <div style="font-size:0.9375rem;font-weight:600;color:var(--ink);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(c.name)}</div>
                    <div style="font-size:0.75rem;color:var(--ink-secondary);margin-top:0.15rem;">${esc(c.instructor)} · ${esc(c.college)}</div>
                  </div>
                  <div style="text-align:right;flex-shrink:0;">
                    <div style="font-size:0.875rem;font-weight:700;color:var(--active-tint);">${c.recommendScore > 0 ? '综合 ' + c.recommendScore.toFixed(1) : '待评分'}</div>
                    <div style="font-size:0.6875rem;color:var(--ink-muted);">${esc(c.courseType)}</div>
                  </div>
                </button>
                ${idx < matchedCourses.length - 1 ? '<div class="hairline" style="margin:0 0.875rem;width:auto;"></div>' : ''}
              `).join('')}
            </div>
          ` : ''}

          ${showExps ? `
            <div class="search-group-sec-title">学长学姐说 (${matchedExps.length})</div>
            <div class="search-result-card">
              ${matchedExps.map((e, idx) => `
                <button class="search-result-row" data-action="openExpDetail" data-id="${esc(e.id)}">
                  <div style="min-width:0;flex:1;">
                    <div style="font-size:0.6875rem;color:var(--active-tint);font-weight:600;">${esc(e.category)} · ${esc(e.year)}</div>
                    <div style="font-size:0.9375rem;font-weight:600;color:var(--ink);margin-top:0.125rem;">${esc(e.title)}</div>
                    <div style="font-size:0.75rem;color:var(--ink-secondary);margin-top:0.125rem;">${esc(e.author.nickname)} · ${esc(e.college)}</div>
                  </div>
                  <span style="width:1rem;height:1rem;color:var(--ink-muted);display:flex;flex-shrink:0;">${ICONS.chevronRight}</span>
                </button>
                ${idx < matchedExps.length - 1 ? '<div class="hairline" style="margin:0 0.875rem;width:auto;"></div>' : ''}
              `).join('')}
            </div>
          ` : ''}

          ${showPosts ? `
            <div class="search-group-sec-title">社区帖子 (${matchedPosts.length})</div>
            <div>${matchedPosts.map(renderEditorialPostCard).join('')}</div>
          ` : ''}

          ${(!showCourses && !showExps && !showPosts) ? `
            <div class="empty-state" style="padding:2.5rem 1.5rem;">
              <div class="empty-state-title">没有找到「${esc(q)}」的直接匹配</div>
              <div class="empty-state-sub">可以点击上方「AI 搜索」按钮，让树窝为你综合提炼相关建议</div>
            </div>
          ` : ''}
        </div>
      `;
    }

    return `
      <div class="screen-container">
        <div class="search-topbar">
          <button class="icon-btn-48" style="width:2.75rem;height:2.75rem;" data-action="goBack" aria-label="返回">${ICONS.arrowBack}</button>
          <div class="search-input-box">
            <span class="search-input-icon">${ICONS.search}</span>
            <input class="search-input-native" id="global-search-input" type="search" placeholder="AI 搜索帖子、选课、经验或提问..." value="${esc(rawQ)}" autocomplete="off" />
            ${q ? `<button class="search-clear-btn" data-action="clearSearch" aria-label="清空">${ICONS.close}</button>` : ''}
          </div>
          <button class="search-ai-submit" data-action="runAiSearch">
            ${ICONS.sparkle}<span>AI搜索</span>
          </button>
        </div>
        ${q ? `
          <div class="h-scroll-row" style="padding:0.125rem 0.875rem 0.5rem;">
            ${scopes.map(s => `<button class="search-chip-pill ${nav.searchScope === s.key ? 'active' : ''}" data-action="searchScope" data-val="${s.key}">${s.label}</button>`).join('')}
          </div>
        ` : ''}
        <div class="scroll-area sub-scroll">${contentHtml}</div>
      </div>
    `;
  }

  // ---------------- Sub-Screen: HOT TOPICS (央美热搜榜 — 1:1 HotTopicsScreen.kt) ----------------
  function renderHotTopicsScreen() {
    const ranked = [...state.hotTopics].sort((a, b) => b.discussionCount - a.discussionCount);
    const totalTagCount = getAllLiveTagCounts().length;
    return `
      <div class="screen-container">
        <div class="sub-topbar transparent">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack" aria-label="返回">${ICONS.arrowBack}</button>
            <div style="display:flex;align-items:center;gap:0.375rem;">
              <span style="width:1.25rem;height:1.25rem;color:var(--vermilion);display:flex;">${ICONS.fire}</span>
              <h1 class="sub-topbar-title">央美热搜榜</h1>
            </div>
          </div>
          <span style="font-size:0.75rem;color:var(--ink-secondary);padding-right:0.75rem;">实时更新</span>
        </div>
        <div class="scroll-area sub-scroll">
          <div style="display:flex;flex-direction:column;gap:0.75rem;padding-top:0.25rem;">
            ${renderAiCampusStatsCard('', visiblePosts().length, totalTagCount)}
            ${renderHotSearchBoardCard(ranked)}
          </div>
        </div>
      </div>
    `;
  }

  function renderTopicDetailScreen(tag) {
    const clean = tag.startsWith('#') ? tag : '#' + tag;
    const topic = state.hotTopics.find(t => t.tag.toLowerCase() === clean.toLowerCase());
    const list = visiblePosts().filter(p => (p.topicTags || []).some(t => t.includes(clean.replace('#', ''))) || (p.body || '').includes(clean.replace('#', '')));
    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">${esc(clean)}</h1>
          </div>
          <button class="icon-btn-48" data-action="openPublishWithTag" data-tag="${esc(clean)}" aria-label="参与话题">${ICONS.edit}</button>
        </div>
        <div class="scroll-area sub-scroll">
          <div class="editorial-hero-banner fluid-aurora-light" style="min-height:8.5rem;justify-content:space-between;">
            <span style="font-size:0.75rem;color:var(--ink-secondary);">${list.length} 篇内容</span>
            <div>
              <div style="font-size:1.375rem;font-weight:800;color:var(--ink);">${esc(clean)}</div>
              <div style="font-size:0.75rem;color:var(--ink-secondary);margin-top:0.25rem;">${esc(topic ? topic.description : `关于「${clean}」的校园匿名讨论。`)}</div>
            </div>
          </div>
          ${list.length ? list.map(renderEditorialPostCard).join('') : `
            <div class="empty-state">
              <div class="empty-state-title">该话题下暂时还没有更多内容</div>
              <button class="btn-grad-sm" style="padding:0.625rem 1.25rem;font-size:0.875rem;" data-action="openPublishWithTag" data-tag="${esc(clean)}">匿名参与 ${esc(clean)}</button>
            </div>
          `}
        </div>
      </div>
    `;
  }

  // ---------------- Sub-Screen: VERIFICATION (央美身份认证 — 1:1 VerificationScreen.kt) ----------------
  function renderVerificationScreen() {
    const p = state.persona;
    const selType = nav.verifyDraftType || p.identityType || '在读学生';
    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack" aria-label="返回">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">央美身份认证</h1>
          </div>
        </div>
        <div class="scroll-area sub-scroll">
          <div class="verify-dark-hero fluid-aurora-dark">
            <div style="display:flex;justify-content:space-between;align-items:center;font-size:0.75rem;color:rgba(255,255,255,0.82);">
              <span>中央美术学院 · CAFA</span>
              <span>${p.isVerified ? '已认证' : '未认证'}</span>
            </div>
            <div>
              <h2 style="margin:0;font-size:1.5rem;font-weight:800;color:#FFFFFF;">确认央美身份</h2>
              <p style="margin:0.375rem 0 0;font-size:0.75rem;line-height:1.5;color:rgba(255,255,255,0.82);">
                仅核验是否为中央美术学院在读学生或毕业校友，不收集学院与年级，认证资料严格保密不公开。
              </p>
            </div>
          </div>
          <div class="verify-steps-strip">
            <div class="verify-step-node active"><span class="verify-step-num">01</span><span>确认身份</span></div>
            <div class="verify-step-node ${p.isVerified ? 'active' : ''}"><span class="verify-step-num">02</span><span>拍照/传图</span></div>
            <div class="verify-step-node ${p.isVerified ? 'active' : ''}"><span class="verify-step-num">03</span><span>立即通过</span></div>
          </div>
          <div style="padding:1rem;display:flex;flex-direction:column;gap:0.875rem;">
            ${p.isVerified ? `
              <div class="course-card-surface" style="margin:0;">
                <div style="display:flex;align-items:center;gap:0.5rem;">
                  <span class="status-dot verified" style="width:0.625rem;height:0.625rem;"></span>
                  <strong style="font-size:1rem;">已完成央美校园身份认证</strong>
                </div>
                <p style="margin:0;font-size:0.8125rem;line-height:1.55;color:var(--ink-secondary);">
                  当前身份：${esc(p.identityType)} · 社区内始终仅展示你的独立匿名形象「${esc(p.nickname)}」。
                </p>
                <div style="display:flex;gap:0.625rem;">
                  <button class="btn-primary-full" style="flex:1;" data-action="goBack">进入树窝</button>
                  <button class="btn-outline-full" style="flex:1;" data-action="resetVerifyDemo">重新演示认证</button>
                </div>
              </div>
            ` : `
              <div class="course-card-surface" style="margin:0;">
                <div style="font-size:0.9375rem;font-weight:700;">选择你的央美身份</div>
                <div style="display:flex;gap:0.625rem;">
                  <button class="mono-chip ${selType === '在读学生' ? 'inverted' : ''}" style="flex:1;justify-content:center;padding:0.75rem;" data-action="verifyPickType" data-val="在读学生">在读学生</button>
                  <button class="mono-chip ${selType === '毕业校友' ? 'inverted' : ''}" style="flex:1;justify-content:center;padding:0.75rem;" data-action="verifyPickType" data-val="毕业校友">毕业校友</button>
                </div>
                <div style="font-size:0.75rem;line-height:1.5;color:var(--ink-secondary);">
                  支持学生证、校园一卡通、录取通知书或毕业证书照片，上传后自动完成核验并脱敏销毁。
                </div>
                <label class="btn-primary-full" style="cursor:pointer;">
                  <span>上传央美证件照片 / 立即认证</span>
                  <input type="file" accept="image/*" id="verify-file-input" style="display:none;" />
                </label>
                <button class="btn-outline-full" data-action="completeVerification">快速模拟认证通过（体验用）</button>
              </div>
            `}
          </div>
        </div>
      </div>
    `;
  }

  // ---------------- Sub-Screen: PROFILE COLLECTION & SETTINGS ----------------
  function renderProfileCollectionScreen(colKey) {
    const titles = {
      MY_POSTS: '我的帖子', MY_COMMENTS: '我的评论', MY_BOOKMARKS: '我的收藏',
      MY_DRAFTS: '我的草稿', MY_TRADES: '我的交易', MY_HELP: '我的求助'
    };
    let body = '';
    if (colKey === 'MY_DRAFTS') {
      body = state.drafts.length ? state.drafts.map(d => `
        <div class="post-card" style="padding:1rem;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span class="mono-chip">${esc((D.CATEGORIES[d.category] || {}).titleCn || '草稿')} · 草稿</span>
            <button style="font-size:0.75rem;color:var(--vermilion);" data-action="deleteDraft" data-id="${esc(d.id)}">删除</button>
          </div>
          <div style="font-weight:700;margin-top:0.5rem;">${esc(d.title || '无标题草稿')}</div>
          <p style="margin:0.3125rem 0 0;font-size:0.8125rem;color:var(--ink-secondary);">${esc(d.body)}</p>
        </div>
      `).join('') : `<div class="empty-state"><div class="empty-state-title">暂无草稿</div><div class="empty-state-sub">发布页面支持随时暂存未写完的想法。</div></div>`;
    } else if (colKey === 'MY_COMMENTS') {
      const mine = [];
      state.posts.forEach(post => {
        (post.comments || []).forEach(c => {
          if (c.author.id === state.persona.id) mine.push({ post, c });
        });
      });
      body = mine.length ? mine.map(({ post, c }) => `
        <div class="post-card" style="padding:1rem;" data-action="openPost" data-id="${esc(post.id)}">
          <div style="font-size:0.75rem;color:var(--ink-muted);">回复于「${esc(post.title || '树洞内容')}」</div>
          <div style="margin:0.375rem 0;font-size:0.875rem;">${esc(c.content)}</div>
          <div style="font-size:0.75rem;color:var(--ink-muted);">${esc(c.createdAt)}</div>
        </div>
      `).join('') : `<div class="empty-state"><div class="empty-state-title">暂无评论记录</div></div>`;
    } else {
      let items = [];
      if (colKey === 'MY_POSTS') items = state.posts.filter(p => p.isMine);
      else if (colKey === 'MY_BOOKMARKS') items = state.posts.filter(p => p.isBookmarked);
      else if (colKey === 'MY_TRADES') items = state.posts.filter(p => p.isMine && p.category === 'MARKETPLACE');
      else if (colKey === 'MY_HELP') items = state.posts.filter(p => p.isMine && (p.category === 'LOST_FOUND' || p.category === 'MUTUAL_AID'));
      body = items.length ? items.map(renderEditorialPostCard).join('') : `<div class="empty-state"><div class="empty-state-title">这里暂时空着</div><div class="empty-state-sub">当你发布或收藏内容后，记录会汇集在这里。</div></div>`;
    }

    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">${titles[colKey] || '记录'}</h1>
          </div>
        </div>
        <div class="scroll-area sub-scroll" style="padding-top:0.5rem;">${body}</div>
      </div>
    `;
  }

  function renderSettingsScreen() {
    const s = state.settings;
    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">设置</h1>
          </div>
        </div>
        <div class="scroll-area sub-scroll">
          <div class="settings-sec-header">通知与隐私</div>
          <div class="settings-card">
            <div class="settings-toggle-row" data-action="toggleSetting" data-key="notifyComments" style="cursor:pointer;">
              <div><div class="menu-row-title" style="font-size:0.875rem;">评论与回复通知</div><div class="menu-row-sub">有人回应你的帖子或树洞时提醒</div></div>
              <div class="m3-switch ${s.notifyComments ? 'on' : ''}"><div class="m3-switch-thumb"></div></div>
            </div>
            <div class="hairline"></div>
            <div class="settings-toggle-row" data-action="toggleSetting" data-key="notifyLikes" style="cursor:pointer;">
              <div><div class="menu-row-title" style="font-size:0.875rem;">点赞与收藏通知</div><div class="menu-row-sub">收到共鸣与收藏时记录在消息列表</div></div>
              <div class="m3-switch ${s.notifyLikes ? 'on' : ''}"><div class="m3-switch-thumb"></div></div>
            </div>
            <div class="hairline"></div>
            <div class="settings-toggle-row" data-action="toggleSetting" data-key="allowAnonymousNotes" style="cursor:pointer;">
              <div><div class="menu-row-title" style="font-size:0.875rem;">接收匿名留言</div><div class="menu-row-sub">允许通过认证的同学在树洞给你留言</div></div>
              <div class="m3-switch ${s.allowAnonymousNotes ? 'on' : ''}"><div class="m3-switch-thumb"></div></div>
            </div>
          </div>

          <div class="settings-sec-header">屏蔽管理</div>
          <div class="settings-card" style="padding-top:0.875rem;padding-bottom:0.875rem;display:flex;flex-direction:column;gap:0.625rem;">
            <div style="font-size:0.875rem;font-weight:600;">屏蔽关键词</div>
            <div style="display:flex;gap:0.5rem;">
              <input class="field-input" id="kw-input" placeholder="输入想过滤的词..." />
              <button class="btn-grad-sm" style="min-width:3rem;justify-content:center;" data-action="addBlockedKw">${ICONS.plus}</button>
            </div>
            <div class="chip-wrap">
              ${(s.blockedKeywords || []).length ? s.blockedKeywords.map(kw => `<button class="mono-chip" data-action="removeBlockedKw" data-kw="${esc(kw)}">${esc(kw)} ×</button>`).join('') : '<span style="font-size:0.75rem;color:var(--ink-muted);">暂无屏蔽词</span>'}
            </div>
          </div>

          <div class="settings-sec-header">帮助与反馈</div>
          <div class="settings-card" style="padding-top:0.875rem;padding-bottom:0.875rem;display:flex;flex-direction:column;gap:0.625rem;">
            <textarea class="field-textarea" id="feedback-input" placeholder="给杨梅树窝提建议..."></textarea>
            <button class="btn-primary-full" data-action="submitFeedback">提交匿名反馈</button>
            <button class="btn-outline-full" data-action="resetVerifyDemo">重置为未认证状态（演示测试）</button>
          </div>

          <div class="settings-sec-header">版本、版权与运行内存</div>
          <div class="settings-card" style="padding-top:0.875rem;padding-bottom:1rem;display:flex;flex-direction:column;gap:0.75rem;">
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span style="font-size:0.875rem;font-weight:600;">当前版本</span>
              <span class="mono-chip">v${D.COPYRIGHT.version} (Build ${D.COPYRIGHT.buildCode})</span>
            </div>
            <button class="btn-outline-full" data-action="clearLocalCache">清理图片内存与本地缓存</button>
            <div class="hairline"></div>
            <div class="copyright-statement-box">
              <div style="font-size:0.8125rem;font-weight:600;color:var(--ink);">${D.COPYRIGHT.line}</div>
              <div style="font-size:0.75rem;line-height:1.5;color:var(--ink-secondary);">${D.COPYRIGHT.statement}</div>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  // ---------------- Modals / Bottom Sheets ----------------
  function renderModal() {
    if (!nav.modal) return '';
    const { type, data } = nav.modal;

    if (type === 'personaEditor') {
      const nickLen = (data.nickname || '').trim().length;
      return `
        <div class="sheet-backdrop" data-action="closeModalBackdrop">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <h2 style="margin:0;font-size:1.125rem;font-weight:700;">自定义昵称与形象</h2>
              <button class="icon-btn-48" style="width:2.25rem;height:2.25rem;" data-action="closeModal">${ICONS.close}</button>
            </div>
            <div class="persona-preview-strip">
              ${avatarHtml(data.seed, 'avatar-66')}
              <div style="min-width:0;flex:1;">
                <div style="font-size:0.6875rem;font-weight:600;color:var(--active-tint);">实时预览</div>
                <div style="font-size:1rem;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-top:0.15rem;">${esc(data.nickname || '请输入你的匿名昵称')}</div>
                <div style="font-size:0.6875rem;color:var(--ink-secondary);margin-top:0.15rem;">形象编号 #${Math.abs(data.seed) % 10000}</div>
              </div>
              <button class="btn-aurora-outline-sm" style="gap:0.25rem;" data-action="rollPersonaBoth">
                <span style="width:0.875rem;height:0.875rem;display:flex;">${ICONS.sparkle}</span><span>随机一套</span>
              </button>
            </div>
            <div>
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.4375rem;">
                <span style="font-size:0.875rem;font-weight:700;">修改匿名昵称</span>
                <span style="font-size:0.75rem;color:${nickLen >= 2 && nickLen <= 16 ? 'var(--ink-secondary)' : 'var(--vermilion)'};">${nickLen} / 16</span>
              </div>
              <div style="display:flex;gap:0.5rem;align-items:center;">
                <input class="field-input" id="persona-nick-input" maxlength="16" value="${esc(data.nickname)}" placeholder="输入 2~16 字自定义昵称" />
                <button class="icon-btn-48" style="border-radius:0.75rem;background:var(--surface-soft);color:var(--active-tint);flex-shrink:0;" data-action="rollSingleNick" aria-label="随机抽取昵称">${ICONS.sparkle}</button>
              </div>
            </div>
            <div>
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.5rem;">
                <span style="font-size:0.75rem;color:var(--ink-secondary);">词库不重复灵感推荐</span>
                <button style="font-size:0.75rem;color:var(--active-tint);font-weight:600;display:inline-flex;align-items:center;gap:0.25rem;" data-action="refreshNickBatch">
                  <span style="width:0.875rem;height:0.875rem;display:flex;">${ICONS.refresh}</span>换一批
                </button>
              </div>
              <div class="chip-wrap">
                ${data.batch.map(n => `<button class="mono-chip ${data.nickname === n ? 'inverted' : ''}" data-action="pickNick" data-nick="${esc(n)}">${esc(n)}</button>`).join('')}
              </div>
            </div>
            <div class="hairline"></div>
            <div>
              <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.5rem;">
                <span style="font-size:0.875rem;font-weight:700;">更换匿名形象</span>
                <button style="font-size:0.75rem;color:var(--active-tint);font-weight:600;display:inline-flex;align-items:center;gap:0.25rem;" data-action="refreshAvatarBatch">
                  <span style="width:0.875rem;height:0.875rem;display:flex;">${ICONS.refresh}</span>换一批形象
                </button>
              </div>
              <div class="avatar-grid">
                ${data.seeds.map(sd => `
                  <button class="avatar-pick-item ${data.seed === sd ? 'selected' : ''}" data-action="pickAvatarSeed" data-seed="${sd}">
                    ${avatarHtml(sd, 'avatar-40')}
                    ${data.seed === sd ? '<span class="avatar-pick-check">✓</span>' : ''}
                  </button>
                `).join('')}
              </div>
            </div>
            <button class="btn-primary-full" data-action="savePersona">保存昵称与形象</button>
          </div>
        </div>
      `;
    }

    if (type === 'publish') {
      const pubCats = ['TREE_HOLE', 'MARKETPLACE', 'OPPORTUNITY', 'LOST_FOUND', 'MUTUAL_AID', 'EXHIBITION', 'ART_CREATION'];
      return `
        <div class="sheet-backdrop" data-action="closeModalBackdrop">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <div style="display:flex;align-items:center;gap:0.5rem;">
                ${avatarHtml(state.persona.avatarSeed, 'avatar-32')}
                <div>
                  <div style="font-size:0.9375rem;font-weight:700;">${esc((D.CATEGORIES[data.category] || D.CATEGORIES.TREE_HOLE).titleCn)}</div>
                  <div style="font-size:0.6875rem;color:var(--ink-secondary);">${esc(state.persona.nickname)}</div>
                </div>
              </div>
              <button class="icon-btn-48" style="width:2.25rem;height:2.25rem;" data-action="closeModal">${ICONS.close}</button>
            </div>
            <div class="chip-wrap">
              ${pubCats.map(k => `<button class="mono-chip ${data.category === k ? 'inverted' : ''}" data-action="pubPickCat" data-val="${k}">${D.CATEGORIES[k].titleCn}</button>`).join('')}
            </div>
            <textarea class="field-textarea" id="pub-body" style="min-height:7rem;" placeholder="直接写下此刻的想法、作品进展或求助，无需标题...">${esc(data.body || '')}</textarea>
            ${data.category === 'MARKETPLACE' ? `<input class="field-input" id="pub-price" placeholder="价格（如 ¥ 120）" value="${esc(data.price || '')}" />` : ''}
            <div>
              <div style="font-size:0.75rem;color:var(--ink-secondary);margin-bottom:0.375rem;">快捷地点</div>
              <div class="h-scroll-row" style="padding:0;">
                ${D.CAFA_LOCATIONS.slice(0, 8).map(loc => `<button class="mono-chip ${data.location === loc ? 'inverted' : ''}" data-action="pubPickLoc" data-val="${esc(loc)}">${esc(loc)}</button>`).join('')}
              </div>
            </div>
            <div>
              <div style="font-size:0.75rem;color:var(--ink-secondary);margin-bottom:0.375rem;">话题标签</div>
              <div class="chip-wrap">
                ${D.FIXED_PUBLISH_TAGS.slice(0, 8).map(tg => `<button class="mono-chip ${(data.tags || []).includes(tg) ? 'inverted' : ''}" data-action="pubToggleTag" data-val="${esc(tg)}">${esc(tg)}</button>`).join('')}
              </div>
            </div>
            <div style="display:flex;gap:0.625rem;">
              <button class="btn-outline-full" style="flex:1;" data-action="saveDraft">存草稿</button>
              <button class="btn-primary-full" style="flex:1.6;" data-action="submitPost">发布</button>
            </div>
          </div>
        </div>
      `;
    }

    if (type === 'createSenior') {
      const cohorts = ['2026届', '2025届', '2024届', '2023届', '2022届及以前'];
      const expCats = ['留学', '毕业作品', '考研', '就业', '工作室'];
      return `
        <div class="sheet-backdrop" data-action="closeModalBackdrop">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <h2 style="margin:0;font-size:1.125rem;font-weight:700;">在学长学姐说发帖</h2>
              <button class="icon-btn-48" style="width:2.25rem;height:2.25rem;" data-action="closeModal">${ICONS.close}</button>
            </div>
            <div>
              <div style="font-size:0.8125rem;font-weight:600;margin-bottom:0.4375rem;">你是哪一届的（必填）</div>
              <div class="chip-wrap">
                ${cohorts.map(ch => `<button class="mono-chip ${data.cohort === ch ? 'inverted' : ''}" data-action="expPickCohort" data-val="${ch}">${ch}</button>`).join('')}
              </div>
            </div>
            <div>
              <div style="font-size:0.8125rem;font-weight:600;margin-bottom:0.4375rem;">主题分类</div>
              <div class="chip-wrap">
                ${expCats.map(ct => `<button class="mono-chip ${data.category === ct ? 'inverted' : ''}" data-action="expPickCat" data-val="${ct}">${ct}</button>`).join('')}
              </div>
            </div>
            <textarea class="field-textarea" id="exp-compose-body" style="min-height:7rem;" placeholder="分享你的考研、留学、毕设或工作室避坑经验，帮助后来的央美同学...">${esc(data.body || '')}</textarea>
            <button class="btn-primary-full" data-action="submitSeniorExp">发布经验</button>
          </div>
        </div>
      `;
    }

    if (type === 'allColleges') {
      return `
        <div class="sheet-backdrop" data-action="closeModalBackdrop">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <h2 style="margin:0;font-size:1.125rem;">全部 18 个央美院系</h2>
              <button class="icon-btn-48" style="width:2.25rem;height:2.25rem;" data-action="closeModal">${ICONS.close}</button>
            </div>
            <button class="mono-chip ${nav.courseFilters.college === '全部学院' ? 'inverted' : ''}" data-action="pickCollegeModal" data-val="全部学院">全部学院</button>
            ${Object.entries(D.CAFA_CAMPUS_COLLEGES).map(([campus, list]) => `
              <div>
                <div style="font-size:0.75rem;font-weight:700;color:var(--active-tint);margin-bottom:0.4375rem;">${esc(campus)}</div>
                <div class="college-grid">
                  ${list.map(cl => `<button class="mono-chip ${nav.courseFilters.college === cl ? 'inverted' : ''}" style="justify-content:center;padding:0.5rem 0.5rem;" data-action="pickCollegeModal" data-val="${esc(cl)}">${esc(cl)}</button>`).join('')}
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      `;
    }

    if (type === 'addReview') {
      return `
        <div class="sheet-backdrop" data-action="closeModalBackdrop">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <h2 style="margin:0;font-size:1.125rem;">补充课程真实评价</h2>
            <textarea class="field-textarea" id="rev-content" placeholder="聊聊课堂氛围、作业量、给分情况与选课建议..."></textarea>
            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:0.5rem;">
              <div><div style="font-size:0.75rem;color:var(--ink-secondary);margin-bottom:0.25rem;">推荐 (1-5)</div><input class="field-input" id="rev-rec" type="number" min="1" max="5" value="5" /></div>
              <div><div style="font-size:0.75rem;color:var(--ink-secondary);margin-bottom:0.25rem;">给分 (1-5)</div><input class="field-input" id="rev-diff" type="number" min="1" max="5" value="4" /></div>
              <div><div style="font-size:0.75rem;color:var(--ink-secondary);margin-bottom:0.25rem;">肝度 (1-5)</div><input class="field-input" id="rev-work" type="number" min="1" max="5" value="3" /></div>
            </div>
            <div class="chip-wrap">
              ${D.SUITABLE_OPTIONS.map(s => `<button class="mono-chip ${data.suitable === s ? 'inverted' : ''}" data-action="revPickSuit" data-val="${esc(s)}">${esc(s)}</button>`).join('')}
            </div>
            <button class="btn-primary-full" data-action="submitReview" data-id="${esc(data.courseId)}">提交评价</button>
          </div>
        </div>
      `;
    }

    if (type === 'createCourse') {
      return `
        <div class="sheet-backdrop" data-action="closeModalBackdrop">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <h2 style="margin:0;font-size:1.125rem;">共建选课评价</h2>
            <input class="field-input" id="cc-name" placeholder="课程名称（必填）" />
            <input class="field-input" id="cc-instructor" placeholder="授课教师（必填）" />
            <div class="chip-wrap">
              ${D.COURSE_TYPES.map(t => `<button class="mono-chip ${data.courseType === t ? 'inverted' : ''}" data-action="ccPickType" data-val="${esc(t)}">${esc(t)}</button>`).join('')}
            </div>
            <div class="chip-wrap">
              ${Object.keys(D.CAFA_CAMPUS_COLLEGES).map(cp => `<button class="mono-chip ${data.campus === cp ? 'inverted' : ''}" data-action="ccPickCampus" data-val="${esc(cp)}">${esc(cp)}</button>`).join('')}
            </div>
            <textarea class="field-textarea" id="cc-summary" placeholder="简述这门课的体验、作业量与给分情况..."></textarea>
            <button class="btn-primary-full" data-action="submitNewCourse">添加到选课档案</button>
          </div>
        </div>
      `;
    }

    return '';
  }

  // ---------------- Bottom Nav & FAB Rendering ----------------
  function renderBottomChrome() {
    const isSub = nav.stack.length > 0;
    const navWrap = document.getElementById('bottom-nav-wrap');
    const fab = document.getElementById('main-fab');

    if (isSub) {
      navWrap.classList.add('hidden');
      const top = nav.stack[nav.stack.length - 1];
      if (top && (top.name === 'categoryFeed' || top.name === 'seniorExperience')) {
        fab.classList.remove('hidden');
        fab.style.bottom = 'calc(1.375rem + var(--sab))';
      } else {
        fab.classList.add('hidden');
      }
      return;
    }

    navWrap.classList.remove('hidden');
    fab.classList.remove('hidden');
    fab.style.bottom = 'calc(5.5rem + var(--sab))';

    const unread = unreadCount();
    const tabs = [
      { key: 'home', label: '首页', on: ICONS.homeFilled, off: ICONS.home },
      { key: 'discover', label: '发现', on: ICONS.exploreFilled, off: ICONS.explore },
      { key: 'messages', label: '消息', on: ICONS.mailFilled, off: ICONS.mail },
      { key: 'profile', label: '我的', on: ICONS.personFilled, off: ICONS.person }
    ];

    navWrap.innerHTML = `
      <nav class="aurora-nav-shell" aria-label="主导航">
        <div class="aurora-nav-inner">
          ${tabs.map(t => {
            const active = nav.activeTab === t.key;
            return `
              <button class="nav-tab-btn ${active ? 'active' : ''}" data-action="switchTab" data-tab="${t.key}">
                <span class="nav-icon-wrap">
                  ${active ? t.on : t.off}
                  ${t.key === 'messages' && unread > 0 ? `<span class="red-badge">${unread > 99 ? '99+' : unread}</span>` : ''}
                </span>
                <span class="nav-label">${t.label}</span>
                <span class="nav-active-bar"></span>
              </button>
            `;
          }).join('')}
        </div>
      </nav>
    `;
  }

  // ---------------- Leftward Auto-Scroll Marquee (DiscoverScreen.kt) ----------------
  function stopMarquee() {
    if (nav.marqueeRaf) {
      cancelAnimationFrame(nav.marqueeRaf);
      nav.marqueeRaf = null;
    }
  }

  function startMarquee() {
    stopMarquee();
    const vp = document.getElementById('hot-topics-marquee');
    if (!vp) return;
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

    vp.addEventListener('touchstart', () => { nav.marqueePaused = true; }, { passive: true });
    vp.addEventListener('touchend', () => { setTimeout(() => { nav.marqueePaused = false; }, 1200); }, { passive: true });
    vp.addEventListener('mouseenter', () => { nav.marqueePaused = true; });
    vp.addEventListener('mouseleave', () => { nav.marqueePaused = false; });

    function step() {
      if (!document.body.contains(vp)) {
        stopMarquee();
        return;
      }
      if (!nav.marqueePaused) {
        vp.scrollLeft += 0.72;
        const half = vp.scrollWidth / 2;
        if (half > 0 && vp.scrollLeft >= half) {
          vp.scrollLeft -= half;
        }
      }
      nav.marqueeRaf = requestAnimationFrame(step);
    }
    nav.marqueeRaf = requestAnimationFrame(step);
  }

  // ---------------- Pull-to-Refresh on Home ----------------
  function attachPullToRefresh() {
    const scroller = document.getElementById('home-scroll');
    const ind = document.getElementById('ptr-indicator');
    if (!scroller || !ind) return;
    let startY = 0, pulling = false;
    scroller.addEventListener('touchstart', (e) => {
      if (scroller.scrollTop <= 0 && e.touches.length === 1) {
        startY = e.touches[0].clientY;
        pulling = true;
      }
    }, { passive: true });
    scroller.addEventListener('touchmove', (e) => {
      if (!pulling) return;
      const dy = e.touches[0].clientY - startY;
      if (dy > 58 && scroller.scrollTop <= 0) {
        ind.classList.add('show');
      }
    }, { passive: true });
    scroller.addEventListener('touchend', () => {
      if (pulling && ind.classList.contains('show')) {
        setTimeout(() => {
          ind.classList.remove('show');
          showToast('已刷新树窝最新内容');
        }, 550);
      }
      pulling = false;
    }, { passive: true });
  }

  // ---------------- Master Render ----------------
  function render() {
    applyTheme();
    stopMarquee();

    const viewport = document.getElementById('main-viewport');
    const modalRoot = document.getElementById('modal-root');

    let html = '';
    if (nav.stack.length > 0) {
      const top = nav.stack[nav.stack.length - 1];
      if (top.name === 'postDetail') html = renderPostDetailScreen(top.params.id);
      else if (top.name === 'courseArchive') html = renderCourseArchiveScreen();
      else if (top.name === 'courseDetail') html = renderCourseDetailScreen(top.params.id);
      else if (top.name === 'seniorExperience') html = renderSeniorScreen();
      else if (top.name === 'expDetail') html = renderExpDetailScreen(top.params.id);
      else if (top.name === 'categoryFeed') html = renderCategoryFeedScreen(top.params.catKey);
      else if (top.name === 'search') html = renderSearchScreen();
      else if (top.name === 'hotTopics') html = renderHotTopicsScreen();
      else if (top.name === 'topicDetail') html = renderTopicDetailScreen(top.params.tag);
      else if (top.name === 'profileCollection') html = renderProfileCollectionScreen(top.params.colKey);
      else if (top.name === 'settings') html = renderSettingsScreen();
      else if (top.name === 'verification') html = renderVerificationScreen();
    } else {
      if (nav.activeTab === 'home') html = renderHomeScreen();
      else if (nav.activeTab === 'discover') html = renderDiscoverScreen();
      else if (nav.activeTab === 'messages') html = renderMessagesScreen();
      else if (nav.activeTab === 'profile') html = renderProfileScreen();
    }

    viewport.innerHTML = html;
    modalRoot.innerHTML = renderModal();
    renderBottomChrome();

    if (nav.stack.length === 0 && nav.activeTab === 'discover') {
      startMarquee();
    }
    if (nav.stack.length === 0 && nav.activeTab === 'home') {
      attachPullToRefresh();
    }
    bindDynamicInputs();
  }

  function bindDynamicInputs() {
    const courseSearch = document.getElementById('course-search-input');
    if (courseSearch) {
      courseSearch.addEventListener('input', (e) => {
        nav.courseFilters.query = e.target.value;
        const pos = e.target.selectionStart;
        render();
        const next = document.getElementById('course-search-input');
        if (next) { next.focus(); next.setSelectionRange(pos, pos); }
      });
    }

    const globalSearch = document.getElementById('global-search-input');
    if (globalSearch) {
      globalSearch.addEventListener('input', (e) => {
        nav.searchQuery = e.target.value;
        const pos = e.target.selectionStart;
        render();
        const next = document.getElementById('global-search-input');
        if (next) { next.focus(); next.setSelectionRange(pos, pos); }
      });
      globalSearch.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          triggerAiStats(nav.searchQuery);
        }
      });
    }

    const verifyFileInput = document.getElementById('verify-file-input');
    if (verifyFileInput) {
      verifyFileInput.addEventListener('change', (e) => {
        if (e.target.files && e.target.files.length > 0) {
          state.persona.identityType = nav.verifyDraftType || state.persona.identityType || '在读学生';
          state.persona.isVerified = true;
          saveState();
          render();
          showToast('已核验央美身份凭证，认证通过');
        }
      });
    }

    const personaInput = document.getElementById('persona-nick-input');
    if (personaInput && nav.modal && nav.modal.type === 'personaEditor') {
      personaInput.addEventListener('input', (e) => {
        nav.modal.data.nickname = e.target.value;
      });
    }

    const commentForm = document.getElementById('comment-form');
    if (commentForm) {
      commentForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const input = document.getElementById('comment-input');
        const text = (input && input.value || '').trim();
        if (!text) return;
        const postId = commentForm.getAttribute('data-post-id');
        const post = state.posts.find(p => p.id === postId);
        if (post) {
          post.comments.push({
            id: 'c_' + Date.now(),
            author: { id: state.persona.id, nickname: state.persona.nickname, avatarSeed: state.persona.avatarSeed },
            content: text,
            createdAt: '刚刚',
            likes: 0
          });
          saveState();
          render();
          showToast('评论已发布');
        }
      });
    }
  }

  // ---------------- Event Delegation ----------------
  function pushScreen(name, params) {
    nav.stack.push({ name, params: params || {} });
    render();
  }

  function popScreen() {
    if (nav.stack.length > 0) {
      nav.stack.pop();
      render();
    }
  }

  function openPublishModal(defaultCat) {
    nav.modal = {
      type: 'publish',
      data: { category: defaultCat || 'ART_CREATION', title: '', body: '', price: '', location: '', tags: [] }
    };
    render();
  }

  document.addEventListener('click', (e) => {
    const actionEl = e.target.closest('[data-action]');
    if (actionEl) {
      e.stopPropagation();
      const act = actionEl.getAttribute('data-action');

      if (act === 'closeModalBackdrop') {
        if (e.target === actionEl) { nav.modal = null; render(); }
        return;
      }
      if (act === 'closeModal') { nav.modal = null; render(); return; }
      if (act === 'goBack') { popScreen(); return; }

      if (act === 'switchTab') {
        nav.activeTab = actionEl.getAttribute('data-tab');
        nav.stack = [];
        render();
        return;
      }

      if (act === 'homeTab') {
        state.homeTab = actionEl.getAttribute('data-tab');
        render();
        return;
      }

      if (act === 'toggleFold') {
        const id = actionEl.getAttribute('data-id');
        if (nav.expandedPosts.has(id)) nav.expandedPosts.delete(id);
        else nav.expandedPosts.add(id);
        render();
        return;
      }

      if (act === 'likePost') {
        const p = state.posts.find(x => x.id === actionEl.getAttribute('data-id'));
        if (p) {
          p.isLiked = !p.isLiked;
          p.likeCount += p.isLiked ? 1 : -1;
          saveState();
          render();
        }
        return;
      }

      if (act === 'bookmarkPost') {
        const p = state.posts.find(x => x.id === actionEl.getAttribute('data-id'));
        if (p) {
          p.isBookmarked = !p.isBookmarked;
          p.bookmarkCount += p.isBookmarked ? 1 : -1;
          saveState();
          render();
          showToast(p.isBookmarked ? '已加入我的收藏' : '已取消收藏');
        }
        return;
      }

      if (act === 'sharePost') {
        const p = state.posts.find(x => x.id === actionEl.getAttribute('data-id'));
        if (p && navigator.share) {
          navigator.share({ title: p.title || '杨梅树窝', text: p.body.slice(0, 80), url: location.href }).catch(() => {});
        } else {
          showToast('链接已准备好，可分享给央美同学');
        }
        return;
      }

      if (act === 'openPost') {
        pushScreen('postDetail', { id: actionEl.getAttribute('data-id') });
        return;
      }

      if (act === 'openSearch') {
        pushScreen('search');
        return;
      }

      if (act === 'clearSearch') {
        nav.searchQuery = '';
        nav.searchScope = 'ALL';
        render();
        return;
      }

      if (act === 'searchScope') {
        nav.searchScope = actionEl.getAttribute('data-val');
        render();
        return;
      }

      if (act === 'runAiSearch') {
        triggerAiStats(nav.searchQuery);
        return;
      }

      if (act === 'quickAiPrompt') {
        const promptQ = actionEl.getAttribute('data-q') || '';
        nav.searchQuery = promptQ;
        const top = nav.stack[nav.stack.length - 1];
        if (!top || top.name !== 'search') {
          nav.stack.push({ name: 'search', params: {} });
        }
        triggerAiStats(promptQ);
        return;
      }

      if (act === 'quickSearch') {
        nav.searchQuery = actionEl.getAttribute('data-q');
        render();
        return;
      }

      if (act === 'openPublishWithTag') {
        const tg = actionEl.getAttribute('data-tag');
        nav.modal = {
          type: 'publish',
          data: { category: 'TREE_HOLE', title: '', body: '', price: '', location: '', tags: tg ? [tg] : [] }
        };
        render();
        return;
      }

      if (act === 'openHotTopics') {
        pushScreen('hotTopics');
        return;
      }

      if (act === 'openTopic') {
        pushScreen('topicDetail', { tag: actionEl.getAttribute('data-tag') });
        return;
      }

      if (act === 'openDiscoverTile') {
        const key = actionEl.getAttribute('data-key');
        if (key === 'COURSE_ARCHIVE') pushScreen('courseArchive');
        else if (key === 'SENIOR_EXPERIENCE') pushScreen('seniorExperience');
        else {
          nav.categorySubFilter = '全部';
          pushScreen('categoryFeed', { catKey: key });
        }
        return;
      }

      if (act === 'catSubFilter') {
        nav.categorySubFilter = actionEl.getAttribute('data-val');
        render();
        return;
      }

      if (act === 'openPublishInCat') {
        openPublishModal(actionEl.getAttribute('data-cat'));
        return;
      }

      // Course actions
      if (act === 'courseCampus') {
        nav.courseFilters.campus = actionEl.getAttribute('data-val');
        nav.courseFilters.college = '全部学院';
        render();
        return;
      }
      if (act === 'courseType') {
        nav.courseFilters.type = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'courseCollege') {
        nav.courseFilters.college = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'openAllColleges') {
        nav.modal = { type: 'allColleges', data: {} };
        render();
        return;
      }
      if (act === 'pickCollegeModal') {
        nav.courseFilters.college = actionEl.getAttribute('data-val');
        nav.modal = null;
        render();
        return;
      }
      if (act === 'openCourseDetail') {
        pushScreen('courseDetail', { id: actionEl.getAttribute('data-id') });
        return;
      }
      if (act === 'bookmarkCourse') {
        const c = state.courses.find(x => x.id === actionEl.getAttribute('data-id'));
        if (c) { c.isBookmarked = !c.isBookmarked; saveState(); render(); showToast(c.isBookmarked ? '已收藏课程' : '已取消收藏'); }
        return;
      }
      if (act === 'openAddReview') {
        nav.modal = { type: 'addReview', data: { courseId: actionEl.getAttribute('data-id'), suitable: '跨专业友好' } };
        render();
        return;
      }
      if (act === 'revPickSuit') {
        if (nav.modal) nav.modal.data.suitable = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'submitReview') {
        const c = state.courses.find(x => x.id === actionEl.getAttribute('data-id'));
        const content = (document.getElementById('rev-content')?.value || '').trim();
        if (!content) { showToast('请先写下你的课程体验'); return; }
        const rec = Math.max(1, Math.min(5, Number(document.getElementById('rev-rec')?.value || 5)));
        const diff = Math.max(1, Math.min(5, Number(document.getElementById('rev-diff')?.value || 4)));
        const work = Math.max(1, Math.min(5, Number(document.getElementById('rev-work')?.value || 3)));
        const suit = nav.modal?.data?.suitable || '跨专业友好';
        if (c) {
          c.reviews.unshift({
            id: 'r_' + Date.now(),
            author: { id: state.persona.id, nickname: state.persona.nickname, avatarSeed: state.persona.avatarSeed },
            semester: c.courseType,
            content,
            workload: work,
            difficulty: diff,
            recommend: rec,
            attendanceMode: '偶尔抽查',
            examFormat: '作品汇报',
            suitableTag: suit,
            createdAt: '刚刚',
            helpfulCount: 1
          });
          const len = c.reviews.length;
          c.workloadScore = c.reviews.reduce((a, b) => a + b.workload, 0) / len;
          c.difficultyScore = c.reviews.reduce((a, b) => a + b.difficulty, 0) / len;
          c.recommendScore = c.reviews.reduce((a, b) => a + b.recommend, 0) / len;
          c.suitableFor = suit;
          saveState();
        }
        nav.modal = null;
        render();
        showToast('课程评价已提交');
        return;
      }
      if (act === 'openCreateCourse') {
        nav.modal = { type: 'createCourse', data: { courseType: '跨院选修', campus: '望京本院' } };
        render();
        return;
      }
      if (act === 'ccPickType') { nav.modal.data.courseType = actionEl.getAttribute('data-val'); render(); return; }
      if (act === 'ccPickCampus') { nav.modal.data.campus = actionEl.getAttribute('data-val'); render(); return; }
      if (act === 'submitNewCourse') {
        const name = (document.getElementById('cc-name')?.value || '').trim();
        const instructor = (document.getElementById('cc-instructor')?.value || '').trim();
        const summary = (document.getElementById('cc-summary')?.value || '').trim();
        if (!name || !instructor) { showToast('请填写课程名称和授课教师'); return; }
        const campus = nav.modal.data.campus || '望京本院';
        const college = (D.CAFA_CAMPUS_COLLEGES[campus] || ['设计学院'])[0];
        state.courses.unshift({
          id: 'course_' + Date.now(),
          name, instructor, campus, college,
          major: '人文理论与策展',
          courseType: nav.modal.data.courseType || '跨院选修',
          workloadScore: 3.0, difficultyScore: 4.5, recommendScore: 4.5,
          suitableFor: '跨专业友好', isBookmarked: false,
          summary: summary || '同学共建新课，欢迎大家补充更多评价。',
          reviews: []
        });
        saveState();
        nav.modal = null;
        render();
        showToast('新课程已加入档案');
        return;
      }

      // Senior experience
      if (act === 'expCat') { nav.expCategory = actionEl.getAttribute('data-val'); render(); return; }
      if (act === 'openExpDetail') { pushScreen('expDetail', { id: actionEl.getAttribute('data-id') }); return; }
      if (act === 'bookmarkExp') {
        const ex = state.experiences.find(x => x.id === actionEl.getAttribute('data-id'));
        if (ex) { ex.isBookmarked = !ex.isBookmarked; ex.bookmarkCount += ex.isBookmarked ? 1 : -1; saveState(); render(); showToast(ex.isBookmarked ? '已收藏经验' : '已取消收藏'); }
        return;
      }
      if (act === 'expPickCohort') {
        nav.modal.data.body = document.getElementById('exp-compose-body')?.value || '';
        nav.modal.data.cohort = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'expPickCat') {
        nav.modal.data.body = document.getElementById('exp-compose-body')?.value || '';
        nav.modal.data.category = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'submitSeniorExp') {
        const body = (document.getElementById('exp-compose-body')?.value || '').trim();
        if (!body) { showToast('请先写下你的经验分享'); return; }
        const firstLine = body.split('\n')[0].slice(0, 26);
        state.experiences.unshift({
          id: 'exp_' + Date.now(),
          category: nav.modal.data.category || '留学',
          title: firstLine,
          summary: body.slice(0, 68),
          body,
          author: { id: state.persona.id, nickname: state.persona.nickname, avatarSeed: state.persona.avatarSeed },
          college: '中央美术学院',
          major: nav.modal.data.category || '经验分享',
          year: nav.modal.data.cohort || '2024届',
          readMinutes: Math.max(1, Math.ceil(body.length / 280)),
          likeCount: 1,
          bookmarkCount: 0,
          isBookmarked: false,
          tags: [nav.modal.data.category || '经验', nav.modal.data.cohort || '2024届'],
          comments: []
        });
        saveState();
        nav.modal = null;
        render();
        showToast('经验已发布到学长学姐说');
        return;
      }

      // Messages
      if (act === 'msgFilter') { state.msgFilter = actionEl.getAttribute('data-key'); render(); return; }
      if (act === 'markAllRead') { state.notifications.forEach(n => { n.isUnread = false; }); saveState(); render(); return; }
      if (act === 'readNotif') {
        const n = state.notifications.find(x => x.id === actionEl.getAttribute('data-id'));
        if (n) { n.isUnread = false; saveState(); render(); }
        return;
      }

      // Profile & Persona Editor
      if (act === 'toggleDarkTheme') {
        state.darkTheme = !state.darkTheme;
        saveState();
        render();
        return;
      }
      if (act === 'randomizePersona') {
        const used = new Set(state.posts.map(p => p.author.nickname));
        used.add(state.persona.nickname);
        state.persona.nickname = D.nextUniqueNickname(used);
        state.persona.avatarSeed = D.sampleDistinctAvatarSeeds(1, state.persona.avatarSeed)[0];
        saveState();
        render();
        showToast('已更换全新匿名形象：' + state.persona.nickname);
        return;
      }
      if (act === 'openPersonaEditor') {
        const used = new Set(state.posts.map(p => p.author.nickname));
        nav.modal = {
          type: 'personaEditor',
          data: {
            nickname: state.persona.nickname,
            seed: state.persona.avatarSeed,
            batch: D.sampleNicknameBatch(6, used),
            seeds: D.sampleDistinctAvatarSeeds(10, state.persona.avatarSeed)
          }
        };
        render();
        return;
      }
      if (act === 'refreshNickBatch') {
        const used = new Set(state.posts.map(p => p.author.nickname));
        nav.modal.data.batch = D.sampleNicknameBatch(6, used);
        render();
        return;
      }
      if (act === 'refreshAvatarBatch') {
        nav.modal.data.seeds = D.sampleDistinctAvatarSeeds(10, nav.modal.data.seed);
        render();
        return;
      }
      if (act === 'pickNick') {
        nav.modal.data.nickname = actionEl.getAttribute('data-nick');
        render();
        return;
      }
      if (act === 'pickAvatarSeed') {
        nav.modal.data.seed = Number(actionEl.getAttribute('data-seed'));
        render();
        return;
      }
      if (act === 'rollSingleNick') {
        const used = new Set(state.posts.map(p => p.author.nickname));
        used.add(nav.modal.data.nickname);
        nav.modal.data.nickname = D.nextUniqueNickname(used);
        render();
        return;
      }
      if (act === 'rollPersonaBoth') {
        const used = new Set(state.posts.map(p => p.author.nickname));
        used.add(nav.modal.data.nickname);
        nav.modal.data.nickname = D.nextUniqueNickname(used);
        nav.modal.data.seed = D.sampleDistinctAvatarSeeds(1, nav.modal.data.seed)[0];
        render();
        return;
      }
      if (act === 'savePersona') {
        const nick = (nav.modal.data.nickname || '').trim();
        if (nick.length < 2) { showToast('昵称至少 2 个字'); return; }
        state.persona.nickname = nick;
        state.persona.avatarSeed = nav.modal.data.seed;
        saveState();
        nav.modal = null;
        render();
        showToast('匿名形象已更新');
        return;
      }

      if (act === 'openVerification') {
        nav.verifyDraftType = state.persona.identityType || '在读学生';
        pushScreen('verification');
        return;
      }
      if (act === 'verifyPickType') { nav.verifyDraftType = actionEl.getAttribute('data-val'); render(); return; }
      if (act === 'completeVerification') {
        state.persona.identityType = nav.verifyDraftType || state.persona.identityType || '在读学生';
        state.persona.isVerified = true;
        saveState();
        render();
        showToast('已完成央美校园身份认证');
        return;
      }

      if (act === 'openCollection') {
        pushScreen('profileCollection', { colKey: actionEl.getAttribute('data-key') });
        return;
      }
      if (act === 'openSettings') {
        pushScreen('settings');
        return;
      }
      if (act === 'profileMenu') {
        const k = actionEl.getAttribute('data-key');
        if (k === 'SETTINGS') pushScreen('settings');
        else if (k === 'VERIFY') {
          nav.verifyDraftType = state.persona.identityType || '在读学生';
          pushScreen('verification');
        } else pushScreen('profileCollection', { colKey: k });
        return;
      }
      if (act === 'deleteDraft') {
        state.drafts = state.drafts.filter(d => d.id !== actionEl.getAttribute('data-id'));
        saveState();
        render();
        return;
      }

      // Publish Modal
      if (act === 'pubPickCat') {
        nav.modal.data.title = document.getElementById('pub-title')?.value || '';
        nav.modal.data.body = document.getElementById('pub-body')?.value || '';
        nav.modal.data.category = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'pubPickLoc') {
        nav.modal.data.title = document.getElementById('pub-title')?.value || '';
        nav.modal.data.body = document.getElementById('pub-body')?.value || '';
        const v = actionEl.getAttribute('data-val');
        nav.modal.data.location = nav.modal.data.location === v ? '' : v;
        render();
        return;
      }
      if (act === 'pubToggleTag') {
        nav.modal.data.title = document.getElementById('pub-title')?.value || '';
        nav.modal.data.body = document.getElementById('pub-body')?.value || '';
        const tg = actionEl.getAttribute('data-val');
        const arr = nav.modal.data.tags || [];
        nav.modal.data.tags = arr.includes(tg) ? arr.filter(x => x !== tg) : [...arr, tg];
        render();
        return;
      }
      if (act === 'saveDraft') {
        const title = (document.getElementById('pub-title')?.value || '').trim();
        const body = (document.getElementById('pub-body')?.value || '').trim();
        if (!title && !body) { showToast('内容为空，未保存草稿'); return; }
        state.drafts.unshift({ id: 'draft_' + Date.now(), category: nav.modal.data.category, title, body, createdAt: '刚刚' });
        saveState();
        nav.modal = null;
        render();
        showToast('已保存到我的草稿');
        return;
      }
      if (act === 'submitPost') {
        const title = (document.getElementById('pub-title')?.value || '').trim();
        const body = (document.getElementById('pub-body')?.value || '').trim();
        const price = (document.getElementById('pub-price')?.value || '').trim();
        if (!body && !title) { showToast('先写点内容再发布吧'); return; }
        state.posts.unshift({
          id: 'post_' + Date.now(),
          title,
          body: body || title,
          author: { id: state.persona.id, nickname: state.persona.nickname, avatarSeed: state.persona.avatarSeed },
          category: nav.modal.data.category,
          topicTags: nav.modal.data.tags || [],
          mediaList: [],
          createdAt: '刚刚',
          viewCount: 1,
          likeCount: 0,
          bookmarkCount: 0,
          isLiked: false,
          isBookmarked: false,
          isMine: true,
          price: price || undefined,
          location: nav.modal.data.location || undefined,
          comments: []
        });
        saveState();
        nav.modal = null;
        if (nav.stack.length === 0) nav.activeTab = 'home';
        state.homeTab = 'LATEST';
        render();
        showToast('帖子已匿名发布到树窝');
        return;
      }

      // Settings
      if (act === 'toggleSetting') {
        const k = actionEl.getAttribute('data-key');
        state.settings[k] = !state.settings[k];
        saveState();
        render();
        return;
      }
      if (act === 'addBlockedKw') {
        const kw = (document.getElementById('kw-input')?.value || '').trim();
        if (kw && !state.settings.blockedKeywords.includes(kw)) {
          state.settings.blockedKeywords.push(kw);
          saveState();
          render();
          showToast('已添加屏蔽词：' + kw);
        }
        return;
      }
      if (act === 'removeBlockedKw') {
        const kw = actionEl.getAttribute('data-kw');
        state.settings.blockedKeywords = state.settings.blockedKeywords.filter(x => x !== kw);
        saveState();
        render();
        return;
      }
      if (act === 'submitFeedback') {
        const fb = (document.getElementById('feedback-input')?.value || '').trim();
        if (!fb) { showToast('请输入建议内容'); return; }
        document.getElementById('feedback-input').value = '';
        showToast('感谢反馈，已记录你的建议');
        return;
      }
      if (act === 'resetVerifyDemo') {
        state.persona.isVerified = false;
        saveState();
        render();
        showToast('已重置为未认证状态');
        return;
      }
      if (act === 'clearLocalCache') {
        avatarCache.clear();
        showToast('已清理图片内存与本地缓存');
        return;
      }
      return;
    }

    // Card tap -> open post detail
    const postCard = e.target.closest('[data-open-post]');
    if (postCard) {
      pushScreen('postDetail', { id: postCard.getAttribute('data-open-post') });
    }
  });

  // ---------------- Boot & Splash Screen ----------------
  function boot() {
    applyTheme();
    buildHollowLogos();

    // Render splash hollow logo
    const splashLogo = document.getElementById('splash-logo-img');
    if (splashLogo) splashLogo.src = getLogoSrc(true);

    const enterBtn = document.getElementById('splash-enter-btn');
    const splashEl = document.getElementById('splash-screen');
    if (enterBtn && splashEl) {
      enterBtn.addEventListener('click', () => {
        splashEl.classList.add('exiting');
        setTimeout(() => splashEl.classList.add('hidden'), 340);
      });
    }

    const fab = document.getElementById('main-fab');
    if (fab) {
      fab.innerHTML = ICONS.plus;
      fab.addEventListener('click', () => {
        const top = nav.stack[nav.stack.length - 1];
        if (top && top.name === 'seniorExperience') {
          nav.modal = { type: 'createSenior', data: { cohort: '2024届', category: '留学', body: '' } };
          render();
          return;
        }
        const cat = (top && top.name === 'categoryFeed') ? top.params.catKey : 'TREE_HOLE';
        openPublishModal(cat);
      });
    }

    render();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
