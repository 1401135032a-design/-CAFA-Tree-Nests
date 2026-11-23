package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private val Context.yangmeiDataStore by preferencesDataStore(name = "yangmei_tree_prefs")

class LocalContentModerationService : ContentModerationGateway {
    override fun inspectText(content: String, blockedKeywords: List<String>): ModerationResult {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return ModerationResult(false, "内容不能为空")
        }
        val matched = blockedKeywords.firstOrNull { kw -> kw.isNotBlank() && trimmed.contains(kw, ignoreCase = true) }
        if (matched != null) {
            return ModerationResult(false, "内容包含你设置的屏蔽或受限词：「$matched」")
        }
        return ModerationResult(true)
    }
}

class LocalCloudMediaStorageService : CloudMediaStorageGateway {
    override suspend fun uploadLocalMedia(uriString: String): String {
        // Local prototype retains picker URI directly; future cloud storage uploads and returns CDN URL.
        return uriString
    }
}

class YangmeiRepository(
    private val context: Context,
    val moderationGateway: ContentModerationGateway = LocalContentModerationService(),
    val storageGateway: CloudMediaStorageGateway = LocalCloudMediaStorageService()
) : CommunityDatabaseGateway, CampusAuthVerificationGateway {

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

    private val _posts = MutableStateFlow(MockSeedData.initialPosts)
    override val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _drafts = MutableStateFlow<List<Post>>(
        listOf(
            Post(
                id = "draft_01",
                indexCode = "DRAFT",
                title = "关于春季工作室选修的几点笔记（草稿）",
                body = "整理了一下这学期去旁听雕塑和版画两门课的感受，等下周出成绩再补充完发出来...",
                author = AnonymousPersona("me_local", "花家地灰鸽", 512, IdentityType.STUDENT, false),
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
            id = "me_local",
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
            id = "me_local",
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
        // Discover any tags present in user posts that are not yet in the map
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
            // Deterministic daily shift keyed by local calendar date (YYYY-MM-DD)
            val dailySeed = "${topic.id}|$dayKey".hashCode()
            val normShift = ((dailySeed and 0x7FFFFFFF) % 420) - 180 // -180..+239 daily wave
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
        _posts.update { listOf(post) + it }
        // Increment matching hot topic counts and register any new user-authored custom tags
        if (post.topicTags.isNotEmpty()) {
            _hotTopics.update { topics ->
                val updatedExisting = topics.map { t ->
                    if (post.topicTags.any { it.equals(t.tag, ignoreCase = true) }) {
                        t.copy(postCount = t.postCount + 1, discussionCount = t.discussionCount + 1)
                    } else t
                }
                val newCustomTopics = post.topicTags
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
    }

    override fun saveDraft(draft: Post) {
        _drafts.update { current ->
            val filtered = current.filterNot { it.id == draft.id }
            listOf(draft.copy(isDraft = true, isMine = true)) + filtered
        }
    }

    override fun deleteDraft(draftId: String) {
        _drafts.update { list -> list.filterNot { it.id == draftId } }
    }

    override fun toggleLike(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val liked = !post.isLiked
                    post.copy(
                        isLiked = liked,
                        likeCount = (post.likeCount + if (liked) 1 else -1).coerceAtLeast(0)
                    )
                } else post
            }
        }
    }

    fun incrementPostView(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) post.copy(viewCount = post.viewCount + 1) else post
            }
        }
    }

    override fun toggleBookmark(postId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    val saved = !post.isBookmarked
                    post.copy(
                        isBookmarked = saved,
                        bookmarkCount = (post.bookmarkCount + if (saved) 1 else -1).coerceAtLeast(0)
                    )
                } else post
            }
        }
    }

    override fun addComment(postId: String, comment: Comment) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + comment)
                } else post
            }
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        _posts.update { list ->
            list.map { post ->
                if (post.id == postId) {
                    post.copy(
                        comments = post.comments.map { c ->
                            if (c.id == commentId) {
                                val liked = !c.isLiked
                                c.copy(isLiked = liked, likes = (c.likes + if (liked) 1 else -1).coerceAtLeast(0))
                            } else c
                        }
                    )
                } else post
            }
        }
    }

    override fun votePoll(postId: String, optionId: String) {
        _posts.update { list ->
            list.map { post ->
                val poll = post.poll
                if (post.id == postId && poll != null && poll.votedOptionId == null) {
                    post.copy(
                        poll = poll.copy(
                            votedOptionId = optionId,
                            options = poll.options.map { opt ->
                                if (opt.id == optionId) opt.copy(votes = opt.votes + 1) else opt
                            }
                        )
                    )
                } else post
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
        _notifications.update { list ->
            list.map { n -> if (n.id == notificationId) n.copy(isUnread = false) else n }
        }
    }

    override fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isUnread = false) } }
    }

    override fun pushNotification(item: NotificationItem) {
        _notifications.update { listOf(item) + it }
    }
}
