/* ==========================================================================
   杨梅树窝 (YANGMEI TREE) — Dual-Transport Real-Time Cloud Sync
   - Zero external CDN dependency (NO gstatic.com import!) so the app loads
     reliably in mainland China Wi-Fi, 4G/5G, and WeChat WebView without VPN.
   - Primary transport: Same-origin Netlify Function (/.netlify/functions/sync)
     proxying to Enterprise Cloud Firestore (works in China with zero VPN!).
   - Automatic fallback: Direct Enterprise Firestore REST API v1 (works in
     static previews / Netlify Drop / outside Netlify).
   - Both transports read & write the EXACT same Enterprise Firestore database
     shared with the native Android app, permanently preserving all posts.
   ========================================================================== */
(function () {
  'use strict';

  const PROJECT_ID = 'gen-lang-client-0565357133';
  const DATABASE_ID = 'ai-studio-android-d7347173-7e94-4ced-8479-2d7c2988cee4';
  const DOC_PREFIX = `projects/${PROJECT_ID}/databases/${DATABASE_ID}/documents`;
  const DIRECT_REST_BASE = `https://firestore.googleapis.com/v1/${DOC_PREFIX}`;
  const PROXY_URL = '/.netlify/functions/sync';
  const FEEDBACK_EMAIL = '1401135032@qq.com';
  const SESSION_STORAGE_KEY = 'yangmei_device_session_v1';
  const LEGACY_STORAGE_KEY = 'yangmei_wechat_account_v1';

  let callbacks = {};
  let cloudConnected = false;
  let transportMode = 'auto'; // 'auto' | 'proxy' | 'direct'
  let deviceSession = null;   // { publicUid, vaultId }
  let postsPollTimer = null;
  let commentsPollTimer = null;
  let activeCommentsPostId = null;
  let lastPostsFingerprint = '';
  let lastCommentsFingerprint = '';
  let inFlightPosts = false;

  // ---------------- Pure JS SHA-256 (Works on HTTP & HTTPS, matches Android MessageDigest) ----------------
  function sha256Hex(ascii) {
    const rightRotate = (v, a) => (v >>> a) | (v << (32 - a));
    const utf8 = unescape(encodeURIComponent(String(ascii)));
    const maxWord = Math.pow(2, 32);
    const words = [];
    const asciiBitLength = utf8.length * 8;
    const hash = [];
    const k = [];
    let primeCounter = 0;
    const isComposite = {};
    for (let candidate = 2; primeCounter < 64; candidate++) {
      if (!isComposite[candidate]) {
        for (let i = 0; i < 313; i += candidate) isComposite[i] = candidate;
        hash[primeCounter] = (Math.pow(candidate, 0.5) * maxWord) | 0;
        k[primeCounter++] = (Math.pow(candidate, 1 / 3) * maxWord) | 0;
      }
    }
    let padded = utf8 + '\x80';
    while ((padded.length % 64) - 56) padded += '\x00';
    for (let i = 0; i < padded.length; i++) {
      const j = padded.charCodeAt(i);
      words[i >> 2] |= j << ((3 - i) % 4) * 8;
    }
    words[words.length] = (asciiBitLength / maxWord) | 0;
    words[words.length] = asciiBitLength;

    for (let j = 0; j < words.length;) {
      const w = words.slice(j, (j += 16));
      const oldHash = hash.slice(0);
      for (let i = 0; i < 64; i++) {
        const w15 = w[i - 15];
        const w2 = w[i - 2];
        const a = hash[0];
        const e = hash[4];
        const temp1 =
          hash[7] +
          (rightRotate(e, 6) ^ rightRotate(e, 11) ^ rightRotate(e, 25)) +
          ((e & hash[5]) ^ (~e & hash[6])) +
          k[i] +
          (w[i] =
            i < 16
              ? w[i]
              : (w[i - 16] +
                  (rightRotate(w15, 7) ^ rightRotate(w15, 18) ^ (w15 >>> 3)) +
                  w[i - 7] +
                  (rightRotate(w2, 17) ^ rightRotate(w2, 19) ^ (w2 >>> 10))) |
                0);
        const temp2 =
          (rightRotate(a, 2) ^ rightRotate(a, 13) ^ rightRotate(a, 22)) +
          ((a & hash[1]) ^ (a & hash[2]) ^ (hash[1] & hash[2]));
        hash.unshift((temp1 + temp2) | 0);
        hash[4] = (hash[4] + temp1) | 0;
        hash.pop();
      }
      for (let i = 0; i < 8; i++) hash[i] = (hash[i] + oldHash[i]) | 0;
    }
    let out = '';
    for (let i = 0; i < 8; i++) {
      for (let j = 3; j + 1; j--) {
        const b = (hash[i] >> (j * 8)) & 255;
        out += (b < 16 ? '0' : '') + b.toString(16);
      }
    }
    return out;
  }

  function randomHex(len) {
    let res = '';
    const chars = '0123456789abcdef';
    for (let i = 0; i < len; i++) res += chars[Math.floor(Math.random() * 16)];
    return res;
  }

  function loadOrCreateSession() {
    try {
      const raw = localStorage.getItem(SESSION_STORAGE_KEY) || localStorage.getItem(LEGACY_STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (parsed && parsed.publicUid && parsed.vaultId) {
          const clean = { publicUid: parsed.publicUid, vaultId: parsed.vaultId };
          localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(clean));
          return clean;
        }
      }
    } catch (_) {}
    const fresh = {
      publicUid: 'ym_' + randomHex(20),
      vaultId: 'ymv_' + randomHex(32)
    };
    try {
      localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(fresh));
    } catch (_) {}
    return fresh;
  }

  function computeDeleteProof(postId, vaultId) {
    return sha256Hex(`ym_dp_v1:${postId}:${vaultId}`);
  }

  function computeDeleteVerifier(postId, vaultId) {
    return sha256Hex(computeDeleteProof(postId, vaultId));
  }

  // ---------------- Firestore REST Value Converters (for Direct Fallback) ----------------
  function toFsValue(val) {
    if (val === null || val === undefined) return { nullValue: null };
    if (typeof val === 'boolean') return { booleanValue: val };
    if (typeof val === 'number') {
      if (Number.isInteger(val)) return { integerValue: String(val) };
      return { doubleValue: val };
    }
    if (typeof val === 'string') return { stringValue: val };
    if (Array.isArray(val)) return { arrayValue: { values: val.map(toFsValue) } };
    if (typeof val === 'object') {
      const fields = {};
      Object.keys(val).forEach(k => {
        if (val[k] !== undefined) fields[k] = toFsValue(val[k]);
      });
      return { mapValue: { fields } };
    }
    return { stringValue: String(val) };
  }

  function toFsFields(obj) {
    const out = {};
    Object.keys(obj || {}).forEach(k => {
      if (obj[k] !== undefined) out[k] = toFsValue(obj[k]);
    });
    return out;
  }

  function fromFsValue(v) {
    if (!v || typeof v !== 'object') return undefined;
    if ('stringValue' in v) return v.stringValue;
    if ('integerValue' in v) return Number(v.integerValue);
    if ('doubleValue' in v) return Number(v.doubleValue);
    if ('booleanValue' in v) return Boolean(v.booleanValue);
    if ('nullValue' in v) return null;
    if ('timestampValue' in v) return v.timestampValue;
    if ('arrayValue' in v) return ((v.arrayValue && v.arrayValue.values) || []).map(fromFsValue);
    if ('mapValue' in v) {
      const map = (v.mapValue && v.mapValue.fields) || {};
      const res = {};
      Object.keys(map).forEach(k => { res[k] = fromFsValue(map[k]); });
      return res;
    }
    return undefined;
  }

  function fromFsDoc(doc) {
    if (!doc || !doc.name) return null;
    const id = doc.name.split('/').pop();
    const data = {};
    const fields = doc.fields || {};
    Object.keys(fields).forEach(k => { data[k] = fromFsValue(fields[k]); });
    return { id, data };
  }

  // ---------------- Fetch with Timeout ----------------
  async function fetchWithTimeout(url, options, timeoutMs) {
    const controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
    const timer = controller ? setTimeout(() => controller.abort(), timeoutMs || 9000) : null;
    try {
      return await fetch(url, Object.assign({ cache: 'no-store' }, options || {}, controller ? { signal: controller.signal } : {}));
    } finally {
      if (timer) clearTimeout(timer);
    }
  }

  // ---------------- Proxy Transport (/.netlify/functions/sync) ----------------
  async function proxyGet(params) {
    const qs = new URLSearchParams(Object.assign({}, params, { _t: String(Date.now()) }));
    const res = await fetchWithTimeout(`${PROXY_URL}?${qs.toString()}`, {
      method: 'GET',
      headers: { Accept: 'application/json' }
    }, 9000);
    const ct = (res.headers.get('content-type') || '').toLowerCase();
    if (!ct.includes('application/json')) {
      throw new Error('PROXY_UNAVAILABLE');
    }
    const json = await res.json();
    if (!res.ok || !json || json.ok === false) {
      throw new Error((json && json.error) || `Proxy HTTP ${res.status}`);
    }
    return json;
  }

  async function proxyPost(body) {
    const res = await fetchWithTimeout(PROXY_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(body)
    }, 10000);
    const ct = (res.headers.get('content-type') || '').toLowerCase();
    if (!ct.includes('application/json')) {
      throw new Error('PROXY_UNAVAILABLE');
    }
    const json = await res.json();
    if (!res.ok || !json || json.ok === false) {
      throw new Error((json && json.error) || `Proxy HTTP ${res.status}`);
    }
    return json;
  }

  // ---------------- Direct Firestore REST Transport (Fallback) ----------------
  async function directQuery(parentPath, structuredQuery) {
    const url = parentPath ? `${DIRECT_REST_BASE}/${parentPath}:runQuery` : `${DIRECT_REST_BASE}:runQuery`;
    const res = await fetchWithTimeout(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ structuredQuery })
    }, 9000);
    const text = await res.text();
    if (!res.ok) throw new Error(`Direct query ${res.status}: ${text.slice(0, 180)}`);
    const rows = JSON.parse(text || '[]');
    return Array.isArray(rows)
      ? rows.filter(r => r && r.document).map(r => fromFsDoc(r.document)).filter(Boolean)
      : [];
  }

  async function directCommit(writes) {
    const res = await fetchWithTimeout(`${DIRECT_REST_BASE}:commit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ writes })
    }, 10000);
    const text = await res.text();
    if (!res.ok) throw new Error(`Direct commit ${res.status}: ${text.slice(0, 200)}`);
    return JSON.parse(text || '{}');
  }

  // ---------------- Unified Dual-Transport Execution ----------------
  async function runDual(proxyFn, directFn) {
    if (transportMode === 'direct') {
      try {
        return await directFn();
      } catch (e1) {
        // Retry proxy in case network changed
        const res = await proxyFn();
        transportMode = 'proxy';
        return res;
      }
    }
    try {
      const res = await proxyFn();
      transportMode = 'proxy';
      return res;
    } catch (eProxy) {
      // Fall back to direct Firestore REST API
      const res = await directFn();
      if (eProxy && eProxy.message === 'PROXY_UNAVAILABLE') {
        transportMode = 'direct';
      }
      return res;
    }
  }

  // ---------------- Time & Document Mapping ----------------
  function parseTimestampMs(ts) {
    if (!ts) return Date.now();
    if (typeof ts === 'number') return ts;
    if (typeof ts === 'string') {
      const parsed = Date.parse(ts);
      return Number.isNaN(parsed) ? Date.now() : parsed;
    }
    return Date.now();
  }

  function formatRelativeTime(ts) {
    const ms = parseTimestampMs(ts);
    const diff = Math.max(0, Date.now() - ms);
    const mins = Math.floor(diff / 60000);
    const hrs = Math.floor(mins / 60);
    const days = Math.floor(hrs / 24);
    if (mins < 1) return '刚刚';
    if (mins < 60) return mins + '分钟前';
    if (hrs < 24) return hrs + '小时前';
    if (days < 7) return days + '天前';
    const dt = new Date(ms);
    return (dt.getMonth() + 1) + '月' + dt.getDate() + '日';
  }

  function mapRecordToPost(rec) {
    const d = (rec && rec.data) || {};
    const id = rec.id;
    const createdMs = parseTimestampMs(d.createdAt);
    const mediaList = Array.isArray(d.mediaUrls)
      ? d.mediaUrls.filter(u => typeof u === 'string' && u.length > 0).map((u, idx) => ({
          id: id + '_m_' + idx,
          src: u,
          caption: ''
        }))
      : [];
    const poll = d.pollQuestion && Array.isArray(d.pollOptions) && d.pollOptions.length >= 2
      ? {
          question: d.pollQuestion,
          options: d.pollOptions.map((o, i) => ({
            id: (o && o.id) || ('opt_' + i),
            text: (o && o.text) || '',
            votes: Number((o && o.votes) || 0)
          })),
          votedOptionId: null
        }
      : undefined;

    const isOwned = Boolean(deviceSession && d.authorUid && d.authorUid === deviceSession.publicUid);
    return {
      id,
      _cloud: true,
      _createdMs: createdMs,
      _hasVerifier: Boolean(d.deleteVerifier),
      authorUid: d.authorUid || '',
      indexCode: d.indexCode || 'NO.0413',
      title: d.title || '',
      body: d.body || '',
      author: {
        id: d.authorUid || 'anon',
        nickname: d.authorNickname || '花家地灰鸽',
        avatarSeed: Number(d.authorAvatarSeed || 512),
        identityType: d.authorIdentityType === 'ALUMNI' ? '毕业校友' : '在读学生',
        isVerified: Boolean(d.authorVerified)
      },
      category: d.category || 'TREE_HOLE',
      layoutType: d.layoutType || 'EDITORIAL_TEXT',
      topicTags: Array.isArray(d.topicTags) ? d.topicTags : [],
      mediaList,
      createdAt: formatRelativeTime(d.createdAt),
      viewCount: Number(d.viewCount || 1),
      likeCount: Number(d.likeCount || 0),
      bookmarkCount: Number(d.bookmarkCount || 0),
      commentCount: Number(d.commentCount || 0),
      allowComments: d.allowComments !== false,
      price: d.price || undefined,
      tradeStatus: d.tradeStatus || undefined,
      location: d.location || undefined,
      eventTime: d.eventTime || undefined,
      contactHint: d.contactHint || undefined,
      moodTag: d.moodTag || undefined,
      poll,
      isMine: isOwned,
      comments: []
    };
  }

  function mapRecordToComment(rec) {
    const d = (rec && rec.data) || {};
    return {
      id: rec.id,
      postId: d.postId || '',
      authorUid: d.authorUid || '',
      author: {
        id: d.authorUid || 'anon',
        nickname: d.authorNickname || '花家地灰鸽',
        avatarSeed: Number(d.authorAvatarSeed || 512),
        identityType: d.authorIdentityType === 'ALUMNI' ? '毕业校友' : '在读学生',
        isVerified: Boolean(d.authorVerified)
      },
      content: d.content || '',
      createdAt: formatRelativeTime(d.createdAt),
      _createdMs: parseTimestampMs(d.createdAt),
      likes: Number(d.likes || 0),
      replyToNickname: d.replyToNickname || undefined,
      comfortTag: d.comfortTag || undefined,
      cohortLabel: d.cohortLabel || undefined
    };
  }

  // ---------------- Live Posts Fetch & Fast Polling ----------------
  async function fetchPublicPosts(forceEmit) {
    if (inFlightPosts && !forceEmit) return null;
    inFlightPosts = true;
    try {
      const docs = await runDual(
        async () => {
          const json = await proxyGet({ action: 'posts' });
          return Array.isArray(json.docs) ? json.docs : [];
        },
        async () => {
          return await directQuery('', {
            from: [{ collectionId: 'posts' }],
            where: {
              fieldFilter: {
                field: { fieldPath: 'visibility' },
                op: 'EQUAL',
                value: { stringValue: 'public' }
              }
            }
          });
        }
      );

      const wasConnected = cloudConnected;
      cloudConnected = true;
      const posts = docs
        .filter(d => d && d.id && !String(d.id).startsWith('post_smoketest_'))
        .map(mapRecordToPost)
        .sort((a, b) => (b._createdMs || 0) - (a._createdMs || 0));

      const fp = posts.map(p => `${p.id}:${p.likeCount}:${p.bookmarkCount}:${p.commentCount}:${p.viewCount}`).join('|');
      if (!wasConnected && callbacks.onConnectionChanged) {
        callbacks.onConnectionChanged(true);
      }
      if (forceEmit || !wasConnected || fp !== lastPostsFingerprint) {
        lastPostsFingerprint = fp;
        if (callbacks.onPostsSnapshot) callbacks.onPostsSnapshot(posts);
      }
      return posts;
    } catch (err) {
      console.warn('[YangmeiCloud] fetchPublicPosts failed:', err);
      if (callbacks.onSyncError) callbacks.onSyncError(err);
      throw err;
    } finally {
      inFlightPosts = false;
    }
  }

  function startPostsPolling() {
    if (postsPollTimer) clearInterval(postsPollTimer);
    fetchPublicPosts(true).catch(() => {});
    postsPollTimer = setInterval(() => {
      if (typeof document !== 'undefined' && document.visibilityState === 'hidden') return;
      fetchPublicPosts(false).catch(() => {});
    }, 4000);
  }

  // Ensure total base64 image payload stays safely under Firestore's 1MB single-document limit
  function fitMediaUrlsForFirestore(rawMediaList) {
    const list = (rawMediaList || [])
      .map(m => (typeof m === 'string' ? m : (m && m.src) || ''))
      .filter(u => typeof u === 'string' && u.length > 0)
      .slice(0, 4);
    const out = [];
    let totalBytes = 0;
    const MAX_TOTAL = 680000;
    for (const u of list) {
      if (u.length < 340000 && (totalBytes + u.length) <= MAX_TOTAL) {
        out.push(u);
        totalBytes += u.length;
      }
    }
    return out;
  }

  const cloudApi = {
    FEEDBACK_EMAIL,

    isReady() {
      return true;
    },

    isConnected() {
      return cloudConnected;
    },

    getTransportMode() {
      return transportMode;
    },

    getAccount() {
      if (!deviceSession) deviceSession = loadOrCreateSession();
      return Object.assign({}, deviceSession);
    },

    init(cbs) {
      callbacks = cbs || {};
      deviceSession = loadOrCreateSession();
      if (callbacks.onAccountChanged) callbacks.onAccountChanged(this.getAccount());
      startPostsPolling();

      if (typeof window !== 'undefined') {
        const triggerSync = () => fetchPublicPosts(false).catch(() => {});
        window.addEventListener('focus', triggerSync);
        window.addEventListener('online', triggerSync);
        if (typeof document !== 'undefined') {
          document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible') triggerSync();
          });
        }
      }
    },

    async forceRefresh() {
      return (await fetchPublicPosts(true)) || [];
    },

    async publishPost(post) {
      if (!deviceSession) deviceSession = loadOrCreateSession();
      const postId = String(post.id || ('post_' + Date.now()));
      const mediaUrls = fitMediaUrlsForFirestore(post.mediaList);

      const fields = {
        authorUid: deviceSession.publicUid,
        visibility: 'public',
        deleteVerifier: computeDeleteVerifier(postId, deviceSession.vaultId),
        indexCode: String(post.indexCode || 'NO.0413').slice(0, 24),
        title: String(post.title || '').slice(0, 80),
        body: String(post.body || post.title || ' ').slice(0, 5000),
        category: String(post.category || 'TREE_HOLE').slice(0, 40),
        layoutType: String(post.layoutType || 'EDITORIAL_TEXT').slice(0, 40),
        authorNickname: String((post.author && post.author.nickname) || '花家地灰鸽').slice(0, 40),
        authorAvatarSeed: Math.max(0, Math.min(1000000, Math.floor(Number((post.author && post.author.avatarSeed) || 512)))),
        authorIdentityType: (post.author && post.author.identityType === '毕业校友') ? 'ALUMNI' : 'STUDENT',
        authorVerified: Boolean(post.author && post.author.isVerified !== false),
        topicTags: Array.isArray(post.topicTags) ? post.topicTags.slice(0, 10) : [],
        mediaUrls,
        viewCount: 1,
        likeCount: 0,
        bookmarkCount: 0,
        commentCount: 0,
        allowComments: post.allowComments !== false
      };

      if (post.price) fields.price = String(post.price).slice(0, 40);
      if (post.tradeStatus) fields.tradeStatus = String(post.tradeStatus).slice(0, 60);
      if (post.location) fields.location = String(post.location).slice(0, 80);
      if (post.eventTime) fields.eventTime = String(post.eventTime).slice(0, 60);
      if (post.contactHint) fields.contactHint = String(post.contactHint).slice(0, 100);
      if (post.moodTag) fields.moodTag = String(post.moodTag).slice(0, 40);
      if (post.poll && post.poll.question && Array.isArray(post.poll.options)) {
        fields.pollQuestion = String(post.poll.question).slice(0, 120);
        fields.pollOptions = post.poll.options.slice(0, 6).map(o => ({
          id: String(o.id || ''),
          text: String(o.text || '').slice(0, 80),
          votes: Math.floor(Number(o.votes || 0))
        }));
      }

      await runDual(
        () => proxyPost({ action: 'publishPost', postId, fields }),
        () => directCommit([
          {
            update: {
              name: `${DOC_PREFIX}/posts/${postId}`,
              fields: toFsFields(fields)
            },
            updateTransforms: [
              { fieldPath: 'createdAt', setToServerValue: 'REQUEST_TIME' },
              { fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME' }
            ],
            currentDocument: { exists: false }
          }
        ])
      );
      cloudConnected = true;
      fetchPublicPosts(true).catch(() => {});
      return true;
    },

    async deletePost(postId, fallbackAuthorUid) {
      if (!postId) return false;
      if (!deviceSession) deviceSession = loadOrCreateSession();
      const proof = computeDeleteProof(postId, deviceSession.vaultId);
      const tryDelete = async (proofStr) => {
        await runDual(
          () => proxyPost({ action: 'deletePost', postId, deleteProof: proofStr }),
          () => directCommit([
            {
              update: {
                name: `${DOC_PREFIX}/posts/${postId}`,
                fields: toFsFields({ visibility: 'deleted', deleteProof: proofStr })
              },
              updateMask: { fieldPaths: ['visibility', 'deleteProof'] },
              updateTransforms: [{ fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME' }],
              currentDocument: { exists: true }
            }
          ])
        );
      };

      try {
        await tryDelete(proof);
        fetchPublicPosts(true).catch(() => {});
        return true;
      } catch (_) {
        if (fallbackAuthorUid) {
          try {
            await tryDelete(String(fallbackAuthorUid));
            fetchPublicPosts(true).catch(() => {});
            return true;
          } catch (__) {}
        }
        return false;
      }
    },

    async updatePostCounters(postId, patch) {
      if (!postId || !patch) return false;
      const clean = {};
      const fieldPaths = [];
      ['likeCount', 'bookmarkCount', 'viewCount', 'commentCount'].forEach(k => {
        if (typeof patch[k] === 'number' && Number.isFinite(patch[k])) {
          clean[k] = Math.max(0, Math.floor(patch[k]));
          fieldPaths.push(k);
        }
      });
      if (!fieldPaths.length) return true;

      await runDual(
        () => proxyPost({ action: 'updateCounters', postId, patch: clean }),
        () => directCommit([
          {
            update: {
              name: `${DOC_PREFIX}/posts/${postId}`,
              fields: toFsFields(clean)
            },
            updateMask: { fieldPaths },
            updateTransforms: [{ fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME' }],
            currentDocument: { exists: true }
          }
        ])
      );
      return true;
    },

    observeComments(postId, onComments) {
      this.stopObservingComments();
      if (!postId) return;
      activeCommentsPostId = postId;
      lastCommentsFingerprint = '';

      const loadOnce = async () => {
        if (activeCommentsPostId !== postId) return;
        try {
          const docs = await runDual(
            async () => {
              const json = await proxyGet({ action: 'comments', postId });
              return Array.isArray(json.docs) ? json.docs : [];
            },
            async () => {
              return await directQuery(`posts/${postId}`, {
                from: [{ collectionId: 'comments' }],
                where: {
                  fieldFilter: {
                    field: { fieldPath: 'visibility' },
                    op: 'EQUAL',
                    value: { stringValue: 'public' }
                  }
                }
              });
            }
          );
          if (activeCommentsPostId !== postId) return;
          const list = docs
            .map(mapRecordToComment)
            .sort((a, b) => (a._createdMs || 0) - (b._createdMs || 0));
          const fp = list.map(c => `${c.id}:${c.likes}`).join('|');
          if (fp !== lastCommentsFingerprint) {
            lastCommentsFingerprint = fp;
            if (onComments) onComments(list);
          }
        } catch (_) {}
      };

      loadOnce();
      commentsPollTimer = setInterval(() => {
        if (typeof document !== 'undefined' && document.visibilityState === 'hidden') return;
        loadOnce();
      }, 3000);
    },

    stopObservingComments() {
      activeCommentsPostId = null;
      if (commentsPollTimer) {
        clearInterval(commentsPollTimer);
        commentsPollTimer = null;
      }
    },

    async addComment(postId, postAuthorUid, comment, nextCommentCount) {
      if (!deviceSession) deviceSession = loadOrCreateSession();
      const commentId = String(comment.id || ('c_' + Date.now()));
      const fields = {
        postId,
        postAuthorUid: postAuthorUid || deviceSession.publicUid,
        authorUid: deviceSession.publicUid,
        visibility: 'public',
        authorNickname: String((comment.author && comment.author.nickname) || '花家地灰鸽').slice(0, 40),
        authorAvatarSeed: Math.max(0, Math.min(1000000, Math.floor(Number((comment.author && comment.author.avatarSeed) || 512)))),
        authorIdentityType: (comment.author && comment.author.identityType === '毕业校友') ? 'ALUMNI' : 'STUDENT',
        authorVerified: true,
        content: String(comment.content || '').slice(0, 1500),
        likes: 0
      };
      if (comment.replyToNickname) fields.replyToNickname = String(comment.replyToNickname).slice(0, 40);
      if (comment.comfortTag) fields.comfortTag = String(comment.comfortTag).slice(0, 40);

      await runDual(
        () => proxyPost({ action: 'addComment', postId, commentId, fields, nextCommentCount }),
        async () => {
          await directCommit([
            {
              update: {
                name: `${DOC_PREFIX}/posts/${postId}/comments/${commentId}`,
                fields: toFsFields(fields)
              },
              updateTransforms: [{ fieldPath: 'createdAt', setToServerValue: 'REQUEST_TIME' }],
              currentDocument: { exists: false }
            }
          ]);
          if (typeof nextCommentCount === 'number') {
            await directCommit([
              {
                update: {
                  name: `${DOC_PREFIX}/posts/${postId}`,
                  fields: toFsFields({ commentCount: Math.max(0, Math.floor(nextCommentCount)) })
                },
                updateMask: { fieldPaths: ['commentCount'] },
                updateTransforms: [{ fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME' }],
                currentDocument: { exists: true }
              }
            ]).catch(() => {});
          }
        }
      );
      return true;
    },

    async submitFeedback(content, nickname) {
      const cleanContent = String(content || '').trim();
      if (!cleanContent) throw new Error('请输入建议内容');
      if (!deviceSession) deviceSession = loadOrCreateSession();

      const feedbackId = 'fb_' + Date.now() + '_' + Math.floor(Math.random() * 10000);
      const fields = {
        userId: deviceSession.publicUid,
        authorNickname: String(nickname || '匿名同学').slice(0, 40),
        content: cleanContent.slice(0, 2000),
        targetEmail: FEEDBACK_EMAIL,
        platform: 'web',
        appVersion: '1.0.0'
      };

      try {
        await runDual(
          () => proxyPost({ action: 'submitFeedback', feedbackId, fields }),
          () => directCommit([
            {
              update: {
                name: `${DOC_PREFIX}/feedbacks/${feedbackId}`,
                fields: toFsFields(fields)
              },
              updateTransforms: [{ fieldPath: 'createdAt', setToServerValue: 'REQUEST_TIME' }],
              currentDocument: { exists: false }
            }
          ])
        );
      } catch (e) {
        console.warn('[YangmeiCloud] feedback Firestore log skipped:', e);
      }

      try {
        const res = await fetchWithTimeout(`https://formsubmit.co/ajax/${FEEDBACK_EMAIL}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json'
          },
          body: JSON.stringify({
            _subject: '【杨梅树窝用户建议】来自 ' + (nickname || '央美同学'),
            _template: 'box',
            nickname: nickname || '匿名同学',
            platform: '杨梅树窝 Web',
            message: cleanContent
          })
        }, 8000);
        if (res.ok) {
          return { emailSent: true, cloudSaved: true, method: 'email' };
        }
      } catch (_) {}

      return { emailSent: false, cloudSaved: true, method: 'mailto' };
    }
  };

  window.YANGMEI_CLOUD = cloudApi;
  if (typeof window !== 'undefined' && typeof CustomEvent !== 'undefined') {
    window.dispatchEvent(new CustomEvent('yangmei-cloud-ready'));
  }
})();
