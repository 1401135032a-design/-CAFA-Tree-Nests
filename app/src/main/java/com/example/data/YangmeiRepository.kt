package com.example.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val Context.yangmeiDataStore by preferencesDataStore(name = "yangmei_tree_prefs")

class LocalCloudMediaStorageService : CloudMediaStorageGateway {
    override suspend fun uploadLocalMedia(uriString: String): String {
        return uriString
    }
}

class YangmeiRepository(
    private val context: Context,
    private val db: FirebaseFirestore,
    private val sessionManager: DeviceSessionManager,
    val moderationGateway: ContentModerationGateway = com.example.ai.ContentModerationService(context),
    val storageGateway: CloudMediaStorageGateway = LocalCloudMediaStorageService()
) : CommunityDatabaseGateway, CampusAuthVerificationGateway {

    private val currentUid: String get() = sessionManager.currentPublicUid

    private object Keys {
        val VERIFICATION_STATUS = stringPreferencesKey("verification_status")
        val IDENTITY_TYPE = stringPreferencesKey("identity_type")
        val COLLEGE = stringPreferencesKey("college")
        val MAJOR = stringPreferencesKey("major")
        val YEAR = stringPreferencesKey("year")
        val ANON_NICKNAME = stringPreferencesKey("anon_nickname")
        val ANON_SEED = intPreferencesKey("anon_seed")
        val SHOW_COLLEGE_TAG = booleanPreferencesKey("show_college_tag")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val NOTIFY_COMMENTS = booleanPreferencesKey("notify_comments")
        val NOTIFY_LIKES = booleanPreferencesKey("notify_likes")
        val NOTIFY_SYSTEM = booleanPreferencesKey("notify_system")
        val ALLOW_ANON_NOTES = booleanPreferencesKey("allow_anon_notes")
        val BLOCKED_USERS = stringPreferencesKey("blocked_users")
        val BLOCKED_KEYWORDS = stringPreferencesKey("blocked_keywords")
        val LAST_HOT_TOPICS_DATE = stringPreferencesKey("last_hot_topics_date")
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val userContentFile: File by lazy { File(context.filesDir, "yangmei_user_content_v1.json") }
    private val deletedPostIds = mutableSetOf<String>()
    private val likedPostIds = mutableSetOf<String>()
    private val bookmarkedPostIds = mutableSetOf<String>()
    private val readNotificationIds = mutableSetOf<String>()
    private val cloudPostDtos = mutableMapOf<String, FirestorePostDto>()
    private val cloudCommentsByPost = mutableMapOf<String, List<Comment>>()
    private var cloudNotificationDtos: List<FirestoreNotificationDto> = emptyList()
    private val localPushedNotifications = mutableListOf<NotificationItem>()
    private val seedPostIds: Set<String> = MockSeedData.initialPosts.map { it.id }.toSet()
    private var localSavedUserPosts: List<Post> = emptyList()
    private var seedExtraComments: MutableMap<String, List<Comment>> = mutableMapOf()

    private val _posts = MutableStateFlow(MockSeedData.initialPosts)
    override val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _drafts = MutableStateFlow<List<Post>>(
        listOf(
            Post(
                id = "draft_01",
                indexCode = "DRAFT",
                title = "关于春季工作室选修的几点笔记（草稿）",
                body = "整理了一下这学期去旁听雕塑和版画两门课的感受，等下周出成绩再补充完发出来...",
                author = AnonymousPersona(currentUid, "花家地灰鸽", 512, IdentityType.STUDENT, false),
                category = PostCategory.STUDY,
                layoutType = PostLayoutType.EDITORIAL_TEXT,
                topicTags = listOf("#央美选课"),
                createdAt = "保存于 昨天",
                isDraft = true,
                isMine = true
            )
        )
    )
    override val drafts: StateFlow<List<Post>> = _drafts.asStateFlow()

    private val _hotTopics = MutableStateFlow(MockSeedData.initialHotTopics)
    override val hotTopics: StateFlow<List<HotTopic>> = _hotTopics.asStateFlow()

    private val _courses = MutableStateFlow(MockSeedData.initialCourses)
    override val courses: StateFlow<List<CourseRecord>> = _courses.asStateFlow()

    private val _experiences = MutableStateFlow(MockSeedData.initialExperiences)
    override val experiences: StateFlow<List<ExperienceArticle>> = _experiences.asStateFlow()

    private val _notifications = MutableStateFlow(MockSeedData.initialNotifications)
    override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _verificationStatus = MutableStateFlow(VerificationStatus.UNVERIFIED)
    override val verificationStatus: StateFlow<VerificationStatus> = _verificationStatus.asStateFlow()

    private val _verificationRecord = MutableStateFlow(VerificationRecord())
    override val verificationRecord: StateFlow<VerificationRecord> = _verificationRecord.asStateFlow()

    private val _currentPersona = MutableStateFlow(
        AnonymousPersona(
            id = currentUid,
            nickname = "花家地灰鸽",
            avatarSeed = 512,
            identityType = IdentityType.STUDENT,
            isVerified = false,
            optionalCollegeTag = null,
            optionalYearTag = null
        )
    )
    override val currentPersona: StateFlow<AnonymousPersona> = _currentPersona.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    val todaySnapshot: TodayCampusSnapshot = MockSeedData.todaySnapshot

    /**
     * Real-time Firestore stream of all shared public community posts across all users.
     * Query matches firestore.rules `whereEqualTo("visibility", "public")` 1:1.
     */
    fun observeSharedCloudPosts(): Flow<List<FirestorePostDto>> {
        return db.collection("posts")
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc ->
                        try {
                            doc.toObject(
                                FirestorePostDto::class.java,
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                            )
                        } catch (e: Exception) {
                            Log.w("YangmeiRepository", "Skipping malformed post doc ${doc.id}", e)
                            null
                        }
                    }
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            }
    }

    fun applyCloudPostsSnapshot(dtos: List<FirestorePostDto>) {
        cloudPostDtos.clear()
        dtos.forEach { dto ->
            if (dto.id.isNotBlank() && !dto.id.startsWith("post_smoketest_") && !deletedPostIds.contains(dto.id)) {
                cloudPostDtos[dto.id] = dto
            }
        }
        rebuildMergedPosts()
    }

    fun observeMyCloudNotifications(): Flow<List<FirestoreNotificationDto>> {
        val uid = currentUid
        if (uid.isBlank()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return db.collection("notifications")
            .whereEqualTo("recipientUid", uid)
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc ->
                        try {
                            doc.toObject(
                                FirestoreNotificationDto::class.java,
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                    .filterNot { it.id.startsWith("ntf_smoketest_") }
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            }
    }

    fun applyCloudNotificationsSnapshot(dtos: List<FirestoreNotificationDto>) {
        cloudNotificationDtos = dtos
        rebuildMergedNotifications()
    }

    private fun rebuildMergedNotifications() {
        val cloudItems = cloudNotificationDtos.map { dto ->
            FirestoreMappers.fromNotificationDto(dto, readNotificationIds)
        }
        val localItems = localPushedNotifications.map { item ->
            item.copy(isUnread = item.isUnread && !readNotificationIds.contains(item.id))
        }
        _notifications.value = cloudItems + localItems
    }

    private fun postSnippetText(post: Post?): String {
        val raw = (post?.title?.takeIf { it.isNotBlank() } ?: post?.body ?: "校园动态")
            .replace(Regex("\\s+"), " ")
            .trim()
        return if (raw.length > 22) raw.take(22) + "…" else raw
    }

    private fun sendCloudNotification(
        recipientUid: String,
        type: String,
        group: String,
        postId: String,
        summary: String
    ) {
        val cleanRecipient = recipientUid.trim()
        if (cleanRecipient.isBlank() || cleanRecipient == currentUid || postId.isBlank()) return
        val persona = _currentPersona.value
        val notifId = "ntf_${System.currentTimeMillis()}_${(100000..999999).random()}"
        ioScope.launch {
            try {
                val payload = FirestoreMappers.toNotificationCreateMap(
                    recipientUid = cleanRecipient,
                    senderUid = currentUid,
                    senderNickname = persona.nickname,
                    senderAvatarSeed = persona.avatarSeed,
                    type = type,
                    group = group,
                    postId = postId,
                    summary = summary
                )
                db.collection("notifications").document(notifId).set(payload).await()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Non-critical notification dispatch
            }
        }
    }

    suspend fun forceRefreshFromCloud(): Result<Int> {
        return try {
            val snapshot = try {
                db.collection("posts")
                    .whereEqualTo("visibility", "public")
                    .get(Source.SERVER)
                    .await()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                db.collection("posts")
                    .whereEqualTo("visibility", "public")
                    .get(Source.DEFAULT)
                    .await()
            }
            val dtos = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(
                        FirestorePostDto::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } catch (_: Exception) {
                    null
                }
            }.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            applyCloudPostsSnapshot(dtos)
            Result.success(dtos.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "posts")
            Result.failure(e)
        }
    }

    fun observePostComments(postId: String): Flow<List<Comment>> {
        return db.collection("posts")
            .document(postId)
            .collection("comments")
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc ->
                        try {
                            doc.toObject(
                                FirestoreCommentDto::class.java,
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                            )?.let { FirestoreMappers.fromCommentDto(it) }
                        } catch (_: Exception) {
                            null
                        }
                    }
            }
    }

    fun applyCloudComments(postId: String, comments: List<Comment>) {
        cloudCommentsByPost[postId] = comments
        rebuildMergedPosts()
    }

    private fun rebuildMergedPosts() {
        val currentMap = _posts.value.associateBy { it.id }

        // 1. Live cloud posts (shared across all users, sorted newest first)
        val cloudPosts = cloudPostDtos.values
            .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
            .map { dto ->
                val existingLocal = currentMap[dto.id]
                val mergedComments = cloudCommentsByPost[dto.id]
                    ?: existingLocal?.comments
                    ?: emptyList()
                FirestoreMappers.fromPostDto(
                    dto = dto,
                    currentUid = currentUid,
                    likedIds = likedPostIds,
                    bookmarkedIds = bookmarkedPostIds,
                    comments = mergedComments
                )
            }
        val cloudIds = cloudPosts.map { it.id }.toSet()

        // 2. Local pending/cached user posts not yet mirrored in cloud snapshot
        val localOnlyUserPosts = localSavedUserPosts
            .filterNot { deletedPostIds.contains(it.id) || cloudIds.contains(it.id) }

        // 3. Bundled campus seed posts (always preserved underneath live community posts)
        val seeds = MockSeedData.initialPosts
            .filterNot { deletedPostIds.contains(it.id) || cloudIds.contains(it.id) }
            .map { seed ->
                val existing = currentMap[seed.id]
                val liked = likedPostIds.contains(seed.id)
                val bookmarked = bookmarkedPostIds.contains(seed.id)
                val extraComments = seedExtraComments[seed.id].orEmpty()
                val baseComments = existing?.comments?.takeIf { it.size >= seed.comments.size } ?: seed.comments
                val existingIds = baseComments.map { it.id }.toSet()
                val mergedComments = baseComments + extraComments.filterNot { existingIds.contains(it.id) }
                seed.copy(
                    isLiked = liked,
                    likeCount = if (liked && !seed.isLiked) seed.likeCount + 1 else seed.likeCount,
                    isBookmarked = bookmarked,
                    bookmarkCount = if (bookmarked && !seed.isBookmarked) seed.bookmarkCount + 1 else seed.bookmarkCount,
                    viewCount = existing?.viewCount ?: seed.viewCount,
                    poll = existing?.poll ?: seed.poll,
                    comments = mergedComments
                )
            }

        _posts.value = cloudPosts + localOnlyUserPosts + seeds
    }

    suspend fun hydrateFromPreferences() {
        val prefs = context.yangmeiDataStore.data.first()
        val statusName = prefs[Keys.VERIFICATION_STATUS]
        val status = VerificationStatus.entries.firstOrNull { it.name == statusName } ?: VerificationStatus.UNVERIFIED
        val idTypeName = prefs[Keys.IDENTITY_TYPE]
        val idType = IdentityType.entries.firstOrNull { it.name == idTypeName } ?: IdentityType.STUDENT
        val college = prefs[Keys.COLLEGE] ?: ""
        val major = prefs[Keys.MAJOR] ?: ""
        val year = prefs[Keys.YEAR] ?: ""
        val nickname = prefs[Keys.ANON_NICKNAME] ?: "花家地灰鸽"
        val seed = prefs[Keys.ANON_SEED] ?: 512
        val showCollege = prefs[Keys.SHOW_COLLEGE_TAG] ?: true

        _verificationStatus.value = status
        _verificationRecord.value = VerificationRecord(
            identityType = idType,
            school = "中央美术学院",
            college = college,
            major = major,
            year = year,
            submittedAt = if (status != VerificationStatus.UNVERIFIED) "已提交校园资料" else ""
        )
        _currentPersona.value = AnonymousPersona(
            id = currentUid,
            nickname = nickname,
            avatarSeed = seed,
            identityType = idType,
            isVerified = status == VerificationStatus.VERIFIED,
            optionalCollegeTag = null,
            optionalYearTag = null
        )

        val blockedUsers = prefs[Keys.BLOCKED_USERS]?.split("|")?.filter { it.isNotBlank() }
            ?: listOf("失焦的石膏像素描")
        val blockedKeywords = prefs[Keys.BLOCKED_KEYWORDS]?.split("|")?.filter { it.isNotBlank() }
            ?: listOf("广告", "代写")

        _settings.value = UserSettings(
            darkTheme = prefs[Keys.DARK_THEME] ?: false,
            showCollegeTag = showCollege,
            notifyComments = prefs[Keys.NOTIFY_COMMENTS] ?: true,
            notifyLikes = prefs[Keys.NOTIFY_LIKES] ?: true,
            notifySystem = prefs[Keys.NOTIFY_SYSTEM] ?: true,
            allowAnonymousNotes = prefs[Keys.ALLOW_ANON_NOTES] ?: true,
            blockedUsers = blockedUsers,
            blockedKeywords = blockedKeywords
        )

        hydrateUserContentFromDisk()
    }

    suspend fun submitFeedbackToCloud(content: String, appVersion: String): Result<Unit> {
        val clean = content.trim()
        if (clean.isEmpty()) return Result.failure(IllegalArgumentException("Empty feedback"))
        val feedbackId = "fb_${System.currentTimeMillis()}_${currentUid.take(6)}"
        return try {
            val payload = FirestoreMappers.toFeedbackCreateMap(
                uid = currentUid,
                nickname = _currentPersona.value.nickname,
                content = clean,
                appVersion = appVersion
            )
            db.collection("feedbacks")
                .document(feedbackId)
                .set(payload)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "feedbacks/$feedbackId")
            Result.failure(e)
        }
    }

    private suspend fun hydrateUserContentFromDisk() = withContext(Dispatchers.IO) {
        try {
            if (!userContentFile.exists()) return@withContext
            val text = userContentFile.readText(Charsets.UTF_8)
            if (text.isBlank()) return@withContext
            val root = JSONObject(text)

            val deletedArr = root.optJSONArray("deletedPostIds")
            deletedPostIds.clear()
            if (deletedArr != null) {
                for (i in 0 until deletedArr.length()) {
                    val id = deletedArr.optString(i)
                    if (id.isNotBlank()) deletedPostIds.add(id)
                }
            }

            val savedMine = mutableListOf<Post>()
            val mineArr = root.optJSONArray("userPosts")
            if (mineArr != null) {
                for (i in 0 until mineArr.length()) {
                    val obj = mineArr.optJSONObject(i) ?: continue
                    val p = decodePostJson(obj) ?: continue
                    if (!deletedPostIds.contains(p.id)) {
                        savedMine.add(p)
                    }
                }
            }
            localSavedUserPosts = savedMine

            root.optJSONArray("likedSeedIds")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val id = arr.optString(i)
                    if (id.isNotBlank()) likedPostIds.add(id)
                }
            }
            root.optJSONArray("bookmarkedSeedIds")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val id = arr.optString(i)
                    if (id.isNotBlank()) bookmarkedPostIds.add(id)
                }
            }
            root.optJSONArray("readNotificationIds")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val id = arr.optString(i)
                    if (id.isNotBlank()) readNotificationIds.add(id)
                }
            }
            seedExtraComments.clear()
            root.optJSONObject("seedComments")?.let { obj ->
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val postId = keys.next()
                    val arr = obj.optJSONArray(postId) ?: continue
                    val list = mutableListOf<Comment>()
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { cObj -> decodeCommentJson(cObj)?.let(list::add) }
                    }
                    if (list.isNotEmpty()) seedExtraComments[postId] = list
                }
            }

            rebuildMergedPosts()
            rebuildMergedNotifications()

            val draftsArr = root.optJSONArray("drafts")
            if (draftsArr != null) {
                val restoredDrafts = mutableListOf<Post>()
                for (i in 0 until draftsArr.length()) {
                    draftsArr.optJSONObject(i)?.let { obj ->
                        decodePostJson(obj)?.let { restoredDrafts.add(it.copy(isDraft = true, isMine = true)) }
                    }
                }
                _drafts.value = restoredDrafts
            }
        } catch (e: Exception) {
            Log.w("YangmeiRepository", "Failed to hydrate local user content", e)
        }
    }

    private fun persistUserContentAsync() {
        val postsSnapshot = _posts.value
        val draftsSnapshot = _drafts.value
        val deletedSnapshot = deletedPostIds.toList()
        val likedSnapshot = likedPostIds.toList()
        val bookmarkedSnapshot = bookmarkedPostIds.toList()
        val readNotifSnapshot = readNotificationIds.toList()
        ioScope.launch {
            try {
                val root = JSONObject()
                root.put("deletedPostIds", JSONArray(deletedSnapshot))

                val userPostsArr = JSONArray()
                postsSnapshot.filter { it.isMine || !seedPostIds.contains(it.id) }.forEach { post ->
                    userPostsArr.put(encodePostJson(post))
                }
                root.put("userPosts", userPostsArr)

                val draftsArr = JSONArray()
                draftsSnapshot.forEach { d -> draftsArr.put(encodePostJson(d)) }
                root.put("drafts", draftsArr)

                root.put("likedSeedIds", JSONArray(likedSnapshot))
                root.put("bookmarkedSeedIds", JSONArray(bookmarkedSnapshot))
                root.put("readNotificationIds", JSONArray(readNotifSnapshot))

                val seedCommentsObj = JSONObject()
                postsSnapshot.filter { seedPostIds.contains(it.id) && !it.isMine }.forEach { post ->
                    val initialCount = MockSeedData.initialPosts.firstOrNull { it.id == post.id }?.comments?.size ?: 0
                    if (post.comments.size > initialCount) {
                        val extraArr = JSONArray()
                        post.comments.drop(initialCount).forEach { c -> extraArr.put(encodeCommentJson(c)) }
                        seedCommentsObj.put(post.id, extraArr)
                    }
                }
                root.put("seedComments", seedCommentsObj)

                val tmpFile = File(context.filesDir, "yangmei_user_content_v1.tmp")
                tmpFile.writeText(root.toString(), Charsets.UTF_8)
                tmpFile.renameTo(userContentFile)
            } catch (e: Exception) {
                Log.w("YangmeiRepository", "Failed to persist local user content", e)
            }
        }
    }

    private fun encodePersonaJson(p: AnonymousPersona): JSONObject = JSONObject().apply {
        put("id", p.id)
        put("nickname", p.nickname)
        put("avatarSeed", p.avatarSeed)
        put("identityType", p.identityType.name)
        put("isVerified", p.isVerified)
    }

    private fun decodePersonaJson(obj: JSONObject?): AnonymousPersona {
        if (obj == null) return _currentPersona.value
        val idType = IdentityType.entries.firstOrNull { it.name == obj.optString("identityType") } ?: IdentityType.STUDENT
        return AnonymousPersona(
            id = obj.optString("id", currentUid),
            nickname = obj.optString("nickname", "花家地灰鸽"),
            avatarSeed = obj.optInt("avatarSeed", 512),
            identityType = idType,
            isVerified = obj.optBoolean("isVerified", true)
        )
    }

    private fun encodeCommentJson(c: Comment): JSONObject = JSONObject().apply {
        put("id", c.id)
        put("postId", c.postId)
        put("author", encodePersonaJson(c.author))
        put("content", c.content)
        put("createdAt", c.createdAt)
        put("likes", c.likes)
        put("isLiked", c.isLiked)
        if (c.replyToNickname != null) put("replyToNickname", c.replyToNickname)
        if (c.comfortTag != null) put("comfortTag", c.comfortTag)
        if (c.cohortLabel != null) put("cohortLabel", c.cohortLabel)
    }

    private fun decodeCommentJson(obj: JSONObject): Comment? {
        val id = obj.optString("id")
        val content = obj.optString("content")
        if (id.isBlank() || content.isBlank()) return null
        return Comment(
            id = id,
            postId = obj.optString("postId"),
            author = decodePersonaJson(obj.optJSONObject("author")),
            content = content,
            createdAt = obj.optString("createdAt", "刚刚"),
            likes = obj.optInt("likes", 0),
            isLiked = obj.optBoolean("isLiked", false),
            replyToNickname = obj.optString("replyToNickname").takeIf { it.isNotBlank() },
            comfortTag = obj.optString("comfortTag").takeIf { it.isNotBlank() },
            cohortLabel = obj.optString("cohortLabel").takeIf { it.isNotBlank() }
        )
    }

    private fun encodePostJson(post: Post): JSONObject = JSONObject().apply {
        put("id", post.id)
        put("indexCode", post.indexCode)
        put("title", post.title)
        put("body", post.body)
        put("author", encodePersonaJson(post.author))
        put("category", post.category.name)
        put("layoutType", post.layoutType.name)
        put("topicTags", JSONArray(post.topicTags))
        val mediaArr = JSONArray()
        post.mediaList.forEach { m ->
            mediaArr.put(JSONObject().apply {
                put("id", m.id)
                if (m.uriString != null) put("uriString", m.uriString)
                put("aspectRatio", m.aspectRatio.toDouble())
                put("caption", m.caption)
            })
        }
        put("mediaList", mediaArr)
        put("createdAt", post.createdAt)
        put("viewCount", post.viewCount)
        put("likeCount", post.likeCount)
        put("bookmarkCount", post.bookmarkCount)
        put("isLiked", post.isLiked)
        put("isBookmarked", post.isBookmarked)
        put("allowComments", post.allowComments)
        val commentsArr = JSONArray()
        post.comments.forEach { commentsArr.put(encodeCommentJson(it)) }
        put("comments", commentsArr)
        if (post.price != null) put("price", post.price)
        if (post.tradeStatus != null) put("tradeStatus", post.tradeStatus)
        if (post.location != null) put("location", post.location)
        if (post.eventTime != null) put("eventTime", post.eventTime)
        if (post.contactHint != null) put("contactHint", post.contactHint)
        if (post.moodTag != null) put("moodTag", post.moodTag)
        post.poll?.let { poll ->
            put("poll", JSONObject().apply {
                put("question", poll.question)
                if (poll.votedOptionId != null) put("votedOptionId", poll.votedOptionId)
                val opts = JSONArray()
                poll.options.forEach { o ->
                    opts.put(JSONObject().apply {
                        put("id", o.id)
                        put("text", o.text)
                        put("votes", o.votes)
                    })
                }
                put("options", opts)
            })
        }
        put("isDraft", post.isDraft)
        put("isMine", post.isMine)
    }

    private fun decodePostJson(obj: JSONObject): Post? {
        val id = obj.optString("id")
        if (id.isBlank()) return null
        val category = PostCategory.entries.firstOrNull { it.name == obj.optString("category") } ?: PostCategory.TREE_HOLE
        val layout = PostLayoutType.entries.firstOrNull { it.name == obj.optString("layoutType") } ?: PostLayoutType.EDITORIAL_TEXT
        val tags = mutableListOf<String>()
        obj.optJSONArray("topicTags")?.let { arr ->
            for (i in 0 until arr.length()) {
                val t = arr.optString(i)
                if (t.isNotBlank()) tags.add(t)
            }
        }
        val media = mutableListOf<PostMedia>()
        obj.optJSONArray("mediaList")?.let { arr ->
            for (i in 0 until arr.length()) {
                val m = arr.optJSONObject(i) ?: continue
                val uri = m.optString("uriString").takeIf { it.isNotBlank() }
                if (uri != null) {
                    media.add(
                        PostMedia(
                            id = m.optString("id", "pm_$i"),
                            uriString = uri,
                            aspectRatio = m.optDouble("aspectRatio", 1.333).toFloat(),
                            caption = m.optString("caption", "")
                        )
                    )
                }
            }
        }
        val comments = mutableListOf<Comment>()
        obj.optJSONArray("comments")?.let { arr ->
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { cObj -> decodeCommentJson(cObj)?.let(comments::add) }
            }
        }
        val poll = obj.optJSONObject("poll")?.let { pObj ->
            val opts = mutableListOf<PollOption>()
            pObj.optJSONArray("options")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    opts.add(PollOption(o.optString("id"), o.optString("text"), o.optInt("votes", 0)))
                }
            }
            if (opts.size >= 2) {
                PollData(
                    question = pObj.optString("question"),
                    options = opts,
                    votedOptionId = pObj.optString("votedOptionId").takeIf { it.isNotBlank() }
                )
            } else null
        }
        return Post(
            id = id,
            indexCode = obj.optString("indexCode", "NO.0413"),
            title = obj.optString("title", ""),
            body = obj.optString("body", ""),
            author = decodePersonaJson(obj.optJSONObject("author")),
            category = category,
            layoutType = layout,
            topicTags = tags,
            mediaList = media,
            createdAt = obj.optString("createdAt", "刚刚"),
            viewCount = obj.optInt("viewCount", 1),
            likeCount = obj.optInt("likeCount", 0),
            bookmarkCount = obj.optInt("bookmarkCount", 0),
            isLiked = obj.optBoolean("isLiked", false),
            isBookmarked = obj.optBoolean("isBookmarked", false),
            allowComments = obj.optBoolean("allowComments", true),
            comments = comments,
            price = obj.optString("price").takeIf { it.isNotBlank() },
            tradeStatus = obj.optString("tradeStatus").takeIf { it.isNotBlank() },
            location = obj.optString("location").takeIf { it.isNotBlank() },
            eventTime = obj.optString("eventTime").takeIf { it.isNotBlank() },
            contactHint = obj.optString("contactHint").takeIf { it.isNotBlank() },
            moodTag = obj.optString("moodTag").takeIf { it.isNotBlank() },
            poll = poll,
            isDraft = obj.optBoolean("isDraft", false),
            isMine = obj.optBoolean("isMine", true)
        )
    }

    suspend fun refreshDailyHotTopics(dayKey: String) {
        recalculateHotTopicsForDay(dayKey)
        context.yangmeiDataStore.edit { prefs ->
            prefs[Keys.LAST_HOT_TOPICS_DATE] = dayKey
        }
    }

    fun recalculateHotTopicsForDay(dayKey: String) {
        val currentPosts = _posts.value
        val existingByTag = LinkedHashMap<String, HotTopic>()
        MockSeedData.initialHotTopics.forEach { t ->
            existingByTag[t.tag.lowercase()] = t
        }
        _hotTopics.value.forEach { t ->
            val key = t.tag.lowercase()
            if (!existingByTag.containsKey(key)) {
                existingByTag[key] = t
            }
        }
        currentPosts.flatMap { it.topicTags }
            .map { if (it.startsWith("#")) it else "#$it" }
            .distinctBy { it.lowercase() }
            .forEachIndexed { idx, tag ->
                val key = tag.lowercase()
                if (!existingByTag.containsKey(key)) {
                    existingByTag[key] = HotTopic(
                        id = "topic_live_${tag.hashCode()}_$idx",
                        indexCode = "TAG",
                        tag = tag,
                        subtitleEn = "CAMPUS TAG",
                        description = "央美同学正在热议的校园话题",
                        postCount = 0,
                        discussionCount = 0,
                        heatStatus = "新"
                    )
                }
            }

        val scored = existingByTag.values.map { topic ->
            val rawTag = topic.tag.removePrefix("#")
            val matching = currentPosts.filter { post ->
                post.topicTags.any { it.equals(topic.tag, ignoreCase = true) } ||
                    post.body.contains(rawTag, ignoreCase = true) ||
                    post.title.contains(rawTag, ignoreCase = true)
            }
            val liveEngagement = matching.sumOf { post ->
                68 + post.likeCount * 4 + post.comments.size * 12 + post.bookmarkCount * 5 + (post.viewCount / 12) +
                    if (post.isMine) 140 else 0
            }
            val dailySeed = "${topic.id}|$dayKey".hashCode()
            val normShift = ((dailySeed and 0x7FFFFFFF) % 420) - 180
            val baseHeat = MockSeedData.initialHotTopics.firstOrNull { it.id == topic.id }?.discussionCount ?: 120
            val dailyHeat = (baseHeat + liveEngagement + normShift).coerceAtLeast(36)
            val isCustomNew = MockSeedData.initialHotTopics.none { it.id == topic.id }

            Triple(
                topic.copy(
                    postCount = (topic.postCount.coerceAtLeast(matching.size) + matching.count { it.isMine }),
                    discussionCount = dailyHeat
                ),
                isCustomNew,
                normShift
            )
        }.sortedByDescending { it.first.discussionCount }

        val topRiserId = scored.maxByOrNull { it.third }?.first?.id
        val ranked = scored.mapIndexed { idx, (topic, isCustomNew, _) ->
            val status = when {
                idx == 0 -> "沸"
                idx == 1 || idx == 2 -> "热"
                isCustomNew || topic.id == topRiserId -> "新"
                idx == 4 -> "荐"
                else -> ""
            }
            topic.copy(
                indexCode = "NO.%02d".format(idx + 1),
                heatStatus = status
            )
        }
        _hotTopics.value = ranked
    }

    override suspend fun submitVerification(record: VerificationRecord) {
        _verificationRecord.value = record.copy(submittedAt = "刚刚提交")
        _verificationStatus.value = VerificationStatus.REVIEWING
        context.yangmeiDataStore.edit { prefs ->
            prefs[Keys.VERIFICATION_STATUS] = VerificationStatus.REVIEWING.name
            prefs[Keys.IDENTITY_TYPE] = record.identityType.name
            prefs[Keys.COLLEGE] = record.college
            prefs[Keys.MAJOR] = record.major
            prefs[Keys.YEAR] = record.year
        }
        pushNotification(
            NotificationItem(
                id = "noti_verify_submit_${System.currentTimeMillis()}",
                type = NotificationType.VERIFICATION,
                sender = AnonymousPersona("sys", "杨梅树窝审核组", 901, IdentityType.ALUMNI, true, "CAFA"),
                summary = "已收到你的${record.identityType.labelCn}认证资料",
                quoteSnippet = "身份材料已加密隔离存储，审核完成后将为你激活独立匿名身份。",
                createdAt = "刚刚",
                isUnread = true
            )
        )
    }

    override suspend fun resolveVerification(approved: Boolean, rejectReason: String?) {
        val nextStatus = if (approved) VerificationStatus.VERIFIED else VerificationStatus.REJECTED
        _verificationStatus.value = nextStatus
        val rec = _verificationRecord.value
        _verificationRecord.value = rec.copy(rejectReason = rejectReason)
        if (approved) {
            _currentPersona.update {
                it.copy(
                    identityType = rec.identityType,
                    isVerified = true,
                    optionalCollegeTag = null,
                    optionalYearTag = null
                )
            }
        }
        context.yangmeiDataStore.edit { prefs ->
            prefs[Keys.VERIFICATION_STATUS] = nextStatus.name
        }
        pushNotification(
            NotificationItem(
                id = "noti_verify_res_${System.currentTimeMillis()}",
                type = NotificationType.VERIFICATION,
                sender = AnonymousPersona("sys", "杨梅树窝审核组", 901, IdentityType.ALUMNI, true, "CAFA"),
                summary = if (approved) "校园身份认证已通过，欢迎进入树窝" else "校园身份认证未通过，请补充材料",
                quoteSnippet = if (approved) {
                    "当前匿名身份：${_currentPersona.value.nickname} · 已开启完整发帖、评论、收藏与投票权限。"
                } else {
                    rejectReason ?: "请确保证明材料清晰可见校园标识。"
                },
                createdAt = "刚刚",
                isUnread = true
            )
        )
    }

    override suspend fun resetVerificationForTesting() {
        _verificationStatus.value = VerificationStatus.UNVERIFIED
        _currentPersona.update { it.copy(isVerified = false) }
        context.yangmeiDataStore.edit { prefs ->
            prefs[Keys.VERIFICATION_STATUS] = VerificationStatus.UNVERIFIED.name
        }
    }

    override suspend fun updateAnonymousPersona(
        nickname: String,
        avatarSeed: Int,
        collegeTag: String?,
        yearTag: String?
    ) {
        _currentPersona.update {
            it.copy(
                nickname = nickname,
                avatarSeed = avatarSeed,
                optionalCollegeTag = null,
                optionalYearTag = null
            )
        }
        context.yangmeiDataStore.edit { prefs ->
            prefs[Keys.ANON_NICKNAME] = nickname
            prefs[Keys.ANON_SEED] = avatarSeed
            if (collegeTag != null) prefs[Keys.COLLEGE] = collegeTag
            if (yearTag != null) prefs[Keys.YEAR] = yearTag
        }
    }

    suspend fun updateSettings(transform: (UserSettings) -> UserSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        context.yangmeiDataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = updated.darkTheme
            prefs[Keys.SHOW_COLLEGE_TAG] = updated.showCollegeTag
            prefs[Keys.NOTIFY_COMMENTS] = updated.notifyComments
            prefs[Keys.NOTIFY_LIKES] = updated.notifyLikes
            prefs[Keys.NOTIFY_SYSTEM] = updated.notifySystem
            prefs[Keys.ALLOW_ANON_NOTES] = updated.allowAnonymousNotes
            prefs[Keys.BLOCKED_USERS] = updated.blockedUsers.joinToString("|")
            prefs[Keys.BLOCKED_KEYWORDS] = updated.blockedKeywords.joinToString("|")
        }
    }

    override fun insertPost(post: Post) {
        val authoredPost = post.copy(
            author = post.author.copy(id = currentUid),
            isMine = true
        )
        localSavedUserPosts = listOf(authoredPost) + localSavedUserPosts.filterNot { it.id == authoredPost.id }
        _posts.update { listOf(authoredPost) + it.filterNot { p -> p.id == authoredPost.id } }

        if (authoredPost.topicTags.isNotEmpty()) {
            _hotTopics.update { topics ->
                val updatedExisting = topics.map { t ->
                    if (authoredPost.topicTags.any { it.equals(t.tag, ignoreCase = true) }) {
                        t.copy(postCount = t.postCount + 1, discussionCount = t.discussionCount + 1)
                    } else t
                }
                val newCustomTopics = authoredPost.topicTags
                    .distinctBy { it.lowercase() }
                    .filter { tag -> updatedExisting.none { it.tag.equals(tag, ignoreCase = true) } }
                    .mapIndexed { idx, tag ->
                        HotTopic(
                            id = "topic_custom_${System.currentTimeMillis()}_$idx",
                            indexCode = "TAG",
                            tag = if (tag.startsWith("#")) tag else "#$tag",
                            subtitleEn = "CUSTOM TAG",
                            description = "同学自定义校园标签 · 搜索关键词即可找到相关内容",
                            postCount = 1,
                            discussionCount = 1,
                            heatStatus = "新"
                        )
                    }
                updatedExisting + newCustomTopics
            }
        }
        persistUserContentAsync()

        // Push immediately to shared Firestore cloud collection with SHA-256 deleteVerifier
        val uidSnapshot = currentUid
        val verifier = sessionManager.computeDeleteVerifier(authoredPost.id)
        ioScope.launch {
            try {
                val payload = FirestoreMappers.toPostCreateMap(authoredPost, uidSnapshot, verifier)
                db.collection("posts")
                    .document(authoredPost.id)
                    .set(payload)
                    .await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "posts/${authoredPost.id}")
            }
        }
    }

    override fun deletePost(postId: String) {
        val existingPost = _posts.value.firstOrNull { it.id == postId }
        val fallbackAuthorUid = cloudPostDtos[postId]?.authorUid ?: existingPost?.author?.id ?: currentUid
        deletedPostIds.add(postId)
        cloudPostDtos.remove(postId)
        localSavedUserPosts = localSavedUserPosts.filterNot { it.id == postId }
        _posts.update { list -> list.filterNot { it.id == postId } }
        persistUserContentAsync()

        if (!seedPostIds.contains(postId)) {
            val proof = sessionManager.computeDeleteProof(postId)
            ioScope.launch {
                try {
                    db.collection("posts").document(postId).update(
                        mapOf(
                            "visibility" to "deleted",
                            "deleteProof" to proof,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Fallback for legacy posts created before deleteVerifier existed
                    try {
                        db.collection("posts").document(postId).update(
                            mapOf(
                                "visibility" to "deleted",
                                "deleteProof" to fallbackAuthorUid,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    } catch (e2: CancellationException) {
                        throw e2
                    } catch (e2: Exception) {
                        handleFirestoreError(e2, OperationType.DELETE, "posts/$postId")
                    }
                }
            }
        }
    }

    override fun saveDraft(draft: Post) {
        _drafts.update { current ->
            val filtered = current.filterNot { it.id == draft.id }
            listOf(draft.copy(isDraft = true, isMine = true)) + filtered
        }
        persistUserContentAsync()
    }

    override fun deleteDraft(draftId: String) {
        _drafts.update { list -> list.filterNot { it.id == draftId } }
        persistUserContentAsync()
    }

    override fun toggleLike(postId: String) {
        var nowLiked = false
        var newLikeCount = 0
        var targetPost: Post? = null
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    targetPost = post
                    nowLiked = !post.isLiked
                    newLikeCount = (post.likeCount + if (nowLiked) 1 else -1).coerceAtLeast(0)
                    post.copy(isLiked = nowLiked, likeCount = newLikeCount)
                } else post
            }
        }
        if (nowLiked) likedPostIds.add(postId) else likedPostIds.remove(postId)
        persistUserContentAsync()

        if (cloudPostDtos.containsKey(postId)) {
            if (nowLiked) {
                val recipientUid = cloudPostDtos[postId]?.authorUid ?: targetPost?.author?.id.orEmpty()
                sendCloudNotification(
                    recipientUid = recipientUid,
                    type = "LIKE",
                    group = "REACTIONS",
                    postId = postId,
                    summary = "赞了你的帖子「${postSnippetText(targetPost)}」"
                )
            }
            ioScope.launch {
                try {
                    db.collection("posts").document(postId).update(
                        mapOf(
                            "likeCount" to newLikeCount,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.UPDATE, "posts/$postId")
                }
            }
        }
    }

    fun incrementPostView(postId: String) {
        var nextViewCount = 0
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    nextViewCount = post.viewCount + 1
                    post.copy(viewCount = nextViewCount)
                } else post
            }
        }
        if (cloudPostDtos.containsKey(postId) && nextViewCount > 0) {
            ioScope.launch {
                try {
                    db.collection("posts").document(postId).update(
                        mapOf(
                            "viewCount" to nextViewCount,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Non-critical telemetry counter
                }
            }
        }
    }

    override fun toggleBookmark(postId: String) {
        var nowSaved = false
        var newBookmarkCount = 0
        var targetPost: Post? = null
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    targetPost = post
                    nowSaved = !post.isBookmarked
                    newBookmarkCount = (post.bookmarkCount + if (nowSaved) 1 else -1).coerceAtLeast(0)
                    post.copy(isBookmarked = nowSaved, bookmarkCount = newBookmarkCount)
                } else post
            }
        }
        if (nowSaved) bookmarkedPostIds.add(postId) else bookmarkedPostIds.remove(postId)
        persistUserContentAsync()

        if (cloudPostDtos.containsKey(postId)) {
            if (nowSaved) {
                val recipientUid = cloudPostDtos[postId]?.authorUid ?: targetPost?.author?.id.orEmpty()
                sendCloudNotification(
                    recipientUid = recipientUid,
                    type = "BOOKMARK",
                    group = "REACTIONS",
                    postId = postId,
                    summary = "收藏了你的帖子「${postSnippetText(targetPost)}」"
                )
            }
            ioScope.launch {
                try {
                    db.collection("posts").document(postId).update(
                        mapOf(
                            "bookmarkCount" to newBookmarkCount,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.UPDATE, "posts/$postId")
                }
            }
        }
    }

    override fun addComment(postId: String, comment: Comment) {
        val authoredComment = comment.copy(author = comment.author.copy(id = currentUid))
        var nextCommentCount = 0
        var postAuthorUid = currentUid
        var targetPost: Post? = null
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    targetPost = post
                    postAuthorUid = cloudPostDtos[postId]?.authorUid ?: post.author.id
                    val updatedComments = post.comments + authoredComment
                    nextCommentCount = updatedComments.size
                    post.copy(comments = updatedComments)
                } else post
            }
        }
        if (seedPostIds.contains(postId)) {
            seedExtraComments[postId] = seedExtraComments[postId].orEmpty() + authoredComment
        }
        persistUserContentAsync()

        if (cloudPostDtos.containsKey(postId)) {
            sendCloudNotification(
                recipientUid = postAuthorUid,
                type = "COMMENT",
                group = "COMMENTS",
                postId = postId,
                summary = "评论了你的帖子「${postSnippetText(targetPost)}」：${authoredComment.content.take(60)}"
            )
            ioScope.launch {
                try {
                    val payload = FirestoreMappers.toCommentCreateMap(
                        postId = postId,
                        postAuthorUid = postAuthorUid,
                        comment = authoredComment,
                        authorUid = currentUid
                    )
                    db.collection("posts")
                        .document(postId)
                        .collection("comments")
                        .document(authoredComment.id)
                        .set(payload)
                        .await()
                    db.collection("posts")
                        .document(postId)
                        .update(
                            mapOf(
                                "commentCount" to nextCommentCount,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        .await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.CREATE, "posts/$postId/comments/${authoredComment.id}")
                }
            }
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        var nextLikes = 0
        var nowLiked = false
        var targetComment: Comment? = null
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    post.copy(
                        comments = post.comments.map { c ->
                            if (c.id == commentId) {
                                targetComment = c
                                val liked = !c.isLiked
                                nowLiked = liked
                                nextLikes = (c.likes + if (liked) 1 else -1).coerceAtLeast(0)
                                c.copy(isLiked = liked, likes = nextLikes)
                            } else c
                        }
                    )
                } else post
            }
        }
        persistUserContentAsync()

        if (cloudPostDtos.containsKey(postId)) {
            if (nowLiked) {
                val commentObj = targetComment
                if (commentObj != null) {
                    sendCloudNotification(
                        recipientUid = commentObj.author.id,
                        type = "COMMENT_LIKE",
                        group = "REACTIONS",
                        postId = postId,
                        summary = "赞了你的评论「${commentObj.content.take(24)}」"
                    )
                }
            }
            ioScope.launch {
                try {
                    db.collection("posts")
                        .document(postId)
                        .collection("comments")
                        .document(commentId)
                        .update("likes", nextLikes)
                        .await()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun votePoll(postId: String, optionId: String) {
        var updatedOptions: List<Map<String, Any>>? = null
        _posts.update { list ->
            list.map { post ->
                val poll = post.poll
                if (post.id == postId && poll != null && poll.votedOptionId == null) {
                    val newOpts = poll.options.map { opt ->
                        if (opt.id == optionId) opt.copy(votes = opt.votes + 1) else opt
                    }
                    updatedOptions = newOpts.map { opt ->
                        mapOf("id" to opt.id, "text" to opt.text, "votes" to opt.votes)
                    }
                    post.copy(poll = poll.copy(votedOptionId = optionId, options = newOpts))
                } else post
            }
        }
        persistUserContentAsync()

        val cloudOpts = updatedOptions
        if (cloudPostDtos.containsKey(postId) && cloudOpts != null) {
            ioScope.launch {
                try {
                    db.collection("posts").document(postId).update(
                        mapOf(
                            "pollOptions" to cloudOpts,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.UPDATE, "posts/$postId")
                }
            }
        }
    }

    override fun addCourse(course: CourseRecord) {
        _courses.update { listOf(course) + it }
    }

    override fun addCourseReview(courseId: String, review: CourseReview) {
        _courses.update { list ->
            list.map { course ->
                if (course.id == courseId) {
                    val updatedReviews = listOf(review) + course.reviews
                    val avgWork = updatedReviews.map { it.workload }.average().toFloat()
                    val avgDiff = updatedReviews.map { it.difficulty }.average().toFloat()
                    val avgRec = updatedReviews.map { it.recommend }.average().toFloat()
                    course.copy(
                        reviews = updatedReviews,
                        workloadScore = avgWork,
                        difficultyScore = avgDiff,
                        recommendScore = avgRec
                    )
                } else course
            }
        }
    }

    override fun toggleCourseBookmark(courseId: String) {
        _courses.update { list ->
            list.map { c -> if (c.id == courseId) c.copy(isBookmarked = !c.isBookmarked) else c }
        }
    }

    override fun toggleExperienceBookmark(articleId: String) {
        _experiences.update { list ->
            list.map { a ->
                if (a.id == articleId) {
                    val saved = !a.isBookmarked
                    a.copy(isBookmarked = saved, bookmarkCount = (a.bookmarkCount + if (saved) 1 else -1).coerceAtLeast(0))
                } else a
            }
        }
    }

    override fun toggleExperienceLike(articleId: String) {
        _experiences.update { list ->
            list.map { a ->
                if (a.id == articleId) {
                    val liked = !a.isLiked
                    a.copy(isLiked = liked, likeCount = (a.likeCount + if (liked) 1 else -1).coerceAtLeast(0))
                } else a
            }
        }
    }

    fun addExperienceArticle(article: ExperienceArticle) {
        _experiences.update { listOf(article) + it }
    }

    override fun addExperienceComment(articleId: String, comment: Comment) {
        _experiences.update { list ->
            list.map { a -> if (a.id == articleId) a.copy(comments = a.comments + comment) else a }
        }
    }

    override fun markNotificationRead(notificationId: String) {
        readNotificationIds.add(notificationId)
        _notifications.update { list ->
            list.map { n -> if (n.id == notificationId) n.copy(isUnread = false) else n }
        }
        persistUserContentAsync()
    }

    override fun markAllNotificationsRead() {
        _notifications.value.forEach { readNotificationIds.add(it.id) }
        _notifications.update { list -> list.map { it.copy(isUnread = false) } }
        persistUserContentAsync()
    }

    override fun pushNotification(item: NotificationItem) {
        localPushedNotifications.add(0, item)
        rebuildMergedNotifications()
    }
}
