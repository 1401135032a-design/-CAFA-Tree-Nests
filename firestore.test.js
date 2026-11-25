const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const crypto = require('node:crypto');
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails
} = require('@firebase/rules-unit-testing');

const PROJECT_ID = 'gen-lang-client-0565357133';
const DATABASE_ID = 'ai-studio-android-d7347173-7e94-4ced-8479-2d7c2988cee4';

function sha256Hex(str) {
  return crypto.createHash('sha256').update(str, 'utf8').digest('hex');
}

function nowSafeDate() {
  return new Date(Date.now() - 1000);
}

let testEnv;

test.before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: '127.0.0.1',
      port: 8085,
      rules: fs.readFileSync('firestore.rules', 'utf8')
    }
  });
});

test.after(async () => {
  if (testEnv) await testEnv.cleanup();
});

test.beforeEach(async () => {
  await testEnv.clearFirestore();
});

function getDb() {
  return testEnv.unauthenticatedContext().firestore(DATABASE_ID);
}

test('1. Public posts create, list by visibility==public, and reject ghost fields', async () => {
  const db = getDb();
  const deleteProof = sha256Hex('ym_dp_v1:post_001:wx_vault_secret_1234567890');
  const deleteVerifier = sha256Hex(deleteProof);

  const validPost = {
    authorUid: 'wx_pub_abc123456789',
    visibility: 'public',
    deleteVerifier,
    indexCode: 'NO.0501',
    title: '7号楼夜晚画室',
    body: '今晚画完素描一起去吃夜宵吗？',
    category: 'TREE_HOLE',
    layoutType: 'EDITORIAL_TEXT',
    authorNickname: '花家地灰鸽',
    authorAvatarSeed: 512,
    authorIdentityType: 'STUDENT',
    authorVerified: true,
    topicTags: ['#央美日常'],
    mediaUrls: [],
    viewCount: 1,
    likeCount: 0,
    bookmarkCount: 0,
    commentCount: 0,
    allowComments: true,
    createdAt: nowSafeDate(),
    updatedAt: nowSafeDate()
  };

  await assertSucceeds(db.collection('posts').doc('post_001').set(validPost));
  await assertSucceeds(db.collection('posts').where('visibility', '==', 'public').get());

  // Unfiltered list must be denied because deleted tombstones exist in collection
  await assertFails(db.collection('posts').get());

  // Ghost field injection must be denied
  await assertFails(
    db.collection('posts').doc('post_002').set({ ...validPost, isAdmin: true })
  );
});

test('2. Author-verified SHA-256 tombstone delete succeeds only with matching proof and hides doc', async () => {
  const db = getDb();
  const deleteProof = sha256Hex('ym_dp_v1:post_del:wx_vault_secret_9999999999');
  const deleteVerifier = sha256Hex(deleteProof);

  await assertSucceeds(
    db.collection('posts').doc('post_del').set({
      authorUid: 'wx_pub_owner001',
      visibility: 'public',
      deleteVerifier,
      indexCode: 'NO.0502',
      title: '准备删除的帖子',
      body: '测试左滑删除功能',
      category: 'MUTUAL_AID',
      layoutType: 'EDITORIAL_TEXT',
      authorNickname: '央美同学',
      authorAvatarSeed: 108,
      authorIdentityType: 'STUDENT',
      authorVerified: true,
      topicTags: [],
      mediaUrls: [],
      viewCount: 1,
      likeCount: 0,
      bookmarkCount: 0,
      commentCount: 0,
      allowComments: true,
      createdAt: nowSafeDate(),
      updatedAt: nowSafeDate()
    })
  );

  // Wrong proof must FAIL
  await assertFails(
    db.collection('posts').doc('post_del').update({
      visibility: 'deleted',
      deleteProof: 'wrong_secret_proof_123456',
      updatedAt: nowSafeDate()
    })
  );

  // Physical delete must FAIL
  await assertFails(db.collection('posts').doc('post_del').delete());

  // Valid SHA-256 proof must SUCCEED
  await assertSucceeds(
    db.collection('posts').doc('post_del').update({
      visibility: 'deleted',
      deleteProof,
      updatedAt: nowSafeDate()
    })
  );

  // Tombstoned post is immediately unreadable
  await assertFails(db.collection('posts').doc('post_del').get());
});

test('3. Community counter updates allow ±1 like/bookmark and block tampering', async () => {
  const db = getDb();
  await assertSucceeds(
    db.collection('posts').doc('post_cnt').set({
      authorUid: 'wx_pub_owner002',
      visibility: 'public',
      indexCode: 'NO.0503',
      title: '出闲置油画框',
      body: '九成新，花家地自提',
      category: 'MARKETPLACE',
      layoutType: 'MARKETPLACE_CARD',
      authorNickname: '木炭条',
      authorAvatarSeed: 222,
      authorIdentityType: 'STUDENT',
      authorVerified: true,
      topicTags: ['#闲置交换'],
      mediaUrls: [],
      viewCount: 1,
      likeCount: 0,
      bookmarkCount: 0,
      commentCount: 0,
      allowComments: true,
      price: '¥45',
      createdAt: nowSafeDate(),
      updatedAt: nowSafeDate()
    })
  );

  // +1 like allowed
  await assertSucceeds(
    db.collection('posts').doc('post_cnt').update({
      likeCount: 1,
      updatedAt: nowSafeDate()
    })
  );

  // +100 like inflation denied
  await assertFails(
    db.collection('posts').doc('post_cnt').update({
      likeCount: 101,
      updatedAt: nowSafeDate()
    })
  );

  // Modifying title by arbitrary user denied
  await assertFails(
    db.collection('posts').doc('post_cnt').update({
      title: '被篡改的标题',
      updatedAt: nowSafeDate()
    })
  );
});

test('4. Comments create and like work under existing post', async () => {
  const db = getDb();
  await assertSucceeds(
    db.collection('posts').doc('post_c1').set({
      authorUid: 'wx_pub_owner003',
      visibility: 'public',
      indexCode: 'NO.0504',
      title: '找校园搭子',
      body: '周末看展有人一起吗',
      category: 'RELATIONSHIP',
      layoutType: 'EDITORIAL_TEXT',
      authorNickname: '美术馆晚风',
      authorAvatarSeed: 333,
      authorIdentityType: 'STUDENT',
      authorVerified: true,
      topicTags: [],
      mediaUrls: [],
      viewCount: 1,
      likeCount: 0,
      bookmarkCount: 0,
      commentCount: 0,
      allowComments: true,
      createdAt: nowSafeDate(),
      updatedAt: nowSafeDate()
    })
  );

  await assertSucceeds(
    db.collection('posts').doc('post_c1').collection('comments').doc('c_001').set({
      postId: 'post_c1',
      postAuthorUid: 'wx_pub_owner003',
      authorUid: 'wx_pub_other999',
      visibility: 'public',
      authorNickname: '松节油同学',
      authorAvatarSeed: 444,
      authorIdentityType: 'STUDENT',
      authorVerified: true,
      content: '我可以！下午两点展厅门口见',
      likes: 0,
      createdAt: nowSafeDate()
    })
  );

  await assertSucceeds(
    db.collection('posts').doc('post_c1').collection('comments')
      .where('visibility', '==', 'public').get()
  );
});

test('5. Legacy WeChat collections (account_locks & accounts) are strictly denied', async () => {
  const db = getDb();
  await assertFails(db.collection('account_locks').doc('wxl_test_12345678901234567890').set({ createdAt: nowSafeDate() }));
  await assertFails(db.collection('account_locks').get());
  await assertFails(db.collection('accounts').doc('wxv_test_12345678901234567890').set({ createdAt: nowSafeDate() }));
  await assertFails(db.collection('accounts').get());
});

test('6. Feedback dropbox allows write-only create to 1401135032@qq.com and denies reads', async () => {
  const db = getDb();
  await assertSucceeds(
    db.collection('feedbacks').doc('fb_001').set({
      userId: 'wx_pub_001',
      authorNickname: '央美同学',
      content: '希望增加更多校园互助标签',
      targetEmail: '1401135032@qq.com',
      platform: 'web',
      appVersion: '1.0.0',
      createdAt: nowSafeDate()
    })
  );

  // Wrong target email rejected
  await assertFails(
    db.collection('feedbacks').doc('fb_002').set({
      userId: 'wx_pub_001',
      authorNickname: '央美同学',
      content: 'test',
      targetEmail: 'other@example.com',
      platform: 'web',
      appVersion: '1.0.0',
      createdAt: nowSafeDate()
    })
  );

  // Reading feedbacks denied
  await assertFails(db.collection('feedbacks').doc('fb_001').get());
});

test('7. Notifications allow valid create and recipient-scoped public list, and block tampering/delete', async () => {
  const db = getDb();
  const validNotif = {
    recipientUid: 'ym_recipient_001',
    senderUid: 'ym_sender_002',
    senderNickname: '花家地松节油',
    senderAvatarSeed: 204,
    type: 'LIKE',
    group: 'REACTIONS',
    postId: 'post_001',
    summary: '赞了你的帖子「今晚画室有人吗」',
    visibility: 'public',
    createdAt: nowSafeDate()
  };

  await assertSucceeds(db.collection('notifications').doc('ntf_001').set(validNotif));

  // Querying recipient's public notifications succeeds
  await assertSucceeds(
    db.collection('notifications')
      .where('recipientUid', '==', 'ym_recipient_001')
      .where('visibility', '==', 'public')
      .get()
  );

  // Unfiltered list denied
  await assertFails(db.collection('notifications').get());

  // Invalid type or ghost field denied
  await assertFails(
    db.collection('notifications').doc('ntf_002').set({ ...validNotif, type: 'ADMIN_ALERT' })
  );
  await assertFails(
    db.collection('notifications').doc('ntf_003').set({ ...validNotif, extraField: true })
  );

  // Update and delete denied
  await assertFails(db.collection('notifications').doc('ntf_001').update({ summary: 'tampered' }));
  await assertFails(db.collection('notifications').doc('ntf_001').delete());
});
