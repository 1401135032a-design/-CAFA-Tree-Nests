package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CourseRecord
import com.example.data.CrossCollegeCourseData
import com.example.data.MockSeedData
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState

/**
 * Community-built Course Evaluation Hub ("选课评价").
 * Covers all CAFA campuses (望京本院, 燕郊校区, 上海校区) & all colleges,
 * categorized into 跨院选修 & 公共选修, with scores & Q&A consensus calculated dynamically from student reviews.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseArchiveScreen(
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onSubmitReview: (
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
    ) -> Unit,
    onCreateCourse: (
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
    ) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCampus by remember { mutableStateOf("全部校区") }
    var selectedCollege by remember { mutableStateOf("全部学院") }
    var selectedType by remember { mutableStateOf("全部类型") }
    var activeCourseId by remember { mutableStateOf<String?>(null) }
    var showCreateCourse by remember { mutableStateOf(false) }
    var showAllCollegesSheet by remember { mutableStateOf(false) }

    val campuses = remember { listOf("全部校区") + MockSeedData.cafaCampuses }
    val collegesForCampus = remember(selectedCampus) {
        if (selectedCampus == "全部校区") {
            listOf("全部学院") + MockSeedData.cafaColleges
        } else {
            listOf("全部学院") + (MockSeedData.cafaCampusColleges[selectedCampus] ?: emptyList())
        }
    }
    val types = remember { listOf("全部类型") + MockSeedData.courseTypes }

    LaunchedEffect(selectedCampus) {
        if (!collegesForCampus.contains(selectedCollege)) {
            selectedCollege = "全部学院"
        }
    }

    val filteredCourses = remember(uiState.courses, searchQuery, selectedCampus, selectedCollege, selectedType) {
        uiState.courses.filter { c ->
            val matchQuery = searchQuery.isBlank() ||
                c.name.contains(searchQuery, true) ||
                c.instructor.contains(searchQuery, true) ||
                c.college.contains(searchQuery, true) ||
                c.campus.contains(searchQuery, true) ||
                c.major.contains(searchQuery, true) ||
                c.summary.contains(searchQuery, true) ||
                c.topExamAnswer.contains(searchQuery, true)
            val matchCampus = selectedCampus == "全部校区" || c.campus == selectedCampus
            val matchCollege = selectedCollege == "全部学院" || c.college == selectedCollege
            val matchType = selectedType == "全部类型" || c.courseType == selectedType
            matchQuery && matchCampus && matchCollege && matchType
        }
    }

    val groupedCourses = remember(filteredCourses) {
        val canonicalOrder = MockSeedData.courseCategories
        filteredCourses
            .groupBy { it.major.ifBlank { "其他方向" } }
            .entries
            .sortedBy { (cat, _) ->
                val idx = canonicalOrder.indexOf(cat)
                if (idx >= 0) idx else Int.MAX_VALUE
            }
    }

    val activeCourse = remember(uiState.courses, activeCourseId) {
        uiState.courses.firstOrNull { it.id == activeCourseId }
    }

    if (showCreateCourse) {
        BackHandler { showCreateCourse = false }
        CreateCommunityCourseView(
            onClose = { showCreateCourse = false },
            onSubmit = { name, instructor, campus, college, courseType, semester, content, w, g, r, att, exam, suit ->
                onCreateCourse(name, instructor, campus, college, courseType, semester, content, w, g, r, att, exam, suit) {
                    showCreateCourse = false
                }
            }
        )
        return
    }

    if (activeCourse != null) {
        BackHandler { activeCourseId = null }
        CourseDetailView(
            course = activeCourse,
            onBack = { activeCourseId = null },
            onToggleBookmark = { onToggleBookmark(activeCourse.id) },
            onSubmitReview = { semester, content, w, d, r, att, exam, suit, onSuccess ->
                onSubmitReview(activeCourse.id, semester, content, w, d, r, att, exam, suit, onSuccess)
            }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("选课评价", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("course_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(YangmeiPrimaryGradient)
                            .clickable { showCreateCourse = true }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("course_create_btn"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = PaperWhite, modifier = Modifier.size(15.dp))
                        Text("共建课程", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = PaperWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("course_archive_list"),
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "course_filters") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "由央美本院、燕郊、上海三校区同学评分回答共同计算结果，欢迎补充新课或参与打分。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索课程名、授课教师或学院...") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("course_search_input")
                    )

                    // 1. Campus Filter (望京本院 / 燕郊校区 / 上海校区)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(campuses) { campus ->
                            MonoTagChip(
                                text = campus,
                                inverted = selectedCampus == campus,
                                onClick = { selectedCampus = campus }
                            )
                        }
                    }

                    // 2. Type Filter (跨院选修 / 公共选修 / 体育选修)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(types) { t ->
                            MonoTagChip(
                                text = t,
                                inverted = selectedType == t,
                                onClick = { selectedType = t }
                            )
                        }
                    }

                    // Cross-college election rule notice (from student handbook)
                    if (selectedType == "全部类型" || selectedType == "跨院选修") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = AuroraIndigo,
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(14.dp)
                            )
                            Text(
                                text = CrossCollegeCourseData.CROSS_COLLEGE_NOTICE,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 3. Complete CAFA Colleges Filter with prominent "更多" full-college picker
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "按学院筛选（当前：$selectedCollege）",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuroraIndigo.copy(alpha = 0.12f))
                                    .clickable { showAllCollegesSheet = true }
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                                    .testTag("course_more_colleges_btn"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "更多学院 (${MockSeedData.cafaColleges.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AuroraIndigo
                                )
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = "展开全部学院",
                                    tint = AuroraIndigo,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        val quickColleges = remember(collegesForCampus, selectedCollege) {
                            val head = collegesForCampus.take(5).toMutableList()
                            if (selectedCollege != "全部学院" && !head.contains(selectedCollege)) {
                                head.add(1, selectedCollege)
                            }
                            head.distinct()
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(quickColleges) { col ->
                                MonoTagChip(
                                    text = col,
                                    inverted = selectedCollege == col,
                                    onClick = { selectedCollege = col }
                                )
                            }
                            item(key = "col_more_chip") {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .border(1.dp, AuroraIndigo.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                                        .background(AuroraIndigo.copy(alpha = 0.10f))
                                        .clickable { showAllCollegesSheet = true }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("course_more_colleges_chip"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "更多",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AuroraIndigo
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.ExpandMore,
                                        contentDescription = "选择全部学院",
                                        tint = AuroraIndigo,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredCourses.isEmpty()) {
                item(key = "course_empty") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = SlateAccent, modifier = Modifier.size(32.dp))
                            Text(
                                text = if (uiState.courses.isEmpty()) "暂无选课评价内容" else "该学院/类型下暂未收录选课评价",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text("欢迎做第一位共建者，录入课程并打分！", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(YangmeiPrimaryGradient)
                                    .clickable { showCreateCourse = true }
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = if (uiState.courses.isEmpty()) "立即共建第一门课程" else "立即共建此学院课程",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PaperWhite
                                )
                            }
                        }
                    }
                }
            } else {
                groupedCourses.forEach { (categoryName, coursesInGroup) ->
                    item(key = "cat_header_$categoryName") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(YangmeiPrimaryGradient)
                                )
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${coursesInGroup.size} 门课程",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(coursesInGroup, key = { it.id }) { course ->
                        CourseEvaluationCard(
                            course = course,
                            onClick = { activeCourseId = course.id },
                            onToggleBookmark = { onToggleBookmark(course.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAllCollegesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAllCollegesSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "选择央美全部学院（共 ${MockSeedData.cafaColleges.size} 个）",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "涵盖望京本院、燕郊校区与上海校区全部院系",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showAllCollegesSheet = false }) {
                        Icon(Icons.Outlined.Close, contentDescription = "关闭")
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .testTag("all_colleges_picker_flow")
                ) {
                    MonoTagChip(
                        text = "全部学院",
                        inverted = selectedCollege == "全部学院",
                        onClick = {
                            selectedCollege = "全部学院"
                            showAllCollegesSheet = false
                        }
                    )
                    MockSeedData.cafaColleges.forEach { col ->
                        MonoTagChip(
                            text = col,
                            inverted = selectedCollege == col,
                            onClick = {
                                val targetCampus = MockSeedData.campusForCollege(col)
                                if (selectedCampus != "全部校区" && selectedCampus != targetCampus) {
                                    selectedCampus = "全部校区"
                                }
                                selectedCollege = col
                                showAllCollegesSheet = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseEvaluationCard(
    course: CourseRecord,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    GradientBorderCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("course_card_${course.id}"),
        outerCornerRadius = 16.dp,
        borderWidth = 2.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val (typeFg, typeBg) = when (course.courseType) {
                        "跨院选修" -> SlateAccent to SlateTintBg
                        "体育选修" -> AuroraIndigo to AuroraIndigo.copy(alpha = 0.14f)
                        else -> Color(0xFF1A7A42) to MintTintBg
                    }
                    Text(
                        text = course.courseType,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = typeFg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(typeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                    Text(
                        text = "${course.campus} · ${course.college}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (course.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "收藏选课评价",
                        tint = SlateAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "授课教师：${course.instructor} · ${course.major}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (course.summary.isNotBlank()) {
                    Text(
                        text = course.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val hasReviews = course.reviews.isNotEmpty()
            // Community-Calculated Metric Summary Bar (soft gradient background, no black box)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(YangmeiFeaturedCardGradient)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CourseMetricCell("综合推荐", if (hasReviews) String.format("%.1f", course.computedRecommend) else "--", highlight = true)
                CourseMetricCell("给分友好", if (hasReviews) String.format("%.1f", course.computedGradingFriendly) else "--")
                CourseMetricCell("作业强度", if (hasReviews) String.format("%.1f", course.computedWorkload) else "--")
                CourseMetricCell("参与共建", "${course.reviews.size} 份")
            }

            // Auto-calculated Q&A Consensus Row (horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasReviews) {
                    ConsensusBadge("考勤：${course.topAttendanceAnswer}")
                    ConsensusBadge("考核：${course.topExamAnswer}")
                    course.topSuitableTags.forEach { tag ->
                        ConsensusBadge(text = tag, isWarning = tag == "慎选避雷")
                    }
                } else {
                    ConsensusBadge(course.suitableFor.ifBlank { "待补充评价" })
                }
            }
        }
    }
}

@Composable
private fun ConsensusBadge(text: String, isWarning: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isWarning) FontWeight.SemiBold else FontWeight.Normal,
        color = if (isWarning) VermilionRed else SlateAccent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isWarning) VermilionRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}

@Composable
private fun CourseMetricCell(label: String, value: String, highlight: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (highlight) SlatePrimary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CourseDetailView(
    course: CourseRecord,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onSubmitReview: (
        semester: String,
        content: String,
        workload: Int,
        difficulty: Int,
        recommend: Int,
        attendanceMode: String,
        examFormat: String,
        suitableTag: String,
        onSuccess: () -> Unit
    ) -> Unit
) {
    var showReviewSheet by remember { mutableStateOf(false) }
    var semester by remember { mutableStateOf("2026 春季") }
    var content by remember { mutableStateOf("") }
    var workload by remember { mutableIntStateOf(3) }
    var gradingFriendly by remember { mutableIntStateOf(4) }
    var recommend by remember { mutableIntStateOf(5) }
    var attendance by remember { mutableStateOf(MockSeedData.courseAttendanceOptions[1]) }
    var examFormat by remember { mutableStateOf(MockSeedData.courseExamOptions[0]) }
    var suitableTag by remember { mutableStateOf(MockSeedData.courseSuitableOptions[0]) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${course.campus} · ${course.college}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(course.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回选课列表")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (course.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "收藏课程",
                            tint = SlateAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Community-Calculated Overview Card
            item(key = "cd_header") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.5.dp,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MonoTagChip(text = course.courseType, inverted = true)
                            MonoTagChip(text = course.campus)
                            MonoTagChip(text = course.college)
                        }
                        Text(course.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("授课教师：${course.instructor} · ${course.major}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (course.summary.isNotBlank()) {
                            Text(
                                text = course.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Calculated dashboard
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(YangmeiFeaturedCardGradient)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("大家评分计算结果", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (course.reviews.isEmpty()) "暂未收到评分" else "基于 ${course.reviews.size} 份同学回答",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateAccent
                                )
                            }
                            if (course.reviews.isEmpty()) {
                                Text(
                                    text = "该课程已收录至跨院选修清单，欢迎上过的同学点击下方按钮给出第一份评分与选课避坑建议。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                ScoreMeterRow("综合推荐", course.computedRecommend, highlight = true)
                                ScoreMeterRow("给分友好", course.computedGradingFriendly)
                                ScoreMeterRow("作业强度", course.computedWorkload)

                                EditorialHairline(thickness = 0.6.dp)

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ConsensusBadge("多数回答考勤：${course.topAttendanceAnswer}")
                                    ConsensusBadge("主要考核：${course.topExamAnswer}")
                                    course.topSuitableTags.forEach { tag ->
                                        ConsensusBadge("共识：$tag", isWarning = tag == "慎选避雷")
                                    }
                                }
                            }
                        }

                        // Toggle rating & Q&A form button (gradient)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(YangmeiPrimaryGradient)
                                .clickable { showReviewSheet = !showReviewSheet }
                                .testTag("course_write_review_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Outlined.RateReview, contentDescription = null, tint = PaperWhite, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (showReviewSheet) "收起评分回答" else "参与评分与回答（实时计入结果）",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PaperWhite
                                )
                            }
                        }

                        if (showReviewSheet) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(0.9.dp, SlatePrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.45f))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("你的评分与选课问答", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = semester,
                                    onValueChange = { semester = it },
                                    label = { Text("修读学期") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                RatingPickerRow("综合推荐", recommend) { recommend = it }
                                RatingPickerRow("给分友好", gradingFriendly) { gradingFriendly = it }
                                RatingPickerRow("作业强度", workload) { workload = it }

                                QuestionChoiceGroup(
                                    question = "考勤方式如何？",
                                    options = MockSeedData.courseAttendanceOptions,
                                    selected = attendance,
                                    onSelect = { attendance = it }
                                )
                                QuestionChoiceGroup(
                                    question = "期末主要考核形式？",
                                    options = MockSeedData.courseExamOptions,
                                    selected = examFormat,
                                    onSelect = { examFormat = it }
                                )
                                QuestionChoiceGroup(
                                    question = "这门课最适合哪类同学？",
                                    options = MockSeedData.courseSuitableOptions,
                                    selected = suitableTag,
                                    onSelect = { suitableTag = it }
                                )

                                OutlinedTextField(
                                    value = content,
                                    onValueChange = { content = it },
                                    label = { Text("补充你的真实体验、老师风格或避雷建议") },
                                    minLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("course_review_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(YangmeiPrimaryGradient)
                                        .clickable {
                                            onSubmitReview(
                                                semester, content, workload, gradingFriendly, recommend,
                                                attendance, examFormat, suitableTag
                                            ) {
                                                content = ""
                                                showReviewSheet = false
                                            }
                                        }
                                        .testTag("course_review_submit_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("提交评分并更新结果", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = PaperWhite)
                                }
                            }
                        }
                    }
                }
            }

            item(key = "cd_reviews_title") {
                Text(
                    text = "同学真实评价 (${course.reviews.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            items(course.reviews, key = { it.id }) { rev ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnonymousAvatar(seed = rev.author.avatarSeed, size = 30.dp)
                                Column {
                                    Text(rev.author.nickname, style = MaterialTheme.typography.titleSmall)
                                    Text("${rev.semester} · ${rev.createdAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                            MonoTagChip(text = "推荐 ${rev.recommend}/5", inverted = true)
                        }
                        Text(rev.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ConsensusBadge("给分 ${rev.difficulty}/5")
                            ConsensusBadge("作业 ${rev.workload}/5")
                            ConsensusBadge(rev.attendanceMode)
                            ConsensusBadge(rev.examFormat)
                            ConsensusBadge(rev.suitableTag)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateCommunityCourseView(
    onClose: () -> Unit,
    onSubmit: (
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
        suitableTag: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }
    var campus by remember { mutableStateOf(MockSeedData.cafaCampuses.first()) }
    var college by remember { mutableStateOf(MockSeedData.cafaCampusColleges[campus]!!.first()) }
    var courseType by remember { mutableStateOf(MockSeedData.courseTypes.first()) }
    var semester by remember { mutableStateOf("2026 春季") }
    var content by remember { mutableStateOf("") }
    var recommend by remember { mutableIntStateOf(5) }
    var gradingFriendly by remember { mutableIntStateOf(4) }
    var workload by remember { mutableIntStateOf(3) }
    var attendance by remember { mutableStateOf(MockSeedData.courseAttendanceOptions[1]) }
    var examFormat by remember { mutableStateOf(MockSeedData.courseExamOptions[0]) }
    var suitableTag by remember { mutableStateOf(MockSeedData.courseSuitableOptions[0]) }

    val availableColleges = remember { MockSeedData.cafaColleges }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("共建选课评价", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "create_course_form") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("课程归属（覆盖央美全部 18 个院系）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                        QuestionChoiceGroup("所在校区", MockSeedData.cafaCampuses, campus) { campus = it }
                        QuestionChoiceGroup("选课类型", MockSeedData.courseTypes, courseType) { courseType = it }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("开设学院（全部可选）", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "共 ${availableColleges.size} 个院系",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateAccent
                                )
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableColleges.forEach { col ->
                                    MonoTagChip(
                                        text = col,
                                        inverted = college == col,
                                        onClick = {
                                            college = col
                                            campus = MockSeedData.campusForCollege(col)
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("课程名称（必填）") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = instructor,
                            onValueChange = { instructor = it },
                            label = { Text("授课教师") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        EditorialHairline()

                        Text("给出首条评分与回答", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        RatingPickerRow("综合推荐", recommend) { recommend = it }
                        RatingPickerRow("给分友好", gradingFriendly) { gradingFriendly = it }
                        RatingPickerRow("作业强度", workload) { workload = it }

                        QuestionChoiceGroup("考勤方式", MockSeedData.courseAttendanceOptions, attendance) { attendance = it }
                        QuestionChoiceGroup("考核形式", MockSeedData.courseExamOptions, examFormat) { examFormat = it }
                        QuestionChoiceGroup("适合标签", MockSeedData.courseSuitableOptions, suitableTag) { suitableTag = it }

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            label = { Text("写下你的真实选课体验与建议") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(YangmeiPrimaryGradient)
                                .clickable {
                                    onSubmit(
                                        name, instructor, campus, college, courseType,
                                        semester, content, workload, gradingFriendly, recommend,
                                        attendance, examFormat, suitableTag
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("发布共建课程", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = PaperWhite)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionChoiceGroup(
    question: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(question, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { opt ->
                MonoTagChip(text = opt, inverted = selected == opt, onClick = { onSelect(opt) })
            }
        }
    }
}

@Composable
private fun ScoreMeterRow(label: String, score: Float, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(64.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((score / 5f).coerceIn(0.06f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (highlight) YangmeiPrimaryGradient else androidx.compose.ui.graphics.SolidColor(SlateAccent.copy(alpha = 0.72f)))
            )
        }
        Text(
            text = String.format("%.1f", score),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RatingPickerRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..5).forEach { n ->
                val active = n <= value
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (active) Modifier.background(YangmeiPrimaryGradient)
                            else Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        )
                        .clickable { onChange(n) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$n",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (active) PaperWhite else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
