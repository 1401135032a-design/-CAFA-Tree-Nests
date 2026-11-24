package com.example.data

import kotlinx.coroutines.flow.StateFlow

/**
 * Reserved backend integration boundary interfaces.
 * Currently backed by local reactive simulation + DataStore persistence;
 * ready to swap in remote DB, real campus verification, moderation, and cloud storage SDKs.
 */

interface CommunityDatabaseGateway {
    val posts: StateFlow<List<Post>>
    val drafts: StateFlow<List<Post>>
    val hotTopics: StateFlow<List<HotTopic>>
    val courses: StateFlow<List<CourseRecord>>
    val experiences: StateFlow<List<ExperienceArticle>>
    val notifications: StateFlow<List<NotificationItem>>

    fun insertPost(post: Post)
    fun deletePost(postId: String)
    fun saveDraft(draft: Post)
    fun deleteDraft(draftId: String)
    fun toggleLike(postId: String)
    fun toggleBookmark(postId: String)
    fun addComment(postId: String, comment: Comment)
    fun votePoll(postId: String, optionId: String)
    fun addCourse(course: CourseRecord)
    fun addCourseReview(courseId: String, review: CourseReview)
    fun toggleCourseBookmark(courseId: String)
    fun toggleExperienceBookmark(articleId: String)
    fun toggleExperienceLike(articleId: String)
    fun addExperienceComment(articleId: String, comment: Comment)
    fun markNotificationRead(notificationId: String)
    fun markAllNotificationsRead()
    fun pushNotification(item: NotificationItem)
}

interface CampusAuthVerificationGateway {
    val verificationStatus: StateFlow<VerificationStatus>
    val verificationRecord: StateFlow<VerificationRecord>
    val currentPersona: StateFlow<AnonymousPersona>

    suspend fun submitVerification(record: VerificationRecord)
    suspend fun resolveVerification(approved: Boolean, rejectReason: String? = null)
    suspend fun resetVerificationForTesting()
    suspend fun updateAnonymousPersona(nickname: String, avatarSeed: Int, collegeTag: String?, yearTag: String?)
}

data class ModerationResult(
    val passed: Boolean,
    val reason: String? = null,
    val category: String? = null
)

interface ContentModerationGateway {
    fun inspectText(content: String, blockedKeywords: List<String> = emptyList()): ModerationResult

    suspend fun inspectSubmission(
        textParts: List<String>,
        mediaUris: List<String> = emptyList(),
        blockedKeywords: List<String> = emptyList()
    ): ModerationResult

    suspend fun inspectImages(mediaUris: List<String>): ModerationResult
}

interface CloudMediaStorageGateway {
    suspend fun uploadLocalMedia(uriString: String): String
}
