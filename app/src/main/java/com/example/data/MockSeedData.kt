package com.example.data

import com.example.R

object MockSeedData {

    val cafaCampusColleges: Map<String, List<String>> = linkedMapOf(
        "望京本院" to listOf(
            "中国画学院",
            "书法学院",
            "实验艺术与科技艺术学院",
            "人文学院",
            "设计学院",
            "建筑学院",
            "艺术管理与教育学院",
            "修复学院",
            "油画系",
            "版画系",
            "雕塑系",
            "壁画系",
            "造型学科基础部",
            "研究生院",
            "马克思主义学院"
        ),
        "燕郊校区" to listOf(
            "城市设计学院",
            "继续教育学院"
        ),
        "上海校区" to listOf(
            "(中法)艺术与设计管理学院"
        )
    )

    val cafaCampuses: List<String> = cafaCampusColleges.keys.toList()

    // Exact canonical order of all 18 CAFA colleges & departments
    val cafaColleges: List<String> = listOf(
        "中国画学院",
        "书法学院",
        "实验艺术与科技艺术学院",
        "人文学院",
        "设计学院",
        "建筑学院",
        "城市设计学院",
        "艺术管理与教育学院",
        "(中法)艺术与设计管理学院",
        "修复学院",
        "油画系",
        "版画系",
        "雕塑系",
        "壁画系",
        "造型学科基础部",
        "研究生院",
        "继续教育学院",
        "马克思主义学院"
    )

    fun campusForCollege(college: String): String =
        cafaCampusColleges.entries.firstOrNull { (_, list) -> list.contains(college) }?.key ?: "望京本院"

    val cafaLocationPresets: List<String> = listOf(
        "央美望京校区",
        "5号楼教学楼",
        "7号楼设计楼",
        "央美美术馆",
        "电教楼报告厅",
        "14号楼雕塑系",
        "北苑餐厅",
        "花家地南街",
        "央美燕郊校区",
        "城市设计学院楼",
        "央美上海校区（中法）",
        "798艺术区"
    )

    val courseTypes = listOf("跨院选修", "公共选修", "体育选修")
    val courseCategories = CrossCollegeCourseData.categories
    val courseAttendanceOptions = listOf("不点名", "偶尔抽查", "线上签到", "每节签到")
    val courseExamOptions = listOf("作品汇报", "期末展览", "课程论文")
    val courseSuitableOptions = listOf("水课", "跨专业友好", "适合进作品集", "轻松拿学分", "硬核高收获", "慎选避雷")
    val seniorCohortOptions = listOf("2026届", "2025届", "2024届", "2023届", "2022届", "2021届", "2020届及以前")

    val anonymousNicknamePool: List<String> = AnonymousNicknameLexicon.curatedClassics

    val treeHoleMoods = listOf("开心", "焦虑", "迷茫", "生气", "失落", "想念", "无聊", "求安慰")
    val treeHoleComfortOptions = listOf("抱抱", "我也一样", "给你一点建议", "陪你聊聊")
    val fixedPublishTags = listOf(
        "#情绪树洞",
        "#恋爱情感",
        "#央美选课",
        "#校园日常",
        "#作品分享",
        "#作业DDL",
        "#看展打卡",
        "#闲置画材",
        "#求助互助",
        "#毕业求职"
    )

    private val p1 = AnonymousPersona("u_01", "七号楼的松节油", 1013, IdentityType.STUDENT, true)
    private val p2 = AnonymousPersona("u_02", "切卡纸切到天亮", 2027, IdentityType.STUDENT, true)
    private val p3 = AnonymousPersona("u_03", "灰砖墙下的慢门", 3041, IdentityType.ALUMNI, true)
    private val p4 = AnonymousPersona("u_04", "无题第14号草图", 4057, IdentityType.STUDENT, true)
    private val p5 = AnonymousPersona("u_05", "美术馆地下一层", 5077, IdentityType.STUDENT, true)
    private val p6 = AnonymousPersona("u_06", "留白处的炭条", 6089, IdentityType.ALUMNI, true)
    private val p7 = AnonymousPersona("u_07", "亚麻布底料", 7103, IdentityType.STUDENT, true)
    private val p8 = AnonymousPersona("u_08", "望京阴天散步", 8117, IdentityType.STUDENT, true)

    val todaySnapshot = TodayCampusSnapshot(
        dateLabel = "NOV 21 / CAFA",
        newDiscussions = 148,
        hotPostTitle = "美术馆冬季材料展布展现场：那种灰调子的空间光线真的绝了",
        hotPostId = "post_01",
        campusBrief = "今晚 19:00 电教楼报告厅当代排版讲座 · 7号楼西侧通道临时施工绕行"
    )

    val initialHotTopics = listOf(
        HotTopic(
            id = "topic_02",
            indexCode = "01",
            tag = "#今天又被作业逼疯了",
            subtitleEn = "STUDIO DEADLINE",
            description = "凌晨四点的模型台、怎么调都不对的灰度与反复推翻的方案。",
            postCount = 214,
            discussionCount = 13900,
            heatStatus = "沸",
            darkPoster = true
        ),
        HotTopic(
            id = "topic_01",
            indexCode = "02",
            tag = "#央美选课",
            subtitleEn = "COURSE SELECTION",
            description = "跨院选修避雷与宝藏公选课推荐，作业量与给分真实体验。",
            postCount = 186,
            discussionCount = 11200,
            heatStatus = "热",
            darkPoster = false
        ),
        HotTopic(
            id = "topic_03",
            indexCode = "03",
            tag = "#美术馆冬季材料展",
            subtitleEn = "EXHIBITION RADAR",
            description = "美术馆新展、地下室快闪与各工作室结课汇报展打卡。",
            postCount = 129,
            discussionCount = 9640,
            heatStatus = "新",
            darkPoster = true
        ),
        HotTopic(
            id = "topic_04",
            indexCode = "04",
            tag = "#毕业之后",
            subtitleEn = "AFTER GRADUATION",
            description = "离开花家地之后，校友们在独立创作、工作室与留学路上的坐标。",
            postCount = 163,
            discussionCount = 8750,
            heatStatus = "热",
            darkPoster = false
        ),
        HotTopic(
            id = "topic_07",
            indexCode = "05",
            tag = "#花家地食堂新窗口测评",
            subtitleEn = "CANTEEN REVIEW",
            description = "食堂二楼新开的窗口到底值不值得排队，真实测评汇总。",
            postCount = 94,
            discussionCount = 7420,
            heatStatus = "新",
            darkPoster = false
        ),
        HotTopic(
            id = "topic_05",
            indexCode = "06",
            tag = "#艺术生就业",
            subtitleEn = "ART CAREERS",
            description = "作品集反馈、独立工作室薪资透明度与策展行业一线观察。",
            postCount = 97,
            discussionCount = 6180,
            heatStatus = "荐",
            darkPoster = true
        ),
        HotTopic(
            id = "topic_06",
            indexCode = "07",
            tag = "#宿舍生活",
            subtitleEn = "DORM LIFE",
            description = "画框收纳、松节油通风与望京周边生活实用指南。",
            postCount = 112,
            discussionCount = 5430,
            heatStatus = "",
            darkPoster = true
        ),
        HotTopic(
            id = "topic_08",
            indexCode = "08",
            tag = "#情绪树洞深夜电台",
            subtitleEn = "LATE NIGHT TREE HOLE",
            description = "给疲惫的夜晚留一个温柔、不被打扰的树洞角落。",
            postCount = 158,
            discussionCount = 4890,
            heatStatus = "热",
            darkPoster = true
        ),
        HotTopic(
            id = "topic_09",
            indexCode = "09",
            tag = "#画材闲置回血",
            subtitleEn = "ART SUPPLIES",
            description = "结课用不完的颜料、亚麻布、木框与相机校内低价转。",
            postCount = 83,
            discussionCount = 3920,
            heatStatus = "",
            darkPoster = false
        ),
        HotTopic(
            id = "topic_10",
            indexCode = "10",
            tag = "#毕设进度条打卡",
            subtitleEn = "THESIS PROGRESS",
            description = "每天记录一点毕设进展，互相监督绝不烂尾。",
            postCount = 76,
            discussionCount = 3150,
            heatStatus = "新",
            darkPoster = false
        )
    )

    val initialPosts = listOf(
        Post(
            id = "post_01",
            indexCode = "NO.0412",
            title = "美术馆冬季材料展布展现场：那种灰调子的空间光线真的绝了",
            body = "下午路过美术馆二层，刚好碰上装置组的同学在调最后的顶光。半透明纤维织物悬在清水混凝土墙面之前，下午三点半的斜阳一打进来，整个空间像一块缓缓流动的灰色切片。\n\n明天上午十点正式对校内开放，强烈建议带速写本或胶片机在工作日下午去，人少且光影层次最干净。",
            author = p5,
            category = PostCategory.EXHIBITION,
            layoutType = PostLayoutType.IMAGE_FIRST,
            topicTags = listOf("#校园展览", "#艺术创作"),
            mediaList = listOf(
                PostMedia("m1", drawableRes = R.drawable.img_cafa_exhibition, aspectRatio = 4f / 3f, caption = "美术馆二层展厅 · 下午三点光影"),
                PostMedia("m2", drawableRes = R.drawable.img_campus_architecture, aspectRatio = 16f / 9f, caption = "展馆外立面灰砖与阴天")
            ),
            createdAt = "28分钟前",
            viewCount = 1286,
            likeCount = 94,
            bookmarkCount = 41,
            comments = listOf(
                Comment("c1", "post_01", p1, "刚去完回来！二层转角那个石膏与麻布的拼接太迷人了，站着看了二十分钟。", "15分钟前", likes = 12),
                Comment("c2", "post_01", p4, "谢谢repo！请问展期持续到下周五吗？想赶完这周模型再去。", "9分钟前", likes = 4, replyToNickname = "美术馆地下一层"),
                Comment("c2_op", "post_01", p5, "展期持续到 12 月 10 号哦，下周五完全来得及，推荐工作日下午三点左右去光线最通透！", "5分钟前", likes = 9, replyToNickname = "无题第14号草图")
            ),
            location = "中央美术学院美术馆 2F",
            eventTime = "11.22 — 12.10"
        ),
        Post(
            id = "post_02",
            indexCode = "NO.0411",
            title = "毕业三年后再回看在央美熬过的那些夜：原来最珍贵的不是画得像，而是敢推翻重来",
            body = "大四做毕设那会儿，导师在倒数第三周指着我画了两个月的整面墙说：「太顺手了，太安全了，你只是在重复你已经会的东西。」那天晚上我在七号楼楼梯间坐到保安锁门，气得想直接退学。\n\n但工作三年、在甲方和算法审美之间来回拉扯之后，才突然明白当年那种被逼着打破惯性的痛苦有多奢侈。致还在工作室和方案死磕的学弟学妹：允许自己画出“丑”的草稿，那是真正开始生长的地方。",
            author = p3,
            category = PostCategory.SENIOR_EXPERIENCE,
            layoutType = PostLayoutType.EDITORIAL_TEXT,
            topicTags = listOf("#毕业之后", "#艺术生就业"),
            createdAt = "1小时前",
            viewCount = 2430,
            likeCount = 186,
            bookmarkCount = 112,
            comments = listOf(
                Comment("c3", "post_02", p2, "正在对着一桌废卡纸怀疑人生，刷到这段话瞬间稳住了呼吸。", "42分钟前", likes = 27),
                Comment("c4", "post_02", p6, "太真实了。离开学校后很少有人会认真花一小时批评你的作品结构了。", "20分钟前", likes = 19)
            )
        ),
        Post(
            id = "post_03",
            indexCode = "NO.0410",
            title = "【出闲置】温莎牛顿艺术家级油画颜料套装 + 猪鬃笔刷与调色刀",
            body = "这学期转向综合材料与影像，去年囤的一整套温莎牛顿颜料和几把顺手的猪鬃画笔基本没怎么动（钛白用了三分之一，其余几乎全新）。附送一本未拆封的细纹水彩速写本。\n\n仅限花家地校内面交，美术馆门口或七号楼楼下都可，同院学弟妹来拿可以小刀或换胶片卷。",
            author = p7,
            category = PostCategory.MARKETPLACE,
            layoutType = PostLayoutType.MARKETPLACE_ITEM,
            topicTags = listOf("#闲置交换", "#画材"),
            mediaList = listOf(
                PostMedia("m3", drawableRes = R.drawable.img_art_supplies, aspectRatio = 4f / 3f, caption = "实拍清单，九成新")
            ),
            createdAt = "2小时前",
            viewCount = 618,
            likeCount = 29,
            bookmarkCount = 34,
            price = "¥ 240",
            tradeStatus = "可小刀 / 可换物",
            location = "花家地校区 · 7号楼",
            comments = listOf(
                Comment("c5", "post_03", p1, "请问调色刀和那几支长杆猪鬃单出吗？明天中午可在七号楼下自提。", "1小时前", likes = 2)
            )
        ),
        Post(
            id = "post_05",
            indexCode = "NO.0408",
            title = "北窗阴天的第三遍罩染，灰蓝和暖灰终于咬合上了",
            body = "连续画废了两张 120×160 的麻布，今天索性停下来把画面刮掉一半，只留下底层粗糙的亚麻肌理和几块克制的冷灰。突然发现少画一点，画面的呼吸感反而出来了。记录一下下午四点半的工作室角落。",
            author = p1,
            category = PostCategory.ART_CREATION,
            layoutType = PostLayoutType.IMAGE_FIRST,
            topicTags = listOf("#艺术创作", "#今天又被作业逼疯了"),
            mediaList = listOf(
                PostMedia("m4", drawableRes = R.drawable.img_studio_painting, aspectRatio = 3f / 4f, caption = "120×160cm · 布面油画与炭粉，进行中")
            ),
            createdAt = "4小时前",
            viewCount = 1892,
            likeCount = 142,
            bookmarkCount = 67,
            comments = listOf(
                Comment("c7", "post_05", p7, "那个留白和刮刀肌理的张力好棒！阴天北窗光永远是最诚实的光。", "3小时前", likes = 11)
            )
        ),
        Post(
            id = "post_06",
            indexCode = "NO.0407",
            title = "",
            body = "其实很羡慕那些一进工作室就能立刻进入心流的人。每次交阶段汇报前我都觉得自己像个混进来的冒牌货，看着旁边同学的作品那么笃定，我却连最基本的构图都在反复自我否定。今晚不想硬撑了，先去操场走两圈吹吹冷风。",
            author = p8,
            category = PostCategory.TREE_HOLE,
            layoutType = PostLayoutType.TREE_HOLE_MOOD,
            topicTags = listOf("#今天又被作业逼疯了"),
            moodTag = "迷茫",
            createdAt = "5小时前",
            viewCount = 945,
            likeCount = 78,
            bookmarkCount = 19,
            comments = listOf(
                Comment("c8", "post_06", p3, "在央美几乎每个人都经历过冒牌货综合征，只是大家展墙上都只挂了最后成功的那一张。吹吹风，明天会轻一点。", "4小时前", likes = 31, comfortTag = "抱抱"),
                Comment("c9", "post_06", p2, "刚从操场走完三圈回来，冷风对降温焦虑特别有效。", "3小时前", likes = 14, comfortTag = "我也一样")
            )
        ),
        Post(
            id = "post_07",
            indexCode = "NO.0406",
            title = "【急寻】黑色硬壳 A5 速写本，可能落在电教楼 302 或食堂二楼窗边",
            body = "封面左下角贴了一张灰色美纹纸标签，里面有我这两周做毕业展前期调研的几十页草图和采访笔记，对我特别重要！如果哪位同学捡到了麻烦评论区留言一下，请喝咖啡/奶茶表达感谢！",
            author = p2,
            category = PostCategory.LOST_FOUND,
            layoutType = PostLayoutType.HELP_REQUEST,
            topicTags = listOf("#寻物", "#校园互助"),
            location = "电教楼 302 / 食堂 2F",
            eventTime = "今日 12:30 左右",
            createdAt = "5小时前",
            viewCount = 732,
            likeCount = 35,
            bookmarkCount = 8,
            comments = listOf(
                Comment("c10", "post_07", p5, "帮顶！刚下课路过 302 帮你看了一眼讲台旁边，物业阿姨说下午收了一本黑皮本子放一楼值班室了，快去问问！", "2小时前", likes = 22)
            )
        ),
        Post(
            id = "post_09",
            indexCode = "NO.0404",
            title = "周六上午拼车去燕郊建材市场买木龙骨和亚麻布，还差 2 位同学",
            body = "已经约好熟悉的金杯师傅，早上 9:00 从北门出发，大概中午 1 点前返回花家地。车费 AA 人均 35 左右，顺路还可以一起买画框条。要去的话评论区留个言我统一拉拼车接龙。",
            author = p7,
            category = PostCategory.MUTUAL_AID,
            layoutType = PostLayoutType.HELP_REQUEST,
            topicTags = listOf("#校园互助", "#宿舍生活"),
            location = "花家地北门集合",
            eventTime = "本周六 09:00",
            createdAt = "昨天",
            viewCount = 486,
            likeCount = 21,
            bookmarkCount = 15,
            comments = emptyList()
        ),
        Post(
            id = "post_10",
            indexCode = "NO.0403",
            title = "在央美谈恋爱是不是都自带“陪你刷白墙”属性？",
            body = "昨天晚上十一点被对象拉去雕塑系工作室帮忙打磨石膏底座，两个人都满身白灰，最后蹲在楼道里分了一盒凉掉的炒河粉。虽然手指磨得发酸，但那一刻觉得灰头土脸的校园生活也挺浪漫的。",
            author = p8,
            category = PostCategory.RELATIONSHIP,
            layoutType = PostLayoutType.EDITORIAL_TEXT,
            topicTags = listOf("#宿舍生活"),
            createdAt = "昨天",
            viewCount = 1674,
            likeCount = 132,
            bookmarkCount = 24,
            comments = listOf(
                Comment("c11", "post_10", p1, "是的，央美最高级别的浪漫就是布展前夜帮你拧膨胀螺丝。", "18小时前", likes = 45)
            )
        )
    )

    val initialCourses: List<CourseRecord> = CrossCollegeCourseData.courses

    val initialExperiences = listOf(
        ExperienceArticle(
            id = "exp_01",
            indexCode = "ARCH // 01",
            title = "皇艺 RCA & 伦艺 CSM 视传与交互双 Offer：我的作品集叙事重构笔记",
            summary = "不堆砌炫技渲染，如何把央美日常的田野调研与平面实验，整理成考官三分钟就能看懂的逻辑骨架。",
            body = "很多央美同学申海外名校时最大的优势其实是扎实的材料感与概念深度，而最大的短板往往是“只放了最终成品图，省略了推演过程”。\n\n1. 项目排序策略：第一个项目放最具个人方法论代表性的实验性课题，不要放最商业的作业。\n2. 过程视觉化：把草图、废稿、失败的印刷打样、和导师讨论的批注扫描进去，考官极看重迭代轨迹。\n3. 排版克制：整本作品集只用两套字号体系和统一边距网格，让作品本身说话，不要让装饰边框抢戏。\n4. 面试准备：针对每个项目准备 90 秒英文电梯陈述，重点讲清“为什么这个问题只能用这种视觉媒介来回答”。",
            category = ExperienceCategory.STUDY_ABROAD,
            college = "设计学院",
            major = "视觉传达设计",
            year = "2025届",
            tags = listOf("作品集", "RCA", "排版方法论"),
            author = p3,
            media = PostMedia("em1", drawableRes = R.drawable.img_campus_architecture, aspectRatio = 16f / 9f),
            readMinutes = 7,
            likeCount = 214,
            bookmarkCount = 198,
            comments = listOf(
                Comment("ec1", "exp_01", p4, "第 2 点太受用了，之前一直把试错草稿全藏起来了，明天立刻重新扫描进过程页！", "2天前", likes = 16, cohortLabel = "2025届")
            )
        ),
        ExperienceArticle(
            id = "exp_02",
            indexCode = "ARCH // 02",
            title = "毕设布展避坑全纪录：从预算控制、木工外包到展签打光的 12 条实战经验",
            summary = "花最少的冤枉钱，让你的毕业作品在展厅里拥有美术馆级的呈现完成度。",
            body = "每年五月毕业季，真正决定作品现场气场的，往往有 40% 取决于最后十天的空间呈现。\n\n• 展墙规划：提前两周拿卷尺去量展位净高和插座位置，不要只看群里发的平面 CAD 图。\n• 预算分配：把钱花在主画面装裱与精准射灯上，展签用 300g 哑光特种纸自己排版裁切，比外面打印店高档十倍。\n• 留白即权威：宁可少挂两张勉强凑数的小稿，也要保证核心作品周围有至少 80cm 的呼吸边距。\n• 现场文档：在展位不起眼的角落放一本纸质过程手册，策展人和画廊主非常喜欢翻看。",
            category = ExperienceCategory.GRADUATION_WORK,
            college = "油画系",
            major = "油画第三工作室",
            year = "2024届",
            tags = listOf("毕业展", "布展灯光", "空间呈现"),
            author = p6,
            media = PostMedia("em2", drawableRes = R.drawable.img_cafa_exhibition, aspectRatio = 4f / 3f),
            readMinutes = 6,
            likeCount = 179,
            bookmarkCount = 243
        ),
        ExperienceArticle(
            id = "exp_03",
            indexCode = "ARCH // 03",
            title = "央美造型类考研高分复盘：专业理论背诵框架与创作卷破题思路",
            summary = "如何在最后三个月把中外美术史串成时间轴网络，并在 3 小时创作卷中稳住画面节奏。",
            body = "美术史论最忌讳按章节死记硬背孤立名词。我的做法是建立“横向同期对照表”——把同一世纪的东西方材料革新、赞助人制度与代表流派画在一张 A1 牛皮纸上贴在墙上。\n\n创作卷部分：评卷老师每天看几千张画，前 5 秒看的是黑白灰大关系与构图秩序。前 20 分钟一定要用炭笔定死三个小构图稿，不要急着抠局部细节。",
            category = ExperienceCategory.GRAD_EXAM,
            college = "造型学科基础部",
            major = "版画方向",
            year = "2026级研究生",
            tags = listOf("考研经验", "中外美史", "创作构图"),
            author = p1,
            readMinutes = 8,
            likeCount = 148,
            bookmarkCount = 165
        ),
        ExperienceArticle(
            id = "exp_04",
            indexCode = "ARCH // 04",
            title = "从独立设计工作室到科技大厂品牌组：艺术学院毕业生求职的真实取舍",
            summary = "对比了两类团队的工作节奏、审美话语权、成长曲线和薪资结构，给正在纠结实习方向的你。",
            body = "大四实习我先去了一家 12 人的北京先锋平面工作室，每天都能做极具实验性的艺术书和展览视觉，但薪资微薄且经常跟着印刷厂通宵；毕业后为了稳定去了大厂品牌设计部，系统化能力提升极快，但也需要学会在多方业务评审里坚持设计底线。\n\n建议大三暑假先去小型先锋工作室打开审美天花板，大四再去平台型团队补齐工程协作与商业沟通短板。",
            category = ExperienceCategory.EMPLOYMENT,
            college = "设计学院",
            major = "艺术设计学",
            year = "2023届校友",
            tags = listOf("秋招求职", "设计工作室", "实习路线"),
            author = p3,
            readMinutes = 5,
            likeCount = 162,
            bookmarkCount = 144
        ),
        ExperienceArticle(
            id = "exp_05",
            indexCode = "ARCH // 05",
            title = "工作室选择指南：如何判断一个工作室的学术脉络是否适合自己？",
            summary = "别只听学长一句“给分高”或“很松”，去看往届毕业展和导师近五年的创作脉络。",
            body = "分工作室前最有效的方法有三个：\n1. 去图书馆翻近三年的毕业作品集，看该工作室学生的作品是千篇一律像导师，还是百花齐放各具面貌；\n2. 旁听至少两节该工作室的日常评图，观察导师是如何提问的；\n3. 诚实评估自己需要“强框架训练”还是“高自由度探索”。",
            category = ExperienceCategory.STUDIO,
            college = "油画系",
            major = "绘画基础",
            year = "2024级",
            tags = listOf("工作室分流", "专业选择"),
            author = p7,
            media = PostMedia("em3", drawableRes = R.drawable.img_studio_painting, aspectRatio = 3f / 4f),
            readMinutes = 4,
            likeCount = 119,
            bookmarkCount = 132
        )
    )

    val initialNotifications: List<NotificationItem> = emptyList()
}
