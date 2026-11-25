/* ==========================================================================
   杨梅树窝 (YANGMEI TREE) — 1:1 Android-Parity Interactive Web Engine
   ========================================================================== */
(function () {
  'use strict';

  const D = window.YANGMEI_DATA;
  const STORAGE_KEY = 'yangmei_tree_web_state_v3';
  const LEGACY_STORAGE_KEYS = ['yangmei_tree_web_state_v2', 'yangmei_tree_web_state_v1', 'yangmei_tree_web_state'];
  const SESSION_BACKUP_KEY = 'yangmei_tree_web_session_backup';
  const IDB_NAME = 'yangmei_tree_persistent_db';
  const IDB_STORE = 'state_kv';
  const IDB_KEY = 'app_state_v3';

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
    close: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6L6 18M6 6l12 12"/></svg>',
    badge: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><rect x="3" y="5" width="18" height="14" rx="2"/><circle cx="9" cy="12" r="2.2"/><path d="M14 10h4M14 14h3" stroke-linecap="round"/></svg>',
    school: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><path d="M2 9l10-5 10 5-10 5-10-5z"/><path d="M6 11v5c0 1.8 2.7 3 6 3s6-1.2 6-3v-5"/><path d="M22 9v6" stroke-linecap="round"/></svg>',
    camera: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><path d="M4 7h3l2-2h6l2 2h3a1.5 1.5 0 0 1 1.5 1.5v10A1.5 1.5 0 0 1 20 20H4a1.5 1.5 0 0 1-1.5-1.5v-10A1.5 1.5 0 0 1 4 7z"/><circle cx="12" cy="13" r="3.5"/></svg>',
    photo: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="8.5" cy="9.5" r="1.5"/><path d="M21 15l-4.5-4.5L7 20"/></svg>',
    checkCircle: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M8.5 12.5l2.3 2.3 4.8-5.2"/></svg>',
    check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12.5l4.5 4.5L19 7.5"/></svg>',
    alert: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>',
    trash: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/></svg>',
    shieldCheck: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><path d="M9 12l2 2 4-4"/></svg>',
    shieldAlert: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><path d="M12 8v4M12 16h.01"/></svg>'
  };

  function catIcon(key) {
    const info = D.CATEGORIES[key];
    return ICONS[(info && info.icon) || 'sparkle'] || ICONS.sparkle;
  }

  // ---------------- Pre-baked Iridescent Hollow Logo (matches YangmeiBrandMark.kt, zero flash) ----------------
  const STATIC_LOGO_DARK = './web/logo-dark.png';
  const STATIC_LOGO_LIGHT = './web/logo-light.png';

  function buildHollowLogos() {
    // Pre-warm light variant in browser cache silently; splash dark logo is already preloaded in HTML
    const preloadLight = new Image();
    preloadLight.src = STATIC_LOGO_LIGHT;
  }

  function getLogoSrc(onDark) {
    return onDark ? STATIC_LOGO_DARK : STATIC_LOGO_LIGHT;
  }

  function refreshLogoImages() {
    document.querySelectorAll('img[data-yangmei-logo]').forEach(function (el) {
      const dark = el.getAttribute('data-yangmei-logo') === 'dark';
      const target = getLogoSrc(dark);
      if (el.getAttribute('src') !== target) el.src = target;
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
      _savedAt: 0,
      persona: {
        id: 'u_me',
        nickname: '美术馆北窗光',
        avatarSeed: 1091,
        identityType: '在读学生',
        isVerified: false
      },
      verificationStatus: 'UNVERIFIED', // UNVERIFIED | REVIEWING | VERIFIED | REJECTED
      verificationRecord: {
        identityType: '在读学生',
        proofType: '学生证',
        proofLabel: '',
        proofDataUrl: '',
        rejectReason: ''
      },
      darkTheme: false,
      homeTab: 'RECOMMENDED',
      msgFilter: 'ALL',
      posts: clone(D.INITIAL_POSTS),
      deletedPostIds: [],
      courses: clone(D.INITIAL_COURSES),
      experiences: clone(D.INITIAL_EXPERIENCES),
      hotTopics: clone(D.INITIAL_HOT_TOPICS),
      hotTopicsDate: '',
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

  function normalizeLoadedState(parsed, fromLegacyKey) {
    const def = defaultState();
    if (!parsed || typeof parsed !== 'object') return def;
    const merged = Object.assign(def, parsed);
    merged.persona = Object.assign(defaultState().persona, parsed.persona || {});
    merged.verificationRecord = Object.assign(defaultState().verificationRecord, parsed.verificationRecord || {});
    merged.settings = Object.assign(defaultState().settings, parsed.settings || {});
    merged.deletedPostIds = Array.isArray(parsed.deletedPostIds) ? parsed.deletedPostIds : [];
    merged.drafts = Array.isArray(parsed.drafts) ? parsed.drafts : [];
    merged.notifications = Array.isArray(parsed.notifications) ? parsed.notifications : [];

    // Preserve all user-authored posts and merge seed posts cleanly without resurrecting deleted posts
    const deletedSet = new Set(merged.deletedPostIds);
    const parsedPosts = Array.isArray(parsed.posts) ? parsed.posts : [];
    const seenIds = new Set();
    const cleanedPosts = [];
    parsedPosts.forEach(p => {
      if (!p || !p.id || deletedSet.has(p.id) || seenIds.has(p.id)) return;
      seenIds.add(p.id);
      cleanedPosts.push(p);
    });
    D.INITIAL_POSTS.forEach(seed => {
      if (!deletedSet.has(seed.id) && !seenIds.has(seed.id)) {
        seenIds.add(seed.id);
        cleanedPosts.push(clone(seed));
      }
    });
    merged.posts = cleanedPosts;

    // On legacy migration, preserve verification status only if explicitly VERIFIED
    if (fromLegacyKey && !parsed.verificationStatus) {
      merged.verificationStatus = merged.persona.isVerified ? 'VERIFIED' : 'UNVERIFIED';
    }
    if (merged.verificationStatus !== 'VERIFIED') {
      merged.persona.isVerified = false;
    } else {
      merged.persona.isVerified = true;
      // Privacy hygiene & quota safety: destroy verification photo once verified
      if (merged.verificationRecord.proofDataUrl) {
        merged.verificationRecord.proofDataUrl = '';
      }
    }
    return merged;
  }

  function readJsonStorage(storageObj, key) {
    try {
      const raw = storageObj.getItem(key);
      if (!raw) return null;
      return JSON.parse(raw);
    } catch (e) {
      return null;
    }
  }

  function loadState() {
    // 1. Primary localStorage key
    const primary = readJsonStorage(localStorage, STORAGE_KEY);
    if (primary) return normalizeLoadedState(primary, false);

    // 2. Auto-migrate from any previous version key so updates never wipe user posts
    for (let i = 0; i < LEGACY_STORAGE_KEYS.length; i++) {
      const legacy = readJsonStorage(localStorage, LEGACY_STORAGE_KEYS[i]);
      if (legacy) {
        const migrated = normalizeLoadedState(legacy, true);
        try {
          localStorage.setItem(STORAGE_KEY, JSON.stringify(migrated));
        } catch (e) { /* will be handled by compact fallback below */ }
        return migrated;
      }
    }

    // 3. Fallback to sessionStorage backup (helps in strict mobile WebViews)
    const sessionSnap = readJsonStorage(sessionStorage, SESSION_BACKUP_KEY);
    if (sessionSnap) return normalizeLoadedState(sessionSnap, false);

    return defaultState();
  }

  // IndexedDB dual-layer persistence (survives 5MB localStorage quota & mobile browser reloads)
  function openStateIDB() {
    return new Promise((resolve, reject) => {
      if (!window.indexedDB) { reject(new Error('no idb')); return; }
      try {
        const req = window.indexedDB.open(IDB_NAME, 1);
        req.onupgradeneeded = () => {
          const db = req.result;
          if (!db.objectStoreNames.contains(IDB_STORE)) {
            db.createObjectStore(IDB_STORE);
          }
        };
        req.onsuccess = () => resolve(req.result);
        req.onerror = () => reject(req.error);
      } catch (e) {
        reject(e);
      }
    });
  }

  function writeStateToIDB(snapshot) {
    openStateIDB().then(db => {
      try {
        const tx = db.transaction(IDB_STORE, 'readwrite');
        tx.objectStore(IDB_STORE).put(snapshot, IDB_KEY);
      } catch (e) { /* ignore */ }
    }).catch(() => { /* ignore */ });
  }

  function readStateFromIDB() {
    return openStateIDB().then(db => new Promise((resolve) => {
      try {
        const tx = db.transaction(IDB_STORE, 'readonly');
        const req = tx.objectStore(IDB_STORE).get(IDB_KEY);
        req.onsuccess = () => resolve(req.result || null);
        req.onerror = () => resolve(null);
      } catch (e) {
        resolve(null);
      }
    })).catch(() => null);
  }

  function buildCompactSnapshotForQuota(srcState) {
    // Strip only oversized inline base64 images so text posts/comments/verification NEVER fail to persist
    const copy = clone(srcState);
    if (copy.verificationRecord && copy.verificationRecord.proofDataUrl && copy.verificationRecord.proofDataUrl.length > 60000) {
      copy.verificationRecord.proofDataUrl = '';
    }
    (copy.posts || []).forEach(p => {
      if (Array.isArray(p.mediaList)) {
        p.mediaList = p.mediaList.filter(m => m && typeof m.src === 'string' && (!m.src.startsWith('data:') || m.src.length <= 180000));
      }
    });
    return copy;
  }

  let state = loadState();
  let idbHydrated = false;
  let userHasMutated = false;

  function saveState() {
    userHasMutated = true;
    state._savedAt = Date.now();
    const fullSnapshot = clone(state);

    // 1. Async write full state (with all images) to IndexedDB
    writeStateToIDB(fullSnapshot);

    // 2. Synchronous write to localStorage with quota-safe compact fallback
    let serialized = '';
    try {
      serialized = JSON.stringify(fullSnapshot);
      localStorage.setItem(STORAGE_KEY, serialized);
    } catch (e) {
      try {
        const compact = buildCompactSnapshotForQuota(fullSnapshot);
        serialized = JSON.stringify(compact);
        localStorage.setItem(STORAGE_KEY, serialized);
      } catch (e2) { /* ignore if storage is completely disabled */ }
    }

    // 3. Mirror to sessionStorage as an extra WebView safety net
    if (serialized) {
      try {
        sessionStorage.setItem(SESSION_BACKUP_KEY, serialized);
      } catch (e) { /* ignore */ }
    }
  }

  function hydrateFromIDB() {
    readStateFromIDB().then(idbRaw => {
      idbHydrated = true;
      if (!idbRaw || typeof idbRaw !== 'object') {
        // Seed IndexedDB from current localStorage state if it has user data
        if (state._savedAt > 0 || (state.posts || []).some(p => p.isMine)) {
          writeStateToIDB(clone(state));
        }
        return;
      }
      if (userHasMutated) return;
      const idbState = normalizeLoadedState(idbRaw, false);
      const localMineCount = (state.posts || []).filter(p => p.isMine).length;
      const idbMineCount = (idbState.posts || []).filter(p => p.isMine).length;
      const idbNewer = (idbState._savedAt || 0) > (state._savedAt || 0);

      if (idbNewer || (idbMineCount > localMineCount && !(state.deletedPostIds || []).length)) {
        // Restore newer full state (including any compressed post photos trimmed from localStorage)
        state = idbState;
        applyTheme();
        recalculateHotTopicsForDay(getLocalDayKey(), true);
        try {
          localStorage.setItem(STORAGE_KEY, JSON.stringify(buildCompactSnapshotForQuota(state)));
        } catch (e) { /* ignore */ }
        render();
      } else if (idbMineCount > 0) {
        // Restore any full mediaList images from IDB for matching posts if localStorage trimmed them
        const idbById = new Map((idbState.posts || []).map(p => [p.id, p]));
        let restoredMedia = false;
        (state.posts || []).forEach(p => {
          const fromIdb = idbById.get(p.id);
          if (fromIdb && (fromIdb.mediaList || []).length > (p.mediaList || []).length) {
            p.mediaList = clone(fromIdb.mediaList);
            restoredMedia = true;
          }
        });
        if (restoredMedia) render();
      }
    });
  }

  // Client-side Canvas Image Compression (prevents 5MB phone photos from overflowing storage)
  function compressImageFile(file, maxEdge, quality, onDone) {
    const reader = new FileReader();
    reader.onerror = () => onDone('');
    reader.onload = () => {
      const rawDataUrl = String(reader.result || '');
      const img = new Image();
      img.onload = () => {
        try {
          let w = img.naturalWidth || img.width;
          let h = img.naturalHeight || img.height;
          if (!w || !h) { onDone(rawDataUrl); return; }
          if (w > maxEdge || h > maxEdge) {
            if (w >= h) {
              h = Math.max(1, Math.round((h * maxEdge) / w));
              w = maxEdge;
            } else {
              w = Math.max(1, Math.round((w * maxEdge) / h));
              h = maxEdge;
            }
          }
          const canvas = document.createElement('canvas');
          canvas.width = w;
          canvas.height = h;
          const ctx = canvas.getContext('2d');
          ctx.drawImage(img, 0, 0, w, h);
          const compressed = canvas.toDataURL('image/jpeg', quality || 0.78);
          onDone(compressed && compressed.length < rawDataUrl.length ? compressed : rawDataUrl);
        } catch (e) {
          onDone(rawDataUrl);
        }
      };
      img.onerror = () => onDone(rawDataUrl);
      img.src = rawDataUrl;
    };
    reader.readAsDataURL(file);
  }

  // ---------------- Silent Daily 00:00 Hot Topics Engine ----------------
  function getLocalDayKey() {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  function strHash(str) {
    let h = 0;
    for (let i = 0; i < str.length; i++) {
      h = ((h << 5) - h + str.charCodeAt(i)) | 0;
    }
    return Math.abs(h);
  }

  function recalculateHotTopicsForDay(dayKey, skipSave) {
    const keyDay = dayKey || getLocalDayKey();
    const baseByTag = new Map();
    D.INITIAL_HOT_TOPICS.forEach(t => baseByTag.set(t.tag.toLowerCase(), clone(t)));
    (state.hotTopics || []).forEach(t => {
      const k = t.tag.toLowerCase();
      if (!baseByTag.has(k)) baseByTag.set(k, clone(t));
    });
    (state.posts || []).forEach(post => {
      (post.topicTags || []).forEach(rawT => {
        const tag = rawT.startsWith('#') ? rawT : '#' + rawT;
        const k = tag.toLowerCase();
        if (!baseByTag.has(k)) {
          baseByTag.set(k, {
            id: 'topic_live_' + strHash(k),
            tag,
            description: '央美同学正在热议的校园话题',
            discussionCount: 120,
            heatStatus: '新'
          });
        }
      });
    });

    const scored = Array.from(baseByTag.values()).map(topic => {
      const clean = topic.tag.replace(/^#/, '').toLowerCase();
      const matching = (state.posts || []).filter(p =>
        (p.topicTags || []).some(t => t.toLowerCase() === topic.tag.toLowerCase()) ||
        (p.body || '').toLowerCase().includes(clean) ||
        (p.title || '').toLowerCase().includes(clean)
      );
      const liveEngagement = matching.reduce((acc, p) =>
        acc + 68 + (p.likeCount || 0) * 4 + ((p.comments || []).length * 12) +
        (p.bookmarkCount || 0) * 5 + Math.floor((p.viewCount || 0) / 12) + (p.isMine ? 140 : 0),
        0
      );
      const normShift = (strHash(topic.id + '|' + keyDay) % 420) - 180;
      const baseOrigin = D.INITIAL_HOT_TOPICS.find(x => x.id === topic.id);
      const baseHeat = baseOrigin ? baseOrigin.discussionCount : 120;
      const dailyHeat = Math.max(36, baseHeat + liveEngagement + normShift);
      return {
        topic: Object.assign({}, topic, { discussionCount: dailyHeat }),
        isCustomNew: !baseOrigin,
        shift: normShift
      };
    }).sort((a, b) => b.topic.discussionCount - a.topic.discussionCount);

    let topRiserId = '';
    let maxShift = -9999;
    scored.forEach(item => {
      if (item.shift > maxShift) {
        maxShift = item.shift;
        topRiserId = item.topic.id;
      }
    });

    state.hotTopics = scored.map((item, idx) => {
      let status = '';
      if (idx === 0) status = '沸';
      else if (idx === 1 || idx === 2) status = '热';
      else if (item.isCustomNew || item.topic.id === topRiserId) status = '新';
      else if (idx === 4) status = '荐';
      return Object.assign({}, item.topic, { heatStatus: status });
    });
    state.hotTopicsDate = keyDay;
    if (!skipSave) saveState();
  }

  let midnightTimer = null;
  function scheduleSilentMidnightHotTopics() {
    clearTimeout(midnightTimer);
    const now = new Date();
    const nextMidnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 1, 0);
    const delayMs = Math.max(1000, nextMidnight.getTime() - now.getTime());
    midnightTimer = setTimeout(() => {
      recalculateHotTopicsForDay(getLocalDayKey());
      render();
      scheduleSilentMidnightHotTopics();
    }, delayMs);
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
    verifyDraft: {
      identityType: '在读学生',
      proofType: '学生证',
      proofLabel: '',
      proofDataUrl: '',
      agreedPolicy: false
    },
    verifyAuditStep: 0,
    verifyAuditTimers: [],
    pubDraft: null,
    modal: null,       // null | { type, data }
    marqueeRaf: null,
    marqueePaused: false,
    homeTabsScrollLeft: 0,
    scrollMap: {},
    hScrollMap: {},
    lastScreenKey: '',
    cloudConnected: false,
    account: null,
    cloudPosts: [],
    cloudSyncing: false,
    cloudError: '',
    moderating: false,
    feedbackSending: false
  };

  function getCloud() {
    return window.YANGMEI_CLOUD || null;
  }

  function getModeration() {
    return window.YANGMEI_MODERATION || null;
  }

  function showModerationBlock(reason) {
    nav.modal = {
      type: 'moderationBlock',
      data: {
        reason: reason || '自动审查未通过：内容或图片涉嫌违法违规及不良信息，禁止发布。'
      }
    };
    renderModalOnly();
  }

  async function moderateBeforePublish(textParts, mediaUrls) {
    const mod = getModeration();
    if (!mod) return { passed: true };
    const blockedKeywords = (state.settings && state.settings.blockedKeywords) || [];
    return await mod.inspectSubmission({
      textParts: Array.isArray(textParts) ? textParts : [textParts],
      mediaUrls: Array.isArray(mediaUrls) ? mediaUrls : [],
      blockedKeywords
    });
  }

  const autoSyncAttempted = new Set();

  function mergeCloudPostsIntoState(cloudPosts) {
    nav.cloudPosts = Array.isArray(cloudPosts) ? cloudPosts : [];
    const deletedSet = new Set(Array.isArray(state.deletedPostIds) ? state.deletedPostIds : []);
    const existingById = new Map((state.posts || []).map(p => [p.id, p]));
    const seedIds = new Set((D.INITIAL_POSTS || []).map(p => p.id));
    const currentUid = (nav.account && nav.account.publicUid) || (state.persona && state.persona.id) || '';

    const mergedCloud = nav.cloudPosts
      .filter(cp => cp && cp.id && !deletedSet.has(cp.id))
      .map(cp => {
        const local = existingById.get(cp.id);
        const isMine = Boolean(cp.isMine || (currentUid && cp.authorUid === currentUid) || (local && local.isMine));
        return {
          ...cp,
          _cloud: true,
          isMine,
          isLiked: local ? Boolean(local.isLiked) : false,
          isBookmarked: local ? Boolean(local.isBookmarked) : false,
          comments: (local && Array.isArray(local.comments) && local.comments.length >= (cp.comments || []).length)
            ? local.comments
            : (cp.comments || [])
        };
      });
    const cloudIdSet = new Set(mergedCloud.map(p => p.id));

    const localOnlyPosts = (state.posts || []).filter(p =>
      p && p.id && !deletedSet.has(p.id) && !cloudIdSet.has(p.id) && !seedIds.has(p.id) && !p._cloud
    );
    const seedPosts = (state.posts || []).filter(p =>
      p && p.id && !deletedSet.has(p.id) && !cloudIdSet.has(p.id) && seedIds.has(p.id)
    );

    state.posts = [...mergedCloud, ...localOnlyPosts, ...seedPosts];
    recalculateHotTopicsForDay(getLocalDayKey(), true);

    // Automatically retry uploading any unsynced local posts created by this user
    const cloud = getCloud();
    if (cloud && cloud.isReady() && nav.cloudConnected && localOnlyPosts.length > 0) {
      const acc = cloud.getAccount();
      localOnlyPosts.forEach(lp => {
        if (!lp || !lp.isMine || autoSyncAttempted.has(lp.id)) return;
        autoSyncAttempted.add(lp.id);
        if (acc && acc.publicUid) {
          lp.authorUid = lp.authorUid || acc.publicUid;
          if (lp.author) lp.author.id = lp.author.id || acc.publicUid;
        }
        cloud.publishPost(lp)
          .then(() => {
            lp._cloud = true;
            saveState();
            cloud.forceRefresh().catch(() => {});
          })
          .catch(() => {
            setTimeout(() => autoSyncAttempted.delete(lp.id), 20000);
          });
      });
    }
  }

  function initCloudBridge() {
    const cloud = getCloud();
    if (!cloud || cloud._yangmeiBound) return;
    cloud._yangmeiBound = true;
    cloud.init({
      onConnectionChanged(connected) {
        nav.cloudConnected = Boolean(connected);
        if (connected) nav.cloudError = '';
      },
      onAccountChanged(acc) {
        nav.account = acc;
        if (acc && acc.publicUid && state.persona) {
          state.persona.id = acc.publicUid;
        }
        const top = nav.stack.length > 0 ? nav.stack[nav.stack.length - 1] : null;
        if (!top || (top.name !== 'publish' && top.name !== 'verification' && top.name !== 'settings')) {
          render();
        }
      },
      onPostsSnapshot(posts) {
        nav.cloudConnected = true;
        nav.cloudError = '';
        mergeCloudPostsIntoState(posts);
        const top = nav.stack.length > 0 ? nav.stack[nav.stack.length - 1] : null;
        if (!top || (top.name !== 'publish' && top.name !== 'verification' && top.name !== 'settings' && top.name !== 'search')) {
          render();
        }
      },
      onSyncError(err) {
        nav.cloudError = (err && err.message) || '云端同步暂不可用';
        const top = nav.stack.length > 0 ? nav.stack[nav.stack.length - 1] : null;
        if (!top && nav.activeTab === 'home') {
          render();
        }
      }
    });
  }

  function requireVerified(actionLabel, onAllowed) {
    if (state.persona && state.persona.isVerified && state.verificationStatus === 'VERIFIED') {
      if (typeof onAllowed === 'function') onAllowed();
      return true;
    }
    nav.modal = { type: 'verificationGate', data: { actionLabel: actionLabel || '参与社区互动' } };
    render();
    return false;
  }

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
  function renderEditorialPostCard(post, options) {
    const allowDelete = Boolean(options && options.allowDelete && post.isMine);
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
          <div class="post-footer-right">
            ${allowDelete ? `
              <button class="footer-delete-btn" data-action="confirmDeletePost" data-id="${esc(post.id)}" aria-label="删除帖子">
                ${ICONS.trash}<span>删除</span>
              </button>
            ` : ''}
            <button class="footer-share-btn" data-action="sharePost" data-id="${esc(post.id)}" aria-label="分享">${ICONS.share}</button>
          </div>
        </div>
      </article>
    `;
  }

  // ---------------- Screen 1: HOME (首页) ----------------
  const HOME_TABS = [
    { key: 'RECOMMENDED', label: '推荐', icon: ICONS.sparkle },
    { key: 'LATEST', label: '最新', icon: ICONS.schedule },
    { key: 'RELATIONSHIP', label: '恋爱情感', icon: ICONS.heart },
    { key: 'MARKETPLACE', label: '闲置交换', icon: ICONS.store },
    { key: 'OPPORTUNITY', label: '校园机会', icon: ICONS.work },
    { key: 'LOST_FOUND', label: '寻物', icon: ICONS.find },
    { key: 'MUTUAL_AID', label: '校园互助', icon: ICONS.handshake }
  ];

  function getFilteredHomePosts(tabKey) {
    const all = visiblePosts();
    if (tabKey === 'RECOMMENDED') {
      const seedIds = new Set((D.INITIAL_POSTS || []).map(p => p.id));
      return [...all].sort((a, b) => {
        const aLive = (a._cloud || !seedIds.has(a.id)) ? 1 : 0;
        const bLive = (b._cloud || !seedIds.has(b.id)) ? 1 : 0;
        if (aLive !== bLive) return bLive - aLive;
        if (aLive === 1) {
          const aTs = Number(a._createdMs || a.createdAtMs || 0);
          const bTs = Number(b._createdMs || b.createdAtMs || 0);
          if (aTs !== bTs) return bTs - aTs;
        }
        return (b.likeCount + (b.comments.length * 3)) - (a.likeCount + (a.comments.length * 3));
      });
    }
    if (tabKey === 'RELATIONSHIP') return all.filter(p => p.category === 'RELATIONSHIP');
    if (tabKey === 'MARKETPLACE') return all.filter(p => p.category === 'MARKETPLACE');
    if (tabKey === 'OPPORTUNITY') return all.filter(p => p.category === 'OPPORTUNITY');
    if (tabKey === 'LOST_FOUND') return all.filter(p => p.category === 'LOST_FOUND');
    if (tabKey === 'MUTUAL_AID') return all.filter(p => p.category === 'MUTUAL_AID');
    return all;
  }

  function renderHomeFeedInnerHtml(tabKey) {
    const list = getFilteredHomePosts(tabKey);
    const liveCount = Array.isArray(nav.cloudPosts) ? nav.cloudPosts.length : 0;
    const statusText = nav.cloudConnected
      ? (liveCount > 0 ? `全校实时互通已开启 · 已同步 ${liveCount} 条校园动态` : '全校实时互通已开启 · 大家发帖即时可见')
      : (nav.cloudError ? '云端同步重连中 · 点击右侧刷新重试' : '正在连接全校实时树窝...');
    const cloudBar = `
      <div class="cloud-sync-strip">
        <div style="display:flex;align-items:center;gap:0.4rem;min-width:0;">
          <span class="cloud-sync-dot ${nav.cloudConnected ? '' : 'offline'}"></span>
          <span style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
            ${statusText}
          </span>
        </div>
        <div style="display:flex;align-items:center;gap:0.35rem;flex-shrink:0;">
          <button class="cloud-sync-btn subtle" data-action="cloudRefreshNow">${nav.cloudSyncing ? '同步中...' : '刷新'}</button>
        </div>
      </div>`;

    const feedHtml = list.length
      ? list.map(renderEditorialPostCard).join('')
      : `<div class="empty-state">
          <div class="empty-state-title">当前分区暂时还没有内容</div>
          <div class="empty-state-sub">下拉可刷新最新内容，或点击右下角 + 号发布第一条帖子</div>
        </div>`;

    return `
      <div class="ptr-indicator" id="ptr-indicator">✦ 正在同步全校最新内容...</div>
      ${cloudBar}
      ${feedHtml}
    `;
  }

  function renderHomeScreen() {
    if (!HOME_TABS.some(t => t.key === state.homeTab)) {
      state.homeTab = 'RECOMMENDED';
    }
    const tabsHtml = HOME_TABS.map(t => `
      <button class="aurora-tab-pill ${state.homeTab === t.key ? 'active' : ''}" data-action="homeTab" data-tab="${t.key}">
        ${t.icon}<span>${t.label}</span>
      </button>
    `).join('');

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
          ${renderHomeFeedInnerHtml(state.homeTab)}
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
          <div style="display:flex;align-items:center;">
            ${post.isMine ? `<button class="icon-btn-48" style="color:var(--vermilion);" data-action="confirmDeletePost" data-id="${esc(post.id)}" aria-label="删除帖子">${ICONS.trash}</button>` : ''}
            <button class="icon-btn-48" data-action="sharePost" data-id="${esc(post.id)}" aria-label="分享">${ICONS.share}</button>
          </div>
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
  function getProofTypeOptions(idType) {
    return idType === '毕业校友'
      ? ['毕业证书', '学位证书', '校友卡', '学生证留存']
      : ['学生证', '校园一卡通', '录取通知书', '教务系统截图'];
  }

  function renderVerificationScreen() {
    const p = state.persona;
    const status = state.verificationStatus || (p.isVerified ? 'VERIFIED' : 'UNVERIFIED');

    // 1:1 VerificationSuccessScreen
    if (status === 'VERIFIED' && p.isVerified) {
      return `
        <div class="screen-container">
          <div class="scroll-area sub-scroll" style="padding:0;display:flex;flex-direction:column;justify-content:space-between;">
            <div class="verify-success-hero fluid-aurora-dark" style="padding-top:calc(1.5rem + var(--sat));">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-size:0.875rem;font-weight:600;color:rgba(255,255,255,0.9);">央美身份核验通过</span>
                <span class="status-dot verified" style="width:0.625rem;height:0.625rem;"></span>
              </div>
              <div>
                <h2 style="margin:0;font-size:2rem;line-height:1.22;font-weight:800;color:#FFFFFF;">欢迎回到<br/>杨梅树下。</h2>
                <p style="margin:0.625rem 0 0;font-size:0.875rem;line-height:1.55;color:rgba(255,255,255,0.82);">
                  你的中央美术学院${esc(p.identityType)}身份已确认。接下来你将以独立匿名形象自由交流。
                </p>
              </div>
              <div class="verify-frosted-card">
                ${avatarHtml(p.avatarSeed, 'avatar-52')}
                <div style="min-width:0;flex:1;">
                  <div style="font-size:0.6875rem;color:rgba(255,255,255,0.74);">匿名身份已激活</div>
                  <div style="font-size:1.1875rem;font-weight:700;color:#FFFFFF;margin-top:0.125rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(p.nickname)}</div>
                  <div style="font-size:0.75rem;color:rgba(255,255,255,0.8);margin-top:0.125rem;">中央美术学院 · ${esc(p.identityType)}</div>
                </div>
              </div>
            </div>

            <div style="background:var(--surface);padding:1.5rem 1.5rem calc(1.75rem + var(--sab));display:flex;flex-direction:column;gap:0.75rem;">
              <p style="margin:0;font-size:0.75rem;line-height:1.5;color:var(--ink-secondary);">
                已解锁完整权限：发布帖子 · 匿名评论与回复 · 参与投票 · 选课评价共建
              </p>
              <button class="btn-primary-full" style="min-height:3.25rem;border-radius:0.875rem;" data-action="goBack">
                <span>进入杨梅树窝</span>
                <span style="width:1.125rem;height:1.125rem;display:flex;">${ICONS.arrowRight}</span>
              </button>
              <button style="font-size:0.75rem;color:var(--ink-muted);padding:0.5rem;align-self:center;" data-action="resetVerifyDemo">
                重置为未认证访客
              </button>
            </div>
          </div>
        </div>
      `;
    }

    const d = nav.verifyDraft;
    const statusLabel = status === 'REVIEWING' ? '审核中' : status === 'REJECTED' ? '未通过' : '未认证';
    const proofOptions = getProofTypeOptions(d.identityType);
    if (!proofOptions.includes(d.proofType)) d.proofType = proofOptions[0];
    const hasPhoto = Boolean(d.proofDataUrl);
    const canSubmit = hasPhoto && Boolean(d.agreedPolicy);

    let bodySection = '';
    if (status === 'REVIEWING') {
      const step = Math.max(1, Math.min(3, nav.verifyAuditStep || 1));
      const pct = step === 1 ? 34 : step === 2 ? 70 : 96;
      const rec = state.verificationRecord || {};
      bodySection = `
        <div style="padding:1.125rem;">
          <div class="pub-card" style="padding:1.25rem;gap:1rem;">
            <div style="display:flex;align-items:center;gap:0.625rem;">
              <span class="ai-spinner" style="width:1.25rem;height:1.25rem;flex-shrink:0;"></span>
              <div>
                <div style="font-size:1.125rem;font-weight:700;color:var(--ink);">身份核验中</div>
                <div style="font-size:0.75rem;color:var(--ink-secondary);margin-top:0.1rem;">中央美术学院 · ${esc(rec.identityType || d.identityType)}</div>
              </div>
            </div>

            <div class="verify-audit-progress-track">
              <div class="verify-audit-progress-fill" style="width:${pct}%;"></div>
            </div>

            ${rec.proofDataUrl ? `
              <div style="display:flex;align-items:center;gap:0.75rem;padding:0.625rem;border-radius:0.75rem;background:var(--surface-soft);">
                <img class="verify-photo-thumb" style="width:3.25rem;height:3.25rem;" src="${rec.proofDataUrl}" alt="审核中凭证预览" />
                <div style="min-width:0;flex:1;">
                  <div style="font-size:0.875rem;font-weight:600;color:var(--ink);">${esc(rec.proofLabel || '央美身份凭证照片')}</div>
                  <div style="font-size:0.6875rem;color:var(--ink-secondary);margin-top:0.1875rem;">凭证已加密隔离，核验完成后自动脱敏销毁</div>
                </div>
              </div>
            ` : ''}

            <div style="display:flex;flex-direction:column;gap:0.625rem;">
              <div class="verify-audit-stage ${step > 1 ? 'done' : step === 1 ? 'active' : ''}">
                <span class="verify-audit-dot">${step > 1 ? `<span style="width:0.75rem;height:0.75rem;display:flex;">${ICONS.check}</span>` : ''}</span>
                <span>凭证图片清晰度与完整性校验</span>
              </div>
              <div class="verify-audit-stage ${step > 2 ? 'done' : step === 2 ? 'active' : ''}">
                <span class="verify-audit-dot">${step > 2 ? `<span style="width:0.75rem;height:0.75rem;display:flex;">${ICONS.check}</span>` : ''}</span>
                <span>中央美术学院（CAFA）本校标识核验</span>
              </div>
              <div class="verify-audit-stage ${step >= 3 ? 'active' : ''}">
                <span class="verify-audit-dot"></span>
                <span>生成隔离匿名社区身份并解锁权限</span>
              </div>
            </div>

            <div class="hairline"></div>
            <button class="btn-outline-full" data-action="cancelVerifyReview">撤回并重新上传凭证</button>
          </div>
        </div>
      `;
    } else {
      bodySection = `
        ${status === 'REJECTED' ? `
          <div class="verify-rejected-banner">
            <span style="width:1.125rem;height:1.125rem;color:var(--vermilion);display:flex;flex-shrink:0;">${ICONS.alert}</span>
            <div>
              <div style="font-size:0.875rem;font-weight:700;color:var(--vermilion);">上次认证未通过</div>
              <div style="font-size:0.75rem;line-height:1.45;color:var(--ink);margin-top:0.2rem;">
                ${esc((state.verificationRecord && state.verificationRecord.rejectReason) || '请重新上传能清晰辨认中央美术学院标识的证明。')}
              </div>
            </div>
          </div>
        ` : ''}

        <div style="padding:1rem 1.125rem;display:flex;flex-direction:column;gap:0.625rem;">
          <h3 class="verify-section-label">第一步 · 选择你的央美身份</h3>
          <div class="identity-choice-grid">
            <button class="identity-choice-card ${d.identityType === '在读学生' ? 'selected' : ''}" data-action="verifyPickType" data-val="在读学生">
              <div class="identity-choice-top">
                <span class="identity-choice-icon">${ICONS.badge}</span>
                ${d.identityType === '在读学生' ? `<span style="width:1.125rem;height:1.125rem;display:flex;color:#FFFFFF;">${ICONS.checkCircle}</span>` : ''}
              </div>
              <div>
                <div class="identity-choice-title">在读学生认证</div>
                <div class="identity-choice-desc">学生证 / 一卡通 / 录取通知书</div>
              </div>
            </button>
            <button class="identity-choice-card ${d.identityType === '毕业校友' ? 'selected' : ''}" data-action="verifyPickType" data-val="毕业校友">
              <div class="identity-choice-top">
                <span class="identity-choice-icon">${ICONS.school}</span>
                ${d.identityType === '毕业校友' ? `<span style="width:1.125rem;height:1.125rem;display:flex;color:#FFFFFF;">${ICONS.checkCircle}</span>` : ''}
              </div>
              <div>
                <div class="identity-choice-title">毕业校友认证</div>
                <div class="identity-choice-desc">毕业证 / 学位证 / 校友凭证</div>
              </div>
            </button>
          </div>
        </div>

        <div style="padding:0 1.125rem 1.5rem;">
          <div class="pub-card" style="padding:1.125rem;gap:0.875rem;">
            <div class="verify-school-banner">
              <div style="display:flex;align-items:center;gap:0.5rem;">
                <span style="width:1.125rem;height:1.125rem;color:var(--active-tint);display:flex;">${ICONS.school}</span>
                <span style="font-size:0.75rem;color:var(--ink-secondary);">认证院校</span>
              </div>
              <span style="font-size:0.875rem;font-weight:700;color:var(--ink);">中央美术学院（CAFA）</span>
            </div>

            <div style="display:flex;flex-direction:column;gap:0.5rem;">
              <div class="verify-section-label">第二步 · 选择凭证类型</div>
              <div class="chip-wrap">
                ${proofOptions.map(opt => `<button class="mono-chip ${d.proofType === opt ? 'inverted' : ''}" data-action="verifyPickProofType" data-val="${esc(opt)}">${esc(opt)}</button>`).join('')}
              </div>
            </div>

            ${hasPhoto ? `
              <button class="verify-photo-preview" data-action="openVerifyPhotoSheet">
                <img class="verify-photo-thumb" src="${d.proofDataUrl}" alt="已选认证照片预览" />
                <div style="min-width:0;flex:1;">
                  <div style="display:flex;align-items:center;gap:0.3125rem;">
                    <span style="width:1rem;height:1rem;color:#10B981;display:flex;flex-shrink:0;">${ICONS.checkCircle}</span>
                    <span style="font-size:0.875rem;font-weight:600;color:var(--ink);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(d.proofLabel || ('已选择「' + d.proofType + '」照片'))}</span>
                  </div>
                  <div style="font-size:0.6875rem;color:var(--ink-secondary);margin-top:0.2rem;">点击可重新拍照或从相册更换照片</div>
                </div>
              </button>
            ` : `
              <button class="verify-upload-zone" data-action="openVerifyPhotoSheet">
                <span class="verify-upload-circle">${ICONS.camera}</span>
                <div style="font-size:0.875rem;font-weight:700;color:var(--ink);">拍照或从相册上传「${esc(d.proofType)}」</div>
                <div style="font-size:0.75rem;line-height:1.45;color:var(--ink-secondary);">需清晰可见中央美术学院（CAFA）校名或标识，姓名与学号可自行打码</div>
              </button>
            `}

            <button class="verify-policy-row" data-action="verifyTogglePolicy">
              <span class="verify-checkbox ${d.agreedPolicy ? 'checked' : ''}">
                ${d.agreedPolicy ? ICONS.check : ''}
              </span>
              <span style="font-size:0.75rem;line-height:1.5;color:var(--ink-secondary);">
                我确认上传的为本人中央美术学院有效身份凭证，同意仅用于本校边界核验，审核后原图将脱敏销毁且绝不公开展示。
              </span>
            </button>

            <button class="btn-primary-full" style="min-height:3.25rem;border-radius:0.875rem;opacity:${canSubmit ? '1' : '0.52'};" data-action="submitVerification">
              <span>${hasPhoto ? '提交认证审核' : '请先上传凭证照片'}</span>
              <span style="width:1.125rem;height:1.125rem;display:flex;">${ICONS.arrowRight}</span>
            </button>

            <div style="font-size:0.6875rem;line-height:1.5;color:var(--ink-muted);">
              未认证状态下仅支持浏览，完成核验后方可发帖、评论、投票及参与选课评价共建。
            </div>
          </div>
        </div>
      `;
    }

    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack" aria-label="返回">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">央美身份认证</h1>
          </div>
        </div>
        <div class="scroll-area sub-scroll" style="padding:0;">
          <div class="verify-dark-hero fluid-aurora-dark">
            <div style="display:flex;justify-content:space-between;align-items:center;font-size:0.75rem;color:rgba(255,255,255,0.82);">
              <span>中央美术学院 · CAFA</span>
              <span>${statusLabel}</span>
            </div>
            <div>
              <h2 style="margin:0;font-size:1.5rem;font-weight:800;color:#FFFFFF;">确认央美身份</h2>
              <p style="margin:0.375rem 0 0;font-size:0.75rem;line-height:1.5;color:rgba(255,255,255,0.82);">
                仅核验是否为中央美术学院在读学生或毕业校友，不收集学院与年级，认证资料严格保密不公开。
              </p>
            </div>
          </div>
          <div class="verify-steps-strip">
            <div class="verify-step-node active"><span class="verify-step-num">01</span><span>选择身份</span></div>
            <div class="verify-step-node ${hasPhoto || status !== 'UNVERIFIED' ? 'active' : ''}"><span class="verify-step-num">02</span><span>上传凭证</span></div>
            <div class="verify-step-node ${status === 'REVIEWING' || status === 'VERIFIED' ? 'active' : ''}"><span class="verify-step-num">03</span><span>核验通过</span></div>
          </div>
          ${bodySection}
        </div>
      </div>
    `;
  }

  // ---------------- Sub-Screen: PARTITION-SPECIFIC PUBLISH SCREEN (1:1 PublishScreen.kt) ----------------
  const PARTITION_SPECS = {
    TREE_HOLE: {
      headerTitle: '匿名树洞 · 安静倾诉',
      headerHint: '在这里卸下防备，支持标记此刻情绪，可随时关闭评论仅留作自语',
      placeholder: '在杨梅树下放心倾诉此刻的心情、压力或深夜想法，这里没人知道你是谁...',
      mediaHint: '可附一张此刻看到的风景或随手涂鸦',
      locationPrompt: '添加此刻所在位置（可选）',
      curatedTags: ['#深夜树洞', '#情绪树洞', '#画室日常', '#花家地漫游', '#匿名倾诉'],
      ctaLabel: '投递树洞'
    },
    MARKETPLACE: {
      headerTitle: '闲置交换 · 校内流转',
      headerHint: '填好分类、价格、成色与交接方式，方便央美同学快速定位并私信交换微信',
      placeholder: '描述画材/物品品牌、规格、使用痕迹、购买渠道与校内面交安排...',
      mediaHint: '建议附上 1~4 张实物实拍图，成交率更高',
      locationPrompt: '面交 / 自提地点（如：望京北门 / 燕郊宿舍）',
      curatedTags: ['#闲置画材', '#二手出物', '#教材流转', '#望京自提', '#燕郊自提'],
      ctaLabel: '发布闲置'
    },
    OPPORTUNITY: {
      headerTitle: '校园机会 · 兼职与协作',
      headerHint: '清晰写明招募类型、报酬范围与技能要求，让合适专业的同学一眼看到',
      placeholder: '说明岗位职责、技能要求（如手绘/3D/排版/布展/拍摄）、时间周期与联系投递方式...',
      mediaHint: '可附项目海报、参考风格图或招募简章',
      locationPrompt: '工作地点（如：望京周边 / 798 / 线上远程）',
      curatedTags: ['#设计实习', '#艺术兼职', '#项目合作', '#展览助理', '#模特招募'],
      ctaLabel: '发布招募'
    },
    LOST_FOUND: {
      headerTitle: '失物招领 · 校园互助找回',
      headerHint: '先选「失物寻找」或「拾物招领」，写明时间与具体地点，重要证件注意遮挡隐私',
      placeholder: '描述物品外观特征（颜色、型号、挂件、贴纸或内部标识），方便失主/拾主快速核对...',
      mediaHint: '附物品实物照或发现位置环境图更易找回',
      locationPrompt: '丢失 / 拾获地点（推荐填写，如：美术馆 / 7号楼）',
      curatedTags: ['#失物招领', '#校园寻物', '#一卡通', '#速写本', '#急寻'],
      ctaLabel: '发布寻物'
    },
    MUTUAL_AID: {
      headerTitle: '校园互助 · 搭子与应急',
      headerHint: '拼车买材料、找看展/自习搭子、借工具或代取跑腿，写清时间与人数更易成行',
      placeholder: '说明出发时间、人数需求、工具型号或具体互助细节，方便顺路的同学直接响应...',
      mediaHint: '可附路线截图、材料清单或参考图',
      locationPrompt: '集合或互助地点（如：望京西门 / 红星美凯龙）',
      curatedTags: ['#校园互助', '#拼车买材料', '#寻找搭子', '#画材借用', '#代取跑腿'],
      ctaLabel: '发起互助'
    },
    EXHIBITION: {
      headerTitle: '展览资讯 · 看展与导览',
      headerHint: '记录校内新展、毕业展、快闪空间或 798 周边展览，方便同学错峰观展',
      placeholder: '分享展览主题、参展艺术家、布展亮点、导览安排或你的看展笔记...',
      mediaHint: '建议附展览海报或现场空间照片',
      locationPrompt: '展览场馆（如：央美美术馆 / 7号楼展厅 / 798）',
      curatedTags: ['#校园展览', '#央美美术馆', '#看展日记', '#毕业季', '#798看展'],
      ctaLabel: '发布展讯'
    },
    ART_CREATION: {
      headerTitle: '艺术创作 · 作品与过程手记',
      headerHint: '标记创作媒介与当前阶段，和不同院系同学交流材料技法、构图与策展思路',
      placeholder: '记录创作灵感、材料技法实验、色彩推敲，或聊聊卡壳的地方听听大家建议...',
      mediaHint: '上传最多 4 张草图、过程细节或成品局部',
      locationPrompt: '所在工作室 / 校区（可选）',
      curatedTags: ['#艺术创作', '#毕业作品', '#材料实验', '#工作室日常', '#作品互评'],
      ctaLabel: '发布作品'
    },
    RELATIONSHIP: {
      headerTitle: '恋爱情感 · 匿名聊聊心动与困惑',
      headerHint: '纯匿名安全空间，聊聊校园里的心动瞬间、亲密关系相处或情感提问',
      placeholder: '写下关于心动、相处、遗憾或想听听大家看法的故事...',
      mediaHint: '可附一张氛围感配图（可选）',
      locationPrompt: '偶遇或纪念地点（可选）',
      curatedTags: ['#恋爱情感', '#央美心动', '#情感树洞', '#脱单随缘', '#关系讨论'],
      ctaLabel: '匿名发布'
    }
  };

  function makeDefaultPubDraft(catKey, locked, initialTag) {
    const valid = PARTITION_SPECS[catKey] ? catKey : 'TREE_HOLE';
    return {
      category: valid,
      locked: Boolean(locked),
      body: '',
      tags: initialTag ? [initialTag] : [],
      customTagInput: '',
      mediaList: [],
      allowComments: true,
      location: '',
      eventTime: '',
      // Partition fields
      mood: '迷茫',
      marketSubType: '画材',
      marketCondition: '九五新',
      price: '',
      tradeStatus: '望京面交',
      oppType: '兼职招募',
      oppReward: '',
      oppMode: '线上远程',
      lfType: '失物寻找',
      lfItemName: '',
      aidType: '拼车买材料',
      aidUrgency: '今天急需',
      aidReward: '',
      exhType: '校内个展',
      exhEntry: '校内免费',
      artMedium: '油画',
      artStage: '进行中求建议',
      relVibe: '情感树洞'
    };
  }

  function syncPubInputsToDraft() {
    if (!nav.pubDraft) return;
    const d = nav.pubDraft;
    const val = id => { const el = document.getElementById(id); return el ? el.value : null; };
    const b = val('pub-body'); if (b !== null) d.body = b;
    const pr = val('pub-price'); if (pr !== null) d.price = pr;
    const tm = val('pub-event-time'); if (tm !== null) d.eventTime = tm;
    const rew = val('pub-opp-reward'); if (rew !== null) d.oppReward = rew;
    const lfn = val('pub-lf-item'); if (lfn !== null) d.lfItemName = lfn;
    const ar = val('pub-aid-reward'); if (ar !== null) d.aidReward = ar;
    const ct = val('pub-custom-tag'); if (ct !== null) d.customTagInput = ct;
  }

  function publishCatLabel(k) {
    if (k === 'TREE_HOLE') return '情绪树洞';
    return (D.CATEGORIES[k] && D.CATEGORIES[k].titleCn) || k;
  }

  function renderPublishScreen() {
    if (!nav.pubDraft) nav.pubDraft = makeDefaultPubDraft('TREE_HOLE', false);
    const d = nav.pubDraft;
    const pubCats = ['TREE_HOLE', 'MARKETPLACE', 'OPPORTUNITY', 'LOST_FOUND', 'MUTUAL_AID'];
    if (!d.locked && !pubCats.includes(d.category)) {
      d.category = 'TREE_HOLE';
    }
    const catDisplayName = publishCatLabel(d.category);
    const spec = PARTITION_SPECS[d.category] || PARTITION_SPECS.TREE_HOLE;

    const suggestedTags = Array.from(new Set(spec.curatedTags.concat(D.FIXED_PUBLISH_TAGS))).slice(0, 10);
    const customTags = (d.tags || []).filter(t => !suggestedTags.includes(t));

    // Partition-specific structured form card
    let structuredCard = '';
    if (d.category === 'TREE_HOLE') {
      const moods = ['迷茫', '焦虑', '疲惫', '释怀', '小确幸', '深夜emo', '想吐槽'];
      structuredCard = `
        <div class="pub-card">
          <div class="pub-card-title"><span>此刻情绪</span></div>
          <div class="h-scroll-row" style="padding:0;">
            ${moods.map(m => `<button class="mono-chip ${d.mood === m ? 'inverted' : ''}" data-action="pubSetField" data-field="mood" data-val="${esc(m)}">${esc(m)}</button>`).join('')}
          </div>
        </div>`;
    } else if (d.category === 'MARKETPLACE') {
      structuredCard = `
        <div class="pub-card">
          <div class="pub-card-title"><span>闲置物品信息</span></div>
          <div class="chip-wrap">
            ${['画材', '教材', '电子产品', '家具', '生活用品', '服饰'].map(t => `<button class="mono-chip ${d.marketSubType === t ? 'inverted' : ''}" data-action="pubSetField" data-field="marketSubType" data-val="${esc(t)}">${esc(t)}</button>`).join('')}
          </div>
          <div class="pub-two-col">
            <input class="field-input" id="pub-price" placeholder="出物价格（如 ¥120）" value="${esc(d.price)}" />
            <input class="field-input" id="pub-event-time" placeholder="可面交时间（如 今晚）" value="${esc(d.eventTime)}" />
          </div>
          <div style="font-size:0.75rem;color:var(--ink-secondary);">成色</div>
          <div class="h-scroll-row" style="padding:0;">
            ${['全新未拆', '九五新', '九成新', '实用成色'].map(c => `<button class="mono-chip ${d.marketCondition === c ? 'inverted' : ''}" data-action="pubSetField" data-field="marketCondition" data-val="${esc(c)}">${esc(c)}</button>`).join('')}
          </div>
          <div style="font-size:0.75rem;color:var(--ink-secondary);">交接方式</div>
          <div class="h-scroll-row" style="padding:0;">
            ${['望京面交', '燕郊自提', '上海自提', '同城邮寄', '可换物'].map(m => `<button class="mono-chip ${d.tradeStatus === m ? 'inverted' : ''}" data-action="pubSetField" data-field="tradeStatus" data-val="${esc(m)}">${esc(m)}</button>`).join('')}
          </div>
        </div>`;
    } else if (d.category === 'OPPORTUNITY') {
      structuredCard = `
        <div class="pub-card">
          <div class="pub-card-title"><span>招募 / 机会要素</span></div>
          <div class="chip-wrap">
            ${['兼职招募', '实习招聘', '项目合作', '展览协作', '模特招募'].map(t => `<button class="mono-chip ${d.oppType === t ? 'inverted' : ''}" data-action="pubSetField" data-field="oppType" data-val="${esc(t)}">${esc(t)}</button>`).join('')}
          </div>
          <div class="pub-two-col">
            <input class="field-input" id="pub-opp-reward" placeholder="薪资 / 报酬（如 日薪260）" value="${esc(d.oppReward)}" />
            <input class="field-input" id="pub-event-time" placeholder="周期 / 截止时间" value="${esc(d.eventTime)}" />
          </div>
          <div style="font-size:0.75rem;color:var(--ink-secondary);">协作方式</div>
          <div class="h-scroll-row" style="padding:0;">
            ${['线上远程', '望京周边', '校内协作', '短期驻场'].map(m => `<button class="mono-chip ${d.oppMode === m ? 'inverted' : ''}" data-action="pubSetField" data-field="oppMode" data-val="${esc(m)}">${esc(m)}</button>`).join('')}
          </div>
        </div>`;
    } else if (d.category === 'LOST_FOUND') {
      structuredCard = `
        <div class="pub-card">
          <div class="pub-card-title"><span>寻物 / 招领信息</span></div>
          <div class="pub-seg-row">
            <button class="pub-seg-btn ${d.lfType === '失物寻找' ? 'active' : ''}" data-action="pubSetField" data-field="lfType" data-val="失物寻找">失物寻找</button>
            <button class="pub-seg-btn ${d.lfType === '拾物招领' ? 'active' : ''}" data-action="pubSetField" data-field="lfType" data-val="拾物招领">拾物招领</button>
          </div>
          <input class="field-input" id="pub-lf-item" placeholder="物品名称（如：校园一卡通 / 黑色速写本 / AirPods）" value="${esc(d.lfItemName)}" />
          <input class="field-input" id="pub-event-time" placeholder="${d.lfType === '失物寻找' ? '遗失时间（如：今天上午 10:30）' : '拾获时间（如：刚刚在食堂二楼）'}" value="${esc(d.eventTime)}" />
          <div class="h-scroll-row" style="padding:0;">
            ${['刚刚', '今天上午', '今天下午', '昨天'].map(t => `<button class="mono-chip ${d.eventTime === t ? 'inverted' : ''}" data-action="pubSetField" data-field="eventTime" data-val="${esc(t)}">${esc(t)}</button>`).join('')}
          </div>
        </div>`;
    } else if (d.category === 'MUTUAL_AID') {
      structuredCard = `
        <div class="pub-card">
          <div class="pub-card-title"><span>互助需求卡片</span></div>
          <div class="chip-wrap">
            ${['拼车买材料', '寻找搭子', '代取跑腿', '学习互助', '借物求助'].map(t => `<button class="mono-chip ${d.aidType === t ? 'inverted' : ''}" data-action="pubSetField" data-field="aidType" data-val="${esc(t)}">${esc(t)}</button>`).join('')}
          </div>
          <div class="h-scroll-row" style="padding:0;">
            ${['今天急需', '本周内', '随时可约'].map(u => `<button class="mono-chip ${d.aidUrgency === u ? 'inverted' : ''}" data-action="pubSetField" data-field="aidUrgency" data-val="${esc(u)}">${esc(u)}</button>`).join('')}
          </div>
          <div class="pub-two-col">
            <input class="field-input" id="pub-event-time" placeholder="约定时间（如 今晚18:30）" value="${esc(d.eventTime)}" />
            <input class="field-input" id="pub-aid-reward" placeholder="分摊/酬谢（可选，如AA）" value="${esc(d.aidReward)}" />
          </div>
        </div>`;
    } else if (d.category === 'EXHIBITION') {
      structuredCard = `
        <div class="pub-card">
          <div class="pub-card-title"><span>展览档案信息</span></div>
          <div class="chip-wrap">
            ${['校内个展', '群展联展', '毕业展', '讲座导览', '校外看展'].map(t => `<button class="mono-chip ${d.exhType === t ? 'inverted' : ''}" data-action="pubSetField" data-field="exhType" data-val="${esc(t)}">${esc(t)}</button>`).join('')}
          </div>
          <input class="field-input" id="pub-event-time" placeholder="展期 / 开幕时间（如：5.12 - 5.28 · 9:30-17:00）" value="${esc(d.eventTime)}" />
          <div class="h-scroll-row" style="padding:0;">
            ${['校内免费', '需预约', '开幕导览', '凭证免票'].map(e => `<button class="mono-chip ${d.exhEntry === e ? 'inverted' : ''}" data-action="pubSetField" data-field="exhEntry" data-val="${esc(e)}">${esc(e)}</button>`).join('')}
          </div>
        </div>`;
    } else if (d.category === 'ART_CREATION') {
      structuredCard = `
        <div class="pub-card">
          <div class="pub-card-title"><span>创作档案标签</span></div>
          <div class="chip-wrap">
            ${['油画', '国画', '版画', '雕塑', '实验艺术', '设计排版', '影像装置', '速写'].map(m => `<button class="mono-chip ${d.artMedium === m ? 'inverted' : ''}" data-action="pubSetField" data-field="artMedium" data-val="${esc(m)}">${esc(m)}</button>`).join('')}
          </div>
          <div style="font-size:0.75rem;color:var(--ink-secondary);">创作阶段</div>
          <div class="h-scroll-row" style="padding:0;">
            ${['灵感草图', '进行中求建议', '成品展示', '毕设打磨'].map(s => `<button class="mono-chip ${d.artStage === s ? 'inverted' : ''}" data-action="pubSetField" data-field="artStage" data-val="${esc(s)}">${esc(s)}</button>`).join('')}
          </div>
        </div>`;
    } else if (d.category === 'RELATIONSHIP') {
      structuredCard = `
        <div class="pub-card">
          <div class="pub-card-title"><span>情感氛围</span></div>
          <div class="h-scroll-row" style="padding:0;">
            ${['情感树洞', '心动记录', '恋爱求助', '脱单随缘'].map(v => `<button class="mono-chip ${d.relVibe === v ? 'inverted' : ''}" data-action="pubSetField" data-field="relVibe" data-val="${esc(v)}">${esc(v)}</button>`).join('')}
          </div>
        </div>`;
    }

    return `
      <div class="screen-container">
        <div class="pub-topbar">
          <div style="display:flex;align-items:center;gap:0.375rem;min-width:0;">
            <button class="icon-btn-48" style="width:2.5rem;height:2.5rem;" data-action="closePublishScreen" aria-label="关闭发布页">${ICONS.close}</button>
            ${avatarHtml(state.persona.avatarSeed, 'avatar-24')}
            <div style="min-width:0;">
              <div style="font-size:0.875rem;font-weight:700;color:var(--ink);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                ${d.locked ? '发布到 · ' + esc(catDisplayName) : esc(catDisplayName)}
              </div>
              <div style="font-size:0.6875rem;color:var(--ink-secondary);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${esc(state.persona.nickname)}</div>
            </div>
          </div>
          <div style="display:flex;align-items:center;gap:0.375rem;flex-shrink:0;">
            <button style="font-size:0.8125rem;color:var(--ink-secondary);padding:0.375rem 0.5rem;" data-action="saveDraft" ${nav.moderating ? 'disabled' : ''}>存草稿</button>
            <button class="btn-grad-sm" style="padding:0.5rem 0.875rem;font-size:0.8125rem;opacity:${nav.moderating ? '0.72' : '1'};" data-action="submitPost" ${nav.moderating ? 'disabled' : ''}>${nav.moderating ? '安全审查中...' : esc(spec.ctaLabel)}</button>
          </div>
        </div>

        <div class="scroll-area sub-scroll" style="padding:1rem;display:flex;flex-direction:column;gap:0.875rem;">
          <div class="safety-guard-banner">
            <span style="width:1.125rem;height:1.125rem;display:flex;color:var(--active-tint);flex-shrink:0;margin-top:0.06rem;">${ICONS.shieldCheck}</span>
            <span>系统已开启图文自动安全审查：严禁发布违法违规、色情低俗、暴力血腥、诈骗赌博、人肉隐私或恶意攻击等不良文字及图片。</span>
          </div>
          ${d.locked ? `
            <div class="pub-partition-hero">
              <span class="pub-partition-badge">${catIcon(d.category)}</span>
              <div style="min-width:0;flex:1;">
                <div style="font-size:0.875rem;font-weight:700;color:var(--ink);">${esc(spec.headerTitle)}</div>
                <div style="font-size:0.75rem;line-height:1.45;color:var(--ink-secondary);margin-top:0.15rem;">${esc(spec.headerHint)}</div>
              </div>
            </div>
          ` : `
            <div class="pub-card">
              <div class="pub-card-title">
                <span>选择发布分区</span>
                <span style="font-size:0.75rem;font-weight:500;color:var(--ink-secondary);">当前：${esc(catDisplayName)}</span>
              </div>
              <div class="chip-wrap">
                ${pubCats.map(k => `<button class="mono-chip ${d.category === k ? 'inverted' : ''}" data-action="pubPickCat" data-val="${k}">${esc(publishCatLabel(k))}</button>`).join('')}
              </div>
              <div style="font-size:0.75rem;line-height:1.45;color:var(--ink-secondary);">${esc(spec.headerHint)}</div>
            </div>
          `}

          ${structuredCard}

          <div class="pub-card">
            <textarea class="field-textarea" id="pub-body" style="min-height:8.5rem;border:none;padding:0;background:transparent;font-size:0.9375rem;" placeholder="${esc(spec.placeholder)}">${esc(d.body)}</textarea>
            <label class="pub-media-hint-bar">
              <span style="width:1.125rem;height:1.125rem;color:var(--active-tint);display:flex;flex-shrink:0;">${ICONS.photo}</span>
              <span>${d.mediaList.length ? '已添加 ' + d.mediaList.length + '/4 张图片 · 点击继续添加' : esc(spec.mediaHint)}</span>
              <input type="file" accept="image/*" multiple id="pub-media-input" style="display:none;" />
            </label>
            ${d.mediaList.length ? `
              <div class="pub-media-strip">
                ${d.mediaList.map((m, idx) => `
                  <div class="pub-media-thumb-wrap">
                    <img src="${m.src}" alt="已选图片" />
                    <button class="pub-media-remove" data-action="pubRemoveMedia" data-idx="${idx}" aria-label="移除图片">${ICONS.close}</button>
                  </div>
                `).join('')}
              </div>
            ` : ''}
          </div>

          <div class="pub-card">
            <div class="pub-card-title">
              <span style="display:flex;align-items:center;gap:0.375rem;">
                <span style="width:1rem;height:1rem;color:var(--active-tint);display:flex;">${ICONS.loc}</span>
                <span>${d.location ? esc(d.location) : esc(spec.locationPrompt)}</span>
              </span>
              ${d.location ? `<button style="font-size:0.75rem;color:var(--ink-muted);" data-action="pubSetField" data-field="location" data-val="">清除</button>` : ''}
            </div>
            <div class="h-scroll-row" style="padding:0;">
              ${D.CAFA_LOCATIONS.slice(0, 10).map(loc => `<button class="mono-chip ${d.location === loc ? 'inverted' : ''}" data-action="pubPickLoc" data-val="${esc(loc)}">${esc(loc)}</button>`).join('')}
            </div>
          </div>

          <div class="pub-card">
            <div class="pub-card-title">
              <span>${esc(catDisplayName)}推荐标签</span>
              <span style="font-size:0.75rem;font-weight:500;color:var(--ink-muted);">已选 ${(d.tags || []).length}/5</span>
            </div>
            <div style="display:flex;gap:0.5rem;">
              <input class="field-input" id="pub-custom-tag" placeholder="#输入自定义标签" value="${esc(d.customTagInput)}" maxlength="18" />
              <button class="btn-grad-sm" style="flex-shrink:0;padding:0 0.875rem;" data-action="pubAddCustomTag">添加</button>
            </div>
            ${customTags.length ? `
              <div class="chip-wrap">
                ${customTags.map(tg => `<button class="mono-chip inverted" data-action="pubToggleTag" data-val="${esc(tg)}">${esc(tg)} ×</button>`).join('')}
              </div>
            ` : ''}
            <div class="chip-wrap">
              ${suggestedTags.map(tg => `<button class="mono-chip ${(d.tags || []).includes(tg) ? 'inverted' : ''}" data-action="pubToggleTag" data-val="${esc(tg)}">${esc(tg)}</button>`).join('')}
            </div>
          </div>
        </div>

        <div class="pub-bottom-bar">
          <div style="display:flex;align-items:center;gap:0.5rem;">
            <label class="icon-btn-48" style="width:2.625rem;height:2.625rem;cursor:pointer;" aria-label="添加图片">
              ${ICONS.photo}
              <input type="file" accept="image/*" multiple id="pub-media-toolbar-input" style="display:none;" />
            </label>
          </div>
          <button style="display:flex;align-items:center;gap:0.5rem;" data-action="pubToggleComments">
            <span style="font-size:0.75rem;color:var(--ink-secondary);">${d.allowComments ? '允许评论' : '关闭评论'}</span>
            <div class="m3-switch ${d.allowComments ? 'on' : ''}"><div class="m3-switch-thumb"></div></div>
          </button>
        </div>
      </div>
    `;
  }

  // ---------------- Sub-Screen: PROFILE COLLECTION & SETTINGS ----------------
  function renderSwipeDeleteWrapper(itemId, deleteAction, innerHtml) {
    return `
      <div class="swipe-delete-wrap" data-swipe-id="${esc(itemId)}">
        <button class="swipe-delete-action" type="button" data-action="${esc(deleteAction)}" data-id="${esc(itemId)}" aria-label="删除">
          ${ICONS.trash}
          <span>删除</span>
        </button>
        <div class="swipe-delete-content">${innerHtml}</div>
      </div>
    `;
  }

  function renderProfileCollectionScreen(colKey) {
    const titles = {
      MY_POSTS: '我的帖子', MY_COMMENTS: '我的评论', MY_BOOKMARKS: '我的收藏',
      MY_DRAFTS: '我的草稿', MY_TRADES: '我的交易', MY_HELP: '我的求助'
    };
    let body = '';
    let showSwipeHint = false;
    if (colKey === 'MY_DRAFTS') {
      showSwipeHint = state.drafts.length > 0;
      body = state.drafts.length ? state.drafts.map(d => renderSwipeDeleteWrapper(d.id, 'deleteDraft', `
        <div class="post-card" style="padding:1rem;">
          <div style="display:flex;justify-content:space-between;align-items:center;">
            <span class="mono-chip">${esc((D.CATEGORIES[d.category] || {}).titleCn || '草稿')} · 草稿</span>
            <button class="footer-delete-btn" data-action="deleteDraft" data-id="${esc(d.id)}">${ICONS.trash}<span>删除</span></button>
          </div>
          <div style="font-weight:700;margin-top:0.5rem;">${esc(d.title || '无标题草稿')}</div>
          <p style="margin:0.3125rem 0 0;font-size:0.8125rem;color:var(--ink-secondary);">${esc(d.body)}</p>
        </div>
      `)).join('') : `<div class="empty-state"><div class="empty-state-title">暂无草稿</div><div class="empty-state-sub">发布页面支持随时暂存未写完的想法。</div></div>`;
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
      const allowDelete = colKey === 'MY_POSTS' || colKey === 'MY_TRADES' || colKey === 'MY_HELP';
      if (colKey === 'MY_POSTS') items = state.posts.filter(p => p.isMine);
      else if (colKey === 'MY_BOOKMARKS') items = state.posts.filter(p => p.isBookmarked);
      else if (colKey === 'MY_TRADES') items = state.posts.filter(p => p.isMine && p.category === 'MARKETPLACE');
      else if (colKey === 'MY_HELP') items = state.posts.filter(p => p.isMine && (p.category === 'LOST_FOUND' || p.category === 'MUTUAL_AID'));
      showSwipeHint = allowDelete && items.length > 0;
      body = items.length
        ? items.map(p => {
            const cardHtml = renderEditorialPostCard(p, { allowDelete });
            return allowDelete ? renderSwipeDeleteWrapper(p.id, 'confirmDeletePost', cardHtml) : cardHtml;
          }).join('')
        : `<div class="empty-state"><div class="empty-state-title">这里暂时空着</div><div class="empty-state-sub">当你发布或收藏内容后，记录会汇集在这里。</div></div>`;
    }

    return `
      <div class="screen-container">
        <div class="sub-topbar">
          <div class="sub-topbar-left">
            <button class="icon-btn-48" data-action="goBack">${ICONS.arrowBack}</button>
            <h1 class="sub-topbar-title">${titles[colKey] || '记录'}</h1>
          </div>
        </div>
        <div class="scroll-area sub-scroll" style="padding-top:0.375rem;">
          ${showSwipeHint ? `<div class="swipe-hint-bar"><span>向左滑动帖子卡片可快速删除</span><span>← 左滑</span></div>` : ''}
          ${body}
        </div>
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
            <div style="font-size:0.75rem;color:var(--ink-secondary);line-height:1.45;">
              收件邮箱：1401135032@qq.com（提交后将直接投递至开发者邮箱并留存云端记录）
            </div>
            <textarea class="field-textarea" id="feedback-input" placeholder="给杨梅树窝提建议..."></textarea>
            <button class="btn-primary-full" data-action="submitFeedback">${nav.feedbackSending ? '正在发送至邮箱...' : '发送建议到 1401135032@qq.com'}</button>
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

    if (type === 'verificationGate') {
      return `
        <div class="gate-backdrop" data-action="closeModalBackdrop">
          <div class="gb-card gate-dialog">
            <div class="gb-card-inner">
              <div class="gate-hero fluid-aurora-dark">
                <h2 style="margin:0;font-size:1.1875rem;font-weight:700;color:#FFFFFF;">认证央美身份后即可互动</h2>
              </div>
              <div style="padding:1.25rem;display:flex;flex-direction:column;gap:0.75rem;">
                <p style="margin:0;font-size:0.875rem;line-height:1.55;color:var(--ink);">
                  「${esc(data.actionLabel || '发布校园内容')}」仅面向通过中央美术学院在读学生或毕业校友认证的成员开放。
                </p>
                <p style="margin:0;font-size:0.75rem;line-height:1.5;color:var(--ink-secondary);">
                  实名核验资料严格保密，社区内仅以独立匿名身份发声。
                </p>
                <div style="display:flex;gap:0.625rem;margin-top:0.25rem;">
                  <button class="btn-outline-full" style="flex:1;border-radius:0.5rem;" data-action="closeModal">先逛逛</button>
                  <button class="btn-primary-full" style="flex:1.3;border-radius:0.5rem;" data-action="gateGoVerify">
                    <span>去认证</span>
                    <span style="width:1rem;height:1rem;display:flex;">${ICONS.arrowRight}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      `;
    }

    if (type === 'verifyPhotoSource') {
      const proofType = (nav.verifyDraft && nav.verifyDraft.proofType) || '学生证';
      return `
        <div class="sheet-backdrop" data-action="closeModalBackdrop">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <h2 style="margin:0;font-size:1.0625rem;font-weight:700;">选择认证照片上传方式</h2>
              <button class="icon-btn-48" style="width:2.25rem;height:2.25rem;" data-action="closeModal">${ICONS.close}</button>
            </div>
            <p style="margin:0;font-size:0.75rem;line-height:1.5;color:var(--ink-secondary);">
              请上传清晰的「${esc(proofType)}」照片（含中央美术学院标识，可对姓名与学号打码）
            </p>
            <label class="photo-source-option">
              <span class="photo-source-icon" style="color:#D946EF;">${ICONS.camera}</span>
              <div>
                <div style="font-size:0.875rem;font-weight:600;color:var(--ink);">现场拍照</div>
                <div style="font-size:0.6875rem;color:var(--ink-secondary);margin-top:0.125rem;">调用相机实时拍摄本校身份证明</div>
              </div>
              <input type="file" accept="image/*" capture="environment" id="verify-camera-input" style="display:none;" />
            </label>
            <label class="photo-source-option">
              <span class="photo-source-icon" style="color:var(--active-tint);">${ICONS.photo}</span>
              <div>
                <div style="font-size:0.875rem;font-weight:600;color:var(--ink);">从相册选择照片</div>
                <div style="font-size:0.6875rem;color:var(--ink-secondary);margin-top:0.125rem;">上传手机相册中的学生证 / 一卡通 / 通知书 / 毕业证图片</div>
              </div>
              <input type="file" accept="image/*" id="verify-album-input" style="display:none;" />
            </label>
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

    if (type === 'deletePostConfirm') {
      return `
        <div class="gate-backdrop" data-action="closeModalBackdrop">
          <div class="gate-dialog" role="dialog" aria-modal="true" aria-label="确认删除帖子">
            <h3 class="gate-title">确认删除这条帖子？</h3>
            <p class="gate-desc">删除后将无法恢复，该帖下的评论与互动记录也会一并移除。</p>
            <div class="gate-actions">
              <button class="btn-outline-full" style="flex:1;min-height:2.75rem;" data-action="closeModal">取消</button>
              <button class="btn-danger-full" data-action="executeDeletePost" data-id="${esc(data.postId)}">确认删除</button>
            </div>
          </div>
        </div>
      `;
    }

    if (type === 'moderationBlock') {
      return `
        <div class="sheet-backdrop" data-action="closeModalBackdrop">
          <div class="sheet-panel">
            <div class="sheet-handle"></div>
            <div style="display:flex;flex-direction:column;align-items:center;text-align:center;gap:0.625rem;padding:0.25rem 0;">
              <div class="mod-block-badge">${ICONS.shieldAlert}</div>
              <h2 style="margin:0;font-size:1.125rem;font-weight:700;color:var(--vermilion);">自动安全审查未通过</h2>
              <p style="margin:0;font-size:0.875rem;line-height:1.6;color:var(--ink);">${esc(data.reason || '检测到内容或图片涉嫌违法违规及不良信息，无法发布。')}</p>
              <p style="margin:0;font-size:0.75rem;line-height:1.5;color:var(--ink-secondary);">请遵守国家法律法规及中央美术学院校园社区规范，修改或更换合规文字与图片后重试。</p>
            </div>
            <button class="btn-primary-full" data-action="closeModal">我知道了，返回修改</button>
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
        const cloud = getCloud();
        if (cloud && cloud.isReady()) {
          cloud.forceRefresh()
            .then(() => {
              ind.classList.remove('show');
              showToast('已同步全校最新内容');
            })
            .catch(() => {
              ind.classList.remove('show');
              showToast('已刷新本地内容');
            });
        } else {
          setTimeout(() => {
            ind.classList.remove('show');
            showToast('已刷新树窝最新内容');
          }, 160);
        }
      }
      pulling = false;
    }, { passive: true });
  }

  // ---------------- Swipe-Left-to-Reveal-Delete Gesture ----------------
  let activeSwipedWrap = null;
  let lastSwipeDragTs = 0;

  function closeOpenSwipes(exceptEl) {
    document.querySelectorAll('.swipe-delete-wrap.open').forEach(el => {
      if (el !== exceptEl) {
        el.classList.remove('open');
        const c = el.querySelector('.swipe-delete-content');
        if (c) c.style.transform = '';
      }
    });
    if (!exceptEl) activeSwipedWrap = null;
  }

  function attachSwipeDeleteHandlers() {
    const wraps = document.querySelectorAll('.swipe-delete-wrap');
    if (!wraps.length) return;
    const ACTION_WIDTH = 88; // 5.5rem

    wraps.forEach(wrap => {
      const content = wrap.querySelector('.swipe-delete-content');
      if (!content) return;

      let startX = 0, startY = 0, baseOffset = 0, curOffset = 0;
      let tracking = false, lockedHorizontal = false, lockedVertical = false;

      function onStart(clientX, clientY) {
        startX = clientX;
        startY = clientY;
        baseOffset = wrap.classList.contains('open') ? -ACTION_WIDTH : 0;
        curOffset = baseOffset;
        tracking = true;
        lockedHorizontal = false;
        lockedVertical = false;
      }

      function onMove(clientX, clientY, ev) {
        if (!tracking || lockedVertical) return;
        const dx = clientX - startX;
        const dy = clientY - startY;
        if (!lockedHorizontal) {
          if (Math.abs(dy) > 10 && Math.abs(dy) > Math.abs(dx)) {
            lockedVertical = true;
            tracking = false;
            return;
          }
          if (Math.abs(dx) > 8 && Math.abs(dx) > Math.abs(dy)) {
            lockedHorizontal = true;
            closeOpenSwipes(wrap);
            wrap.classList.add('swiping');
          } else {
            return;
          }
        }
        if (ev && ev.cancelable) ev.preventDefault();
        curOffset = Math.max(-ACTION_WIDTH, Math.min(0, baseOffset + dx));
        content.style.transform = `translate3d(${curOffset}px, 0, 0)`;
      }

      function onEnd() {
        if (!tracking) return;
        tracking = false;
        wrap.classList.remove('swiping');
        if (lockedHorizontal) {
          lastSwipeDragTs = Date.now();
          const shouldOpen = curOffset < -ACTION_WIDTH * 0.4;
          content.style.transform = '';
          if (shouldOpen) {
            wrap.classList.add('open');
            activeSwipedWrap = wrap;
          } else {
            wrap.classList.remove('open');
            if (activeSwipedWrap === wrap) activeSwipedWrap = null;
          }
        }
      }

      content.addEventListener('touchstart', (e) => {
        if (e.touches.length === 1) onStart(e.touches[0].clientX, e.touches[0].clientY);
      }, { passive: true });
      content.addEventListener('touchmove', (e) => {
        if (e.touches.length === 1) onMove(e.touches[0].clientX, e.touches[0].clientY, e);
      }, { passive: false });
      content.addEventListener('touchend', onEnd, { passive: true });
      content.addEventListener('touchcancel', onEnd, { passive: true });

      // Desktop / mouse drag support for easy testing in preview
      content.addEventListener('mousedown', (e) => {
        if (e.button !== 0) return;
        onStart(e.clientX, e.clientY);
        const mm = (ev) => onMove(ev.clientX, ev.clientY, ev);
        const mu = () => {
          onEnd();
          window.removeEventListener('mousemove', mm);
          window.removeEventListener('mouseup', mu);
        };
        window.addEventListener('mousemove', mm);
        window.addEventListener('mouseup', mu);
      });
    });
  }

  function getCurrentScreenKey() {
    if (nav.stack.length > 0) {
      const top = nav.stack[nav.stack.length - 1];
      const p = top.params || {};
      return 'stack:' + top.name + ':' + (p.id || p.colKey || p.catKey || p.tag || '');
    }
    return 'tab:' + nav.activeTab;
  }

  function renderModalOnly() {
    const modalRoot = document.getElementById('modal-root');
    if (modalRoot) {
      modalRoot.innerHTML = renderModal();
    }
    bindDynamicInputs();
  }

  // ---------------- Master Render (Vertical + Horizontal Scroll-Preserving) ----------------
  function render(opts) {
    applyTheme();
    stopMarquee();

    const prevTabsRow = document.getElementById('home-tabs-row');
    if (prevTabsRow) {
      nav.homeTabsScrollLeft = prevTabsRow.scrollLeft;
    }

    const viewport = document.getElementById('main-viewport');
    const modalRoot = document.getElementById('modal-root');

    // Capture numeric scroll positions BEFORE replacing viewport.innerHTML
    // (detaching DOM nodes resets their scrollTop/scrollLeft to 0 in Chrome/Safari!)
    const prevScroller = viewport ? viewport.querySelector('.scroll-area') : null;
    const prevScrollTop = prevScroller ? prevScroller.scrollTop : 0;
    const prevHScrolls = viewport
      ? Array.from(viewport.querySelectorAll('.h-scroll-row')).map(el => el.scrollLeft)
      : [];
    if (nav.lastScreenKey) {
      if (prevScroller) nav.scrollMap[nav.lastScreenKey] = prevScrollTop;
      if (prevHScrolls.length) nav.hScrollMap[nav.lastScreenKey] = prevHScrolls;
    }

    const nextKey = getCurrentScreenKey();
    const isSameScreen = nextKey === nav.lastScreenKey;

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
      else if (top.name === 'publish') html = renderPublishScreen();
    } else {
      if (nav.activeTab === 'home') html = renderHomeScreen();
      else if (nav.activeTab === 'discover') html = renderDiscoverScreen();
      else if (nav.activeTab === 'messages') html = renderMessagesScreen();
      else if (nav.activeTab === 'profile') html = renderProfileScreen();
    }

    viewport.innerHTML = html;
    modalRoot.innerHTML = renderModal();
    renderBottomChrome();

    // Restore vertical + horizontal scroll positions so tapping bottom/right tags NEVER jumps back!
    const nextScroller = viewport.querySelector('.scroll-area');
    const nextHRows = Array.from(viewport.querySelectorAll('.h-scroll-row'));
    const resetScroll = Boolean(opts && opts.resetScroll);
    const savedTop = resetScroll
      ? 0
      : (isSameScreen ? prevScrollTop : (nav.scrollMap[nextKey] || 0));
    const savedHScrolls = resetScroll
      ? []
      : (isSameScreen ? prevHScrolls : (nav.hScrollMap[nextKey] || []));

    const applyScrollRestoration = () => {
      if (nextScroller && !resetScroll && savedTop > 0) {
        nextScroller.scrollTop = savedTop;
      }
      if (!resetScroll && savedHScrolls.length) {
        nextHRows.forEach((row, idx) => {
          const left = savedHScrolls[idx];
          if (typeof left === 'number' && left > 0) row.scrollLeft = left;
        });
      }
      const tabsRowNow = document.getElementById('home-tabs-row');
      if (tabsRowNow && nav.homeTabsScrollLeft > 0) {
        tabsRowNow.scrollLeft = nav.homeTabsScrollLeft;
      }
    };

    applyScrollRestoration();
    if (typeof requestAnimationFrame === 'function') {
      requestAnimationFrame(() => {
        if (getCurrentScreenKey() === nextKey) applyScrollRestoration();
      });
    }
    nav.lastScreenKey = nextKey;

    const nextTabsRow = document.getElementById('home-tabs-row');
    if (nextTabsRow) {
      nextTabsRow.addEventListener('scroll', () => {
        nav.homeTabsScrollLeft = nextTabsRow.scrollLeft;
      }, { passive: true });
    }

    if (nav.stack.length === 0 && nav.activeTab === 'discover') {
      startMarquee();
    }
    if (nav.stack.length === 0 && nav.activeTab === 'home') {
      attachPullToRefresh();
    }
    attachSwipeDeleteHandlers();
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

    function handleVerifyFileSelected(file, sourceLabel) {
      if (!file) return;
      if (file.type && !file.type.startsWith('image/')) {
        showToast('请上传图片格式的央美身份凭证');
        return;
      }
      compressImageFile(file, 720, 0.72, (compressedUrl) => {
        if (!compressedUrl) {
          showToast('照片读取失败，请重试');
          return;
        }
        nav.verifyDraft.proofDataUrl = compressedUrl;
        nav.verifyDraft.proofLabel = `已${sourceLabel}「${nav.verifyDraft.proofType}」照片`;
        nav.modal = null;
        render();
        showToast('凭证照片已上传，请确认下方声明后提交审核');
      });
    }

    const verifyCameraInput = document.getElementById('verify-camera-input');
    if (verifyCameraInput) {
      verifyCameraInput.addEventListener('change', (e) => {
        if (e.target.files && e.target.files[0]) {
          handleVerifyFileSelected(e.target.files[0], '现场拍摄');
        }
      });
    }
    const verifyAlbumInput = document.getElementById('verify-album-input');
    if (verifyAlbumInput) {
      verifyAlbumInput.addEventListener('change', (e) => {
        if (e.target.files && e.target.files[0]) {
          handleVerifyFileSelected(e.target.files[0], '从相册选择');
        }
      });
    }

    function bindPubMediaPicker(elId) {
      const inp = document.getElementById(elId);
      if (!inp) return;
      inp.addEventListener('change', (e) => {
        if (!nav.pubDraft || !e.target.files || !e.target.files.length) return;
        syncPubInputsToDraft();
        const remaining = Math.max(0, 4 - nav.pubDraft.mediaList.length);
        const files = Array.from(e.target.files).slice(0, remaining);
        inp.value = '';
        const mod = getModeration();
        files.forEach((file, idx) => {
          compressImageFile(file, 1080, 0.78, async (compressedUrl) => {
            if (!nav.pubDraft || !compressedUrl) return;
            if (mod && typeof mod.inspectImageDataUrl === 'function') {
              const check = await mod.inspectImageDataUrl(compressedUrl, `所选图片 #${idx + 1}`);
              if (!check.passed) {
                showModerationBlock(check.reason);
                return;
              }
            }
            if (!nav.pubDraft || nav.pubDraft.mediaList.length >= 4) return;
            nav.pubDraft.mediaList.push({ src: compressedUrl });
            render();
          });
        });
      });
    }
    bindPubMediaPicker('pub-media-input');
    bindPubMediaPicker('pub-media-toolbar-input');

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
        if (!text || nav.moderating) return;
        requireVerified('发表评论与回复', async () => {
          nav.moderating = true;
          const modRes = await moderateBeforePublish([text], []);
          nav.moderating = false;
          if (!modRes.passed) {
            showModerationBlock(modRes.reason);
            return;
          }
          const postId = commentForm.getAttribute('data-post-id');
          const post = state.posts.find(p => p.id === postId);
          if (post) {
            const newComment = {
              id: 'c_' + Date.now(),
              author: { id: state.persona.id, nickname: state.persona.nickname, avatarSeed: state.persona.avatarSeed, identityType: state.persona.identityType },
              content: text,
              createdAt: '刚刚',
              likes: 0
            };
            post.comments.push(newComment);
            recalculateHotTopicsForDay(getLocalDayKey());
            saveState();
            const cloud = getCloud();
            if (cloud && cloud.isReady() && post._cloud) {
              cloud.addComment(postId, post.authorUid || post.author.id, newComment, post.comments.length).catch(() => {});
            }
            render();
            showToast('评论已发布');
          }
        });
      });
    }
  }

  // ---------------- Event Delegation ----------------
  function syncCommentObserver() {
    const cloud = getCloud();
    if (!cloud) return;
    const top = nav.stack.length > 0 ? nav.stack[nav.stack.length - 1] : null;
    if (top && top.name === 'postDetail' && top.params && top.params.id && cloud.isReady()) {
      const targetId = top.params.id;
      const targetPost = state.posts.find(p => p.id === targetId);
      if (targetPost && targetPost._cloud) {
        cloud.observeComments(targetId, (cloudComments) => {
          const post = state.posts.find(p => p.id === targetId);
          if (!post) return;
          const byId = new Map();
          (post.comments || []).forEach(c => byId.set(c.id, c));
          cloudComments.forEach(c => byId.set(c.id, c));
          post.comments = Array.from(byId.values());
          saveState();
          const cur = nav.stack.length > 0 ? nav.stack[nav.stack.length - 1] : null;
          if (cur && cur.name === 'postDetail' && cur.params && cur.params.id === targetId) {
            render();
          }
        });
        return;
      }
    }
    cloud.stopObservingComments();
  }

  function pushScreen(name, params) {
    nav.stack.push({ name, params: params || {} });
    syncCommentObserver();
    render({ resetScroll: true });
  }

  function popScreen() {
    if (nav.stack.length > 0) {
      nav.stack.pop();
      syncCommentObserver();
      render();
    }
  }

  function openPublishScreen(defaultCat, locked, initialTag) {
    requireVerified('发布校园内容', () => {
      nav.pubDraft = makeDefaultPubDraft(defaultCat || 'TREE_HOLE', Boolean(locked), initialTag);
      const top = nav.stack[nav.stack.length - 1];
      if (top && top.name === 'publish') {
        render({ resetScroll: true });
      } else {
        pushScreen('publish', { category: nav.pubDraft.category, locked: nav.pubDraft.locked });
      }
    });
  }

  function openVerificationFlow() {
    nav.verifyDraft = {
      identityType: state.persona.identityType || '在读学生',
      proofType: getProofTypeOptions(state.persona.identityType || '在读学生')[0],
      proofLabel: (state.verificationRecord && state.verificationRecord.proofLabel) || '',
      proofDataUrl: (state.verificationRecord && state.verificationRecord.proofDataUrl) || '',
      agreedPolicy: false
    };
    pushScreen('verification');
  }

  function clearVerifyAuditTimers() {
    (nav.verifyAuditTimers || []).forEach(t => clearTimeout(t));
    nav.verifyAuditTimers = [];
  }

  document.addEventListener('click', (e) => {
    const actionEl = e.target.closest('[data-action]');
    if (actionEl) {
      e.stopPropagation();
      const act = actionEl.getAttribute('data-action');

      if (act === 'closeModalBackdrop') {
        if (e.target === actionEl) { nav.modal = null; renderModalOnly(); }
        return;
      }
      if (act === 'closeModal') { nav.modal = null; renderModalOnly(); return; }
      if (act === 'goBack') { popScreen(); return; }

      if (act === 'switchTab') {
        nav.activeTab = actionEl.getAttribute('data-tab');
        nav.stack = [];
        render();
        return;
      }

      if (act === 'homeTab') {
        const nextTab = actionEl.getAttribute('data-tab');
        if (!nextTab) return;
        state.homeTab = nextTab;
        const tabsRow = document.getElementById('home-tabs-row');
        const feedScroll = document.getElementById('home-scroll');
        if (tabsRow && feedScroll) {
          tabsRow.querySelectorAll('.aurora-tab-pill').forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('data-tab') === nextTab);
          });
          // Keep the horizontal tab bar's scrollLeft completely stable (never snap/jump!)
          nav.homeTabsScrollLeft = tabsRow.scrollLeft;
          feedScroll.innerHTML = renderHomeFeedInnerHtml(nextTab);
          feedScroll.scrollTop = 0;
          nav.scrollMap['tab:home'] = 0;
          attachPullToRefresh();
        } else {
          render();
        }
        return;
      }

      if (act === 'cloudRefreshNow') {
        const cloud = getCloud();
        if (!cloud || !cloud.isReady()) return;
        nav.cloudSyncing = true;
        render();
        cloud.forceRefresh()
          .then(() => {
            nav.cloudSyncing = false;
            render();
            showToast('已同步全校最新动态');
          })
          .catch(() => {
            nav.cloudSyncing = false;
            render();
            showToast('刷新失败，请检查网络连接');
          });
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
          p.likeCount = Math.max(0, (p.likeCount || 0) + (p.isLiked ? 1 : -1));
          saveState();
          const cloud = getCloud();
          if (cloud && cloud.isReady() && p._cloud) {
            cloud.updatePostCounters(p.id, { likeCount: p.likeCount }).catch(() => {});
          }
          render();
        }
        return;
      }

      if (act === 'bookmarkPost') {
        const p = state.posts.find(x => x.id === actionEl.getAttribute('data-id'));
        if (p) {
          p.isBookmarked = !p.isBookmarked;
          p.bookmarkCount = Math.max(0, (p.bookmarkCount || 0) + (p.isBookmarked ? 1 : -1));
          saveState();
          const cloud = getCloud();
          if (cloud && cloud.isReady() && p._cloud) {
            cloud.updatePostCounters(p.id, { bookmarkCount: p.bookmarkCount }).catch(() => {});
          }
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
        openPublishScreen('TREE_HOLE', false, tg);
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
        openPublishScreen(actionEl.getAttribute('data-cat'), true);
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
        const cid = actionEl.getAttribute('data-id');
        requireVerified('发布选课评价', () => {
          nav.modal = { type: 'addReview', data: { courseId: cid, suitable: '跨专业友好' } };
          render();
        });
        return;
      }
      if (act === 'revPickSuit') {
        if (nav.modal) nav.modal.data.suitable = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'submitReview') {
        if (nav.moderating) return;
        const cid = actionEl.getAttribute('data-id');
        requireVerified('发布选课评价', async () => {
          const c = state.courses.find(x => x.id === cid);
          const content = (document.getElementById('rev-content')?.value || '').trim();
          if (!content) { showToast('请先写下你的课程体验'); return; }
          nav.moderating = true;
          const modRes = await moderateBeforePublish([content], []);
          nav.moderating = false;
          if (!modRes.passed) {
            showModerationBlock(modRes.reason);
            return;
          }
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
        });
        return;
      }
      if (act === 'openCreateCourse') {
        requireVerified('共建选课档案', () => {
          nav.modal = { type: 'createCourse', data: { courseType: '跨院选修', campus: '望京本院' } };
          render();
        });
        return;
      }
      if (act === 'ccPickType') { nav.modal.data.courseType = actionEl.getAttribute('data-val'); render(); return; }
      if (act === 'ccPickCampus') { nav.modal.data.campus = actionEl.getAttribute('data-val'); render(); return; }
      if (act === 'submitNewCourse') {
        if (nav.moderating) return;
        requireVerified('共建选课档案', async () => {
          const name = (document.getElementById('cc-name')?.value || '').trim();
          const instructor = (document.getElementById('cc-instructor')?.value || '').trim();
          const summary = (document.getElementById('cc-summary')?.value || '').trim();
          if (!name || !instructor) { showToast('请填写课程名称和授课教师'); return; }
          nav.moderating = true;
          const modRes = await moderateBeforePublish([name, instructor, summary], []);
          nav.moderating = false;
          if (!modRes.passed) {
            showModerationBlock(modRes.reason);
            return;
          }
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
        });
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
        if (nav.moderating) return;
        requireVerified('在学长学姐说发帖', async () => {
          const body = (document.getElementById('exp-compose-body')?.value || '').trim();
          if (!body) { showToast('请先写下你的经验分享'); return; }
          nav.moderating = true;
          const modRes = await moderateBeforePublish([body], []);
          nav.moderating = false;
          if (!modRes.passed) {
            showModerationBlock(modRes.reason);
            return;
          }
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
        });
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
        const mod = getModeration();
        if (mod && typeof mod.inspectText === 'function') {
          const nickRes = mod.inspectText(nick, (state.settings && state.settings.blockedKeywords) || []);
          if (!nickRes.passed) {
            showModerationBlock(nickRes.reason);
            return;
          }
        }
        state.persona.nickname = nick;
        state.persona.avatarSeed = nav.modal.data.seed;
        saveState();
        nav.modal = null;
        render();
        showToast('匿名形象已更新');
        return;
      }

      if (act === 'gateGoVerify') {
        nav.modal = null;
        openVerificationFlow();
        return;
      }
      if (act === 'openVerification') {
        openVerificationFlow();
        return;
      }
      if (act === 'verifyPickType') {
        nav.verifyDraft.identityType = actionEl.getAttribute('data-val');
        const opts = getProofTypeOptions(nav.verifyDraft.identityType);
        if (!opts.includes(nav.verifyDraft.proofType)) nav.verifyDraft.proofType = opts[0];
        render();
        return;
      }
      if (act === 'verifyPickProofType') {
        nav.verifyDraft.proofType = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'openVerifyPhotoSheet') {
        nav.modal = { type: 'verifyPhotoSource', data: {} };
        render();
        return;
      }
      if (act === 'verifyTogglePolicy') {
        nav.verifyDraft.agreedPolicy = !nav.verifyDraft.agreedPolicy;
        render();
        return;
      }
      if (act === 'submitVerification') {
        if (!nav.verifyDraft.proofDataUrl) {
          showToast('请先拍照或从相册上传央美身份凭证照片');
          nav.modal = { type: 'verifyPhotoSource', data: {} };
          render();
          return;
        }
        if (!nav.verifyDraft.agreedPolicy) {
          showToast('请先勾选凭证真实性与隐私核验确认');
          return;
        }
        clearVerifyAuditTimers();
        state.verificationStatus = 'REVIEWING';
        state.verificationRecord = {
          identityType: nav.verifyDraft.identityType,
          proofType: nav.verifyDraft.proofType,
          proofLabel: `${nav.verifyDraft.proofType} · ${nav.verifyDraft.proofLabel || '央美身份凭证'}`,
          proofDataUrl: nav.verifyDraft.proofDataUrl,
          rejectReason: ''
        };
        nav.verifyAuditStep = 1;
        saveState();
        render();

        nav.verifyAuditTimers.push(setTimeout(() => {
          if (state.verificationStatus !== 'REVIEWING') return;
          nav.verifyAuditStep = 2;
          render();
        }, 950));
        nav.verifyAuditTimers.push(setTimeout(() => {
          if (state.verificationStatus !== 'REVIEWING') return;
          nav.verifyAuditStep = 3;
          render();
        }, 2000));
        nav.verifyAuditTimers.push(setTimeout(() => {
          if (state.verificationStatus !== 'REVIEWING') return;
          state.verificationStatus = 'VERIFIED';
          state.persona.identityType = state.verificationRecord.identityType || '在读学生';
          state.persona.isVerified = true;
          // Destroy proof image after verification completes (privacy + frees storage quota)
          state.verificationRecord.proofDataUrl = '';
          nav.verifyDraft.proofDataUrl = '';
          nav.verifyAuditStep = 0;
          saveState();
          render();
          showToast('央美本校身份核验已通过，已为你解锁发帖与互动权限');
        }, 2900));
        return;
      }
      if (act === 'cancelVerifyReview') {
        clearVerifyAuditTimers();
        nav.verifyAuditStep = 0;
        state.verificationStatus = 'UNVERIFIED';
        state.persona.isVerified = false;
        saveState();
        render();
        showToast('已撤回认证申请，可重新上传凭证');
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
        else if (k === 'VERIFY') openVerificationFlow();
        else pushScreen('profileCollection', { colKey: k });
        return;
      }
      if (act === 'deleteDraft') {
        state.drafts = state.drafts.filter(d => d.id !== actionEl.getAttribute('data-id'));
        saveState();
        render();
        return;
      }
      if (act === 'confirmDeletePost') {
        const pid = actionEl.getAttribute('data-id');
        if (pid) {
          nav.modal = { type: 'deletePostConfirm', data: { postId: pid } };
          renderModalOnly();
        }
        return;
      }
      if (act === 'executeDeletePost') {
        const pid = actionEl.getAttribute('data-id');
        if (pid) {
          const target = state.posts.find(p => p.id === pid);
          state.posts = state.posts.filter(p => p.id !== pid);
          if (!Array.isArray(state.deletedPostIds)) state.deletedPostIds = [];
          if (!state.deletedPostIds.includes(pid)) state.deletedPostIds.push(pid);
          recalculateHotTopicsForDay(getLocalDayKey());
          saveState();
          const cloud = getCloud();
          if (cloud && cloud.isReady() && target && (target._cloud || target.isMine)) {
            cloud.deletePost(pid, target.authorUid || (target.author && target.author.id)).catch(() => {});
          }
          // If deleting from inside Post Detail, pop back cleanly
          const top = nav.stack.length > 0 ? nav.stack[nav.stack.length - 1] : null;
          if (top && top.name === 'postDetail' && top.params && top.params.id === pid) {
            nav.stack.pop();
            syncCommentObserver();
          }
        }
        nav.modal = null;
        render();
        showToast('帖子已删除');
        return;
      }

      // Full-screen Partition-Specific Publish Screen Actions
      if (act === 'closePublishScreen') {
        nav.pubDraft = null;
        popScreen();
        return;
      }
      if (act === 'pubPickCat') {
        syncPubInputsToDraft();
        if (nav.pubDraft) nav.pubDraft.category = actionEl.getAttribute('data-val');
        render();
        return;
      }
      if (act === 'pubSetField') {
        syncPubInputsToDraft();
        const f = actionEl.getAttribute('data-field');
        const v = actionEl.getAttribute('data-val');
        if (nav.pubDraft && f) nav.pubDraft[f] = v;
        render();
        return;
      }
      if (act === 'pubPickLoc') {
        syncPubInputsToDraft();
        const v = actionEl.getAttribute('data-val');
        if (nav.pubDraft) nav.pubDraft.location = nav.pubDraft.location === v ? '' : v;
        render();
        return;
      }
      if (act === 'pubToggleTag') {
        syncPubInputsToDraft();
        const tg = actionEl.getAttribute('data-val');
        if (nav.pubDraft) {
          const arr = nav.pubDraft.tags || [];
          if (arr.includes(tg)) nav.pubDraft.tags = arr.filter(x => x !== tg);
          else if (arr.length < 5) nav.pubDraft.tags = [...arr, tg];
          else showToast('最多选择 5 个话题标签');
        }
        render();
        return;
      }
      if (act === 'pubAddCustomTag') {
        syncPubInputsToDraft();
        if (nav.pubDraft) {
          const clean = (nav.pubDraft.customTagInput || '').trim().replace(/^#/, '').replace(/\s+/g, '').slice(0, 16);
          if (clean) {
            const formatted = '#' + clean;
            if ((nav.pubDraft.tags || []).length >= 5) {
              showToast('最多选择 5 个话题标签');
            } else if (!nav.pubDraft.tags.includes(formatted)) {
              nav.pubDraft.tags.push(formatted);
              nav.pubDraft.customTagInput = '';
            }
          }
        }
        render();
        return;
      }
      if (act === 'pubRemoveMedia') {
        syncPubInputsToDraft();
        const idx = Number(actionEl.getAttribute('data-idx'));
        if (nav.pubDraft && !Number.isNaN(idx)) {
          nav.pubDraft.mediaList.splice(idx, 1);
        }
        render();
        return;
      }
      if (act === 'pubToggleComments') {
        syncPubInputsToDraft();
        if (nav.pubDraft) nav.pubDraft.allowComments = !nav.pubDraft.allowComments;
        render();
        return;
      }
      if (act === 'saveDraft') {
        if (nav.moderating) return;
        syncPubInputsToDraft();
        const d = nav.pubDraft;
        const body = (d && d.body || '').trim();
        if (!body) { showToast('内容为空，未保存草稿'); return; }
        const mod = getModeration();
        if (mod && typeof mod.inspectText === 'function') {
          const check = mod.inspectText(body, (state.settings && state.settings.blockedKeywords) || []);
          if (!check.passed) {
            showModerationBlock(check.reason);
            return;
          }
        }
        state.drafts.unshift({ id: 'draft_' + Date.now(), category: d.category, title: '', body, createdAt: '刚刚' });
        saveState();
        nav.pubDraft = null;
        popScreen();
        showToast('已保存到我的草稿');
        return;
      }
      if (act === 'submitPost') {
        if (nav.moderating) return;
        syncPubInputsToDraft();
        requireVerified('发布校园内容', async () => {
          const d = nav.pubDraft;
          const rawBody = (d && d.body || '').trim();
          if (!rawBody) { showToast('先写点内容再发布吧'); return; }

          const autoTags = [];
          let prefix = '';
          let finalTitle = '';
          let finalPrice;
          let finalMood;
          if (d.category === 'TREE_HOLE') {
            finalMood = d.mood;
          } else if (d.category === 'MARKETPLACE') {
            autoTags.push('#' + d.marketSubType);
            finalPrice = d.price.trim() ? (d.price.trim().startsWith('¥') ? d.price.trim() : '¥ ' + d.price.trim()) : undefined;
            prefix = `【${d.marketSubType} · ${d.marketCondition} · ${d.tradeStatus}】`;
          } else if (d.category === 'OPPORTUNITY') {
            autoTags.push('#' + d.oppType);
            prefix = `【${d.oppType} · ${d.oppMode}${d.oppReward.trim() ? ' · 报酬 ' + d.oppReward.trim() : ''}】`;
            finalTitle = d.oppType;
          } else if (d.category === 'LOST_FOUND') {
            autoTags.push('#' + d.lfType);
            prefix = `【${d.lfType}${d.lfItemName.trim() ? ' ' + d.lfItemName.trim() : ''}】`;
            finalTitle = (d.lfType + ' ' + d.lfItemName.trim()).trim().slice(0, 15);
          } else if (d.category === 'MUTUAL_AID') {
            autoTags.push('#' + d.aidType);
            prefix = `【${d.aidType} · ${d.aidUrgency}${d.aidReward.trim() ? ' · ' + d.aidReward.trim() : ''}】`;
          } else if (d.category === 'EXHIBITION') {
            autoTags.push('#' + d.exhType);
            prefix = `【${d.exhType} · ${d.exhEntry}】`;
          } else if (d.category === 'ART_CREATION') {
            autoTags.push('#' + d.artMedium);
            prefix = `【${d.artMedium} · ${d.artStage}】`;
          } else if (d.category === 'RELATIONSHIP') {
            autoTags.push('#' + d.relVibe);
            finalMood = d.relVibe;
          }

          const finalBody = prefix && !rawBody.startsWith('【') ? `${prefix}\n${rawBody}` : rawBody;
          const mergedTags = Array.from(new Set([...(d.tags || []), ...autoTags])).slice(0, 6);

          nav.moderating = true;
          render();
          const modRes = await moderateBeforePublish(
            [finalTitle, finalBody, mergedTags.join(' '), d.location || '', d.eventTime || '', finalPrice || ''],
            (d.mediaList || []).slice(0, 4)
          );
          nav.moderating = false;
          if (!modRes.passed) {
            render();
            showModerationBlock(modRes.reason);
            return;
          }

          const newPost = {
            id: 'post_' + Date.now(),
            indexCode: 'NO.0' + (413 + state.posts.length),
            title: finalTitle,
            body: finalBody,
            author: {
              id: (nav.account && nav.account.publicUid) || state.persona.id,
              nickname: state.persona.nickname,
              avatarSeed: state.persona.avatarSeed,
              identityType: state.persona.identityType,
              isVerified: true
            },
            category: d.category,
            layoutType: d.category === 'MARKETPLACE' ? 'MARKETPLACE_ITEM'
              : d.category === 'TREE_HOLE' ? 'TREE_HOLE_MOOD'
              : (d.category === 'LOST_FOUND' || d.category === 'MUTUAL_AID' || d.category === 'OPPORTUNITY') ? 'HELP_REQUEST'
              : ((d.mediaList || []).length > 0 ? 'IMAGE_FIRST' : 'EDITORIAL_TEXT'),
            topicTags: mergedTags,
            mediaList: (d.mediaList || []).slice(0, 4),
            createdAt: '刚刚',
            createdAtMs: Date.now(),
            viewCount: 1,
            likeCount: 0,
            bookmarkCount: 0,
            isLiked: false,
            isBookmarked: false,
            isMine: true,
            _cloud: false,
            allowComments: d.allowComments,
            price: finalPrice,
            moodTag: finalMood,
            location: d.location || undefined,
            eventTime: d.eventTime || undefined,
            comments: []
          };
          state.posts.unshift(newPost);
          recalculateHotTopicsForDay(getLocalDayKey());
          saveState();
          const cloud = getCloud();
          if (cloud && cloud.isReady()) {
            const acc = cloud.getAccount();
            newPost.authorUid = acc.publicUid;
            newPost.author.id = acc.publicUid;
            autoSyncAttempted.add(newPost.id);
            cloud.publishPost(newPost)
              .then(() => {
                newPost._cloud = true;
                saveState();
                cloud.forceRefresh().catch(() => {});
              })
              .catch(() => {
                newPost._cloud = false;
                saveState();
                setTimeout(() => autoSyncAttempted.delete(newPost.id), 15000);
                showToast('已保存到本地，云端同步重试中…');
              });
          }
          const catTitle = publishCatLabel(d.category);
          nav.pubDraft = null;
          popScreen();
          if (nav.stack.length === 0) {
            nav.activeTab = 'home';
            state.homeTab = 'LATEST';
          }
          render();
          showToast('已通过安全审查并匿名发布至「' + catTitle + '」');
        });
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
        const inputEl = document.getElementById('feedback-input');
        const fb = (inputEl && inputEl.value || '').trim();
        if (!fb) { showToast('请输入建议内容'); return; }
        if (nav.feedbackSending) return;
        nav.feedbackSending = true;
        render();
        const cloud = getCloud();
        const sendPromise = cloud
          ? cloud.submitFeedback(fb, state.persona.nickname, D.COPYRIGHT.version)
          : Promise.resolve({ emailSent: false, cloudSaved: false });
        sendPromise.then((res) => {
          nav.feedbackSending = false;
          render();
          if (res && res.emailSent) {
            showToast('建议已发送至 1401135032@qq.com');
          } else {
            const subject = encodeURIComponent('【杨梅树窝建议反馈】');
            const body = encodeURIComponent(fb + '\n\n---\n来自匿名同学：' + state.persona.nickname);
            window.location.href = `mailto:1401135032@qq.com?subject=${subject}&body=${body}`;
            showToast('已打开邮箱客户端，收件人：1401135032@qq.com');
          }
        });
        return;
      }
      if (act === 'resetVerifyDemo') {
        clearVerifyAuditTimers();
        nav.verifyAuditStep = 0;
        state.verificationStatus = 'UNVERIFIED';
        state.persona.isVerified = false;
        state.verificationRecord = {
          identityType: state.persona.identityType || '在读学生',
          proofType: '学生证',
          proofLabel: '',
          proofDataUrl: '',
          rejectReason: ''
        };
        nav.verifyDraft = {
          identityType: state.persona.identityType || '在读学生',
          proofType: '学生证',
          proofLabel: '',
          proofDataUrl: '',
          agreedPolicy: false
        };
        saveState();
        render();
        showToast('已重置为未认证访客状态');
        return;
      }
      if (act === 'clearLocalCache') {
        avatarCache.clear();
        showToast('已清理图片内存与本地缓存');
        return;
      }
      return;
    }

    // If a swipe-delete row is currently open or just finished dragging, close it instead of opening post
    if (Date.now() - lastSwipeDragTs < 260) return;
    const openSwipe = document.querySelector('.swipe-delete-wrap.open');
    if (openSwipe) {
      closeOpenSwipes(null);
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
    // Silently compute today's hot topics ranking without prematurely overwriting IDB before hydration
    recalculateHotTopicsForDay(getLocalDayKey(), true);
    scheduleSilentMidnightHotTopics();

    // Splash hollow logo is preloaded directly in index.html (./web/logo-dark.png) — avoid re-assigning src to prevent flicker
    const splashLogo = document.getElementById('splash-logo-img');
    if (splashLogo && !splashLogo.getAttribute('src')) splashLogo.src = getLogoSrc(true);

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
      fab.addEventListener('click', (e) => {
        e.stopPropagation();
        const top = nav.stack[nav.stack.length - 1];
        if (top && top.name === 'seniorExperience') {
          requireVerified('在学长学姐说发帖', () => {
            nav.modal = { type: 'createSenior', data: { cohort: '2024届', category: '留学', body: '' } };
            render();
          });
          return;
        }
        if (top && top.name === 'categoryFeed') {
          openPublishScreen(top.params.catKey, true);
          return;
        }
        openPublishScreen('TREE_HOLE', false);
      });
    }

    // Flush state before mobile browser / WebView exits or backgrounds
    const flushStateOnExit = () => {
      if (idbHydrated || userHasMutated) saveState();
    };
    window.addEventListener('pagehide', flushStateOnExit);
    window.addEventListener('beforeunload', flushStateOnExit);
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'hidden') flushStateOnExit();
    });

    // Hydrate from IndexedDB mirror if available (recovers posts/photos across WebView resets)
    hydrateFromIDB();

    // Connect real-time shared Cloud Firestore bridge
    if (window.YANGMEI_CLOUD) {
      initCloudBridge();
    } else {
      window.addEventListener('yangmei-cloud-ready', initCloudBridge, { once: true });
    }

    render();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
