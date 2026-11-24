/* ==========================================================================
   杨梅树窝 (YANGMEI TREE) — Automatic Text & Image Content Safety Moderation
   - Layer 1: Instant deterministic Chinese + English illegal/harmful compliance
     engine (Unicode NFKC + zero-width/separator anti-evasion + doxxing guard).
   - Layer 2: Client-side Canvas image pre-screening (MIME/format validation +
     conservative YCbCr explicit-flesh & deep blood-crimson graphic gore guard).
   - Layer 3: Optional serverless multimodal check (/.netlify/functions/moderate)
     with graceful timeout fallback so offline/static previews remain responsive.
   ========================================================================== */
(function () {
  'use strict';

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

  const ID_CARD_RE = /(?:^|[^\d])[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx](?:[^\d]|$)/;
  const ZERO_WIDTH_RE = /[\u200B-\u200D\uFEFF\u00AD]/g;
  const SEP_STRIP_RE = /[\s`~!@#$%^&*()\-_=+[\]{};:'",.<>/?\\|·—。，、；：？！“”‘’（）【】《》]+/g;

  function normalizeText(raw) {
    try {
      return String(raw || '').normalize('NFKC').replace(ZERO_WIDTH_RE, '').toLowerCase();
    } catch (_) {
      return String(raw || '').replace(ZERO_WIDTH_RE, '').toLowerCase();
    }
  }

  function escapeRegExp(s) {
    return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  function inspectText(content, blockedKeywords) {
    const trimmed = String(content || '').trim();
    if (!trimmed) {
      return { passed: false, category: 'EMPTY', reason: '内容不能为空' };
    }
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
            passed: false,
            category: cat.code,
            reason: `自动审查未通过：内容涉嫌包含${cat.label}，请修改后再发布。`
          };
        }
      }
    }

    if (ID_CARD_RE.test(trimmed)) {
      return {
        passed: false,
        category: 'PRIVACY_DOXXING',
        reason: '自动审查未通过：请勿发布身份证号等个人敏感隐私信息。'
      };
    }

    const personalList = Array.isArray(blockedKeywords) ? blockedKeywords : [];
    for (const kw of personalList) {
      const cleanKw = String(kw || '').trim();
      if (cleanKw && normalized.includes(normalizeText(cleanKw))) {
        return {
          passed: false,
          category: 'USER_BLOCKED',
          reason: `内容包含你设置的屏蔽词：「${cleanKw}」`
        };
      }
    }

    return { passed: true, category: 'SAFE', reason: '审查通过' };
  }

  function inspectImageDataUrl(dataUrl, indexLabel) {
    return new Promise((resolve) => {
      const src = String(dataUrl || '');
      if (!src) {
        resolve({ passed: true, category: 'SAFE', previewJpeg: '' });
        return;
      }
      // Allow bundled safe relative assets without re-scanning
      if (src.startsWith('./') || src.startsWith('app/') || src.startsWith('assets/')) {
        resolve({ passed: true, category: 'SAFE', previewJpeg: '' });
        return;
      }
      if (src.startsWith('data:') && !/^data:image\/(jpeg|jpg|png|webp|gif);base64,/i.test(src)) {
        resolve({
          passed: false,
          category: 'INVALID_MEDIA',
          reason: `${indexLabel || '所选图片'}格式不受支持，仅允许常见合规图片格式（JPG/PNG/WebP/GIF）。`
        });
        return;
      }

      const img = new Image();
      img.crossOrigin = 'anonymous';
      img.onload = function () {
        try {
          if (!img.naturalWidth || !img.naturalHeight) {
            resolve({
              passed: false,
              category: 'INVALID_MEDIA',
              reason: `${indexLabel || '所选图片'}无法解析，请重新选择有效图片。`
            });
            return;
          }

          // 96x96 offscreen pixel safety floor (matches Android ContentModerationService)
          const size = 96;
          const canvas = document.createElement('canvas');
          canvas.width = size;
          canvas.height = size;
          const ctx = canvas.getContext('2d', { willReadFrequently: true });
          ctx.drawImage(img, 0, 0, size, size);
          const data = ctx.getImageData(0, 0, size, size).data;

          const total = size * size;
          let totalSkin = 0;
          let centerPixels = 0;
          let centerSkin = 0;
          let bloodPixels = 0;

          for (let y = 0; y < size; y++) {
            const inCenterY = y >= 16 && y <= 79;
            for (let x = 0; x < size; x++) {
              const i = (y * size + x) * 4;
              const r = data[i];
              const g = data[i + 1];
              const b = data[i + 2];

              const cb = 128 - 0.168736 * r - 0.331264 * g + 0.5 * b;
              const cr = 128 + 0.5 * r - 0.418688 * g - 0.081312 * b;
              const maxRgb = Math.max(r, g, b);
              const minRgb = Math.min(r, g, b);
              const isSkin =
                r > 95 && g > 40 && b > 20 &&
                (maxRgb - minRgb) > 15 &&
                Math.abs(r - g) > 15 && r > g && r > b &&
                cb >= 77 && cb <= 127 && cr >= 133 && cr <= 173;

              if (isSkin) totalSkin++;
              if (inCenterY && x >= 16 && x <= 79) {
                centerPixels++;
                if (isSkin) centerSkin++;
              }
              if (r >= 110 && r <= 215 && g < 32 && b < 32 && (r - g) > 95) {
                bloodPixels++;
              }
            }
          }

          const totalSkinRatio = totalSkin / total;
          const centerSkinRatio = centerPixels > 0 ? centerSkin / centerPixels : 0;
          const bloodRatio = bloodPixels / total;

          if (totalSkinRatio > 0.72 && centerSkinRatio > 0.80) {
            resolve({
              passed: false,
              category: 'PORNOGRAPHY',
              reason: `${indexLabel || '所选图片'}疑似包含大面积裸露或低俗不适画面，已被自动审查拦截。`
            });
            return;
          }
          if (bloodRatio > 0.48) {
            resolve({
              passed: false,
              category: 'VIOLENCE_GORE',
              reason: `${indexLabel || '所选图片'}疑似包含重度血腥暴力画面，已被自动审查拦截。`
            });
            return;
          }

          // Compact JPEG thumbnail for server-side multimodal check
          let previewJpeg = '';
          try {
            const maxDim = 512;
            const scale = Math.min(1, maxDim / Math.max(img.naturalWidth, img.naturalHeight));
            const c2 = document.createElement('canvas');
            c2.width = Math.max(1, Math.round(img.naturalWidth * scale));
            c2.height = Math.max(1, Math.round(img.naturalHeight * scale));
            c2.getContext('2d').drawImage(img, 0, 0, c2.width, c2.height);
            previewJpeg = c2.toDataURL('image/jpeg', 0.72);
          } catch (_) {}

          resolve({ passed: true, category: 'SAFE', previewJpeg });
        } catch (_) {
          resolve({ passed: true, category: 'SAFE', previewJpeg: '' });
        }
      };
      img.onerror = function () {
        resolve({
          passed: false,
          category: 'INVALID_MEDIA',
          reason: `${indexLabel || '所选图片'}读取失败或已损坏，请更换有效图片。`
        });
      };
      img.src = src;
    });
  }

  async function inspectSubmission(opts) {
    const textParts = Array.isArray(opts && opts.textParts) ? opts.textParts : [];
    const mediaUrls = Array.isArray(opts && opts.mediaUrls) ? opts.mediaUrls : [];
    const blockedKeywords = Array.isArray(opts && opts.blockedKeywords) ? opts.blockedKeywords : [];

    const combinedText = textParts
      .map(s => String(s || '').trim())
      .filter(Boolean)
      .join('\n');

    // 1. Local deterministic text check
    if (combinedText) {
      const textRes = inspectText(combinedText, blockedKeywords);
      if (!textRes.passed) return textRes;
    }

    // 2. Local image canvas safety screening
    const compactImages = [];
    for (let i = 0; i < Math.min(4, mediaUrls.length); i++) {
      const item = mediaUrls[i];
      const src = typeof item === 'string' ? item : (item && item.src) || '';
      if (!src) continue;
      const imgRes = await inspectImageDataUrl(src, `第 ${i + 1} 张图片`);
      if (!imgRes.passed) return imgRes;
      if (imgRes.previewJpeg) compactImages.push(imgRes.previewJpeg);
    }

    // 3. Optional server-side multimodal check (Netlify Function) with short timeout
    if (combinedText || compactImages.length > 0) {
      try {
        const controller = typeof AbortController !== 'undefined' ? new AbortController() : null;
        const timer = controller ? setTimeout(() => controller.abort(), 8000) : null;
        const res = await fetch('/.netlify/functions/moderate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ text: combinedText, images: compactImages }),
          signal: controller ? controller.signal : undefined
        });
        if (timer) clearTimeout(timer);
        if (res.ok) {
          const data = await res.json();
          if (data && data.safe === false) {
            return {
              passed: false,
              category: data.category || 'SAFETY_BLOCKED',
              reason: data.reason || '自动审查未通过：内容或图片涉嫌违法违规及不良信息，禁止发布。'
            };
          }
        }
      } catch (_) {
        // Local text + pixel safety engine already validated cleanly
      }
    }

    return { passed: true, category: 'SAFE', reason: '审查通过' };
  }

  window.YANGMEI_MODERATION = {
    inspectText,
    inspectImageDataUrl,
    inspectSubmission
  };
})();
