package com.example.data

/**
 * Curated cross-college elective ("跨院选修") course evaluations organized by subject category,
 * campus, and college from CAFA student community feedback.
 */
object CrossCollegeCourseData {

    const val CROSS_COLLEGE_NOTICE =
        "选课规则提醒：跨校区线下课通常不可互选（望京本院与燕郊城院线下课不可互选，中法跨校暂不支持），具体选课限制与年级要求请以当学期教务系统备注为准。"

    val categories: List<String> = listOf(
        "影视动画与游戏",
        "绘画造型与绘本",
        "工艺首饰与木艺",
        "空间建筑与材料",
        "潮流品牌与IP设计",
        "人文理论与策展"
    )

    private val rev1 = AnonymousPersona("cr_u01", "七号楼的松节油", 1101, IdentityType.STUDENT, true)
    private val rev2 = AnonymousPersona("cr_u02", "燕郊晚风速写", 2203, IdentityType.STUDENT, true)
    private val rev3 = AnonymousPersona("cr_u03", "北窗灰调子", 3307, IdentityType.STUDENT, true)
    private val rev4 = AnonymousPersona("cr_u04", "木工坊刨花", 4409, IdentityType.STUDENT, true)
    private val rev5 = AnonymousPersona("cr_u05", "动画系拉片人", 5519, IdentityType.STUDENT, true)
    private val rev6 = AnonymousPersona("cr_u06", "城院早八人", 6613, IdentityType.STUDENT, true)
    private val rev7 = AnonymousPersona("cr_u07", "美术馆慢门", 7723, IdentityType.ALUMNI, true)
    private val rev8 = AnonymousPersona("cr_u08", "望京卡纸切割机", 8831, IdentityType.STUDENT, true)

    val courses: List<CourseRecord> = listOf(
        // ==================== 一、影视动画与游戏 ====================
        CourseRecord(
            id = "course_styy",
            code = "CIT-AV01",
            name = "视听语言",
            instructor = "李炫",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "影视动画与游戏",
            courseType = "跨院选修",
            workloadScore = 2.8f,
            difficultyScore = 4.7f,
            recommendScore = 4.9f,
            suitableFor = "适合进作品集",
            summary = "游戏与影视圈资深大佬授课，课堂拉片干货极多，结课画故事板/分镜，作业量适中偏少、老师温柔负责。",
            reviews = listOf(
                CourseReview(
                    id = "r_styy_1",
                    author = rev5,
                    semester = "跨院选修",
                    content = "喜欢漫画、动画和影视分镜的无脑冲！老师北影毕业、游戏圈在职大佬，上课带着拉片看电影全是干货，结课作业是画故事板，作业量适中偏少不肝，老师超级温柔负责。",
                    workload = 2,
                    difficulty = 5,
                    recommend = 5,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "适合进作品集",
                    createdAt = "已整理",
                    helpfulCount = 46
                ),
                CourseReview(
                    id = "r_styy_2",
                    author = rev2,
                    semester = "跨院选修",
                    content = "讲各种分镜与视听语言专业知识，想进游戏行业或对电影动画感兴趣必冲！作业是画分镜镜头，只要相对认真画完老师完全不会为难，非常包容；纯想划水的就不太建议。",
                    workload = 3,
                    difficulty = 5,
                    recommend = 5,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "硬核高收获",
                    createdAt = "已整理",
                    helpfulCount = 34
                ),
                CourseReview(
                    id = "r_styy_3",
                    author = rev3,
                    semester = "跨院选修",
                    content = "专业能力很硬核，不过在学生里的评价略微两极分化：老师比较倾向引导大家按他的专业标准来，喜欢高标准指导的会非常喜欢，不喜欢被框住的可能会觉得不够自由。",
                    workload = 3,
                    difficulty = 4,
                    recommend = 4,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "硬核高收获",
                    createdAt = "已整理",
                    helpfulCount = 19
                )
            )
        ),
        CourseRecord(
            id = "course_xcpjj",
            code = "CIT-AV02",
            name = "现成品剪辑",
            instructor = "宋松",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "影视动画与游戏",
            courseType = "跨院选修",
            workloadScore = 2.0f,
            difficultyScore = 3.8f,
            recommendScore = 4.8f,
            suitableFor = "跨专业友好",
            summary = "网上找现成素材剪辑视频，题材完全自由、不主张肝；宋老师和蔼好玩，零基础友好，给分偏平均但用心做能拿高分。",
            reviews = listOf(
                CourseReview(
                    id = "r_xcpjj_1",
                    author = rev1,
                    semester = "跨院选修",
                    content = "宋松老师人超好、很和蔼，上课让大家做作品主要是开心且能学到东西，不咋肝也不主张硬肝。零基础不会累，我当时零基础去也很受照顾。",
                    workload = 2,
                    difficulty = 4,
                    recommend = 5,
                    attendanceMode = "不点名",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 31
                ),
                CourseReview(
                    id = "r_xcpjj_2",
                    author = rev6,
                    semester = "跨院选修",
                    content = "剪辑方向不固定非常自由，接受一切题材！找自己感兴趣的内容认真剪一个视频就能拿高分，老师还会提很细致的修改建议，能聊很多让人很放松。",
                    workload = 2,
                    difficulty = 4,
                    recommend = 5,
                    attendanceMode = "不点名",
                    examFormat = "作品汇报",
                    suitableTag = "轻松拿学分",
                    createdAt = "已整理",
                    helpfulCount = 24
                ),
                CourseReview(
                    id = "r_xcpjj_3",
                    author = rev8,
                    semester = "跨院选修",
                    content = "课很轻松，就是在网上搜集现成影像素材做剪辑创作。整体给分比较平均、不算特别高，但只要认真跟着做也不会特别低。",
                    workload = 2,
                    difficulty = 3,
                    recommend = 4,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "轻松拿学分",
                    createdAt = "已整理",
                    helpfulCount = 17
                )
            )
        ),
        CourseRecord(
            id = "course_dgdh",
            code = "CIT-AV03",
            name = "定格动画",
            instructor = "尹刚",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "影视动画与游戏",
            courseType = "跨院选修",
            workloadScore = 3.5f,
            difficultyScore = 4.5f,
            recommendScore = 4.8f,
            suitableFor = "跨专业友好",
            summary = "和蔼有趣的老教授授课，喜欢手工制作体验极佳；拍摄定格动画需要耐心动手，给分合理偏高，口碑极好。",
            reviews = listOf(
                CourseReview(
                    id = "r_dgdh_1",
                    author = rev4,
                    semester = "跨院选修",
                    content = "尹老师超级好，是个有趣又和蔼的老教授，爱跟学生唠嗑也用心讲知识，快退休了还能选到他的课特别难得，强烈推荐！",
                    workload = 3,
                    difficulty = 5,
                    recommend = 5,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 29
                ),
                CourseReview(
                    id = "r_dgdh_2",
                    author = rev2,
                    semester = "跨院选修",
                    content = "课程很好玩，主要靠自己动手制作与逐帧拍摄，拍定格的过程会稍微有点麻烦。喜欢手工的同学强烈建议选，打分很公正，做得好一定高分。",
                    workload = 4,
                    difficulty = 4,
                    recommend = 5,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "适合进作品集",
                    createdAt = "已整理",
                    helpfulCount = 21
                )
            )
        ),
        CourseRecord(
            id = "course_dmxc",
            code = "CIT-AV04",
            name = "动漫乡村",
            instructor = "李礼 / 朱崇轩",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "影视动画与游戏",
            courseType = "跨院选修",
            workloadScore = 2.5f,
            difficultyScore = 3.7f,
            recommendScore = 3.7f,
            suitableFor = "轻松拿学分",
            summary = "朱崇轩老师佛系温和、沟通友好；两位老师评价略有差异，整体压力较小，想深入学习可多主动找老师交流。",
            reviews = listOf(
                CourseReview(
                    id = "r_dmxc_1",
                    author = rev6,
                    semester = "跨院选修",
                    content = "朱崇轩老师人很好、很佛系也很温和，像长辈一样好说话，想学东西可以多主动找他；虽然有时不太善言辞，但不太会卡人，给分中规中矩（有次作业漏交也给了 80）。",
                    workload = 2,
                    difficulty = 4,
                    recommend = 4,
                    attendanceMode = "不点名",
                    examFormat = "作品汇报",
                    suitableTag = "轻松拿学分",
                    createdAt = "已整理",
                    helpfulCount = 18
                ),
                CourseReview(
                    id = "r_dmxc_2",
                    author = rev3,
                    semester = "跨院选修",
                    content = "李礼老师评价有些分歧：有同学觉得李礼老师人还行、给分不错，也有同学建议优先选朱老师班，选课前可以留意一下具体分组。",
                    workload = 3,
                    difficulty = 3,
                    recommend = 3,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 12
                )
            )
        ),
        CourseRecord(
            id = "course_sygc",
            code = "CIT-AV05",
            name = "摄影观察",
            instructor = "待补充（水课推荐）",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "影视动画与游戏",
            courseType = "跨院选修",
            workloadScore = 1.5f,
            difficultyScore = 3.0f,
            recommendScore = 3.6f,
            suitableFor = "水课",
            summary = "同学们在求水课推荐中提到的轻松课程：小组出勤压力低、后期上课时间灵活，但最终给分略显玄学。",
            reviews = listOf(
                CourseReview(
                    id = "r_sygc_1",
                    author = rev8,
                    semester = "跨院选修",
                    content = "摄影观察比较水，小组里有一个人去上课就可以，后期上课时间安排非常灵活，几乎不占用太多精力，不过最终给分比较玄学。",
                    workload = 1,
                    difficulty = 3,
                    recommend = 4,
                    attendanceMode = "不点名",
                    examFormat = "作品汇报",
                    suitableTag = "水课",
                    createdAt = "已整理",
                    helpfulCount = 22
                )
            )
        ),

        // ==================== 二、绘画造型与绘本 ====================
        CourseRecord(
            id = "course_hb",
            code = "CIT-DR01",
            name = "绘本",
            instructor = "冯旭",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "绘画造型与绘本",
            courseType = "跨院选修",
            workloadScore = 3.0f,
            difficultyScore = 4.2f,
            recommendScore = 4.8f,
            suitableFor = "跨专业友好",
            summary = "绘本方向人气口碑课！冯老师幽默专业、包容度极高，手绘板绘均可，微肝但上课非常快乐，抢课难度较高。",
            reviews = listOf(
                CourseReview(
                    id = "r_hb_1",
                    author = rev2,
                    semester = "跨院选修",
                    content = "各方面都特别好！冯老师很专业也很搞笑，包容度超大，手绘板绘都可以，给予学生的自由度很高。虽然稍微有点肝，但上课真的很快乐，是认真在搞创作的老师。",
                    workload = 3,
                    difficulty = 5,
                    recommend = 5,
                    attendanceMode = "线上签到",
                    examFormat = "作品汇报",
                    suitableTag = "适合进作品集",
                    createdAt = "已整理",
                    helpfulCount = 41
                ),
                CourseReview(
                    id = "r_hb_2",
                    author = rev7,
                    semester = "跨院选修",
                    content = "网课不是每节课都强制开摄像头，自愿成分较大，主要是第一节课和最后一节课会开。会基础儿童画/插画就能上手，给分整体偏中上（部分同学反馈高分、也有人觉得不算特别高），就是不太好抢。",
                    workload = 3,
                    difficulty = 4,
                    recommend = 5,
                    attendanceMode = "线上签到",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 27
                )
            )
        ),
        CourseRecord(
            id = "course_rwzx",
            code = "CIT-DR02",
            name = "人物造型基础与艺用解剖",
            instructor = "张姿",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "绘画造型与绘本",
            courseType = "跨院选修",
            workloadScore = 5.0f,
            difficultyScore = 4.2f,
            recommendScore = 3.5f,
            suitableFor = "硬核高收获",
            summary = "评价两极分化明显的硬核造型课：老师极度认真负责、认真交作业给分不错，但作业非常肝且教学风格偏强势严格。",
            reviews = listOf(
                CourseReview(
                    id = "r_rwzx_1",
                    author = rev3,
                    semester = "跨院选修",
                    content = "张姿老师将心比心地负责，也很护短，只是因为太负责容易显得强势。每次作业大概耗时 3-6 小时确实很肝，但认真学绝对有所得，完成要求给分挺高的。需要别人约束提高自制力或喜欢古典素描感的同学很适合。",
                    workload = 5,
                    difficulty = 5,
                    recommend = 4,
                    attendanceMode = "每节签到",
                    examFormat = "作品汇报",
                    suitableTag = "硬核高收获",
                    createdAt = "已整理",
                    helpfulCount = 35
                ),
                CourseReview(
                    id = "r_rwzx_2",
                    author = rev6,
                    semester = "跨院选修",
                    content = "外院同学普遍反映非常肝！老师风格偏严格强势，部分同学会觉得压迫感较强甚至有心理负担。如果你喜欢宽松自由的氛围或抗压心态偏敏感，一定要慎重考虑。",
                    workload = 5,
                    difficulty = 3,
                    recommend = 3,
                    attendanceMode = "每节签到",
                    examFormat = "作品汇报",
                    suitableTag = "慎选避雷",
                    createdAt = "已整理",
                    helpfulCount = 38
                )
            )
        ),
        CourseRecord(
            id = "course_swsm",
            code = "CIT-DR03",
            name = "稍微素描",
            instructor = "苏海江",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "绘画造型与绘本",
            courseType = "跨院选修",
            workloadScore = 5.0f,
            difficultyScore = 2.0f,
            recommendScore = 1.2f,
            suitableFor = "慎选避雷",
            summary = "高能预警！文档中城院同学一致强烈劝退的超高强度素描课，作业量极大、耗时极长且打分体验普遍不佳。",
            reviews = listOf(
                CourseReview(
                    id = "r_swsm_1",
                    author = rev6,
                    semester = "跨院选修",
                    content = "城院人公认的超高强度素描课，非常消耗精力与时间。除非你本身极其热爱高强度肝素描，否则真心建议放过自己、慎重选择。",
                    workload = 5,
                    difficulty = 2,
                    recommend = 1,
                    attendanceMode = "每节签到",
                    examFormat = "作品汇报",
                    suitableTag = "慎选避雷",
                    createdAt = "已整理",
                    helpfulCount = 64
                ),
                CourseReview(
                    id = "r_swsm_2",
                    author = rev2,
                    semester = "跨院选修",
                    content = "多位上过的同学一致反馈体验很痛苦，作业压力和打分标准都很令人崩溃，选前请务必做好充分的心理和时间准备。",
                    workload = 5,
                    difficulty = 2,
                    recommend = 1,
                    attendanceMode = "每节签到",
                    examFormat = "作品汇报",
                    suitableTag = "慎选避雷",
                    createdAt = "已整理",
                    helpfulCount = 51
                )
            )
        ),

        // ==================== 三、工艺首饰与木艺 ====================
        CourseRecord(
            id = "course_mysj",
            code = "CIT-CR01",
            name = "木艺与设计",
            instructor = "白云泽",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "工艺首饰与木艺",
            courseType = "跨院选修",
            workloadScore = 3.8f,
            difficultyScore = 4.7f,
            recommendScore = 4.7f,
            suitableFor = "跨专业友好",
            summary = "人气极高的手工木艺课！白老师温柔耐心、给分高且自由度好；纯手工削刻木头较费手，汇报次数稍多但要求宽松。",
            reviews = listOf(
                CourseReview(
                    id = "r_mysj_1",
                    author = rev4,
                    semester = "跨院选修",
                    content = "白老师人特别好，温柔耐心又可爱，还喜欢和学生唠嗑，闭眼选！自由度和肝度都很友好，打分非常不错；注：白老师是木工车间的专业课老师，不是车间师傅。",
                    workload = 3,
                    difficulty = 5,
                    recommend = 5,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 43
                ),
                CourseReview(
                    id = "r_mysj_2",
                    author = rev1,
                    semester = "跨院选修",
                    content = "纯手工制作体验，需要自己锯木头、刻木头做玩偶，优点是汇报随便讲一下 PPT 就过关、给分不错；缺点是汇报频次稍高，而且刻木头真的很费手、容易起茧或小划伤，手残党和力气小的同学要提前有心理准备。",
                    workload = 4,
                    difficulty = 5,
                    recommend = 4,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "适合进作品集",
                    createdAt = "已整理",
                    helpfulCount = 32
                )
            )
        ),
        CourseRecord(
            id = "course_ctgy",
            code = "CIT-CR02",
            name = "传统工艺",
            instructor = "贺佳",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "工艺首饰与木艺",
            courseType = "跨院选修",
            workloadScore = 4.0f,
            difficultyScore = 4.5f,
            recommendScore = 4.5f,
            suitableFor = "硬核高收获",
            summary = "贺老师非常温柔有趣、给分很高，但平时汇报和任务节点较多、略微偏肝，认真完成体验很棒。",
            reviews = listOf(
                CourseReview(
                    id = "r_ctgy_1",
                    author = rev7,
                    semester = "跨院选修",
                    content = "我上过，贺老师人很好、很温柔有趣，给分相当不错！就是课程流程稍微麻烦一点，平时汇报有点多，会觉得有点肝。",
                    workload = 4,
                    difficulty = 5,
                    recommend = 5,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "硬核高收获",
                    createdAt = "已整理",
                    helpfulCount = 23
                ),
                CourseReview(
                    id = "r_ctgy_2",
                    author = rev3,
                    semester = "跨院选修",
                    content = "老师人特别温柔，不过课程里的事儿确实不少，属于“温柔但扎实推进”的节奏，不怕麻烦的话很值得选。",
                    workload = 4,
                    difficulty = 4,
                    recommend = 4,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 15
                )
            )
        ),
        CourseRecord(
            id = "course_zzfs",
            code = "CIT-CR03",
            name = "织造方式研究",
            instructor = "杨华",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "工艺首饰与木艺",
            courseType = "跨院选修",
            workloadScore = 2.5f,
            difficultyScore = 3.8f,
            recommendScore = 3.8f,
            suitableFor = "轻松拿学分",
            summary = "主要设计编织结构，自由度高、多数同学觉得活少分高且佛系；部分学期反馈需节节课汇报进度并记考勤。",
            reviews = listOf(
                CourseReview(
                    id = "r_zzfs_1",
                    author = rev8,
                    semester = "跨院选修",
                    content = "杨华老师上课挺佛系，自由度蛮高，作业就是设计一种编织结构，活少分高、比较轻松！认真做并且多和老师积极沟通的话给分很高。",
                    workload = 2,
                    difficulty = 5,
                    recommend = 5,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "轻松拿学分",
                    createdAt = "已整理",
                    helpfulCount = 26
                ),
                CourseReview(
                    id = "r_zzfs_2",
                    author = rev6,
                    semester = "跨院选修",
                    content = "提示下不同学期要求可能有差异：去年上过时是每节课所有人都要汇报一遍进度，不来上课还会被记考勤，也有同学反馈那期给分偏低，选之前留意一下节奏。",
                    workload = 3,
                    difficulty = 3,
                    recommend = 3,
                    attendanceMode = "每节签到",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 18
                )
            )
        ),
        CourseRecord(
            id = "course_gjzb",
            code = "CIT-CR04",
            name = "高级珠宝的“天下”——从地理角度看高级珠宝",
            instructor = "余瑶",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "工艺首饰与木艺",
            courseType = "跨院选修",
            workloadScore = 3.0f,
            difficultyScore = 4.0f,
            recommendScore = 4.0f,
            suitableFor = "跨专业友好",
            summary = "余瑶老师口碑很好，人温柔随和；该门课具体给分与作业量目前样本较少，欢迎上过的同学继续补充。",
            reviews = listOf(
                CourseReview(
                    id = "r_gjzb_1",
                    author = rev2,
                    semester = "跨院选修",
                    content = "上过余瑶老师别的课，老师人非常温柔、随和、好沟通！这门从地理角度看高级珠宝的新课目前打分细节不多，期待上过的同学补充。",
                    workload = 3,
                    difficulty = 4,
                    recommend = 4,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 14
                )
            )
        ),

        // ==================== 四、空间建筑与材料 ====================
        CourseRecord(
            id = "course_kjmh",
            code = "CIT-SP01",
            name = "空间盲盒",
            instructor = "张欣",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "空间建筑与材料",
            courseType = "跨院选修",
            workloadScore = 2.0f,
            difficultyScore = 4.2f,
            recommendScore = 4.5f,
            suitableFor = "水课",
            summary = "创意导向的轻松好课！不拼工作量只看想法是否有趣，适合喜欢实验艺术与当代艺术的同学，老师超好。",
            reviews = listOf(
                CourseReview(
                    id = "r_kjmh_1",
                    author = rev5,
                    semester = "跨院选修",
                    content = "老师非常有趣、人超好！喜欢实验艺术和当代艺术的可以冲。课程很轻松不肝，老师看重的不是工作量多大，而是方案本身有没有意思，去年大家都比较轻松、给分也不错。",
                    workload = 2,
                    difficulty = 4,
                    recommend = 5,
                    attendanceMode = "不点名",
                    examFormat = "作品汇报",
                    suitableTag = "水课",
                    createdAt = "已整理",
                    helpfulCount = 25
                )
            )
        ),
        CourseRecord(
            id = "course_kjzg",
            code = "CIT-SP02",
            name = "空间建构",
            instructor = "马华园",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "空间建筑与材料",
            courseType = "跨院选修",
            workloadScore = 5.0f,
            difficultyScore = 2.5f,
            recommendScore = 1.8f,
            suitableFor = "慎选避雷",
            summary = "多数同学劝退：作业多且临近结课仍易加量、方案需反复修改，上课时间常改至周末或晚间；少数喜欢发散思路、主动沟通的同学尚可适应。",
            reviews = listOf(
                CourseReview(
                    id = "r_kjzg_1",
                    author = rev6,
                    semester = "跨院选修",
                    content = "空间五工（原空间三工）老师，选前慎重！经常不在正常白天时间上课，改到周日或晚间补课，微信回复也不太及时。方案反复改很多遍，临近结课还容易临时加作业，节奏偏混乱。",
                    workload = 5,
                    difficulty = 2,
                    recommend = 1,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "慎选避雷",
                    createdAt = "已整理",
                    helpfulCount = 44
                ),
                CourseReview(
                    id = "r_kjzg_2",
                    author = rev3,
                    semester = "跨院选修",
                    content = "老师讲解风格比较抽象跳跃，有时不太直接给明确答案，还容易延迟结课。不过也有人喜欢，如果你非常有自己独立想法、愿意多找老师深入交流的话就没问题。",
                    workload = 5,
                    difficulty = 3,
                    recommend = 2,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "慎选避雷",
                    createdAt = "已整理",
                    helpfulCount = 28
                )
            )
        ),
        CourseRecord(
            id = "course_ysgz",
            code = "CIT-SP03",
            name = "艺术构造",
            instructor = "钱亮",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "空间建筑与材料",
            courseType = "跨院选修",
            workloadScore = 3.5f,
            difficultyScore = 2.5f,
            recommendScore = 2.0f,
            suitableFor = "慎选避雷",
            summary = "老师非常爱讲谐音梗，文档中有上过的同学直接标注避雷，建议选课前多向同院学长打听具体作业要求。",
            reviews = listOf(
                CourseReview(
                    id = "r_ysgz_1",
                    author = rev8,
                    semester = "跨院选修",
                    content = "钱老师是最爱讲谐音梗的人，但表格里多位同学直接给出了避雷提示，建议谨慎选择或先了解清楚具体考核方式。",
                    workload = 4,
                    difficulty = 2,
                    recommend = 2,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "慎选避雷",
                    createdAt = "已整理",
                    helpfulCount = 16
                )
            )
        ),
        CourseRecord(
            id = "course_mhtsd",
            code = "CIT-SP04",
            name = "梦幻“砼”时代",
            instructor = "张欣",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "空间建筑与材料",
            courseType = "跨院选修",
            workloadScore = 0f,
            difficultyScore = 0f,
            recommendScore = 0f,
            suitableFor = "待补充评价",
            summary = "混凝土材料相关跨院选修课，文档反馈去年未能成功开课，目前暂无实测打分，欢迎新开课后补充体验。",
            reviews = emptyList()
        ),

        // ==================== 五、潮流品牌与IP设计 ====================
        CourseRecord(
            id = "course_cpp",
            code = "CIT-BR01",
            name = "潮品牌研究与创新图形",
            instructor = "胡雪琴",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "潮流品牌与IP设计",
            courseType = "跨院选修",
            workloadScore = 4.5f,
            difficultyScore = 4.6f,
            recommendScore = 4.8f,
            suitableFor = "硬核高收获",
            summary = "含金量极高的前沿品牌图形课！胡老师温柔爱夸人、不常点名（轮到汇报需到场），虽然作业多偏肝，但收获大且给分高。",
            reviews = listOf(
                CourseReview(
                    id = "r_cpp_1",
                    author = rev1,
                    semester = "跨院选修",
                    content = "老师超棒、很温柔还经常夸学生！课程内容非常前沿，含金量很高，能学到实打实的干货。虽然课很肝，但分不低且特别有收获感，看到这门课快选！",
                    workload = 5,
                    difficulty = 5,
                    recommend = 5,
                    attendanceMode = "不点名",
                    examFormat = "作品汇报",
                    suitableTag = "硬核高收获",
                    createdAt = "已整理",
                    helpfulCount = 33
                ),
                CourseReview(
                    id = "r_cpp_2",
                    author = rev7,
                    semester = "跨院选修",
                    content = "基本不点名，但轮到自己 pre 汇报那天一定要去。一学年大概会有七八次作业，节奏紧凑，注意查看教务系统课程备注（部分学期可能限城院大三选课）。",
                    workload = 4,
                    difficulty = 4,
                    recommend = 5,
                    attendanceMode = "不点名",
                    examFormat = "作品汇报",
                    suitableTag = "适合进作品集",
                    createdAt = "已整理",
                    helpfulCount = 24
                )
            )
        ),
        CourseRecord(
            id = "course_ipxx",
            code = "CIT-BR02",
            name = "IP形象设计与衍生品开发",
            instructor = "冯薇",
            campus = "燕郊校区",
            college = "城市设计学院",
            major = "潮流品牌与IP设计",
            courseType = "跨院选修",
            workloadScore = 4.0f,
            difficultyScore = 3.5f,
            recommendScore = 3.5f,
            suitableFor = "硬核高收获",
            summary = "偏向真实项目实践的 IP 衍生开发课，不水，往期课程会结合老师承接的实际项目推进。",
            reviews = listOf(
                CourseReview(
                    id = "r_ipxx_1",
                    author = rev2,
                    semester = "跨院选修",
                    content = "不水！上次开课是直接跟着做老师接的实际商业/设计项目，有一定工作量，想攒落地项目经验的同学可以考虑。",
                    workload = 4,
                    difficulty = 4,
                    recommend = 4,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "硬核高收获",
                    createdAt = "已整理",
                    helpfulCount = 15
                )
            )
        ),

        // ==================== 六、人文理论与策展 ====================
        CourseRecord(
            id = "course_yszl",
            code = "AMG-TH01",
            name = "艺术治疗的多场景实践",
            instructor = "康俐",
            campus = "望京本院",
            college = "艺术管理与教育学院",
            major = "人文理论与策展",
            courseType = "跨院选修",
            workloadScore = 3.0f,
            difficultyScore = 4.0f,
            recommendScore = 4.0f,
            suitableFor = "跨专业友好",
            summary = "艺管院开设的跨学科实践课，对艺管以外的跨专业同学比较友好，最终打分可达 85+。",
            reviews = listOf(
                CourseReview(
                    id = "r_yszl_1",
                    author = rev8,
                    semester = "跨院选修",
                    content = "对除艺管以外的其他专业同学应该挺友好的，跨专业听起来没有太高门槛，老师打分还可以，认真完成基本能拿到 85+。",
                    workload = 3,
                    difficulty = 4,
                    recommend = 4,
                    attendanceMode = "偶尔抽查",
                    examFormat = "作品汇报",
                    suitableTag = "跨专业友好",
                    createdAt = "已整理",
                    helpfulCount = 19
                )
            )
        ),
        CourseRecord(
            id = "course_xms",
            code = "HUM-TH02",
            name = "西美史",
            instructor = "邵亦扬",
            campus = "望京本院",
            college = "人文学院",
            major = "人文理论与策展",
            courseType = "跨院选修",
            workloadScore = 0f,
            difficultyScore = 0f,
            recommendScore = 0f,
            suitableFor = "待补充评价",
            summary = "人文学院邵亦扬教授西方美术史课程，已收录至跨院选修清单，欢迎上过的同学补充考勤与给分体验。",
            reviews = emptyList()
        )
    )
}
