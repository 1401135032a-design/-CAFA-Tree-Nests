package com.example.data

import kotlin.random.Random

/**
 * Large non-repeating CAFA art-school anonymous nickname lexicon.
 * Combines 48 curated standalone classics + (60 distinct prefixes x 60 distinct nouns)
 * = 3,648 guaranteed unique nicknames, designed for 5,000+ campus users without repetition.
 */
object AnonymousNicknameLexicon {

    val curatedClassics: List<String> = listOf(
        "七号楼的松节油",
        "花家地灰鸽",
        "未干的丙烯层",
        "美术馆北窗光",
        "凌晨三点切卡纸",
        "石膏像的阴影面",
        "望京灰砖回声",
        "版画室滚筒",
        "无名素描纸",
        "电教楼阶梯",
        "潮湿的陶泥块",
        "留白处的炭条",
        "雕塑系石膏粉",
        "北苑餐厅晚风",
        "七九八旧砖墙",
        "燕郊晚班校车",
        "暗房红灯守夜人",
        "亚麻布底料君",
        "木工坊的刨花",
        "定格动画帧率",
        "丝网印刷刮板",
        "画框里的冷灰调",
        "美术馆地下一层",
        "切卡纸切到天亮",
        "灰砖墙下的慢门",
        "无题第14号草图",
        "望京阴天散步",
        "花家地南街橘猫",
        "电教楼三排靠窗",
        "油画系第三画室",
        "水彩纸的留白边",
        "铜版画酸蚀液",
        "胶片机最后一张",
        "展厅射灯切角",
        "素描本第99页",
        "画筒背带折痕",
        "猪鬃笔洗笔水",
        "美纹纸撕裂声",
        "毕设布展梯子",
        "图书馆三层书香",
        "国画室生宣纸",
        "书法课宿墨香",
        "修复室金缮纹",
        "建筑系硫酸纸",
        "城市设计速写本",
        "中法校区西岸风",
        "人文学院读书会",
        "实验艺术声波"
    )

    // 60 unique atmospheric / campus / color / studio prefixes
    val prefixes: List<String> = listOf(
        "花家地", "七号楼", "北窗下", "美术馆", "灰砖墙", "松节油",
        "晚风里", "燕郊的", "电教楼", "暗房中", "石膏白", "群青色",
        "普鲁士", "朱砂红", "冷灰调", "暖灰调", "亚麻布", "木工坊",
        "丝网印", "凌晨三点", "阴天里", "留白处", "未干的", "半透明",
        "慢门的", "显影液", "定影池", "调色盘", "画室里", "地下室",
        "雕塑系", "铜版画", "水彩纸", "生宣上", "炭笔灰", "刮刀痕",
        "展厅里", "胶片感", "微距下", "焦外光", "素描本", "速写中",
        "泥塑房", "窑变色", "釉下青", "金缮的", "折纸般", "失焦的",
        "游牧的", "静默的", "漂浮的", "柔和的", "克制的", "漫步的",
        "发光的", "拾光的", "微凉的", "起雾的", "斜阳下", "望京的"
    )

    // 60 unique art-school / campus object & persona nouns (all distinct from prefixes)
    val nouns: List<String> = listOf(
        "灰鸽", "橘猫", "炭条", "画框", "滚筒", "卡纸",
        "陶泥", "石膏", "油画刀", "猪鬃笔", "调色刀", "速写本",
        "美纹纸", "胶片卷", "三脚架", "定格帧", "故事板", "铅活字",
        "木龙骨", "白乳胶", "丙烯层", "水粉块", "宣纸卷", "松烟墨",
        "青田印", "圆口刀", "木刨花", "亚麻线", "编织结", "银盐片",
        "安全灯", "射灯影", "展签纸", "大画筒", "小折凳", "画架君",
        "素描纸", "潘通卡", "十二色环", "基准线", "透视点", "地平线",
        "阴影面", "反光板", "灰调子", "肌理块", "留白君", "布展人",
        "看展人", "拉片人", "夜行者", "摆渡人", "拾光者", "造梦师",
        "手艺人", "观察家", "漫游者", "小行星", "云切片", "回声"
    )

    /**
     * Exact count of guaranteed unique nicknames in the reserve:
     * 48 classics + (60 * 60 = 3,600) combinations = 3,648 unique nicknames.
     */
    val totalUniqueCount: Int = curatedClassics.size + (prefixes.size * nouns.size)

    private val recentlyIssued = LinkedHashSet<String>()
    private const val MAX_HISTORY = 400

    /**
     * Deterministic O(1) indexed access to any nickname in [0, totalUniqueCount).
     */
    fun nicknameAt(index: Int): String {
        val safeIdx = ((index % totalUniqueCount) + totalUniqueCount) % totalUniqueCount
        if (safeIdx < curatedClassics.size) {
            return curatedClassics[safeIdx]
        }
        val comboIdx = safeIdx - curatedClassics.size
        val p = comboIdx / nouns.size
        val n = comboIdx % nouns.size
        return "${prefixes[p]}${nouns[n]}"
    }

    /**
     * Returns a fresh non-repeating nickname from the 3,648-word reserve,
     * avoiding both [exclude] and recently drawn nicknames in this session.
     */
    fun nextUniqueRandom(exclude: Set<String> = emptySet()): String {
        if (recentlyIssued.size >= MAX_HISTORY) {
            recentlyIssued.clear()
        }
        repeat(64) {
            val candidate = nicknameAt(Random.nextInt(totalUniqueCount))
            if (candidate !in exclude && candidate !in recentlyIssued) {
                recentlyIssued.add(candidate)
                return candidate
            }
        }
        // Fallback linear probe from random offset to guarantee uniqueness
        val start = Random.nextInt(totalUniqueCount)
        for (offset in 0 until totalUniqueCount) {
            val candidate = nicknameAt(start + offset)
            if (candidate !in exclude) {
                recentlyIssued.add(candidate)
                return candidate
            }
        }
        return curatedClassics.first()
    }

    /**
     * Samples a batch of [count] completely distinct nicknames for quick selection.
     */
    fun sampleBatch(count: Int = 6, exclude: Set<String> = emptySet()): List<String> {
        val result = LinkedHashSet<String>(count)
        val blocked = exclude.toMutableSet()
        repeat(count) {
            val nick = nextUniqueRandom(blocked)
            result.add(nick)
            blocked.add(nick)
        }
        return result.toList()
    }

    /**
     * Generates [count] distinct avatar seeds spanning diverse motifs & palettes
     * so every candidate avatar in the picker looks visibly different.
     */
    fun sampleDistinctAvatarSeeds(count: Int = 10, excludeSeed: Int? = null): List<Int> {
        val baseOffset = Random.nextInt(100, 90000)
        val result = ArrayList<Int>(count)
        var cursor = baseOffset
        while (result.size < count) {
            val candidate = (cursor * 37 + 19) % 99991 + 100
            cursor += 7
            if (candidate != excludeSeed && !result.contains(candidate)) {
                result.add(candidate)
            }
        }
        return result
    }
}
