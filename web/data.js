/* ==========================================================================
   杨梅树窝 (YANGMEI TREE) — Exact Android Seed Data & 3,648-Word Lexicon
   ========================================================================== */
window.YANGMEI_DATA = (function () {
  const DRAWABLE = './app/src/main/res/drawable/';

  const IMAGES = {
    logo: DRAWABLE + 'ic_yangmei_logo.jpg',
    exhibition: DRAWABLE + 'img_cafa_exhibition.jpg',
    architecture: DRAWABLE + 'img_campus_architecture.jpg',
    artSupplies: DRAWABLE + 'img_art_supplies.jpg',
    studioPainting: DRAWABLE + 'img_studio_painting.jpg'
  };

  const COPYRIGHT = {
    version: '1.0.1',
    buildCode: 2,
    creator: '李明轩',
    line: '© 2026 李明轩（创建者）版权所有 · 保留一切权利',
    statement: '本应用「杨梅树窝」的创意策划、界面视觉与原创程序归创建者李明轩所有。未经创建者书面许可，禁止复制、修改分发或用于商业用途。'
  };

  const CROSS_COLLEGE_NOTICE =
    '选课规则提醒：跨校区线下课通常不可互选（望京本院与燕郊城院线下课不可互选，中法跨校暂不支持），具体选课限制与年级要求请以当学期教务系统备注为准。';

  const CAFA_CAMPUS_COLLEGES = {
    '望京本院': [
      '中国画学院', '书法学院', '实验艺术与科技艺术学院', '人文学院',
      '设计学院', '建筑学院', '艺术管理与教育学院', '修复学院',
      '油画系', '版画系', '雕塑系', '壁画系',
      '造型学科基础部', '研究生院', '马克思主义学院'
    ],
    '燕郊校区': ['城市设计学院', '继续教育学院'],
    '上海校区': ['(中法)艺术与设计管理学院']
  };

  const CAFA_COLLEGES = [
    '中国画学院', '书法学院', '实验艺术与科技艺术学院', '人文学院',
    '设计学院', '建筑学院', '城市设计学院', '艺术管理与教育学院',
    '(中法)艺术与设计管理学院', '修复学院', '油画系', '版画系',
    '雕塑系', '壁画系', '造型学科基础部', '研究生院',
    '继续教育学院', '马克思主义学院'
  ];

  const CAFA_LOCATIONS = [
    '央美望京校区', '5号楼教学楼', '7号楼设计楼', '央美美术馆',
    '电教楼报告厅', '14号楼雕塑系', '北苑餐厅', '花家地南街',
    '央美燕郊校区', '城市设计学院楼', '央美上海校区（中法）', '798艺术区'
  ];

  const COURSE_TYPES = ['跨院选修', '公共选修', '体育选修'];
  const COURSE_CATEGORIES = [
    '影视动画与游戏',
    '绘画造型与绘本',
    '工艺首饰与木艺',
    '空间建筑与材料',
    '潮流品牌与IP设计',
    '人文理论与策展'
  ];
  const ATTENDANCE_OPTIONS = ['不点名', '偶尔抽查', '线上签到', '每节签到'];
  const EXAM_OPTIONS = ['作品汇报', '期末展览', '课程论文'];
  const SUITABLE_OPTIONS = ['水课', '跨专业友好', '适合进作品集', '轻松拿学分', '硬核高收获', '慎选避雷'];
  const COHORT_OPTIONS = ['2026届', '2025届', '2024届', '2023届', '2022届', '2021届', '2020届及以前'];
  const FIXED_PUBLISH_TAGS = [
    '#情绪树洞', '#恋爱情感', '#央美选课', '#校园日常', '#作品分享',
    '#作业DDL', '#看展打卡', '#闲置画材', '#求助互助', '#毕业求职'
  ];

  const CATEGORIES = {
    EXHIBITION: { key: 'EXHIBITION', titleCn: '校园展览', desc: '美术馆与工作室展讯', accent: 'acid', icon: 'museum' },
    SENIOR_EXPERIENCE: { key: 'SENIOR_EXPERIENCE', titleCn: '学长学姐说', desc: '毕设、留学、考研与工作室经验', accent: 'slate', icon: 'stories' },
    MARKETPLACE: { key: 'MARKETPLACE', titleCn: '闲置交换', desc: '画材、教材、相机与家具转手', accent: 'green', icon: 'bag' },
    ART_CREATION: { key: 'ART_CREATION', titleCn: '艺术创作', desc: '工作室草图与阶段记录', accent: 'acid', icon: 'palette' },
    TREE_HOLE: { key: 'TREE_HOLE', titleCn: '情绪树洞', desc: '匿名倾诉与温暖共鸣', accent: 'slate', icon: 'spa' },
    LOST_FOUND: { key: 'LOST_FOUND', titleCn: '寻物', desc: '失物寻找与拾物招领', accent: 'red', icon: 'find' },
    MUTUAL_AID: { key: 'MUTUAL_AID', titleCn: '校园互助', desc: '拼车买材料、找搭子与求助', accent: 'green', icon: 'handshake' },
    RELATIONSHIP: { key: 'RELATIONSHIP', titleCn: '恋爱情感', desc: '心动日常与校园情感故事', accent: 'red', icon: 'heart' },
    COURSE_ARCHIVE: { key: 'COURSE_ARCHIVE', titleCn: '选课评价', desc: '三校区选修避雷与红榜', accent: 'acid', icon: 'book' },
    OPPORTUNITY: { key: 'OPPORTUNITY', titleCn: '校园机会', desc: '实习、兼职、展览与合作招募', accent: 'green', icon: 'work' }
  };

  const CAMPUS_LIFE_TILES = [
    { key: 'COURSE_ARCHIVE', titleCn: '选课评价', desc: '三校区选修避雷与红榜', tintIndex: 0, icon: 'book' },
    { key: 'SENIOR_EXPERIENCE', titleCn: '学长学姐说', desc: '毕设、留学、考研与工作室', tintIndex: 1, icon: 'stories' },
    { key: 'MARKETPLACE', titleCn: '闲置交换', desc: '画材、教材与器材转手', tintIndex: 2, icon: 'bag' },
    { key: 'OPPORTUNITY', titleCn: '校园机会', desc: '兼职、实习、展览与合作', tintIndex: 3, icon: 'work' },
    { key: 'LOST_FOUND', titleCn: '寻物', desc: '速写本、证件与物品找回', tintIndex: 0, icon: 'find' },
    { key: 'MUTUAL_AID', titleCn: '校园互助', desc: '拼车买材料与校园搭子', tintIndex: 1, icon: 'handshake' }
  ];

  // Exact 3,648-word Non-Repeating Nickname Lexicon (AnonymousNicknameLexicon.kt)
  const CURATED_CLASSICS = [
    '七号楼的松节油', '花家地灰鸽', '未干的丙烯层', '美术馆北窗光', '凌晨三点切卡纸', '石膏像的阴影面',
    '望京灰砖回声', '版画室滚筒', '无名素描纸', '电教楼阶梯', '潮湿的陶泥块', '留白处的炭条',
    '雕塑系石膏粉', '北苑餐厅晚风', '七九八旧砖墙', '燕郊晚班校车', '暗房红灯守夜人', '亚麻布底料君',
    '木工坊的刨花', '定格动画帧率', '丝网印刷刮板', '画框里的冷灰调', '美术馆地下一层', '切卡纸切到天亮',
    '灰砖墙下的慢门', '无题第14号草图', '望京阴天散步', '花家地南街橘猫', '电教楼三排靠窗', '油画系第三画室',
    '水彩纸的留白边', '铜版画酸蚀液', '胶片机最后一张', '展厅射灯切角', '素描本第99页', '画筒背带折痕',
    '猪鬃笔洗笔水', '美纹纸撕裂声', '毕设布展梯子', '图书馆三层书香', '国画室生宣纸', '书法课宿墨香',
    '修复室金缮纹', '建筑系硫酸纸', '城市设计速写本', '中法校区西岸风', '人文学院读书会', '实验艺术声波'
  ];

  const PREFIXES = [
    '花家地', '七号楼', '北窗下', '美术馆', '灰砖墙', '松节油',
    '晚风里', '燕郊的', '电教楼', '暗房中', '石膏白', '群青色',
    '普鲁士', '朱砂红', '冷灰调', '暖灰调', '亚麻布', '木工坊',
    '丝网印', '凌晨三点', '阴天里', '留白处', '未干的', '半透明',
    '慢门的', '显影液', '定影池', '调色盘', '画室里', '地下室',
    '雕塑系', '铜版画', '水彩纸', '生宣上', '炭笔灰', '刮刀痕',
    '展厅里', '胶片感', '微距下', '焦外光', '素描本', '速写中',
    '泥塑房', '窑变色', '釉下青', '金缮的', '折纸般', '失焦的',
    '游牧的', '静默的', '漂浮的', '柔和的', '克制的', '漫步的',
    '发光的', '拾光的', '微凉的', '起雾的', '斜阳下', '望京的'
  ];

  const NOUNS = [
    '灰鸽', '橘猫', '炭条', '画框', '滚筒', '卡纸',
    '陶泥', '石膏', '油画刀', '猪鬃笔', '调色刀', '速写本',
    '美纹纸', '胶片卷', '三脚架', '定格帧', '故事板', '铅活字',
    '木龙骨', '白乳胶', '丙烯层', '水粉块', '宣纸卷', '松烟墨',
    '青田印', '圆口刀', '木刨花', '亚麻线', '编织结', '银盐片',
    '安全灯', '射灯影', '展签纸', '大画筒', '小折凳', '画架君',
    '素描纸', '潘通卡', '十二色环', '基准线', '透视点', '地平线',
    '阴影面', '反光板', '灰调子', '肌理块', '留白君', '布展人',
    '看展人', '拉片人', '夜行者', '摆渡人', '拾光者', '造梦师',
    '手艺人', '观察家', '漫游者', '小行星', '云切片', '回声'
  ];

  const TOTAL_NICKNAMES = CURATED_CLASSICS.length + PREFIXES.length * NOUNS.length;
  const recentlyIssued = new Set();

  function nicknameAt(index) {
    const safeIdx = ((index % TOTAL_NICKNAMES) + TOTAL_NICKNAMES) % TOTAL_NICKNAMES;
    if (safeIdx < CURATED_CLASSICS.length) return CURATED_CLASSICS[safeIdx];
    const combo = safeIdx - CURATED_CLASSICS.length;
    return PREFIXES[Math.floor(combo / NOUNS.length)] + NOUNS[combo % NOUNS.length];
  }

  function nextUniqueNickname(excludeSet) {
    if (recentlyIssued.size >= 400) recentlyIssued.clear();
    for (let i = 0; i < 64; i++) {
      const cand = nicknameAt(Math.floor(Math.random() * TOTAL_NICKNAMES));
      if (!excludeSet.has(cand) && !recentlyIssued.has(cand)) {
        recentlyIssued.add(cand);
        return cand;
      }
    }
    const start = Math.floor(Math.random() * TOTAL_NICKNAMES);
    for (let o = 0; o < TOTAL_NICKNAMES; o++) {
      const cand = nicknameAt(start + o);
      if (!excludeSet.has(cand)) {
        recentlyIssued.add(cand);
        return cand;
      }
    }
    return CURATED_CLASSICS[0];
  }

  function sampleNicknameBatch(count, excludeSet) {
    const out = [];
    const blocked = new Set(excludeSet);
    for (let i = 0; i < count; i++) {
      const n = nextUniqueNickname(blocked);
      out.push(n);
      blocked.add(n);
    }
    return out;
  }

  function sampleDistinctAvatarSeeds(count, excludeSeed) {
    const base = 100 + Math.floor(Math.random() * 89900);
    const out = [];
    let cursor = base;
    while (out.length < count) {
      const cand = (cursor * 37 + 19) % 99991 + 100;
      cursor += 7;
      if (cand !== excludeSeed && !out.includes(cand)) out.push(cand);
    }
    return out;
  }

  // Exact 12 Avatar Palettes from FluidVisuals.kt
  const AVATAR_PALETTES = [
    { bgStart: '#E0E7FF', bgEnd: '#F3E8FF', primary: '#4338CA', secondary: '#818CF8', accent: '#F43F5E' },
    { bgStart: '#FFEDD5', bgEnd: '#FCE7F3', primary: '#EA580C', secondary: '#FB7185', accent: '#6366F1' },
    { bgStart: '#D1FAE5', bgEnd: '#E0F2FE', primary: '#047857', secondary: '#38BDF8', accent: '#F59E0B' },
    { bgStart: '#FEF3C7', bgEnd: '#FFE4E6', primary: '#B45309', secondary: '#F472B6', accent: '#0EA5E9' },
    { bgStart: '#E0F2FE', bgEnd: '#EDE9FE', primary: '#0369A1', secondary: '#A78BFA', accent: '#10B981' },
    { bgStart: '#FCE7F3', bgEnd: '#E0E7FF', primary: '#BE185D', secondary: '#6366F1', accent: '#06B6D4' },
    { bgStart: '#ECFCCB', bgEnd: '#D1FAE5', primary: '#3F6212', secondary: '#34D399', accent: '#EC4899' },
    { bgStart: '#EDE9FE', bgEnd: '#CFFAFE', primary: '#6D28D9', secondary: '#22D3EE', accent: '#F97316' },
    { bgStart: '#FFE4E6', bgEnd: '#FEF9C3', primary: '#9F1239', secondary: '#FBBF24', accent: '#4F46E5' },
    { bgStart: '#CFFAFE', bgEnd: '#E0E7FF', primary: '#0E7490', secondary: '#818CF8', accent: '#E11D48' },
    { bgStart: '#F3E8FF', bgEnd: '#FFEDD5', primary: '#7E22CE', secondary: '#FB923C', accent: '#14B8A6' },
    { bgStart: '#DCFCE7', bgEnd: '#FEF3C7', primary: '#166534', secondary: '#F59E0B', accent: '#9333EA' }
  ];

  const P = {
    p1: { id: 'u_01', nickname: '七号楼的松节油', avatarSeed: 1013 },
    p2: { id: 'u_02', nickname: '切卡纸切到天亮', avatarSeed: 2027 },
    p3: { id: 'u_03', nickname: '灰砖墙下的慢门', avatarSeed: 3041 },
    p4: { id: 'u_04', nickname: '无题第14号草图', avatarSeed: 4057 },
    p5: { id: 'u_05', nickname: '美术馆地下一层', avatarSeed: 5077 },
    p6: { id: 'u_06', nickname: '留白处的炭条', avatarSeed: 6089 },
    p7: { id: 'u_07', nickname: '亚麻布底料', avatarSeed: 7103 },
    p8: { id: 'u_08', nickname: '望京阴天散步', avatarSeed: 8117 }
  };

  const INITIAL_HOT_TOPICS = [
    { id: 'topic_02', indexCode: '01', tag: '#今天又被作业逼疯了', description: '凌晨四点的模型台、怎么调都不对的灰度与反复推翻的方案。', postCount: 214, discussionCount: 13900, heatStatus: '沸' },
    { id: 'topic_01', indexCode: '02', tag: '#央美选课', description: '跨院选修避雷与宝藏公选课推荐，作业量与给分真实体验。', postCount: 186, discussionCount: 11200, heatStatus: '热' },
    { id: 'topic_03', indexCode: '03', tag: '#美术馆冬季材料展', description: '美术馆新展、地下室快闪与各工作室结课汇报展打卡。', postCount: 129, discussionCount: 9640, heatStatus: '新' },
    { id: 'topic_04', indexCode: '04', tag: '#毕业之后', description: '离开花家地之后，校友们在独立创作、工作室与留学路上的坐标。', postCount: 163, discussionCount: 8750, heatStatus: '热' },
    { id: 'topic_07', indexCode: '05', tag: '#花家地食堂新窗口测评', description: '食堂二楼新开的窗口到底值不值得排队，真实测评汇总。', postCount: 94, discussionCount: 7420, heatStatus: '新' },
    { id: 'topic_05', indexCode: '06', tag: '#艺术生就业', description: '作品集反馈、独立工作室薪资透明度与策展行业一线观察。', postCount: 97, discussionCount: 6180, heatStatus: '荐' },
    { id: 'topic_06', indexCode: '07', tag: '#宿舍生活', description: '画框收纳、松节油通风与望京周边生活实用指南。', postCount: 112, discussionCount: 5430, heatStatus: '' },
    { id: 'topic_08', indexCode: '08', tag: '#情绪树洞深夜电台', description: '给疲惫的夜晚留一个温柔、不被打扰的树洞角落。', postCount: 158, discussionCount: 4890, heatStatus: '热' },
    { id: 'topic_09', indexCode: '09', tag: '#画材闲置回血', description: '结课用不完的颜料、亚麻布、木框与相机校内低价转。', postCount: 83, discussionCount: 3920, heatStatus: '' },
    { id: 'topic_10', indexCode: '10', tag: '#毕设进度条打卡', description: '每天记录一点毕设进展，互相监督绝不烂尾。', postCount: 76, discussionCount: 3150, heatStatus: '新' }
  ];

  const INITIAL_POSTS = [
    {
      id: 'post_01',
      title: '美术馆冬季材料展布展现场：那种灰调子的空间光线真的绝了',
      body: '下午路过美术馆二层，刚好碰上装置组的同学在调最后的顶光。半透明纤维织物悬在清水混凝土墙面之前，下午三点半的斜阳一打进来，整个空间像一块缓缓流动的灰色切片。\n\n明天上午十点正式对校内开放，强烈建议带速写本或胶片机在工作日下午去，人少且光影层次最干净。',
      author: P.p5,
      category: 'EXHIBITION',
      topicTags: ['#校园展览', '#艺术创作'],
      mediaList: [
        { id: 'm1', src: IMAGES.exhibition, caption: '美术馆二层展厅 · 下午三点光影' },
        { id: 'm2', src: IMAGES.architecture, caption: '展馆外立面灰砖与阴天' }
      ],
      createdAt: '28分钟前',
      viewCount: 1286,
      likeCount: 94,
      bookmarkCount: 41,
      isLiked: false,
      isBookmarked: false,
      isMine: false,
      location: '中央美术学院美术馆 2F',
      comments: [
        { id: 'c1', author: P.p1, content: '刚去完回来！二层转角那个石膏与麻布的拼接太迷人了，站着看了二十分钟。', createdAt: '15分钟前', likes: 12 },
        { id: 'c2', author: P.p4, content: '谢谢repo！请问展期持续到下周五吗？想赶完这周模型再去。', createdAt: '9分钟前', likes: 4, replyToNickname: '美术馆地下一层' },
        { id: 'c2_op', author: P.p5, content: '展期持续到 12 月 10 号哦，下周五完全来得及，推荐工作日下午三点左右去光线最通透！', createdAt: '5分钟前', likes: 9, replyToNickname: '无题第14号草图' }
      ]
    },
    {
      id: 'post_02',
      title: '毕业三年后再回看在央美熬过的那些夜：原来最珍贵的不是画得像，而是敢推翻重来',
      body: '大四做毕设那会儿，导师在倒数第三周指着我画了两个月的整面墙说：「太顺手了，太安全了，你只是在重复你已经会的东西。」那天晚上我在七号楼楼梯间坐到保安锁门，气得想直接退学。\n\n但工作三年、在甲方和算法审美之间来回拉扯之后，才突然明白当年那种被逼着打破惯性的痛苦有多奢侈。致还在工作室和方案死磕的学弟学妹：允许自己画出“丑”的草稿，那是真正开始生长的地方。',
      author: P.p3,
      category: 'SENIOR_EXPERIENCE',
      topicTags: ['#毕业之后', '#艺术生就业'],
      mediaList: [],
      createdAt: '1小时前',
      viewCount: 2430,
      likeCount: 186,
      bookmarkCount: 112,
      isLiked: false,
      isBookmarked: false,
      isMine: false,
      comments: [
        { id: 'c3', author: P.p2, content: '正在对着一桌废卡纸怀疑人生，刷到这段话瞬间稳住了呼吸。', createdAt: '42分钟前', likes: 27 },
        { id: 'c4', author: P.p6, content: '太真实了。离开学校后很少有人会认真花一小时批评你的作品结构了。', createdAt: '20分钟前', likes: 19 }
      ]
    },
    {
      id: 'post_03',
      title: '【出闲置】温莎牛顿艺术家级油画颜料套装 + 猪鬃笔刷与调色刀',
      body: '这学期转向综合材料与影像，去年囤的一整套温莎牛顿颜料和几把顺手的猪鬃画笔基本没怎么动（钛白用了三分之一，其余几乎全新）。附送一本未拆封的细纹水彩速写本。\n\n仅限花家地校内面交，美术馆门口或七号楼楼下都可，同院学弟妹来拿可以小刀或换胶片卷。',
      author: P.p7,
      category: 'MARKETPLACE',
      topicTags: ['#闲置交换', '#画材'],
      mediaList: [{ id: 'm3', src: IMAGES.artSupplies, caption: '实拍清单，九成新' }],
      createdAt: '2小时前',
      viewCount: 618,
      likeCount: 29,
      bookmarkCount: 34,
      isLiked: false,
      isBookmarked: false,
      isMine: false,
      price: '¥ 240',
      location: '花家地校区 · 7号楼',
      comments: [
        { id: 'c5', author: P.p1, content: '请问调色刀和那几支长杆猪鬃单出吗？明天中午可在七号楼下自提。', createdAt: '1小时前', likes: 2 }
      ]
    },
    {
      id: 'post_05',
      title: '北窗阴天的第三遍罩染，灰蓝和暖灰终于咬合上了',
      body: '连续画废了两张 120×160 的麻布，今天索性停下来把画面刮掉一半，只留下底层粗糙的亚麻肌理和几块克制的冷灰。突然发现少画一点，画面的呼吸感反而出来了。记录一下下午四点半的工作室角落。',
      author: P.p1,
      category: 'ART_CREATION',
      topicTags: ['#艺术创作', '#今天又被作业逼疯了'],
      mediaList: [{ id: 'm4', src: IMAGES.studioPainting, caption: '120×160cm · 布面油画与炭粉，进行中' }],
      createdAt: '4小时前',
      viewCount: 1892,
      likeCount: 142,
      bookmarkCount: 67,
      isLiked: false,
      isBookmarked: false,
      isMine: false,
      comments: [
        { id: 'c7', author: P.p7, content: '那个留白和刮刀肌理的张力好棒！阴天北窗光永远是最诚实的光。', createdAt: '3小时前', likes: 11 }
      ]
    },
    {
      id: 'post_06',
      title: '',
      body: '其实很羡慕那些一进工作室就能立刻进入心流的人。每次交阶段汇报前我都觉得自己像个混进来的冒牌货，看着旁边同学的作品那么笃定，我却连最基本的构图都在反复自我否定。今晚不想硬撑了，先去操场走两圈吹吹冷风。',
      author: P.p8,
      category: 'TREE_HOLE',
      topicTags: ['#今天又被作业逼疯了'],
      mediaList: [],
      moodTag: '迷茫',
      createdAt: '5小时前',
      viewCount: 945,
      likeCount: 78,
      bookmarkCount: 19,
      isLiked: false,
      isBookmarked: false,
      isMine: false,
      comments: [
        { id: 'c8', author: P.p3, content: '在央美几乎每个人都经历过冒牌货综合征，只是大家展墙上都只挂了最后成功的那一张。吹吹风，明天会轻一点。', createdAt: '4小时前', likes: 31, comfortTag: '抱抱' },
        { id: 'c9', author: P.p2, content: '刚从操场走完三圈回来，冷风对降温焦虑特别有效。', createdAt: '3小时前', likes: 14, comfortTag: '我也一样' }
      ]
    },
    {
      id: 'post_07',
      title: '【急寻】黑色硬壳 A5 速写本，可能落在电教楼 302 或食堂二楼窗边',
      body: '封面左下角贴了一张灰色美纹纸标签，里面有我这两周做毕业展前期调研的几十页草图和采访笔记，对我特别重要！如果哪位同学捡到了麻烦评论区留言一下，请喝咖啡/奶茶表达感谢！',
      author: P.p2,
      category: 'LOST_FOUND',
      topicTags: ['#寻物', '#校园互助'],
      mediaList: [],
      location: '电教楼 302 / 食堂 2F',
      createdAt: '5小时前',
      viewCount: 732,
      likeCount: 35,
      bookmarkCount: 8,
      isLiked: false,
      isBookmarked: false,
      isMine: false,
      comments: [
        { id: 'c10', author: P.p5, content: '帮顶！刚下课路过 302 帮你看了一眼讲台旁边，物业阿姨说下午收了一本黑皮本子放一楼值班室了，快去问问！', createdAt: '2小时前', likes: 22 }
      ]
    },
    {
      id: 'post_09',
      title: '周六上午拼车去燕郊建材市场买木龙骨和亚麻布，还差 2 位同学',
      body: '已经约好熟悉的金杯师傅，早上 9:00 从北门出发，大概中午 1 点前返回花家地。车费 AA 人均 35 左右，顺路还可以一起买画框条。要去的话评论区留个言我统一拉拼车接龙。',
      author: P.p7,
      category: 'MUTUAL_AID',
      topicTags: ['#校园互助', '#宿舍生活'],
      mediaList: [],
      location: '花家地北门集合',
      createdAt: '昨天',
      viewCount: 486,
      likeCount: 21,
      bookmarkCount: 15,
      isLiked: false,
      isBookmarked: false,
      isMine: false,
      comments: []
    },
    {
      id: 'post_10',
      title: '在央美谈恋爱是不是都自带“陪你刷白墙”属性？',
      body: '昨天晚上十一点被对象拉去雕塑系工作室帮忙打磨石膏底座，两个人都满身白灰，最后蹲在楼道里分了一盒凉掉的炒河粉。虽然手指磨得发酸，但那一刻觉得灰头土脸的校园生活也挺浪漫的。',
      author: P.p8,
      category: 'RELATIONSHIP',
      topicTags: ['#宿舍生活'],
      mediaList: [],
      createdAt: '昨天',
      viewCount: 1674,
      likeCount: 132,
      bookmarkCount: 24,
      isLiked: false,
      isBookmarked: false,
      isMine: false,
      comments: [
        { id: 'c11', author: P.p1, content: '是的，央美最高级别的浪漫就是布展前夜帮你拧膨胀螺丝。', createdAt: '18小时前', likes: 45 }
      ]
    }
  ];

  // Exact 20 Curated Cross-College Courses (CrossCollegeCourseData.kt)
  const R = {
    r1: { id: 'cr_u01', nickname: '七号楼的松节油', avatarSeed: 1101 },
    r2: { id: 'cr_u02', nickname: '燕郊晚风速写', avatarSeed: 2203 },
    r3: { id: 'cr_u03', nickname: '北窗灰调子', avatarSeed: 3307 },
    r4: { id: 'cr_u04', nickname: '木工坊刨花', avatarSeed: 4409 },
    r5: { id: 'cr_u05', nickname: '动画系拉片人', avatarSeed: 5519 },
    r6: { id: 'cr_u06', nickname: '城院早八人', avatarSeed: 6613 },
    r7: { id: 'cr_u07', nickname: '美术馆慢门', avatarSeed: 7723 },
    r8: { id: 'cr_u08', nickname: '望京卡纸切割机', avatarSeed: 8831 }
  };

  const INITIAL_COURSES = [
    // 一、影视动画与游戏
    {
      id: 'course_styy', name: '视听语言', instructor: '李炫', campus: '燕郊校区', college: '城市设计学院',
      major: '影视动画与游戏', courseType: '跨院选修', workloadScore: 2.8, difficultyScore: 4.7, recommendScore: 4.9,
      suitableFor: '适合进作品集', isBookmarked: false,
      summary: '游戏与影视圈资深大佬授课，课堂拉片干货极多，结课画故事板/分镜，作业量适中偏少、老师温柔负责。',
      reviews: [
        { id: 'r_styy_1', author: R.r5, semester: '跨院选修', content: '喜欢漫画、动画和影视分镜的无脑冲！老师北影毕业、游戏圈在职大佬，上课带着拉片看电影全是干货，结课作业是画故事板，作业量适中偏少不肝，老师超级温柔负责。', workload: 2, difficulty: 5, recommend: 5, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '适合进作品集', createdAt: '已整理', helpfulCount: 46 },
        { id: 'r_styy_2', author: R.r2, semester: '跨院选修', content: '讲各种分镜与视听语言专业知识，想进游戏行业或对电影动画感兴趣必冲！作业是画分镜镜头，只要相对认真画完老师完全不会为难，非常包容；纯想划水的就不太建议。', workload: 3, difficulty: 5, recommend: 5, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '硬核高收获', createdAt: '已整理', helpfulCount: 34 },
        { id: 'r_styy_3', author: R.r3, semester: '跨院选修', content: '专业能力很硬核，不过在学生里的评价略微两极分化：老师比较倾向引导大家按他的专业标准来，喜欢高标准指导的会非常喜欢，不喜欢被框住的可能会觉得不够自由。', workload: 3, difficulty: 4, recommend: 4, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '硬核高收获', createdAt: '已整理', helpfulCount: 19 }
      ]
    },
    {
      id: 'course_xcpjj', name: '现成品剪辑', instructor: '宋松', campus: '燕郊校区', college: '城市设计学院',
      major: '影视动画与游戏', courseType: '跨院选修', workloadScore: 2.0, difficultyScore: 3.8, recommendScore: 4.8,
      suitableFor: '跨专业友好', isBookmarked: false,
      summary: '网上找现成素材剪辑视频，题材完全自由、不主张肝；宋老师和蔼好玩，零基础友好，给分偏平均但用心做能拿高分。',
      reviews: [
        { id: 'r_xcpjj_1', author: R.r1, semester: '跨院选修', content: '宋松老师人超好、很和蔼，上课让大家做作品主要是开心且能学到东西，不咋肝也不主张硬肝。零基础不会累，我当时零基础去也很受照顾。', workload: 2, difficulty: 4, recommend: 5, attendanceMode: '不点名', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 31 },
        { id: 'r_xcpjj_2', author: R.r6, semester: '跨院选修', content: '剪辑方向不固定非常自由，接受一切题材！找自己感兴趣的内容认真剪一个视频就能拿高分，老师还会提很细致的修改建议，能聊很多让人很放松。', workload: 2, difficulty: 4, recommend: 5, attendanceMode: '不点名', examFormat: '作品汇报', suitableTag: '轻松拿学分', createdAt: '已整理', helpfulCount: 24 },
        { id: 'r_xcpjj_3', author: R.r8, semester: '跨院选修', content: '课很轻松，就是在网上搜集现成影像素材做剪辑创作。整体给分比较平均、不算特别高，但只要认真跟着做也不会特别低。', workload: 2, difficulty: 3, recommend: 4, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '轻松拿学分', createdAt: '已整理', helpfulCount: 17 }
      ]
    },
    {
      id: 'course_dgdh', name: '定格动画', instructor: '尹刚', campus: '燕郊校区', college: '城市设计学院',
      major: '影视动画与游戏', courseType: '跨院选修', workloadScore: 3.5, difficultyScore: 4.5, recommendScore: 4.8,
      suitableFor: '跨专业友好', isBookmarked: false,
      summary: '和蔼有趣的老教授授课，喜欢手工制作体验极佳；拍摄定格动画需要耐心动手，给分合理偏高，口碑极好。',
      reviews: [
        { id: 'r_dgdh_1', author: R.r4, semester: '跨院选修', content: '尹老师超级好，是个有趣又和蔼的老教授，爱跟学生唠嗑也用心讲知识，快退休了还能选到他的课特别难得，强烈推荐！', workload: 3, difficulty: 5, recommend: 5, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 29 },
        { id: 'r_dgdh_2', author: R.r2, semester: '跨院选修', content: '课程很好玩，主要靠自己动手制作与逐帧拍摄，拍定格的过程会稍微有点麻烦。喜欢手工的同学强烈建议选，打分很公正，做得好一定高分。', workload: 4, difficulty: 4, recommend: 5, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '适合进作品集', createdAt: '已整理', helpfulCount: 21 }
      ]
    },
    {
      id: 'course_dmxc', name: '动漫乡村', instructor: '李礼 / 朱崇轩', campus: '燕郊校区', college: '城市设计学院',
      major: '影视动画与游戏', courseType: '跨院选修', workloadScore: 2.5, difficultyScore: 3.7, recommendScore: 3.7,
      suitableFor: '轻松拿学分', isBookmarked: false,
      summary: '朱崇轩老师佛系温和、沟通友好；两位老师评价略有差异，整体压力较小，想深入学习可多主动找老师交流。',
      reviews: [
        { id: 'r_dmxc_1', author: R.r6, semester: '跨院选修', content: '朱崇轩老师人很好、很佛系也很温和，像长辈一样好说话，想学东西可以多主动找他；虽然有时不太善言辞，但不太会卡人，给分中规中矩（有次作业漏交也给了 80）。', workload: 2, difficulty: 4, recommend: 4, attendanceMode: '不点名', examFormat: '作品汇报', suitableTag: '轻松拿学分', createdAt: '已整理', helpfulCount: 18 },
        { id: 'r_dmxc_2', author: R.r3, semester: '跨院选修', content: '李礼老师评价有些分歧：有同学觉得李礼老师人还行、给分不错，也有同学建议优先选朱老师班，选课前可以留意一下具体分组。', workload: 3, difficulty: 3, recommend: 3, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 12 }
      ]
    },
    {
      id: 'course_sygc', name: '摄影观察', instructor: '待补充（水课推荐）', campus: '燕郊校区', college: '城市设计学院',
      major: '影视动画与游戏', courseType: '跨院选修', workloadScore: 1.5, difficultyScore: 3.0, recommendScore: 3.6,
      suitableFor: '水课', isBookmarked: false,
      summary: '同学们在求水课推荐中提到的轻松课程：小组出勤压力低、后期上课时间灵活，但最终给分略显玄学。',
      reviews: [
        { id: 'r_sygc_1', author: R.r8, semester: '跨院选修', content: '摄影观察比较水，小组里有一个人去上课就可以，后期上课时间安排非常灵活，几乎不占用太多精力，不过最终给分比较玄学。', workload: 1, difficulty: 3, recommend: 4, attendanceMode: '不点名', examFormat: '作品汇报', suitableTag: '水课', createdAt: '已整理', helpfulCount: 22 }
      ]
    },
    // 二、绘画造型与绘本
    {
      id: 'course_hb', name: '绘本', instructor: '冯旭', campus: '燕郊校区', college: '城市设计学院',
      major: '绘画造型与绘本', courseType: '跨院选修', workloadScore: 3.0, difficultyScore: 4.2, recommendScore: 4.8,
      suitableFor: '跨专业友好', isBookmarked: false,
      summary: '绘本方向人气口碑课！冯老师幽默专业、包容度极高，手绘板绘均可，微肝但上课非常快乐，抢课难度较高。',
      reviews: [
        { id: 'r_hb_1', author: R.r2, semester: '跨院选修', content: '各方面都特别好！冯老师很专业也很搞笑，包容度超大，手绘板绘都可以，给予学生的自由度很高。虽然稍微有点肝，但上课真的很快乐，是认真在搞创作的老师。', workload: 3, difficulty: 5, recommend: 5, attendanceMode: '线上签到', examFormat: '作品汇报', suitableTag: '适合进作品集', createdAt: '已整理', helpfulCount: 41 },
        { id: 'r_hb_2', author: R.r7, semester: '跨院选修', content: '网课不是每节课都强制开摄像头，自愿成分较大，主要是第一节课和最后一节课会开。会基础儿童画/插画就能上手，给分整体偏中上，就是不太好抢。', workload: 3, difficulty: 4, recommend: 5, attendanceMode: '线上签到', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 27 }
      ]
    },
    {
      id: 'course_rwzx', name: '人物造型基础与艺用解剖', instructor: '张姿', campus: '燕郊校区', college: '城市设计学院',
      major: '绘画造型与绘本', courseType: '跨院选修', workloadScore: 5.0, difficultyScore: 4.2, recommendScore: 3.5,
      suitableFor: '硬核高收获', isBookmarked: false,
      summary: '评价两极分化明显的硬核造型课：老师极度认真负责、认真交作业给分不错，但作业非常肝且教学风格偏强势严格。',
      reviews: [
        { id: 'r_rwzx_1', author: R.r3, semester: '跨院选修', content: '张姿老师将心比心地负责，也很护短，只是因为太负责容易显得强势。每次作业大概耗时 3-6 小时确实很肝，但认真学绝对有所得，完成要求给分挺高的。', workload: 5, difficulty: 5, recommend: 4, attendanceMode: '每节签到', examFormat: '作品汇报', suitableTag: '硬核高收获', createdAt: '已整理', helpfulCount: 35 },
        { id: 'r_rwzx_2', author: R.r6, semester: '跨院选修', content: '外院同学普遍反映非常肝！老师风格偏严格强势，部分同学会觉得压迫感较强甚至有心理负担。喜欢宽松自由氛围的同学一定要慎重考虑。', workload: 5, difficulty: 3, recommend: 3, attendanceMode: '每节签到', examFormat: '作品汇报', suitableTag: '慎选避雷', createdAt: '已整理', helpfulCount: 38 }
      ]
    },
    {
      id: 'course_swsm', name: '稍微素描', instructor: '苏海江', campus: '燕郊校区', college: '城市设计学院',
      major: '绘画造型与绘本', courseType: '跨院选修', workloadScore: 5.0, difficultyScore: 2.0, recommendScore: 1.2,
      suitableFor: '慎选避雷', isBookmarked: false,
      summary: '高能预警！文档中城院同学一致强烈劝退的超高强度素描课，作业量极大、耗时极长且打分体验普遍不佳。',
      reviews: [
        { id: 'r_swsm_1', author: R.r6, semester: '跨院选修', content: '城院人公认的超高强度素描课，非常消耗精力与时间。除非你本身极其热爱高强度肝素描，否则真心建议放过自己、慎重选择。', workload: 5, difficulty: 2, recommend: 1, attendanceMode: '每节签到', examFormat: '作品汇报', suitableTag: '慎选避雷', createdAt: '已整理', helpfulCount: 64 },
        { id: 'r_swsm_2', author: R.r2, semester: '跨院选修', content: '多位上过的同学一致反馈体验很痛苦，作业压力和打分标准都很令人崩溃，选前请务必做好充分的心理和时间准备。', workload: 5, difficulty: 2, recommend: 1, attendanceMode: '每节签到', examFormat: '作品汇报', suitableTag: '慎选避雷', createdAt: '已整理', helpfulCount: 51 }
      ]
    },
    // 三、工艺首饰与木艺
    {
      id: 'course_mysj', name: '木艺与设计', instructor: '白云泽', campus: '燕郊校区', college: '城市设计学院',
      major: '工艺首饰与木艺', courseType: '跨院选修', workloadScore: 3.8, difficultyScore: 4.7, recommendScore: 4.7,
      suitableFor: '跨专业友好', isBookmarked: false,
      summary: '人气极高的手工木艺课！白老师温柔耐心、给分高且自由度好；纯手工削刻木头较费手，汇报次数稍多但要求宽松。',
      reviews: [
        { id: 'r_mysj_1', author: R.r4, semester: '跨院选修', content: '白老师人特别好，温柔耐心又可爱，还喜欢和学生唠嗑，闭眼选！自由度和肝度都很友好，打分非常不错；注：白老师是木工车间的专业课老师，不是车间师傅。', workload: 3, difficulty: 5, recommend: 5, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 43 },
        { id: 'r_mysj_2', author: R.r1, semester: '跨院选修', content: '纯手工制作体验，需要自己锯木头、刻木头做玩偶，优点是汇报随便讲一下 PPT 就过关、给分不错；缺点是汇报频次稍高，而且刻木头真的很费手。', workload: 4, difficulty: 5, recommend: 4, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '适合进作品集', createdAt: '已整理', helpfulCount: 32 }
      ]
    },
    {
      id: 'course_ctgy', name: '传统工艺', instructor: '贺佳', campus: '燕郊校区', college: '城市设计学院',
      major: '工艺首饰与木艺', courseType: '跨院选修', workloadScore: 4.0, difficultyScore: 4.5, recommendScore: 4.5,
      suitableFor: '硬核高收获', isBookmarked: false,
      summary: '贺老师非常温柔有趣、给分很高，但平时汇报和任务节点较多、略微偏肝，认真完成体验很棒。',
      reviews: [
        { id: 'r_ctgy_1', author: R.r7, semester: '跨院选修', content: '我上过，贺老师人很好、很温柔有趣，给分相当不错！就是课程流程稍微麻烦一点，平时汇报有点多，会觉得有点肝。', workload: 4, difficulty: 5, recommend: 5, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '硬核高收获', createdAt: '已整理', helpfulCount: 23 },
        { id: 'r_ctgy_2', author: R.r3, semester: '跨院选修', content: '老师人特别温柔，不过课程里的事儿确实不少，属于“温柔但扎实推进”的节奏，不怕麻烦的话很值得选。', workload: 4, difficulty: 4, recommend: 4, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 15 }
      ]
    },
    {
      id: 'course_zzfs', name: '织造方式研究', instructor: '杨华', campus: '燕郊校区', college: '城市设计学院',
      major: '工艺首饰与木艺', courseType: '跨院选修', workloadScore: 2.5, difficultyScore: 3.8, recommendScore: 3.8,
      suitableFor: '轻松拿学分', isBookmarked: false,
      summary: '主要设计编织结构，自由度高、多数同学觉得活少分高且佛系；部分学期反馈需节节课汇报进度并记考勤。',
      reviews: [
        { id: 'r_zzfs_1', author: R.r8, semester: '跨院选修', content: '杨华老师上课挺佛系，自由度蛮高，作业就是设计一种编织结构，活少分高、比较轻松！认真做并且多和老师积极沟通的话给分很高。', workload: 2, difficulty: 5, recommend: 5, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '轻松拿学分', createdAt: '已整理', helpfulCount: 26 },
        { id: 'r_zzfs_2', author: R.r6, semester: '跨院选修', content: '提示下不同学期要求可能有差异：去年上过时是每节课所有人都要汇报一遍进度，不来上课还会被记考勤，也有同学反馈那期给分偏低，选之前留意一下节奏。', workload: 3, difficulty: 3, recommend: 3, attendanceMode: '每节签到', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 18 }
      ]
    },
    {
      id: 'course_gjzb', name: '高级珠宝的“天下”——从地理角度看高级珠宝', instructor: '余瑶', campus: '燕郊校区', college: '城市设计学院',
      major: '工艺首饰与木艺', courseType: '跨院选修', workloadScore: 3.0, difficultyScore: 4.0, recommendScore: 4.0,
      suitableFor: '跨专业友好', isBookmarked: false,
      summary: '余瑶老师口碑很好，人温柔随和；该门课具体给分与作业量目前样本较少，欢迎上过的同学继续补充。',
      reviews: [
        { id: 'r_gjzb_1', author: R.r2, semester: '跨院选修', content: '上过余瑶老师别的课，老师人非常温柔、随和、好沟通！这门从地理角度看高级珠宝的新课目前打分细节不多，期待上过的同学补充。', workload: 3, difficulty: 4, recommend: 4, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 14 }
      ]
    },
    // 四、空间建筑与材料
    {
      id: 'course_kjmh', name: '空间盲盒', instructor: '张欣', campus: '燕郊校区', college: '城市设计学院',
      major: '空间建筑与材料', courseType: '跨院选修', workloadScore: 2.0, difficultyScore: 4.2, recommendScore: 4.5,
      suitableFor: '水课', isBookmarked: false,
      summary: '创意导向的轻松好课！不拼工作量只看想法是否有趣，适合喜欢实验艺术与当代艺术的同学，老师超好。',
      reviews: [
        { id: 'r_kjmh_1', author: R.r5, semester: '跨院选修', content: '老师非常有趣、人超好！喜欢实验艺术和当代艺术的可以冲。课程很轻松不肝，老师看重的不是工作量多大，而是方案本身有没有意思，去年大家都比较轻松、给分也不错。', workload: 2, difficulty: 4, recommend: 5, attendanceMode: '不点名', examFormat: '作品汇报', suitableTag: '水课', createdAt: '已整理', helpfulCount: 25 }
      ]
    },
    {
      id: 'course_kjzg', name: '空间建构', instructor: '马华园', campus: '燕郊校区', college: '城市设计学院',
      major: '空间建筑与材料', courseType: '跨院选修', workloadScore: 5.0, difficultyScore: 2.5, recommendScore: 1.8,
      suitableFor: '慎选避雷', isBookmarked: false,
      summary: '多数同学劝退：作业多且临近结课仍易加量、方案需反复修改，上课时间常改至周末或晚间；少数喜欢发散思路、主动沟通的同学尚可适应。',
      reviews: [
        { id: 'r_kjzg_1', author: R.r6, semester: '跨院选修', content: '空间五工（原空间三工）老师，选前慎重！经常不在正常白天时间上课，改到周日或晚间补课，微信回复也不太及时。方案反复改很多遍，临近结课还容易临时加作业，节奏偏混乱。', workload: 5, difficulty: 2, recommend: 1, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '慎选避雷', createdAt: '已整理', helpfulCount: 44 },
        { id: 'r_kjzg_2', author: R.r3, semester: '跨院选修', content: '老师讲解风格比较抽象跳跃，有时不太直接给明确答案，还容易延迟结课。不过也有人喜欢，如果你非常有自己独立想法、愿意多找老师深入交流的话就没问题。', workload: 5, difficulty: 3, recommend: 2, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '慎选避雷', createdAt: '已整理', helpfulCount: 28 }
      ]
    },
    {
      id: 'course_ysgz', name: '艺术构造', instructor: '钱亮', campus: '燕郊校区', college: '城市设计学院',
      major: '空间建筑与材料', courseType: '跨院选修', workloadScore: 3.5, difficultyScore: 2.5, recommendScore: 2.0,
      suitableFor: '慎选避雷', isBookmarked: false,
      summary: '老师非常爱讲谐音梗，文档中有上过的同学直接标注避雷，建议选课前多向同院学长打听具体作业要求。',
      reviews: [
        { id: 'r_ysgz_1', author: R.r8, semester: '跨院选修', content: '钱老师是最爱讲谐音梗的人，但表格里多位同学直接给出了避雷提示，建议谨慎选择或先了解清楚具体考核方式。', workload: 4, difficulty: 2, recommend: 2, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '慎选避雷', createdAt: '已整理', helpfulCount: 16 }
      ]
    },
    {
      id: 'course_mhtsd', name: '梦幻“砼”时代', instructor: '张欣', campus: '燕郊校区', college: '城市设计学院',
      major: '空间建筑与材料', courseType: '跨院选修', workloadScore: 0, difficultyScore: 0, recommendScore: 0,
      suitableFor: '待补充评价', isBookmarked: false,
      summary: '混凝土材料相关跨院选修课，文档反馈去年未能成功开课，目前暂无实测打分，欢迎新开课后补充体验。',
      reviews: []
    },
    // 五、潮流品牌与IP设计
    {
      id: 'course_cpp', name: '潮品牌研究与创新图形', instructor: '胡雪琴', campus: '燕郊校区', college: '城市设计学院',
      major: '潮流品牌与IP设计', courseType: '跨院选修', workloadScore: 4.5, difficultyScore: 4.6, recommendScore: 4.8,
      suitableFor: '硬核高收获', isBookmarked: false,
      summary: '含金量极高的前沿品牌图形课！胡老师温柔爱夸人、不常点名（轮到汇报需到场），虽然作业多偏肝，但收获大且给分高。',
      reviews: [
        { id: 'r_cpp_1', author: R.r1, semester: '跨院选修', content: '老师超棒、很温柔还经常夸学生！课程内容非常前沿，含金量很高，能学到实打实的干货。虽然课很肝，但分不低且特别有收获感，看到这门课快选！', workload: 5, difficulty: 5, recommend: 5, attendanceMode: '不点名', examFormat: '作品汇报', suitableTag: '硬核高收获', createdAt: '已整理', helpfulCount: 33 },
        { id: 'r_cpp_2', author: R.r7, semester: '跨院选修', content: '基本不点名，但轮到自己 pre 汇报那天一定要去。一学年大概会有七八次作业，节奏紧凑，注意查看教务系统课程备注（部分学期可能限城院大三选课）。', workload: 4, difficulty: 4, recommend: 5, attendanceMode: '不点名', examFormat: '作品汇报', suitableTag: '适合进作品集', createdAt: '已整理', helpfulCount: 24 }
      ]
    },
    {
      id: 'course_ipxx', name: 'IP形象设计与衍生品开发', instructor: '冯薇', campus: '燕郊校区', college: '城市设计学院',
      major: '潮流品牌与IP设计', courseType: '跨院选修', workloadScore: 4.0, difficultyScore: 3.5, recommendScore: 3.5,
      suitableFor: '硬核高收获', isBookmarked: false,
      summary: '偏向真实项目实践的 IP 衍生开发课，不水，往期课程会结合老师承接的实际项目推进。',
      reviews: [
        { id: 'r_ipxx_1', author: R.r2, semester: '跨院选修', content: '不水！上次开课是直接跟着做老师接的实际商业/设计项目，有一定工作量，想攒落地项目经验的同学可以考虑。', workload: 4, difficulty: 4, recommend: 4, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '硬核高收获', createdAt: '已整理', helpfulCount: 15 }
      ]
    },
    // 六、人文理论与策展
    {
      id: 'course_yszl', name: '艺术治疗的多场景实践', instructor: '康俐', campus: '望京本院', college: '艺术管理与教育学院',
      major: '人文理论与策展', courseType: '跨院选修', workloadScore: 3.0, difficultyScore: 4.0, recommendScore: 4.0,
      suitableFor: '跨专业友好', isBookmarked: false,
      summary: '艺管院开设的跨学科实践课，对艺管以外的跨专业同学比较友好，最终打分可达 85+。',
      reviews: [
        { id: 'r_yszl_1', author: R.r8, semester: '跨院选修', content: '对除艺管以外的其他专业同学应该挺友好的，跨专业听起来没有太高门槛，老师打分还可以，认真完成基本能拿到 85+。', workload: 3, difficulty: 4, recommend: 4, attendanceMode: '偶尔抽查', examFormat: '作品汇报', suitableTag: '跨专业友好', createdAt: '已整理', helpfulCount: 19 }
      ]
    },
    {
      id: 'course_xms', name: '西美史', instructor: '邵亦扬', campus: '望京本院', college: '人文学院',
      major: '人文理论与策展', courseType: '跨院选修', workloadScore: 0, difficultyScore: 0, recommendScore: 0,
      suitableFor: '待补充评价', isBookmarked: false,
      summary: '人文学院邵亦扬教授西方美术史课程，已收录至跨院选修清单，欢迎上过的同学补充考勤与给分体验。',
      reviews: []
    }
  ];

  // Exact 5 Senior Experience Articles (MockSeedData.kt)
  const INITIAL_EXPERIENCES = [
    {
      id: 'exp_01',
      title: '皇艺 RCA & 伦艺 CSM 视传与交互双 Offer：我的作品集叙事重构笔记',
      summary: '不堆砌炫技渲染，如何把央美日常的田野调研与平面实验，整理成考官三分钟就能看懂的逻辑骨架。',
      body: '很多央美同学申海外名校时最大的优势其实是扎实的材料感与概念深度，而最大的短板往往是“只放了最终成品图，省略了推演过程”。\n\n1. 项目排序策略：第一个项目放最具个人方法论代表性的实验性课题，不要放最商业的作业。\n2. 过程视觉化：把草图、废稿、失败的印刷打样、和导师讨论的批注扫描进去，考官极看重迭代轨迹。\n3. 排版克制：整本作品集只用两套字号体系和统一边距网格，让作品本身说话，不要让装饰边框抢戏。\n4. 面试准备：针对每个项目准备 90 秒英文电梯陈述，重点讲清“为什么这个问题只能用这种视觉媒介来回答”。',
      category: '留学',
      college: '设计学院',
      major: '视觉传达设计',
      year: '2025届',
      tags: ['作品集', 'RCA', '排版方法论'],
      author: P.p3,
      image: IMAGES.architecture,
      readMinutes: 7,
      likeCount: 214,
      bookmarkCount: 198,
      isLiked: false,
      isBookmarked: false,
      comments: [
        { id: 'ec1', author: P.p4, content: '第 2 点太受用了，之前一直把试错草稿全藏起来了，明天立刻重新扫描进过程页！', createdAt: '2天前', likes: 16, cohortLabel: '2025届' }
      ]
    },
    {
      id: 'exp_02',
      title: '毕设布展避坑全纪录：从预算控制、木工外包到展签打光的 12 条实战经验',
      summary: '花最少的冤枉钱，让你的毕业作品在展厅里拥有美术馆级的呈现完成度。',
      body: '每年五月毕业季，真正决定作品现场气场的，往往有 40% 取决于最后十天的空间呈现。\n\n• 展墙规划：提前两周拿卷尺去量展位净高和插座位置，不要只看群里发的平面 CAD 图。\n• 预算分配：把钱花在主画面装裱与精准射灯上，展签用 300g 哑光特种纸自己排版裁切，比外面打印店高档十倍。\n• 留白即权威：宁可少挂两张勉强凑数的小稿，也要保证核心作品周围有至少 80cm 的呼吸边距。\n• 现场文档：在展位不起眼的角落放一本纸质过程手册，策展人和画廊主非常喜欢翻看。',
      category: '毕业作品',
      college: '油画系',
      major: '油画第三工作室',
      year: '2024届',
      tags: ['毕业展', '布展灯光', '空间呈现'],
      author: P.p6,
      image: IMAGES.exhibition,
      readMinutes: 6,
      likeCount: 179,
      bookmarkCount: 243,
      isLiked: false,
      isBookmarked: false,
      comments: []
    },
    {
      id: 'exp_03',
      title: '央美造型类考研高分复盘：专业理论背诵框架与创作卷破题思路',
      summary: '如何在最后三个月把中外美术史串成时间轴网络，并在 3 小时创作卷中稳住画面节奏。',
      body: '美术史论最忌讳按章节死记硬背孤立名词。我的做法是建立“横向同期对照表”——把同一世纪的东西方材料革新、赞助人制度与代表流派画在一张 A1 牛皮纸上贴在墙上。\n\n创作卷部分：评卷老师每天看几千张画，前 5 秒看的是黑白灰大关系与构图秩序。前 20 分钟一定要用炭笔定死三个小构图稿，不要急着抠局部细节。',
      category: '考研',
      college: '造型学科基础部',
      major: '版画方向',
      year: '2026级研究生',
      tags: ['考研经验', '中外美史', '创作构图'],
      author: P.p1,
      image: null,
      readMinutes: 8,
      likeCount: 148,
      bookmarkCount: 165,
      isLiked: false,
      isBookmarked: false,
      comments: []
    },
    {
      id: 'exp_04',
      title: '从独立设计工作室到科技大厂品牌组：艺术学院毕业生求职的真实取舍',
      summary: '对比了两类团队的工作节奏、审美话语权、成长曲线和薪资结构，给正在纠结实习方向的你。',
      body: '大四实习我先去了一家 12 人的北京先锋平面工作室，每天都能做极具实验性的艺术书和展览视觉，但薪资微薄且经常跟着印刷厂通宵；毕业后为了稳定去了大厂品牌设计部，系统化能力提升极快，但也需要学会在多方业务评审里坚持设计底线。\n\n建议大三暑假先去小型先锋工作室打开审美天花板，大四再去平台型团队补齐工程协作与商业沟通短板。',
      category: '就业',
      college: '设计学院',
      major: '艺术设计学',
      year: '2023届校友',
      tags: ['秋招求职', '设计工作室', '实习路线'],
      author: P.p3,
      image: null,
      readMinutes: 5,
      likeCount: 162,
      bookmarkCount: 144,
      isLiked: false,
      isBookmarked: false,
      comments: []
    },
    {
      id: 'exp_05',
      title: '工作室选择指南：如何判断一个工作室的学术脉络是否适合自己？',
      summary: '别只听学长一句“给分高”或“很松”，去看往届毕业展和导师近五年的创作脉络。',
      body: '分工作室前最有效的方法有三个：\n1. 去图书馆翻近三年的毕业作品集，看该工作室学生的作品是千篇一律像导师，还是百花齐放各具面貌；\n2. 旁听至少两节该工作室的日常评图，观察导师是如何提问的；\n3. 诚实评估自己需要“强框架训练”还是“高自由度探索”。',
      category: '工作室',
      college: '油画系',
      major: '绘画基础',
      year: '2024级',
      tags: ['工作室分流', '专业选择'],
      author: P.p7,
      image: IMAGES.studioPainting,
      readMinutes: 4,
      likeCount: 119,
      bookmarkCount: 132,
      isLiked: false,
      isBookmarked: false,
      comments: []
    }
  ];

  return {
    IMAGES,
    COPYRIGHT,
    CROSS_COLLEGE_NOTICE,
    CAFA_CAMPUS_COLLEGES,
    CAFA_COLLEGES,
    CAFA_LOCATIONS,
    COURSE_TYPES,
    COURSE_CATEGORIES,
    ATTENDANCE_OPTIONS,
    EXAM_OPTIONS,
    SUITABLE_OPTIONS,
    COHORT_OPTIONS,
    FIXED_PUBLISH_TAGS,
    CATEGORIES,
    CAMPUS_LIFE_TILES,
    AVATAR_PALETTES,
    INITIAL_HOT_TOPICS,
    INITIAL_POSTS,
    INITIAL_COURSES,
    INITIAL_EXPERIENCES,
    nextUniqueNickname,
    sampleNicknameBatch,
    sampleDistinctAvatarSeeds
  };
})();
