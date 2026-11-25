package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Keep
@IgnoreExtraProperties
data class FirestorePostDto(
    @DocumentId val id: String = "",
    val authorUid: String = "",
    val visibility: String = "public",
    val deleteVerifier: String? = null,
    val indexCode: String = "NO.0413",
    val title: String = "",
    val body: String = "",
    val category: String = PostCategory.TREE_HOLE.name,
    val layoutType: String = PostLayoutType.EDITORIAL_TEXT.name,
    val authorNickname: String = "花家地灰鸽",
    val authorAvatarSeed: Int = 512,
    val authorIdentityType: String = IdentityType.STUDENT.name,
    val authorVerified: Boolean = true,
    val topicTags: List<String> = emptyList(),
    val mediaUrls: List<String> = emptyList(),
    val viewCount: Int = 1,
    val likeCount: Int = 0,
    val bookmarkCount: Int = 0,
    val commentCount: Int = 0,
    val allowComments: Boolean = true,
    val price: String? = null,
    val tradeStatus: String? = null,
    val location: String? = null,
    val eventTime: String? = null,
    val contactHint: String? = null,
    val moodTag: String? = null,
    val pollQuestion: String? = null,
    val pollOptions: List<Map<String, Any>>? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FirestoreCommentDto(
    @DocumentId val id: String = "",
    val postId: String = "",
    val postAuthorUid: String = "",
    val authorUid: String = "",
    val visibility: String = "public",
    val authorNickname: String = "花家地灰鸽",
    val authorAvatarSeed: Int = 512,
    val authorIdentityType: String = IdentityType.STUDENT.name,
    val authorVerified: Boolean = true,
    val content: String = "",
    val likes: Int = 0,
    val replyToNickname: String? = null,
    val comfortTag: String? = null,
    val cohortLabel: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FirestoreNotificationDto(
    @DocumentId val id: String = "",
    val recipientUid: String = "",
    val senderUid: String = "",
    val senderNickname: String = "央美同学",
    val senderAvatarSeed: Int = 512,
    val type: String = "LIKE",
    val group: String = "REACTIONS",
    val postId: String = "",
    val summary: String = "",
    val visibility: String = "public",
    @ServerTimestamp val createdAt: Timestamp? = null
)

object FirestoreMappers {
    const val FEEDBACK_TARGET_EMAIL = "1401135032@qq.com"

    fun formatRelativeTimestamp(ts: Timestamp?): String {
        if (ts == null) return "刚刚"
        val diffMs = (Date().time - ts.toDate().time).coerceAtLeast(0L)
        val minutes = diffMs / 60_000L
        val hours = minutes / 60L
        val days = hours / 24L
        return when {
            minutes < 1L -> "刚刚"
            minutes < 60L -> "${minutes}分钟前"
            hours < 24L -> "${hours}小时前"
            days < 7L -> "${days}天前"
            else -> {
                val cal = java.util.Calendar.getInstance().apply { time = ts.toDate() }
                "${cal.get(java.util.Calendar.MONTH) + 1}月${cal.get(java.util.Calendar.DAY_OF_MONTH)}日"
            }
        }
    }

    fun toPostCreateMap(post: Post, authorUid: String, deleteVerifier: String): Map<String, Any> {
        val safeMedia = post.mediaList
            .mapNotNull { it.uriString }
            .filter { it.isNotBlank() && it.length <= 160_000 }
            .take(4)

        val pollOptionMaps = post.poll?.options?.take(6)?.map { opt ->
            mapOf(
                "id" to opt.id,
                "text" to opt.text.take(60),
                "votes" to opt.votes
            )
        }

        return mapOf<String, Any?>(
            "authorUid" to authorUid,
            "visibility" to "public",
            "deleteVerifier" to deleteVerifier,
            "indexCode" to post.indexCode.take(24).ifBlank { "NO.0413" },
            "title" to post.title.take(80),
            "body" to post.body.take(5000).ifBlank { post.title.take(80).ifBlank { "校园动态" } },
            "category" to post.category.name,
            "layoutType" to post.layoutType.name,
            "authorNickname" to post.author.nickname.take(40).ifBlank { "花家地灰鸽" },
            "authorAvatarSeed" to post.author.avatarSeed.coerceIn(0, 1_000_000),
            "authorIdentityType" to post.author.identityType.name,
            "authorVerified" to post.author.isVerified,
            "topicTags" to post.topicTags.take(10),
            "mediaUrls" to safeMedia,
            "viewCount" to post.viewCount.coerceAtLeast(1),
            "likeCount" to 0,
            "bookmarkCount" to 0,
            "commentCount" to 0,
            "allowComments" to post.allowComments,
            "price" to post.price?.take(40),
            "tradeStatus" to post.tradeStatus?.take(60),
            "location" to post.location?.take(80),
            "eventTime" to post.eventTime?.take(60),
            "contactHint" to post.contactHint?.take(100),
            "moodTag" to post.moodTag?.take(40),
            "pollQuestion" to post.poll?.question?.take(120),
            "pollOptions" to pollOptionMaps,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }

    fun toCommentCreateMap(
        postId: String,
        postAuthorUid: String,
        comment: Comment,
        authorUid: String
    ): Map<String, Any> {
        return mapOf<String, Any?>(
            "postId" to postId,
            "postAuthorUid" to postAuthorUid.ifBlank { authorUid },
            "authorUid" to authorUid,
            "visibility" to "public",
            "authorNickname" to comment.author.nickname.take(40).ifBlank { "花家地灰鸽" },
            "authorAvatarSeed" to comment.author.avatarSeed.coerceIn(0, 1_000_000),
            "authorIdentityType" to comment.author.identityType.name,
            "authorVerified" to comment.author.isVerified,
            "content" to comment.content.take(1500),
            "likes" to 0,
            "replyToNickname" to comment.replyToNickname?.take(40),
            "comfortTag" to comment.comfortTag?.take(40),
            "cohortLabel" to comment.cohortLabel?.take(40),
            "createdAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }

    fun toFeedbackCreateMap(
        uid: String,
        nickname: String,
        content: String,
        appVersion: String
    ): Map<String, Any> {
        return mapOf(
            "userId" to uid,
            "authorNickname" to nickname.take(40).ifBlank { "匿名同学" },
            "content" to content.trim().take(2000),
            "targetEmail" to FEEDBACK_TARGET_EMAIL,
            "platform" to "android",
            "appVersion" to appVersion.take(30),
            "createdAt" to FieldValue.serverTimestamp()
        )
    }

    fun fromPostDto(
        dto: FirestorePostDto,
        currentUid: String,
        likedIds: Set<String>,
        bookmarkedIds: Set<String>,
        comments: List<Comment>
    ): Post {
        val category = PostCategory.entries.firstOrNull { it.name == dto.category } ?: PostCategory.TREE_HOLE
        val layout = PostLayoutType.entries.firstOrNull { it.name == dto.layoutType } ?: PostLayoutType.EDITORIAL_TEXT
        val idType = IdentityType.entries.firstOrNull { it.name == dto.authorIdentityType } ?: IdentityType.STUDENT
        val media = dto.mediaUrls.mapIndexed { idx, url ->
            PostMedia(
                id = "${dto.id}_m_$idx",
                uriString = url,
                aspectRatio = if (idx % 2 == 0) 4f / 3f else 3f / 4f
            )
        }
        val poll = if (!dto.pollQuestion.isNullOrBlank() && !dto.pollOptions.isNullOrEmpty()) {
            val opts = dto.pollOptions.mapIndexedNotNull { idx, map ->
                val text = (map["text"] as? String)?.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
                val optId = (map["id"] as? String)?.takeIf { it.isNotBlank() } ?: "opt_$idx"
                val votes = ((map["votes"] as? Number)?.toInt() ?: 0).coerceAtLeast(0)
                PollOption(optId, text, votes)
            }
            if (opts.size >= 2) PollData(question = dto.pollQuestion, options = opts) else null
        } else null

        return Post(
            id = dto.id,
            indexCode = dto.indexCode.ifBlank { "NO.0413" },
            title = dto.title,
            body = dto.body,
            author = AnonymousPersona(
                id = dto.authorUid.ifBlank { "anon" },
                nickname = dto.authorNickname.ifBlank { "花家地灰鸽" },
                avatarSeed = dto.authorAvatarSeed,
                identityType = idType,
                isVerified = dto.authorVerified
            ),
            category = category,
            layoutType = layout,
            topicTags = dto.topicTags,
            mediaList = media,
            createdAt = formatRelativeTimestamp(dto.createdAt),
            viewCount = dto.viewCount,
            likeCount = dto.likeCount,
            bookmarkCount = dto.bookmarkCount,
            isLiked = likedIds.contains(dto.id),
            isBookmarked = bookmarkedIds.contains(dto.id),
            allowComments = dto.allowComments,
            comments = comments,
            price = dto.price,
            tradeStatus = dto.tradeStatus,
            location = dto.location,
            eventTime = dto.eventTime,
            contactHint = dto.contactHint,
            moodTag = dto.moodTag,
            poll = poll,
            isDraft = false,
            isMine = dto.authorUid == currentUid
        )
    }

    fun fromCommentDto(dto: FirestoreCommentDto): Comment {
        val idType = IdentityType.entries.firstOrNull { it.name == dto.authorIdentityType } ?: IdentityType.STUDENT
        return Comment(
            id = dto.id,
            postId = dto.postId,
            author = AnonymousPersona(
                id = dto.authorUid.ifBlank { "anon" },
                nickname = dto.authorNickname.ifBlank { "花家地灰鸽" },
                avatarSeed = dto.authorAvatarSeed,
                identityType = idType,
                isVerified = dto.authorVerified
            ),
            content = dto.content,
            createdAt = formatRelativeTimestamp(dto.createdAt),
            likes = dto.likes,
            isLiked = false,
            replyToNickname = dto.replyToNickname,
            comfortTag = dto.comfortTag,
            cohortLabel = dto.cohortLabel
        )
    }

    fun toNotificationCreateMap(
        recipientUid: String,
        senderUid: String,
        senderNickname: String,
        senderAvatarSeed: Int,
        type: String,
        group: String,
        postId: String,
        summary: String
    ): Map<String, Any> {
        return mapOf(
            "recipientUid" to recipientUid.take(128),
            "senderUid" to senderUid.take(128),
            "senderNickname" to senderNickname.take(40).ifBlank { "央美同学" },
            "senderAvatarSeed" to senderAvatarSeed.coerceIn(0, 1_000_000),
            "type" to type,
            "group" to group,
            "postId" to postId.take(128),
            "summary" to summary.take(300).ifBlank { "与你产生了新的互动" },
            "visibility" to "public",
            "createdAt" to FieldValue.serverTimestamp()
        )
    }

    fun fromNotificationDto(
        dto: FirestoreNotificationDto,
        readIds: Set<String>
    ): NotificationItem {
        val mappedType = when (dto.type) {
            "COMMENT" -> NotificationType.COMMENT
            "REPLY" -> NotificationType.REPLY
            "BOOKMARK" -> NotificationType.BOOKMARK
            "LIKE", "COMMENT_LIKE" -> NotificationType.LIKE
            else -> NotificationType.LIKE
        }
        return NotificationItem(
            id = dto.id,
            type = mappedType,
            sender = AnonymousPersona(
                id = dto.senderUid.ifBlank { "anon" },
                nickname = dto.senderNickname.ifBlank { "央美同学" },
                avatarSeed = dto.senderAvatarSeed,
                identityType = IdentityType.STUDENT,
                isVerified = true
            ),
            summary = dto.summary.ifBlank { "与你产生了新的互动" },
            quoteSnippet = null,
            targetPostId = dto.postId.takeIf { it.isNotBlank() },
            createdAt = formatRelativeTimestamp(dto.createdAt),
            isUnread = !readIds.contains(dto.id)
        )
    }
}
