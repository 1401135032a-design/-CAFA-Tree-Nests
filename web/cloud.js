/* ==========================================================================
   杨梅树窝 (YANGMEI TREE) — Direct Real-Time Cloud Sync
   - Zero login wall: connects immediately to Enterprise Firestore so all
     campus users see each other's posts & comments in real time.
   - Local persistent device identity + SHA-256 deleteVerifier/deleteProof
     ensures every user can delete their own posts securely without login.
   ========================================================================== */
import { initializeApp } from 'https://www.gstatic.com/firebasejs/11.0.2/firebase-app.js';
import {
  getFirestore,
  collection,
  doc,
  query,
  where,
  onSnapshot,
  setDoc,
  updateDoc,
  getDocs,
  getDocsFromServer,
  serverTimestamp
} from 'https://www.gstatic.com/firebasejs/11.0.2/firebase-firestore.js';

const FIREBASE_CONFIG = {
  apiKey: 'AIzaSyDGv9AO1DM4wl39HokwenxsUSc34PD_mYA',
  authDomain: 'gen-lang-client-0565357133.firebaseapp.com',
  projectId: 'gen-lang-client-0565357133',
  storageBucket: 'gen-lang-client-0565357133.firebasestorage.app',
  messagingSenderId: '110247005184',
  appId: '1:110247005184:android:7d6426bf5e6d873605dce5'
};

const FIRESTORE_DATABASE_ID = 'ai-studio-android-d7347173-7e94-4ced-8479-2d7c2988cee4';
const FEEDBACK_EMAIL = '1401135032@qq.com';
const SESSION_STORAGE_KEY = 'yangmei_device_session_v1';
const LEGACY_STORAGE_KEY = 'yangmei_wechat_account_v1';

let app = null;
let db = null;
let unsubPosts = null;
let unsubComments = null;
let callbacks = {};
let cloudConnected = false;
let deviceSession = null; // { publicUid, vaultId }

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

function formatRelativeTime(ts) {
  if (!ts) return '刚刚';
  let ms = 0;
  try {
    ms = typeof ts.toMillis === 'function' ? ts.toMillis() : (ts instanceof Date ? ts.getTime() : 0);
  } catch (_) {
    ms = 0;
  }
  if (!ms) return '刚刚';
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

function mapDocToPost(docSnap) {
  const d = docSnap.data() || {};
  const createdMs = d.createdAt && typeof d.createdAt.toMillis === 'function' ? d.createdAt.toMillis() : Date.now();
  const mediaList = Array.isArray(d.mediaUrls)
    ? d.mediaUrls.filter(u => typeof u === 'string' && u.length > 0).map((u, idx) => ({
        id: docSnap.id + '_m_' + idx,
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
    id: docSnap.id,
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

function mapDocToComment(docSnap) {
  const d = docSnap.data() || {};
  return {
    id: docSnap.id,
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
    likes: Number(d.likes || 0),
    replyToNickname: d.replyToNickname || undefined,
    comfortTag: d.comfortTag || undefined,
    cohortLabel: d.cohortLabel || undefined
  };
}

function startPostsListener() {
  if (!db) return;
  if (unsubPosts) { unsubPosts(); unsubPosts = null; }
  const q = query(collection(db, 'posts'), where('visibility', '==', 'public'));
  unsubPosts = onSnapshot(
    q,
    { includeMetadataChanges: false },
    (snap) => {
      cloudConnected = true;
      const posts = snap.docs
        .map(mapDocToPost)
        .sort((a, b) => (b._createdMs || 0) - (a._createdMs || 0));
      if (callbacks.onConnectionChanged) callbacks.onConnectionChanged(true);
      if (callbacks.onPostsSnapshot) callbacks.onPostsSnapshot(posts);
    },
    (err) => {
      console.warn('[YangmeiCloud] posts listener error:', err);
      if (callbacks.onSyncError) callbacks.onSyncError(err);
    }
  );
}

const cloudApi = {
  FEEDBACK_EMAIL,

  isReady() {
    return Boolean(db);
  },

  isConnected() {
    return cloudConnected;
  },

  getAccount() {
    if (!deviceSession) deviceSession = loadOrCreateSession();
    return { ...deviceSession };
  },

  init(cbs) {
    callbacks = cbs || {};
    deviceSession = loadOrCreateSession();
    try {
      app = initializeApp(FIREBASE_CONFIG);
      db = getFirestore(app, FIRESTORE_DATABASE_ID);
      if (callbacks.onAccountChanged) callbacks.onAccountChanged(this.getAccount());
      startPostsListener();
    } catch (err) {
      console.warn('[YangmeiCloud] init failed:', err);
    }
  },

  async forceRefresh() {
    if (!db) return [];
    const q = query(collection(db, 'posts'), where('visibility', '==', 'public'));
    let snap;
    try {
      snap = await getDocsFromServer(q);
    } catch (_) {
      snap = await getDocs(q);
    }
    cloudConnected = true;
    const posts = snap.docs
      .map(mapDocToPost)
      .sort((a, b) => (b._createdMs || 0) - (a._createdMs || 0));
    if (callbacks.onConnectionChanged) callbacks.onConnectionChanged(true);
    if (callbacks.onPostsSnapshot) callbacks.onPostsSnapshot(posts);
    return posts;
  },

  async publishPost(post) {
    if (!db) return false;
    if (!deviceSession) deviceSession = loadOrCreateSession();
    const mediaUrls = (post.mediaList || [])
      .map(m => (typeof m === 'string' ? m : m.src))
      .filter(u => typeof u === 'string' && u.length > 0 && u.length < 350000)
      .slice(0, 4);

    const payload = {
      authorUid: deviceSession.publicUid,
      visibility: 'public',
      deleteVerifier: computeDeleteVerifier(post.id, deviceSession.vaultId),
      indexCode: String(post.indexCode || 'NO.0413').slice(0, 24),
      title: String(post.title || '').slice(0, 80),
      body: String(post.body || post.title || ' ').slice(0, 5000),
      category: String(post.category || 'TREE_HOLE').slice(0, 40),
      layoutType: String(post.layoutType || 'EDITORIAL_TEXT').slice(0, 40),
      authorNickname: String((post.author && post.author.nickname) || '花家地灰鸽').slice(0, 40),
      authorAvatarSeed: Math.max(0, Math.min(1000000, Number((post.author && post.author.avatarSeed) || 512))),
      authorIdentityType: (post.author && post.author.identityType === '毕业校友') ? 'ALUMNI' : 'STUDENT',
      authorVerified: Boolean(post.author && post.author.isVerified !== false),
      topicTags: Array.isArray(post.topicTags) ? post.topicTags.slice(0, 10) : [],
      mediaUrls,
      viewCount: 1,
      likeCount: 0,
      bookmarkCount: 0,
      commentCount: 0,
      allowComments: post.allowComments !== false,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    };

    if (post.price) payload.price = String(post.price).slice(0, 40);
    if (post.tradeStatus) payload.tradeStatus = String(post.tradeStatus).slice(0, 60);
    if (post.location) payload.location = String(post.location).slice(0, 80);
    if (post.eventTime) payload.eventTime = String(post.eventTime).slice(0, 60);
    if (post.contactHint) payload.contactHint = String(post.contactHint).slice(0, 100);
    if (post.moodTag) payload.moodTag = String(post.moodTag).slice(0, 40);
    if (post.poll && post.poll.question && Array.isArray(post.poll.options)) {
      payload.pollQuestion = String(post.poll.question).slice(0, 120);
      payload.pollOptions = post.poll.options.slice(0, 6).map(o => ({
        id: String(o.id || ''),
        text: String(o.text || '').slice(0, 80),
        votes: Number(o.votes || 0)
      }));
    }

    await setDoc(doc(db, 'posts', post.id), payload);
    return true;
  },

  async deletePost(postId, fallbackAuthorUid) {
    if (!db || !postId) return false;
    if (!deviceSession) deviceSession = loadOrCreateSession();
    const proof = computeDeleteProof(postId, deviceSession.vaultId);
    try {
      await updateDoc(doc(db, 'posts', postId), {
        visibility: 'deleted',
        deleteProof: proof,
        updatedAt: serverTimestamp()
      });
      return true;
    } catch (_) {
      if (fallbackAuthorUid) {
        try {
          await updateDoc(doc(db, 'posts', postId), {
            visibility: 'deleted',
            deleteProof: String(fallbackAuthorUid),
            updatedAt: serverTimestamp()
          });
          return true;
        } catch (__) {}
      }
      return false;
    }
  },

  async updatePostCounters(postId, patch) {
    if (!db || !postId) return false;
    const clean = { updatedAt: serverTimestamp() };
    if (typeof patch.likeCount === 'number') clean.likeCount = Math.max(0, Math.floor(patch.likeCount));
    if (typeof patch.bookmarkCount === 'number') clean.bookmarkCount = Math.max(0, Math.floor(patch.bookmarkCount));
    if (typeof patch.viewCount === 'number') clean.viewCount = Math.max(0, Math.floor(patch.viewCount));
    if (typeof patch.commentCount === 'number') clean.commentCount = Math.max(0, Math.floor(patch.commentCount));
    await updateDoc(doc(db, 'posts', postId), clean);
    return true;
  },

  observeComments(postId, onComments) {
    if (unsubComments) { unsubComments(); unsubComments = null; }
    if (!db || !postId) return;
    const q = query(collection(db, 'posts', postId, 'comments'), where('visibility', '==', 'public'));
    unsubComments = onSnapshot(
      q,
      (snap) => {
        const list = snap.docs.map(mapDocToComment);
        if (onComments) onComments(list);
      },
      () => {}
    );
  },

  stopObservingComments() {
    if (unsubComments) { unsubComments(); unsubComments = null; }
  },

  async addComment(postId, postAuthorUid, comment, nextCommentCount) {
    if (!db) return false;
    if (!deviceSession) deviceSession = loadOrCreateSession();
    const payload = {
      postId,
      postAuthorUid: postAuthorUid || deviceSession.publicUid,
      authorUid: deviceSession.publicUid,
      visibility: 'public',
      authorNickname: String((comment.author && comment.author.nickname) || '花家地灰鸽').slice(0, 40),
      authorAvatarSeed: Math.max(0, Math.min(1000000, Number((comment.author && comment.author.avatarSeed) || 512))),
      authorIdentityType: (comment.author && comment.author.identityType === '毕业校友') ? 'ALUMNI' : 'STUDENT',
      authorVerified: true,
      content: String(comment.content || '').slice(0, 1500),
      likes: 0,
      createdAt: serverTimestamp()
    };
    if (comment.replyToNickname) payload.replyToNickname = String(comment.replyToNickname).slice(0, 40);
    if (comment.comfortTag) payload.comfortTag = String(comment.comfortTag).slice(0, 40);
    await setDoc(doc(db, 'posts', postId, 'comments', comment.id), payload);
    if (typeof nextCommentCount === 'number') {
      await updateDoc(doc(db, 'posts', postId), {
        commentCount: Math.max(0, Math.floor(nextCommentCount)),
        updatedAt: serverTimestamp()
      }).catch(() => {});
    }
    return true;
  },

  async submitFeedback(content, nickname) {
    const cleanContent = String(content || '').trim();
    if (!cleanContent) throw new Error('请输入建议内容');
    if (!deviceSession) deviceSession = loadOrCreateSession();

    if (db) {
      try {
        const fbId = 'fb_' + Date.now() + '_' + Math.floor(Math.random() * 10000);
        await setDoc(doc(db, 'feedbacks', fbId), {
          userId: deviceSession.publicUid,
          authorNickname: String(nickname || '匿名同学').slice(0, 40),
          content: cleanContent.slice(0, 2000),
          targetEmail: FEEDBACK_EMAIL,
          platform: 'web',
          appVersion: '1.0.0',
          createdAt: serverTimestamp()
        });
      } catch (e) {
        console.warn('[YangmeiCloud] feedback Firestore log skipped:', e);
      }
    }

    try {
      const res = await fetch(`https://formsubmit.co/ajax/${FEEDBACK_EMAIL}`, {
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
      });
      if (res.ok) {
        return { delivered: true, method: 'email' };
      }
    } catch (_) {}

    const subject = encodeURIComponent('【杨梅树窝】产品建议与反馈');
    const body = encodeURIComponent(`匿名昵称：${nickname || '匿名同学'}\n\n建议内容：\n${cleanContent}`);
    window.location.href = `mailto:${FEEDBACK_EMAIL}?subject=${subject}&body=${body}`;
    return { delivered: true, method: 'mailto' };
  }
};

window.YANGMEI_CLOUD = cloudApi;
window.dispatchEvent(new CustomEvent('yangmei-cloud-ready'));
