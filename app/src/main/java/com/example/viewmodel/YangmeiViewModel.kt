package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiStatsUiState
import com.example.ai.CampusAiStatsService
import com.example.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class YangmeiUiState(
    val posts: List<Post> = emptyList(),
    val drafts: List<Post> = emptyList(),
    val hotTopics: List<HotTopic> = emptyList(),
    val courses: List<CourseRecord> = emptyList(),
    val experiences: List<ExperienceArticle> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val verificationRecord: VerificationRecord = VerificationRecord(),
    val currentPersona: AnonymousPersona = AnonymousPersona("me_local", "花家地灰鸽", 512, isVerified = false),
    val settings: UserSettings = UserSettings(),
    val todaySnapshot: TodayCampusSnapshot = MockSeedData.todaySnapshot,
    val verificationGateReason: String? = null,
    val toastMessage: String? = null,
    val editingDraft: Post? = null,
    val wechatDms: Map<String, List<WechatDmMessage>> = emptyMap(),
    val aiStatsState: AiStatsUiState = AiStatsUiState.Idle,
    val isRefreshing: Boolean = false
) {
    val isVerified: Boolean get() = verificationStatus == VerificationStatus.VERIFIED
    val unreadNotificationCount: Int get() = notifications.count { it.isUnread }
    val unreadCommentCount: Int
        get() = notifications.count {
            it.isUnread && (it.type == NotificationType.COMMENT || it.type == NotificationType.REPLY)
        }
    val unreadLikeCount: Int
        get() = notifications.count {
            it.isUnread && (it.type == NotificationType.LIKE || it.type == NotificationType.BOOKMARK)
        }
    val unreadNoteCount: Int
        get() = notifications.count { it.isUnread && it.type == NotificationType.ANONYMOUS_NOTE }
    val unreadSystemCount: Int
        get() = notifications.count {
            it.isUnread && (it.type == NotificationType.SYSTEM || it.type == NotificationType.VERIFICATION)
        }

    val visiblePosts: List<Post>
        get() = posts.filter { post ->
            val blockedAuthor = settings.blockedUsers.contains(post.author.nickname)
            val blockedWord = settings.blockedKeywords.any { kw ->
                kw.isNotBlank() && (post.title.contains(kw, true) || post.body.contains(kw, true))
            }
            !blockedAuthor && !blockedWord
        }
}

class YangmeiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = YangmeiRepository(application.applicationContext)
    private val aiStatsService = CampusAiStatsService()

    private val _verificationGateReason = MutableStateFlow<String?>(null)
    private val _toastMessage = MutableStateFlow<String?>(null)
    private val _editingDraft = MutableStateFlow<Post?>(null)
    private val _wechatDms = MutableStateFlow<Map<String, List<WechatDmMessage>>>(emptyMap())
    private val _aiStatsState = MutableStateFlow<AiStatsUiState>(AiStatsUiState.Idle)
    private val _isRefreshing = MutableStateFlow(false)

    private val contentState = combine(
        repository.posts,
        repository.drafts,
        repository.hotTopics,
        repository.courses,
        repository.experiences
    ) { posts, drafts, topics, courses, experiences ->
        ContentBundle(posts, drafts, topics, courses, experiences)
    }

    private val identityState = combine(
        repository.notifications,
        repository.verificationStatus,
        repository.verificationRecord,
        repository.currentPersona,
        repository.settings
    ) { notifications, status, record, persona, settings ->
        IdentityBundle(notifications, status, record, persona, settings)
    }

    private val transientState = combine(
        _verificationGateReason,
        _toastMessage,
        _editingDraft,
        _wechatDms,
        _aiStatsState
    ) { gateReason, toast, editingDraft, dms, aiStats ->
        TransientBundle(gateReason, toast, editingDraft, dms, aiStats)
    }

    val uiState: StateFlow<YangmeiUiState> = combine(
        contentState,
        identityState,
        transientState,
        _isRefreshing
    ) { content, identity, transient, refreshing ->
        YangmeiUiState(
            posts = content.posts,
            drafts = content.drafts,
            hotTopics = content.topics,
            courses = content.courses,
            experiences = content.experiences,
            notifications = identity.notifications,
            verificationStatus = identity.status,
            verificationRecord = identity.record,
            currentPersona = identity.persona,
            settings = identity.settings,
            todaySnapshot = repository.todaySnapshot,
            verificationGateReason = transient.gateReason,
            toastMessage = transient.toast,
            editingDraft = transient.editingDraft,
            wechatDms = transient.wechatDms,
            aiStatsState = transient.aiStatsState,
            isRefreshing = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = YangmeiUiState(
            posts = MockSeedData.initialPosts,
            hotTopics = MockSeedData.initialHotTopics,
            courses = MockSeedData.initialCourses,
            experiences = MockSeedData.initialExperiences,
            notifications = MockSeedData.initialNotifications
        )
    )

    init {
        viewModelScope.launch {
            repository.hydrateFromPreferences()
        }
    }

    fun refreshHomeFeed() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(450)
            _isRefreshing.value = false
            showToast("已刷新最新校园动态")
        }
    }

    fun consumeToast() {
        _toastMessage.value = null
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun dismissVerificationGate() {
        _verificationGateReason.value = null
    }

    // --- Gemini AI Campus Statistics ---

    fun requestAiStatistics(query: String? = null) {
        val cleanQuery = query?.trim().orEmpty()
        val label = if (cleanQuery.isBlank()) "全站校园标签与热词" else "关键词「$cleanQuery」"
        viewModelScope.launch {
            _aiStatsState.value = AiStatsUiState.Loading(label)
            try {
                val snapshot = uiState.value
                val report = aiStatsService.analyzeCommunityStats(
                    query = cleanQuery.ifBlank { null },
                    posts = snapshot.visiblePosts,
                    courses = snapshot.courses,
                    experiences = snapshot.experiences
                )
                _aiStatsState.value = AiStatsUiState.Success(report)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("YangmeiViewModel", "AI statistics request failed", e)
                _aiStatsState.value = AiStatsUiState.Error(
                    queryLabel = label,
                    message = e.localizedMessage ?: "AI 统计服务请求失败，请检查网络或稍后重试。"
                )
            }
        }
    }

    fun clearAiStatistics() {
        _aiStatsState.value = AiStatsUiState.Idle
    }

    fun requireVerified(actionLabel: String, onVerified: () -> Unit) {
        if (uiState.value.isVerified) {
            onVerified()
        } else {
            _verificationGateReason.value = actionLabel
        }
    }

    // --- Post Interactions ---

    fun recordPostView(postId: String) {
        repository.incrementPostView(postId)
    }

    fun togglePostLike(postId: String) {
        repository.toggleLike(postId)
    }

    fun togglePostBookmark(postId: String) {
        repository.toggleBookmark(postId)
        val isSaved = repository.posts.value.firstOrNull { it.id == postId }?.isBookmarked == true
        showToast(if (isSaved) "已收入你的收藏夹" else "已取消收藏")
    }

    fun voteOnPoll(postId: String, optionId: String) {
        requireVerified("参与校园投票") {
            repository.votePoll(postId, optionId)
            showToast("投票已记录")
        }
    }

    fun addPostComment(
        postId: String,
        content: String,
        replyToNickname: String? = null,
        comfortTag: String? = null
    ) {
        requireVerified("发表评论与回复") {
            val check = repository.moderationGateway.inspectText(content, uiState.value.settings.blockedKeywords)
            if (!check.passed) {
                showToast(check.reason ?: "内容不符合规范")
                return@requireVerified
            }
            val persona = activePostingPersona()
            val comment = Comment(
                id = "c_${System.currentTimeMillis()}",
                postId = postId,
                author = persona,
                content = content.trim(),
                createdAt = "刚刚",
                replyToNickname = replyToNickname,
                comfortTag = comfortTag
            )
            repository.addComment(postId, comment)
            showToast(if (comfortTag != null) "已送出「$comfortTag」" else "评论已发布")
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        requireVerified("点赞评论") {
            repository.toggleCommentLike(postId, commentId)
        }
    }

    fun sharePost(post: Post) {
        showToast("已复制「${post.title.ifBlank { "树洞内容" }}」站内分享口令")
    }

    fun startEditingDraft(draft: Post?) {
        _editingDraft.value = draft
    }

    fun publishNewPost(
        title: String,
        body: String,
        category: PostCategory,
        topicTags: List<String>,
        mediaUris: List<String>,
        allowComments: Boolean,
        price: String?,
        tradeStatus: String?,
        location: String?,
        eventTime: String?,
        moodTag: String?,
        pollQuestion: String?,
        pollOptions: List<String>,
        asDraft: Boolean = false,
        onComplete: (Post) -> Unit = {}
    ) {
        if (!asDraft) {
            if (!uiState.value.isVerified) {
                _verificationGateReason.value = "发布校园内容"
                return
            }
            val check = repository.moderationGateway.inspectText(
                body.ifBlank { title },
                uiState.value.settings.blockedKeywords
            )
            if (!check.passed) {
                showToast(check.reason ?: "请输入有效内容")
                return
            }
        }

        viewModelScope.launch {
            val uploadedMedia = mediaUris.mapIndexed { idx, uri ->
                val remoteOrLocal = repository.storageGateway.uploadLocalMedia(uri)
                PostMedia(
                    id = "pm_${System.currentTimeMillis()}_$idx",
                    uriString = remoteOrLocal,
                    aspectRatio = if (idx % 2 == 0) 4f / 3f else 3f / 4f
                )
            }
            val validPollOptions = pollOptions.map { it.trim() }.filter { it.isNotEmpty() }
            val pollData = if (!pollQuestion.isNullOrBlank() && validPollOptions.size >= 2) {
                PollData(
                    question = pollQuestion.trim(),
                    options = validPollOptions.mapIndexed { idx, text ->
                        PollOption("opt_${System.currentTimeMillis()}_$idx", text, 0)
                    }
                )
            } else null

            val layoutType = when {
                category == PostCategory.MARKETPLACE -> PostLayoutType.MARKETPLACE_ITEM
                category == PostCategory.TREE_HOLE -> PostLayoutType.TREE_HOLE_MOOD
                category == PostCategory.LOST_FOUND || category == PostCategory.MUTUAL_AID || category == PostCategory.OPPORTUNITY -> PostLayoutType.HELP_REQUEST
                pollData != null -> PostLayoutType.TOPIC_POSTER
                uploadedMedia.isNotEmpty() -> PostLayoutType.IMAGE_FIRST
                else -> PostLayoutType.EDITORIAL_TEXT
            }

            val existingDraft = _editingDraft.value
            val postId = if (asDraft && existingDraft != null) existingDraft.id else "post_${System.currentTimeMillis()}"
            val indexNum = 413 + repository.posts.value.size

            val newPost = Post(
                id = postId,
                indexCode = if (asDraft) "DRAFT" else "NO.0$indexNum",
                title = title.trim().take(15),
                body = body.trim(),
                author = activePostingPersona(),
                category = category,
                layoutType = layoutType,
                topicTags = topicTags.distinct(),
                mediaList = uploadedMedia,
                createdAt = if (asDraft) "保存于 刚刚" else "刚刚",
                viewCount = 1,
                allowComments = allowComments,
                price = price?.trim()?.takeIf { it.isNotEmpty() }?.let { if (it.startsWith("¥")) it else "¥ $it" },
                tradeStatus = tradeStatus?.takeIf { it.isNotBlank() },
                location = location?.trim()?.takeIf { it.isNotEmpty() },
                eventTime = eventTime?.trim()?.takeIf { it.isNotEmpty() },
                moodTag = moodTag,
                poll = pollData,
                isDraft = asDraft,
                isMine = true
            )

            if (asDraft) {
                repository.saveDraft(newPost)
                showToast("草稿已保存至「我的草稿」")
            } else {
                if (existingDraft != null) {
                    repository.deleteDraft(existingDraft.id)
                }
                repository.insertPost(newPost)
                showToast("已匿名发布至「${category.titleCn}」")
            }
            _editingDraft.value = null
            onComplete(newPost)
        }
    }

    fun deleteDraft(draftId: String) {
        repository.deleteDraft(draftId)
        showToast("草稿已删除")
    }

    // --- Courses & Experiences ---

    fun toggleCourseBookmark(courseId: String) {
        repository.toggleCourseBookmark(courseId)
        val saved = repository.courses.value.firstOrNull { it.id == courseId }?.isBookmarked == true
        showToast(if (saved) "已收藏该选课评价" else "已取消收藏")
    }

    fun submitCourseReview(
        courseId: String,
        semester: String,
        content: String,
        workload: Int,
        difficulty: Int,
        recommend: Int,
        attendanceMode: String,
        examFormat: String,
        suitableTag: String,
        onSuccess: () -> Unit
    ) {
        if (content.trim().length < 2) {
            showToast("请写下简短的真实上课感受")
            return
        }
        val review = CourseReview(
            id = "cr_${System.currentTimeMillis()}",
            author = activePostingPersona(),
            semester = semester.ifBlank { "2026 春季" },
            content = content.trim(),
            workload = workload.coerceIn(1, 5),
            difficulty = difficulty.coerceIn(1, 5),
            recommend = recommend.coerceIn(1, 5),
            attendanceMode = attendanceMode,
            examFormat = examFormat,
            suitableTag = suitableTag,
            createdAt = "刚刚"
        )
        repository.addCourseReview(courseId, review)
        showToast("评分与回答已计入课程综合结果")
        onSuccess()
    }

    fun createCommunityCourse(
        name: String,
        instructor: String,
        campus: String,
        college: String,
        courseType: String,
        semester: String,
        content: String,
        workload: Int,
        gradingFriendly: Int,
        recommend: Int,
        attendanceMode: String,
        examFormat: String,
        suitableTag: String,
        onSuccess: () -> Unit
    ) {
        if (name.trim().isEmpty()) {
            showToast("请填写课程名称")
            return
        }
        val firstReview = CourseReview(
            id = "cr_${System.currentTimeMillis()}",
            author = activePostingPersona(),
            semester = semester.ifBlank { "2026 春季" },
            content = content.trim().ifBlank { "由校友/同学共建录入的选课评价。" },
            workload = workload.coerceIn(1, 5),
            difficulty = gradingFriendly.coerceIn(1, 5),
            recommend = recommend.coerceIn(1, 5),
            attendanceMode = attendanceMode,
            examFormat = examFormat,
            suitableTag = suitableTag,
            createdAt = "刚刚"
        )
        val prefix = when (campus) {
            "燕郊校区" -> "YJ"
            "上海校区" -> "SH"
            else -> "WJ"
        }
        val newCourse = CourseRecord(
            id = "course_${System.currentTimeMillis()}",
            code = "$prefix-${100 + repository.courses.value.size + 1}",
            name = name.trim(),
            instructor = instructor.trim().ifBlank { "待补充授课教师" },
            campus = campus,
            college = college,
            major = courseType,
            courseType = courseType,
            workloadScore = workload.toFloat(),
            difficultyScore = gradingFriendly.toFloat(),
            recommendScore = recommend.toFloat(),
            suitableFor = suitableTag,
            summary = content.trim().ifBlank { "$campus · $college · 由大家评分共建" },
            reviews = listOf(firstReview)
        )
        repository.addCourse(newCourse)
        showToast("已成功共建「${newCourse.name}」选课评价")
        onSuccess()
    }

    fun toggleExperienceLike(articleId: String) {
        requireVerified("点赞学长学姐说") {
            repository.toggleExperienceLike(articleId)
        }
    }

    fun toggleExperienceBookmark(articleId: String) {
        requireVerified("收藏学长学姐说") {
            repository.toggleExperienceBookmark(articleId)
        }
    }

    fun createExperienceArticle(
        category: ExperienceCategory,
        cohort: String,
        content: String,
        onSuccess: () -> Unit
    ) {
        requireVerified("在学长学姐说发帖") {
            val cleanBody = content.trim()
            if (cleanBody.length < 2) {
                showToast("请输入要分享的经验或提问内容")
                return@requireVerified
            }
            val cleanCohort = cohort.trim().ifBlank { "2024届" }
            val previewLine = cleanBody.lineSequence().firstOrNull { it.isNotBlank() }?.take(36) ?: cleanBody.take(36)
            val nextIdx = repository.experiences.value.size + 1
            val article = ExperienceArticle(
                id = "exp_${System.currentTimeMillis()}",
                indexCode = "ARCH // %02d".format(nextIdx),
                title = previewLine,
                summary = cleanBody.take(72),
                body = cleanBody,
                category = if (category == ExperienceCategory.ALL) ExperienceCategory.MAJOR_STUDY else category,
                college = "央美匿名",
                major = category.labelCn,
                year = cleanCohort,
                tags = listOf(category.labelCn, cleanCohort),
                author = activePostingPersona(),
                readMinutes = (cleanBody.length / 180).coerceAtLeast(1)
            )
            repository.addExperienceArticle(article)
            showToast("已发布至「学长学姐说」（$cleanCohort）")
            onSuccess()
        }
    }

    fun addExperienceComment(articleId: String, content: String, cohortLabel: String) {
        requireVerified("回答学长学姐说") {
            if (content.isBlank()) return@requireVerified
            val cleanCohort = cohortLabel.trim()
            if (cleanCohort.isEmpty()) {
                showToast("请先选择或填写你是哪一届的")
                return@requireVerified
            }
            repository.addExperienceComment(
                articleId,
                Comment(
                    id = "ec_${System.currentTimeMillis()}",
                    postId = articleId,
                    author = activePostingPersona(),
                    content = content.trim(),
                    createdAt = "刚刚",
                    cohortLabel = cleanCohort
                )
            )
            showToast("已以「$cleanCohort」身份回复")
        }
    }

    fun sendWechatDmMessage(post: Post, text: String, wechatId: String? = null) {
        if (post.category != PostCategory.MARKETPLACE && post.category != PostCategory.MUTUAL_AID) {
            showToast("仅二手交易与校园互助支持私信互换微信")
            return
        }
        requireVerified("私信互换微信") {
            val cleanText = text.trim()
            val cleanWx = wechatId?.trim()?.takeIf { it.isNotEmpty() }
            if (cleanText.isEmpty() && cleanWx == null) return@requireVerified

            val me = activePostingPersona()
            val myMsg = WechatDmMessage(
                id = "dm_${System.currentTimeMillis()}",
                postId = post.id,
                senderNickname = me.nickname,
                isFromMe = true,
                text = cleanText.ifEmpty { "你好！我想交换微信沟通，这是我的微信号：" },
                wechatId = cleanWx,
                createdAt = "刚刚"
            )

            val hadPeerReply = _wechatDms.value[post.id]?.any { !it.isFromMe } == true
            _wechatDms.update { map ->
                val list = map[post.id].orEmpty() + myMsg
                map + (post.id to list)
            }

            if (cleanWx != null) {
                showToast("已发送你的微信号，可转移至微信沟通交易")
            }

            if (!hadPeerReply) {
                viewModelScope.launch {
                    delay(450)
                    val peerWx = "cafa_${post.id.takeLast(3)}_${(post.author.avatarSeed % 89 + 10)}"
                    val replyText = if (post.category == PostCategory.MARKETPLACE) {
                        "你好！东西还在，欢迎加我微信细聊校内面交时间和价格～"
                    } else {
                        "你好！看到你的互助私信啦，加我微信方便直接对接细节～"
                    }
                    val peerMsg = WechatDmMessage(
                        id = "dm_peer_${System.currentTimeMillis()}",
                        postId = post.id,
                        senderNickname = post.author.nickname,
                        isFromMe = false,
                        text = replyText,
                        wechatId = peerWx,
                        createdAt = "刚刚"
                    )
                    _wechatDms.update { map ->
                        val list = map[post.id].orEmpty() + peerMsg
                        map + (post.id to list)
                    }
                }
            }
        }
    }

    // --- Verification Workflow ---

    fun submitCampusVerification(
        identityType: IdentityType,
        proofDescription: String,
        proofUri: String?
    ) {
        if (proofUri.isNullOrBlank()) {
            showToast("请先上传学生证、学生卡或录取通知书照片，上传即刻通过认证")
            return
        }
        viewModelScope.launch {
            repository.submitVerification(
                VerificationRecord(
                    identityType = identityType,
                    school = "中央美术学院",
                    college = "",
                    major = "",
                    year = "",
                    proofDescription = proofDescription.trim().ifBlank { "中央美术学院身份凭证照片" },
                    proofUri = proofUri
                )
            )
            repository.resolveVerification(approved = true, rejectReason = null)
            showToast("央美本校认证已立即通过，现在可以发帖了")
        }
    }

    fun simulateVerificationResult(approved: Boolean, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            delay(300)
            repository.resolveVerification(
                approved = approved,
                rejectReason = if (approved) null else "证明材料未能清晰辨认中央美术学院标识，请重新上传清晰图片。"
            )
            onDone()
        }
    }

    fun resetVerificationForDemo() {
        viewModelScope.launch {
            repository.resetVerificationForTesting()
            showToast("已重置为未认证访客状态，可重新体验认证流程")
        }
    }

    // --- Notifications & Settings ---

    fun markNotificationRead(id: String) {
        repository.markNotificationRead(id)
    }

    fun markAllNotificationsRead() {
        repository.markAllNotificationsRead()
        showToast("全部消息已标记已读")
    }

    fun randomizeAnonymousIdentity() {
        viewModelScope.launch {
            val current = uiState.value.currentPersona
            val usedNicknames = buildSet {
                add(current.nickname)
                uiState.value.posts.forEach { post ->
                    add(post.author.nickname)
                    post.comments.forEach { add(it.author.nickname) }
                }
            }
            val nextNick = AnonymousNicknameLexicon.nextUniqueRandom(usedNicknames)
            val nextSeed = AnonymousNicknameLexicon.sampleDistinctAvatarSeeds(1, excludeSeed = current.avatarSeed).first()
            repository.updateAnonymousPersona(
                nickname = nextNick,
                avatarSeed = nextSeed,
                collegeTag = current.optionalCollegeTag,
                yearTag = current.optionalYearTag
            )
            showToast("已切换匿名视觉身份：$nextNick")
        }
    }

    fun updatePersona(
        nickname: String,
        avatarSeed: Int,
        onSuccess: () -> Unit = {}
    ) {
        val cleanNick = nickname.trim()
        if (cleanNick.length < 2) {
            showToast("匿名昵称至少需要 2 个字")
            return
        }
        if (cleanNick.length > 16) {
            showToast("匿名昵称最多 16 个字")
            return
        }
        val check = repository.moderationGateway.inspectText(cleanNick, uiState.value.settings.blockedKeywords)
        if (!check.passed) {
            showToast(check.reason ?: "昵称包含受限词汇，请修改后重试")
            return
        }
        viewModelScope.launch {
            val current = uiState.value.currentPersona
            repository.updateAnonymousPersona(
                nickname = cleanNick,
                avatarSeed = avatarSeed,
                collegeTag = current.optionalCollegeTag,
                yearTag = current.optionalYearTag
            )
            showToast("匿名昵称与形象已更新：$cleanNick")
            onSuccess()
        }
    }

    fun updateNickname(nickname: String) {
        updatePersona(
            nickname = nickname,
            avatarSeed = uiState.value.currentPersona.avatarSeed
        )
    }

    fun updateSettings(transform: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            repository.updateSettings(transform)
        }
    }

    fun blockUser(nickname: String) {
        viewModelScope.launch {
            repository.updateSettings { s ->
                s.copy(blockedUsers = (s.blockedUsers + nickname).distinct())
            }
            showToast("已屏蔽匿名用户「$nickname」")
        }
    }

    fun unblockUser(nickname: String) {
        viewModelScope.launch {
            repository.updateSettings { s ->
                s.copy(blockedUsers = s.blockedUsers.filterNot { it == nickname })
            }
            showToast("已解除屏蔽「$nickname」")
        }
    }

    fun addBlockedKeyword(keyword: String) {
        val kw = keyword.trim()
        if (kw.isEmpty()) return
        viewModelScope.launch {
            repository.updateSettings { s ->
                s.copy(blockedKeywords = (s.blockedKeywords + kw).distinct())
            }
            showToast("已添加屏蔽词「$kw」")
        }
    }

    fun removeBlockedKeyword(keyword: String) {
        viewModelScope.launch {
            repository.updateSettings { s ->
                s.copy(blockedKeywords = s.blockedKeywords.filterNot { it == keyword })
            }
        }
    }

    fun reportContent(summary: String, reason: String) {
        viewModelScope.launch {
            val entry = ReportEntry(
                id = "rep_${System.currentTimeMillis()}",
                targetSummary = summary.take(28),
                reason = reason,
                status = "已受理审核中",
                createdAt = "刚刚"
            )
            repository.updateSettings { s ->
                s.copy(reportHistory = listOf(entry) + s.reportHistory)
            }
            showToast("举报已提交至社区审核组，感谢共同维护树窝边界")
        }
    }

    private fun activePostingPersona(): AnonymousPersona {
        return uiState.value.currentPersona.copy(
            optionalCollegeTag = null,
            optionalYearTag = null
        )
    }

    private data class ContentBundle(
        val posts: List<Post>,
        val drafts: List<Post>,
        val topics: List<HotTopic>,
        val courses: List<CourseRecord>,
        val experiences: List<ExperienceArticle>
    )

    private data class IdentityBundle(
        val notifications: List<NotificationItem>,
        val status: VerificationStatus,
        val record: VerificationRecord,
        val persona: AnonymousPersona,
        val settings: UserSettings
    )

    private data class TransientBundle(
        val gateReason: String?,
        val toast: String?,
        val editingDraft: Post?,
        val wechatDms: Map<String, List<WechatDmMessage>>,
        val aiStatsState: AiStatsUiState
    )
}
