package com.example.data

import androidx.annotation.DrawableRes

enum class IdentityType(val labelCn: String, val labelEn: String) {
    STUDENT("在读学生", "CURRENT STUDENT"),
    ALUMNI("毕业校友", "CAFA ALUMNI")
}

enum class VerificationStatus(val labelCn: String, val labelEn: String) {
    UNVERIFIED("未认证", "UNVERIFIED"),
    REVIEWING("审核中", "IN REVIEW"),
    VERIFIED("已认证", "VERIFIED CAFA"),
    REJECTED("未通过", "REJECTED")
}

/**
 * Private verification payload — strictly isolated from public community identity.
 * Never exposed on posts, comments, or public profiles.
 */
data class VerificationRecord(
    val identityType: IdentityType = IdentityType.STUDENT,
    val school: String = "中央美术学院",
    val college: String = "",
    val major: String = "",
    val year: String = "",
    val proofDescription: String = "",
    val proofUri: String? = null,
    val submittedAt: String = "",
    val rejectReason: String? = null
)

/**
 * Public community persona — stable anonymous identity rendered via generative abstract visuals.
 */
data class AnonymousPersona(
    val id: String,
    val nickname: String,
    val avatarSeed: Int,
    val identityType: IdentityType = IdentityType.STUDENT,
    val isVerified: Boolean = true,
    val optionalCollegeTag: String? = null,
    val optionalYearTag: String? = null
)

enum class PostCategory(
    val indexCode: String,
    val titleCn: String,
    val subtitleEn: String,
    val description: String,
    val accentType: CategoryAccent
) {
    CAMPUS_RANT("01", "校园吐槽", "CAMPUS RANT", "食堂、宿管、DDL与花家地日常", CategoryAccent.INK),
    STUDY("02", "学习交流", "STUDIO & STUDY", "技法探讨、软件插件与作业互评", CategoryAccent.SILVER),
    COURSE_ARCHIVE("03", "选课评价", "COURSE REVIEWS", "跨院与公共选修 · 大家评分共建", CategoryAccent.ACID),
    SENIOR_EXPERIENCE("04", "学长学姐说", "ALUMNI NOTES", "考研留学、工作室与毕设经验", CategoryAccent.INK),
    RELATIONSHIP("05", "恋爱情感", "EMOTION", "心动瞬间、艺术生恋爱与亲密关系", CategoryAccent.RED),
    TREE_HOLE("06", "树洞", "TREE HOLE", "卸下防备的情绪低语与安静陪伴", CategoryAccent.MIST),
    OPPORTUNITY("07", "校园机会", "OPPORTUNITIES", "兼职实习、策展协作与模特招募", CategoryAccent.GREEN),
    MARKETPLACE("08", "闲置交换", "EXCHANGE", "画材颜料、相机数位板与二手书", CategoryAccent.ACID),
    LOST_FOUND("09", "寻物", "LOST & FOUND", "校园卡、速写本与工作室遗失物", CategoryAccent.RED),
    MUTUAL_AID("10", "校园互助", "MUTUAL AID", "代取快递、拼车买材料与找搭子", CategoryAccent.GREEN),
    EXHIBITION("11", "展览资讯", "EXHIBITIONS", "美术馆新展、地下室快闪与看展记", CategoryAccent.INK),
    GRAD_ABROAD("12", "考研留学", "GRAD & ABROAD", "作品集排版、院校申请与上岸复盘", CategoryAccent.SILVER),
    CAREER("13", "毕业求职", "CAREER", "设计工作室、游戏原画与独立艺术", CategoryAccent.INK),
    ART_CREATION("14", "艺术创作", "ARTWORKS", "阶段性草图、装置实验与创作手记", CategoryAccent.SILVER)
}

enum class CategoryAccent {
    INK, SILVER, MIST, ACID, GREEN, RED
}

enum class PostLayoutType {
    EDITORIAL_TEXT,
    IMAGE_FIRST,
    TOPIC_POSTER,
    HELP_REQUEST,
    MARKETPLACE_ITEM,
    TREE_HOLE_MOOD
}

data class PostMedia(
    val id: String,
    @DrawableRes val drawableRes: Int? = null,
    val uriString: String? = null,
    val aspectRatio: Float = 4f / 3f,
    val caption: String = ""
)

data class PollOption(
    val id: String,
    val text: String,
    val votes: Int
)

data class PollData(
    val question: String,
    val options: List<PollOption>,
    val votedOptionId: String? = null
) {
    val totalVotes: Int get() = options.sumOf { it.votes }
}

data class Comment(
    val id: String,
    val postId: String,
    val author: AnonymousPersona,
    val content: String,
    val createdAt: String,
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val replyToNickname: String? = null,
    val comfortTag: String? = null,
    val cohortLabel: String? = null // 哪一届（用于「学长学姐说」回答标注）
)

data class WechatDmMessage(
    val id: String,
    val postId: String,
    val senderNickname: String,
    val isFromMe: Boolean,
    val text: String,
    val wechatId: String? = null,
    val createdAt: String
)

data class Post(
    val id: String,
    val indexCode: String,
    val title: String,
    val body: String,
    val author: AnonymousPersona,
    val category: PostCategory,
    val layoutType: PostLayoutType,
    val topicTags: List<String> = emptyList(),
    val mediaList: List<PostMedia> = emptyList(),
    val createdAt: String,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val bookmarkCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val allowComments: Boolean = true,
    val comments: List<Comment> = emptyList(),
    // Specialized module fields
    val price: String? = null,
    val tradeStatus: String? = null, // e.g., "在售", "可换物", "已出"
    val location: String? = null,
    val eventTime: String? = null,
    val contactHint: String? = null,
    val moodTag: String? = null,
    val poll: PollData? = null,
    val isDraft: Boolean = false,
    val isMine: Boolean = false
)

data class HotTopic(
    val id: String,
    val indexCode: String,
    val tag: String,
    val subtitleEn: String,
    val description: String,
    val postCount: Int,
    val discussionCount: Int,
    val heatStatus: String,
    val darkPoster: Boolean = false
)

data class CourseReview(
    val id: String,
    val author: AnonymousPersona,
    val semester: String,
    val content: String,
    val workload: Int,        // 1..5 作业量
    val difficulty: Int,      // 1..5 给分友好度
    val recommend: Int,       // 1..5 综合推荐度
    val attendanceMode: String = "偶尔抽查", // 考勤回答：不点名 / 偶尔抽查 / 每节签到
    val examFormat: String = "作品汇报",     // 考核回答：作品汇报 / 课程论文 / 期末展览 / 结课作业
    val suitableTag: String = "跨专业友好",  // 适合人群回答
    val createdAt: String,
    val helpfulCount: Int = 0
)

data class CourseRecord(
    val id: String,
    val code: String,
    val name: String,
    val instructor: String,
    val campus: String,     // 望京本院 / 燕郊校区 / 上海校区
    val college: String,
    val major: String,
    val courseType: String, // 跨院选修 / 公共选修
    val workloadScore: Float,
    val difficultyScore: Float, // 给分友好度均值
    val recommendScore: Float,
    val suitableFor: String,
    val summary: String,
    val reviews: List<CourseReview>,
    val isBookmarked: Boolean = false
) {
    val computedRecommend: Float
        get() = if (reviews.isEmpty()) recommendScore else reviews.map { it.recommend }.average().toFloat()

    val computedWorkload: Float
        get() = if (reviews.isEmpty()) workloadScore else reviews.map { it.workload }.average().toFloat()

    val computedGradingFriendly: Float
        get() = if (reviews.isEmpty()) difficultyScore else reviews.map { it.difficulty }.average().toFloat()

    val topAttendanceAnswer: String
        get() = reviews.groupingBy { it.attendanceMode }.eachCount().maxByOrNull { it.value }?.key ?: "暂无数据"

    val topExamAnswer: String
        get() = reviews.groupingBy { it.examFormat }.eachCount().maxByOrNull { it.value }?.key ?: "暂无数据"

    val topSuitableTags: List<String>
        get() = reviews.groupingBy { it.suitableTag }.eachCount()
            .entries.sortedByDescending { it.value }.take(2).map { it.key }
}

enum class ExperienceCategory(val labelCn: String, val labelEn: String) {
    ALL("全部", "ALL"),
    MAJOR_STUDY("专业学习", "STUDY"),
    GRAD_EXAM("考研", "POSTGRAD"),
    STUDY_ABROAD("留学", "ABROAD"),
    EMPLOYMENT("就业", "CAREER"),
    GRADUATION_WORK("毕业作品", "THESIS"),
    INTERNSHIP("实习", "INTERN"),
    STUDIO("工作室", "STUDIO"),
    ART_PRACTICE("艺术创作", "PRACTICE")
}

data class ExperienceArticle(
    val id: String,
    val indexCode: String,
    val title: String,
    val summary: String,
    val body: String,
    val category: ExperienceCategory,
    val college: String,
    val major: String,
    val year: String,
    val tags: List<String>,
    val author: AnonymousPersona,
    val media: PostMedia? = null,
    val readMinutes: Int = 5,
    val likeCount: Int = 0,
    val bookmarkCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val comments: List<Comment> = emptyList()
)

enum class NotificationType(val labelCn: String, val labelEn: String) {
    COMMENT("评论通知", "COMMENT"),
    REPLY("回复通知", "REPLY"),
    LIKE("点赞通知", "LIKE"),
    BOOKMARK("收藏通知", "BOOKMARK"),
    ANONYMOUS_NOTE("匿名留言", "NOTE"),
    SYSTEM("系统通知", "SYSTEM"),
    VERIFICATION("审核通知", "VERIFY")
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val sender: AnonymousPersona,
    val summary: String,
    val quoteSnippet: String? = null,
    val targetPostId: String? = null,
    val createdAt: String,
    val isUnread: Boolean = true
)

data class ReportEntry(
    val id: String,
    val targetSummary: String,
    val reason: String,
    val status: String,
    val createdAt: String
)

data class UserSettings(
    val darkTheme: Boolean = false,
    val showCollegeTag: Boolean = true,
    val notifyComments: Boolean = true,
    val notifyLikes: Boolean = true,
    val notifySystem: Boolean = true,
    val allowAnonymousNotes: Boolean = true,
    val blockedUsers: List<String> = listOf("失焦的石膏像素描"),
    val blockedKeywords: List<String> = listOf("广告", "代写"),
    val reportHistory: List<ReportEntry> = listOf(
        ReportEntry("rep_01", "校外不明机构推销贴", "校外广告引流", "已核实处理", "3天前")
    )
)

data class TodayCampusSnapshot(
    val dateLabel: String,
    val newDiscussions: Int,
    val hotPostTitle: String,
    val hotPostId: String,
    val campusBrief: String
)
