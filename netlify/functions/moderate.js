/* ==========================================================================
   Netlify Function: Automatic Text & Image Content Safety Moderation
   - Runs strict server-side illegal & harmful Chinese/English compliance rules
     with Unicode NFKC + obfuscation separator stripping.
   - If GEMINI_API_KEY is configured in Netlify environment variables, also
     performs multimodal Gemini Vision + Text safety classification on both
     submitted text and attached images (including OCR & visual NSFW/gore).
   ========================================================================== */

const ILLEGAL_CATEGORIES = [
  {
    code: 'PORNOGRAPHY',
    label: '涉黄低俗或违法色情信息',
    patterns: [
      '色情', '黄片', '毛片', '约炮', '一夜情', '援交', '嫖娼', '卖淫', '招嫖',
      '裸聊', '裸照', '露点', '无码', '成人视频', '情色网站', '性交易', '色诱',
      '萝莉岛', '幼女', '炼铜', '恋童', '强奸', '迷奸', '轮奸', '偷拍裙底',
      'porn', 'pornhub', 'xvideos', 'nsfw', 'hentai', 'nude', 'escort', 'csam'
    ]
  },
  {
    code: 'DRUGS_WEAPONS',
    label: '毒品、违禁品或危险武器信息',
    patterns: [
      '冰毒', '海洛因', '可卡因', '摇头丸', '氯胺酮', 'k粉', '吸毒', '贩毒',
      '买大麻', '卖大麻', '大麻种子', '飞叶子', '致幻剂', '迷药', '听话水',
      '春药', '笑气出售', '买卖枪支', '买枪', '造枪', '土制炸弹', '制造炸药',
      '自制炸弹', '管制刀具出售', '雷管', '毒药配方', '剧毒购买',
      'meth', 'heroin', 'cocaine', 'buy gun', 'make a bomb'
    ]
  },
  {
    code: 'VIOLENCE_GORE',
    label: '暴力血腥、恐怖极端或危害公共安全内容',
    patterns: [
      '杀人教程', '砍人', '屠杀', '血腥分尸', '虐杀动物', '虐猫视频', '恐怖袭击',
      '报复社会', '无差别袭击', '纵火教程', '投毒', '爆炸袭击', '买凶杀人',
      'terrorist attack', 'mass shooting', 'beheading', 'gore video'
    ]
  },
  {
    code: 'SELF_HARM',
    label: '自残、自杀诱导或危险伤害信息',
    patterns: [
      '自杀教程', '教你怎么死', '一起自杀', '约死', '烧炭教程', '割腕教程',
      '跳楼教程', '安乐死药', '买安眠药自杀', '鼓励自残', '去死吧你',
      'kill yourself', 'suicide method', 'how to hang yourself'
    ]
  },
  {
    code: 'FRAUD_GAMBLING',
    label: '电信诈骗、网络赌博、洗钱或违法黑产信息',
    patterns: [
      '网络赌博', '网上赌场', '六合彩', '时时彩', '博彩代理', '棋牌上下分', '捕鱼下分',
      '洗钱', '跑分平台', '卖银行卡', '收银行卡', '办假证', '假毕业证', '假学生证',
      '假发票', '代考包过', '替考', '买答案', '考试作弊器', '裸贷', '套路贷',
      '校园贷放款', '刷单日赚', '兼职刷单', '窃听器', '针孔摄像头偷拍', '黑客接单',
      '盗号服务', '社工库', '查开房记录', '查个人信息', '传销拉人',
      'online casino', 'money laundering', 'fake id', 'carding'
    ]
  },
  {
    code: 'HATE_HARASSMENT',
    label: '人肉开盒、恶意侮辱诽谤或仇恨歧视内容',
    patterns: [
      '人肉搜索', '开盒挂人', '全网开盒', '挂身份证', '曝光家庭住址', '全家死光',
      '死全家', '操你妈', '草泥马', '傻逼滚', '贱货去死', '婊子', '智障滚',
      '种族清洗', '纳粹万岁', '法西斯万岁'
    ]
  },
  {
    code: 'EXTREMISM_CULT',
    label: '违法邪教或危害社会稳定的极端内容',
    patterns: [
      '邪教组织', '全能神', '法轮功', '煽动颠覆', '分裂国家', '暴乱集会', '非法传教'
    ]
  }
];

const ID_CARD_RE = /(?<!\d)[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx](?!\d)/;
const ZERO_WIDTH_RE = /[\u200B-\u200D\uFEFF\u00AD]/g;
const SEP_STRIP_RE = /[\s`~!@#$%^&*()\-_=+[\]{};:'",.<>/?\\|·—。，、；：？！“”‘’（）【】《》]+/g;

function normalizeText(raw) {
  return String(raw || '').normalize('NFKC').replace(ZERO_WIDTH_RE, '').toLowerCase();
}

function escapeRegExp(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function checkLocalText(rawText) {
  const trimmed = String(rawText || '').trim();
  if (!trimmed) return { safe: true, category: 'SAFE', reason: '审查通过' };
  const normalized = normalizeText(trimmed);
  const compacted = normalized.replace(SEP_STRIP_RE, '');

  for (const cat of ILLEGAL_CATEGORIES) {
    for (const word of cat.patterns) {
      const target = word.toLowerCase();
      const isAscii = /^[\x00-\x7F]+$/.test(target);
      const matched = isAscii
        ? new RegExp(`(^|[^a-z0-9])${escapeRegExp(target)}([^a-z0-9]|$)`).test(normalized)
        : (normalized.includes(target) || compacted.includes(target.replace(/\s+/g, '')));
      if (matched) {
        return {
          safe: false,
          category: cat.code,
          reason: `自动审查未通过：内容涉嫌包含${cat.label}，请修改后再发布。`
        };
      }
    }
  }
  if (ID_CARD_RE.test(trimmed)) {
    return {
      safe: false,
      category: 'PRIVACY_DOXXING',
      reason: '自动审查未通过：请勿发布身份证号等个人敏感隐私信息。'
    };
  }
  return { safe: true, category: 'SAFE', reason: '审查通过' };
}

function jsonResp(statusCode, body) {
  return {
    statusCode,
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-store'
    },
    body: JSON.stringify(body)
  };
}

exports.handler = async function (event) {
  if (event.httpMethod !== 'POST') {
    return jsonResp(405, { safe: false, reason: 'Method Not Allowed' });
  }

  let payload = {};
  try {
    payload = JSON.parse(event.body || '{}');
  } catch (_) {
    return jsonResp(400, { safe: false, reason: '无效的审查请求数据' });
  }

  const text = String(payload.text || '').slice(0, 4000);
  const images = Array.isArray(payload.images) ? payload.images.slice(0, 4) : [];

  // Layer 1: Server-side deterministic compliance rules
  const ruleCheck = checkLocalText(text);
  if (!ruleCheck.safe) {
    return jsonResp(200, ruleCheck);
  }

  // Layer 2: Optional Gemini Vision + Text multimodal AI moderation if GEMINI_API_KEY is configured
  const apiKey = (process.env.GEMINI_API_KEY || '').trim();
  if (!apiKey || (!text.trim() && images.length === 0)) {
    return jsonResp(200, { safe: true, category: 'SAFE', reason: '审查通过' });
  }

  try {
    const parts = [
      {
        text: [
          '你是中国高校校园社区「杨梅树窝」（中央美术学院）的自动内容与图片安全审查系统。',
          '请严格依据中国法律法规与校园社区安全规范，同时审查文字内容与附带图片。',
          '若包含涉黄低俗/裸露、暴力血腥/虐待动物、恐怖极端、毒品/武器、电信诈骗/赌博/代考假证/裸贷、自残自杀诱导、人肉开盒/隐私泄露/恶意辱骂、邪教/违法煽动，或图片中含违规画面及违法二维码，必须判定 safe=false。',
          '正常的校园吐槽、二手画材、失物招领、情绪倾诉、艺术作品/绘画雕塑/展览照片（不含真实色情裸露与血腥暴力）请判为 safe=true。',
          text.trim() ? `\n【待审文字】\n${text.trim()}` : '',
          images.length > 0 ? `\n【待审图片数量】：${images.length} 张` : ''
        ].join('\n')
      }
    ];

    for (const img of images) {
      const m = String(img || '').match(/^data:(image\/(?:jpeg|jpg|png|webp|gif));base64,([A-Za-z0-9+/=]+)$/);
      if (m && m[2].length < 2_000_000) {
        parts.push({
          inline_data: {
            mime_type: m[1] === 'image/jpg' ? 'image/jpeg' : m[1],
            data: m[2]
          }
        });
      }
    }

    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${encodeURIComponent(apiKey)}`;
    const aiRes = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ role: 'user', parts }],
        generationConfig: {
          responseMimeType: 'application/json',
          responseSchema: {
            type: 'OBJECT',
            properties: {
              safe: { type: 'BOOLEAN' },
              category: { type: 'STRING' },
              reason: { type: 'STRING' }
            },
            required: ['safe', 'category', 'reason']
          }
        }
      })
    });

    if (!aiRes.ok) {
      return jsonResp(200, { safe: true, category: 'LOCAL_FALLBACK', reason: '本地审查通过' });
    }

    const data = await aiRes.json();
    if (data.promptFeedback && data.promptFeedback.blockReason) {
      return jsonResp(200, {
        safe: false,
        category: 'SAFETY_BLOCKED',
        reason: '自动审查未通过：文字或图片触发安全红线，禁止发布违法或不良内容。'
      });
    }

    const candidate = data.candidates && data.candidates[0];
    if (candidate && candidate.finishReason === 'SAFETY') {
      return jsonResp(200, {
        safe: false,
        category: 'SAFETY_BLOCKED',
        reason: '自动审查未通过：内容或图片涉嫌违法违规信息，已被自动拦截。'
      });
    }

    const rawText = candidate && candidate.content && candidate.content.parts && candidate.content.parts[0] && candidate.content.parts[0].text;
    if (rawText) {
      const parsed = JSON.parse(rawText);
      const safe = Boolean(parsed.safe);
      return jsonResp(200, {
        safe,
        category: String(parsed.category || (safe ? 'SAFE' : 'OTHER_HARMFUL')),
        reason: safe
          ? '审查通过'
          : (parsed.reason ? `自动审查未通过：${parsed.reason}` : '自动审查未通过：内容或图片涉嫌违规不良信息，禁止发布。')
      });
    }
  } catch (err) {
    console.warn('[moderate] AI review fallback:', err && err.message);
  }

  return jsonResp(200, { safe: true, category: 'LOCAL_FALLBACK', reason: '本地审查通过' });
};
