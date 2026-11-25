// Netlify Serverless Function: /.netlify/functions/sync
// Same-origin proxy to 杨梅树窝 Enterprise Cloud Firestore REST API.
// Enables instant, VPN-free real-time cross-device sync for all CAFA students
// in mainland China (Wi-Fi, 4G/5G, WeChat browser, iOS Safari, Android Chrome)
// while sharing the exact same Firestore database as the native Android app.

const PROJECT_ID = 'gen-lang-client-0565357133';
const DATABASE_ID = 'ai-studio-android-d7347173-7e94-4ced-8479-2d7c2988cee4';
const DOC_PREFIX = `projects/${PROJECT_ID}/databases/${DATABASE_ID}/documents`;
const REST_BASE = `https://firestore.googleapis.com/v1/${DOC_PREFIX}`;

const JSON_HEADERS = {
  'Content-Type': 'application/json; charset=utf-8',
  'Cache-Control': 'no-store, no-cache, must-revalidate, max-age=0',
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type'
};

// ---------------- Firestore Value Converters ----------------
function toFsValue(val) {
  if (val === null || val === undefined) return { nullValue: null };
  if (typeof val === 'boolean') return { booleanValue: val };
  if (typeof val === 'number') {
    if (Number.isInteger(val)) return { integerValue: String(val) };
    return { doubleValue: val };
  }
  if (typeof val === 'string') return { stringValue: val };
  if (Array.isArray(val)) {
    return { arrayValue: { values: val.map(toFsValue) } };
  }
  if (typeof val === 'object') {
    const fields = {};
    for (const [k, v] of Object.entries(val)) {
      if (v !== undefined) fields[k] = toFsValue(v);
    }
    return { mapValue: { fields } };
  }
  return { stringValue: String(val) };
}

function toFsFields(obj) {
  const out = {};
  for (const [k, v] of Object.entries(obj || {})) {
    if (v !== undefined) out[k] = toFsValue(v);
  }
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
  if ('arrayValue' in v) {
    const arr = (v.arrayValue && v.arrayValue.values) || [];
    return arr.map(fromFsValue);
  }
  if ('mapValue' in v) {
    const map = (v.mapValue && v.mapValue.fields) || {};
    const res = {};
    for (const [k, val] of Object.entries(map)) {
      res[k] = fromFsValue(val);
    }
    return res;
  }
  return undefined;
}

function fromFsDoc(doc) {
  if (!doc || !doc.name) return null;
  const id = doc.name.split('/').pop();
  const data = {};
  const fields = doc.fields || {};
  for (const [k, v] of Object.entries(fields)) {
    data[k] = fromFsValue(v);
  }
  return { id, data };
}

async function runFirestoreQuery(parentPath, structuredQuery) {
  const url = parentPath
    ? `${REST_BASE}/${parentPath}:runQuery`
    : `${REST_BASE}:runQuery`;
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ structuredQuery })
  });
  const text = await res.text();
  if (!res.ok) {
    throw new Error(`Firestore query ${res.status}: ${text.slice(0, 240)}`);
  }
  const rows = JSON.parse(text || '[]');
  return Array.isArray(rows)
    ? rows.filter(r => r && r.document).map(r => fromFsDoc(r.document)).filter(Boolean)
    : [];
}

async function commitFirestore(writes) {
  const res = await fetch(`${REST_BASE}:commit`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ writes })
  });
  const text = await res.text();
  if (!res.ok) {
    throw new Error(`Firestore commit ${res.status}: ${text.slice(0, 300)}`);
  }
  return JSON.parse(text || '{}');
}

function isValidDocId(id) {
  return typeof id === 'string' && id.length >= 1 && id.length <= 128 && /^[a-zA-Z0-9_-]+$/.test(id);
}

exports.handler = async function (event) {
  if (event.httpMethod === 'OPTIONS') {
    return { statusCode: 204, headers: JSON_HEADERS, body: '' };
  }

  try {
    // GET: list public posts or post comments
    if (event.httpMethod === 'GET') {
      const qs = event.queryStringParameters || {};
      const action = qs.action || 'posts';

      if (action === 'posts') {
        const docs = (await runFirestoreQuery('', {
          from: [{ collectionId: 'posts' }],
          where: {
            fieldFilter: {
              field: { fieldPath: 'visibility' },
              op: 'EQUAL',
              value: { stringValue: 'public' }
            }
          }
        })).filter(d => d && d.id && !String(d.id).startsWith('post_smoketest_'));
        return {
          statusCode: 200,
          headers: JSON_HEADERS,
          body: JSON.stringify({ ok: true, docs })
        };
      }

      if (action === 'comments') {
        const postId = qs.postId || '';
        if (!isValidDocId(postId)) {
          return {
            statusCode: 400,
            headers: JSON_HEADERS,
            body: JSON.stringify({ ok: false, error: 'Invalid postId' })
          };
        }
        const docs = await runFirestoreQuery(`posts/${postId}`, {
          from: [{ collectionId: 'comments' }],
          where: {
            fieldFilter: {
              field: { fieldPath: 'visibility' },
              op: 'EQUAL',
              value: { stringValue: 'public' }
            }
          }
        });
        return {
          statusCode: 200,
          headers: JSON_HEADERS,
          body: JSON.stringify({ ok: true, docs })
        };
      }

      return {
        statusCode: 400,
        headers: JSON_HEADERS,
        body: JSON.stringify({ ok: false, error: 'Unknown GET action' })
      };
    }

    // POST: write operations (publishPost, deletePost, updateCounters, addComment, submitFeedback)
    if (event.httpMethod === 'POST') {
      const body = JSON.parse(event.body || '{}');
      const action = body.action || '';

      if (action === 'publishPost') {
        const postId = body.postId;
        const fields = body.fields || {};
        if (!isValidDocId(postId)) {
          return { statusCode: 400, headers: JSON_HEADERS, body: JSON.stringify({ ok: false, error: 'Invalid postId' }) };
        }
        await commitFirestore([
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
        ]);
        return { statusCode: 200, headers: JSON_HEADERS, body: JSON.stringify({ ok: true }) };
      }

      if (action === 'deletePost') {
        const postId = body.postId;
        const deleteProof = String(body.deleteProof || '');
        if (!isValidDocId(postId) || deleteProof.length < 8) {
          return { statusCode: 400, headers: JSON_HEADERS, body: JSON.stringify({ ok: false, error: 'Invalid delete request' }) };
        }
        await commitFirestore([
          {
            update: {
              name: `${DOC_PREFIX}/posts/${postId}`,
              fields: toFsFields({
                visibility: 'deleted',
                deleteProof
              })
            },
            updateMask: { fieldPaths: ['visibility', 'deleteProof'] },
            updateTransforms: [
              { fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME' }
            ],
            currentDocument: { exists: true }
          }
        ]);
        return { statusCode: 200, headers: JSON_HEADERS, body: JSON.stringify({ ok: true }) };
      }

      if (action === 'updateCounters') {
        const postId = body.postId;
        const patch = body.patch || {};
        if (!isValidDocId(postId)) {
          return { statusCode: 400, headers: JSON_HEADERS, body: JSON.stringify({ ok: false, error: 'Invalid postId' }) };
        }
        const allowed = ['likeCount', 'bookmarkCount', 'viewCount', 'commentCount'];
        const clean = {};
        const fieldPaths = [];
        for (const k of allowed) {
          if (typeof patch[k] === 'number' && Number.isFinite(patch[k])) {
            clean[k] = Math.max(0, Math.floor(patch[k]));
            fieldPaths.push(k);
          }
        }
        if (!fieldPaths.length) {
          return { statusCode: 200, headers: JSON_HEADERS, body: JSON.stringify({ ok: true }) };
        }
        await commitFirestore([
          {
            update: {
              name: `${DOC_PREFIX}/posts/${postId}`,
              fields: toFsFields(clean)
            },
            updateMask: { fieldPaths },
            updateTransforms: [
              { fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME' }
            ],
            currentDocument: { exists: true }
          }
        ]);
        return { statusCode: 200, headers: JSON_HEADERS, body: JSON.stringify({ ok: true }) };
      }

      if (action === 'addComment') {
        const postId = body.postId;
        const commentId = body.commentId;
        const fields = body.fields || {};
        if (!isValidDocId(postId) || !isValidDocId(commentId)) {
          return { statusCode: 400, headers: JSON_HEADERS, body: JSON.stringify({ ok: false, error: 'Invalid IDs' }) };
        }
        await commitFirestore([
          {
            update: {
              name: `${DOC_PREFIX}/posts/${postId}/comments/${commentId}`,
              fields: toFsFields(fields)
            },
            updateTransforms: [
              { fieldPath: 'createdAt', setToServerValue: 'REQUEST_TIME' }
            ],
            currentDocument: { exists: false }
          }
        ]);
        if (typeof body.nextCommentCount === 'number') {
          try {
            await commitFirestore([
              {
                update: {
                  name: `${DOC_PREFIX}/posts/${postId}`,
                  fields: toFsFields({ commentCount: Math.max(0, Math.floor(body.nextCommentCount)) })
                },
                updateMask: { fieldPaths: ['commentCount'] },
                updateTransforms: [
                  { fieldPath: 'updatedAt', setToServerValue: 'REQUEST_TIME' }
                ],
                currentDocument: { exists: true }
              }
            ]);
          } catch (_) { /* comment itself already stored */ }
        }
        return { statusCode: 200, headers: JSON_HEADERS, body: JSON.stringify({ ok: true }) };
      }

      if (action === 'submitFeedback') {
        const feedbackId = body.feedbackId;
        const fields = body.fields || {};
        if (!isValidDocId(feedbackId)) {
          return { statusCode: 400, headers: JSON_HEADERS, body: JSON.stringify({ ok: false, error: 'Invalid feedbackId' }) };
        }
        await commitFirestore([
          {
            update: {
              name: `${DOC_PREFIX}/feedbacks/${feedbackId}`,
              fields: toFsFields(fields)
            },
            updateTransforms: [
              { fieldPath: 'createdAt', setToServerValue: 'REQUEST_TIME' }
            ],
            currentDocument: { exists: false }
          }
        ]);
        return { statusCode: 200, headers: JSON_HEADERS, body: JSON.stringify({ ok: true }) };
      }

      return { statusCode: 400, headers: JSON_HEADERS, body: JSON.stringify({ ok: false, error: 'Unknown POST action' }) };
    }

    return { statusCode: 405, headers: JSON_HEADERS, body: JSON.stringify({ ok: false, error: 'Method Not Allowed' }) };
  } catch (err) {
    return {
      statusCode: 500,
      headers: JSON_HEADERS,
      body: JSON.stringify({ ok: false, error: (err && err.message) || 'Sync failed' })
    };
  }
};
