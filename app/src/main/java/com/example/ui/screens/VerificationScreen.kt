package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.YangmeiUiState
import java.io.File

/**
 * Rigorous CAFA-only Campus Verification Screen.
 * Requires identity selection, proof document type selection, actual photo upload/capture with preview,
 * authenticity & privacy confirmation, and a staged verification audit before unlocking posting.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VerificationScreen(
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onSubmitVerification: (
        identityType: IdentityType,
        proofDescription: String,
        proofUri: String?
    ) -> Unit,
    onCancelReview: () -> Unit,
    onResetForDemo: () -> Unit,
    onEnterCommunity: () -> Unit
) {
    if (uiState.verificationStatus == VerificationStatus.VERIFIED) {
        VerificationSuccessScreen(
            uiState = uiState,
            onEnterCommunity = onEnterCommunity,
            onResetDemo = onResetForDemo
        )
        return
    }

    val context = LocalContext.current
    val isDarkTheme = MaterialTheme.colorScheme.background.red < 0.2f
    var selectedType by remember { mutableStateOf(uiState.verificationRecord.identityType) }
    val proofTypeOptions = remember(selectedType) {
        if (selectedType == IdentityType.STUDENT) {
            listOf("学生证", "校园一卡通", "录取通知书", "教务系统截图")
        } else {
            listOf("毕业证书", "学位证书", "校友卡", "学生证留存")
        }
    }
    var selectedProofType by remember(selectedType) { mutableStateOf(proofTypeOptions.first()) }
    var proofLabel by remember { mutableStateOf(uiState.verificationRecord.proofDescription) }
    var proofUri by remember { mutableStateOf(uiState.verificationRecord.proofUri) }
    var agreedPolicy by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    val proofPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            proofUri = uri.toString()
            proofLabel = "已上传「$selectedProofType」照片（来自相册）"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingCameraUri
        if (success && captured != null) {
            proofUri = captured.toString()
            proofLabel = "已拍摄「$selectedProofType」照片（现场拍摄）"
        }
    }

    fun launchAlbumPicker() {
        proofPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun launchLiveCamera() {
        runCatching {
            val uri = createVerificationPhotoUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }.onFailure {
            Toast.makeText(context, "当前设备暂未找到可用相机应用，请尝试从相册上传", Toast.LENGTH_SHORT).show()
        }
    }

    val canSubmit = !proofUri.isNullOrBlank() && agreedPolicy

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "央美身份认证",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("verify_back_btn")) {
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
                .padding(innerPadding)
                .testTag("verification_scroll"),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Soft Cool-Slate Aurora Gradient Hero
            item(key = "verify_hero") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                ) {
                    FluidGrayHeroCanvas(
                        modifier = Modifier.fillMaxSize(),
                        darkVariant = true,
                        seed = 42
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "中央美术学院 · CAFA",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.80f)
                            )
                            Text(
                                text = uiState.verificationStatus.labelCn,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "确认央美身份",
                                style = MaterialTheme.typography.headlineLarge,
                                color = PaperWhite
                            )
                            Text(
                                text = "仅核验是否为中央美术学院在读学生或毕业校友，不收集学院与年级，认证资料严格保密不公开。",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.80f)
                            )
                        }
                    }
                }
            }

            // Clean 3-Step Indicator
            item(key = "verify_steps") {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerifyStepNode("01", "选择身份", true)
                        VerifyStepNode(
                            "02",
                            "上传凭证",
                            !proofUri.isNullOrBlank() || uiState.verificationStatus != VerificationStatus.UNVERIFIED
                        )
                        VerifyStepNode(
                            "03",
                            "核验通过",
                            uiState.verificationStatus == VerificationStatus.REVIEWING ||
                                uiState.verificationStatus == VerificationStatus.VERIFIED
                        )
                    }
                    EditorialHairline()
                }
            }

            if (uiState.verificationStatus == VerificationStatus.REVIEWING) {
                item(key = "verify_reviewing") {
                    val step = uiState.verificationAuditStep.coerceIn(1, 3)
                    val progress = when (step) {
                        1 -> 0.34f
                        2 -> 0.70f
                        else -> 0.96f
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.5.dp,
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.4.dp,
                                    color = AuroraIndigo
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "身份核验中",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "中央美术学院 · ${uiState.verificationRecord.identityType.labelCn}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(99.dp)),
                                color = AuroraIndigo,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            if (!uiState.verificationRecord.proofUri.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = uiState.verificationRecord.proofUri,
                                        contentDescription = "审核中凭证预览",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = uiState.verificationRecord.proofDescription.ifBlank { "央美身份凭证照片" },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "凭证已加密隔离，核验完成后自动脱敏销毁",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                AuditStageRow(
                                    title = "凭证图片清晰度与完整性校验",
                                    done = step > 1,
                                    active = step == 1
                                )
                                AuditStageRow(
                                    title = "中央美术学院（CAFA）本校标识核验",
                                    done = step > 2,
                                    active = step == 2
                                )
                                AuditStageRow(
                                    title = "生成隔离匿名社区身份并解锁权限",
                                    done = false,
                                    active = step >= 3
                                )
                            }

                            EditorialHairline()

                            OutlinedButton(
                                onClick = onCancelReview,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("verify_cancel_review_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("撤回并重新上传凭证")
                            }
                        }
                    }
                }
            } else {
                if (uiState.verificationStatus == VerificationStatus.REJECTED) {
                    item(key = "verify_rejected_notice") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(0.9.dp, VermilionRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .background(SoftRoseTint)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = VermilionRed, modifier = Modifier.size(18.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("上次认证未通过", style = MaterialTheme.typography.titleSmall, color = VermilionRed)
                                Text(
                                    text = uiState.verificationRecord.rejectReason ?: "请重新上传能清晰辨认中央美术学院标识的证明。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkBlack
                                )
                            }
                        }
                    }
                }

                // Step 1: Student or Alumni
                item(key = "identity_picker") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "第一步 · 选择你的央美身份",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IdentityChoiceCard(
                                type = IdentityType.STUDENT,
                                description = "学生证 / 一卡通 / 录取通知书",
                                selected = selectedType == IdentityType.STUDENT,
                                onClick = { selectedType = IdentityType.STUDENT },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("verify_type_student")
                            )
                            IdentityChoiceCard(
                                type = IdentityType.ALUMNI,
                                description = "毕业证 / 学位证 / 校友凭证",
                                selected = selectedType == IdentityType.ALUMNI,
                                onClick = { selectedType = IdentityType.ALUMNI },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("verify_type_alumni")
                            )
                        }
                    }
                }

                // Step 2: CAFA Proof Upload & Authenticity Confirmation
                item(key = "verify_form") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .then(
                                        if (isDarkTheme) Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                        else Modifier.background(YangmeiFeaturedCardGradient)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.School,
                                        contentDescription = null,
                                        tint = if (isDarkTheme) AuroraLightIndigo else SlateAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("认证院校", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "中央美术学院（CAFA）",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Proof document type selector
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "第二步 · 选择凭证类型",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    proofTypeOptions.forEach { option ->
                                        MonoTagChip(
                                            text = option,
                                            inverted = selectedProofType == option,
                                            onClick = { selectedProofType = option }
                                        )
                                    }
                                }
                            }

                            // Proof Photo Upload Zone or Preview Card
                            if (!proofUri.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, FluorescentGreen.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .clickable { showPhotoSourceSheet = true }
                                        .padding(10.dp)
                                        .testTag("verify_photo_preview_card"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = proofUri,
                                        contentDescription = "已选认证照片预览",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = null,
                                                tint = FluorescentGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = proofLabel.ifBlank { "已选择「$selectedProofType」照片" },
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = "点击可重新拍照或从相册更换照片",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(
                                            width = 1.dp,
                                            color = AuroraIndigo.copy(alpha = 0.42f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                        .clickable { showPhotoSourceSheet = true }
                                        .padding(horizontal = 16.dp, vertical = 22.dp)
                                        .testTag("verify_upload_zone"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(AuroraIndigo.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddAPhoto,
                                            contentDescription = "上传央美凭证照片",
                                            tint = if (isDarkTheme) AuroraLightIndigo else AuroraIndigo,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = "拍照或从相册上传「$selectedProofType」",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "需清晰可见中央美术学院（CAFA）校名或标识，姓名与学号可自行打码",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Mandatory authenticity & privacy checkbox
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable { agreedPolicy = !agreedPolicy }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("verify_policy_checkbox_row"),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .then(
                                            if (agreedPolicy) Modifier.background(YangmeiPrimaryGradient)
                                            else Modifier.border(1.3.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (agreedPolicy) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "已勾选确认",
                                            tint = PaperWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "我确认上传的为本人中央美术学院有效身份凭证，同意仅用于本校边界核验，审核后原图将脱敏销毁且绝不公开展示。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Primary Submit CTA
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .alpha(if (canSubmit) 1f else 0.52f)
                                    .background(YangmeiPrimaryGradient)
                                    .clickable {
                                        if (proofUri.isNullOrBlank()) {
                                            Toast.makeText(context, "请先拍照或从相册上传央美身份凭证照片", Toast.LENGTH_SHORT).show()
                                            showPhotoSourceSheet = true
                                        } else if (!agreedPolicy) {
                                            Toast.makeText(context, "请先勾选下方的凭证真实性与隐私核验确认", Toast.LENGTH_SHORT).show()
                                        } else {
                                            onSubmitVerification(
                                                selectedType,
                                                "$selectedProofType · ${proofLabel.ifBlank { "中央美术学院身份凭证" }}",
                                                proofUri
                                            )
                                        }
                                    }
                                    .testTag("verify_submit_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (proofUri.isNullOrBlank()) "请先上传凭证照片" else "提交认证审核",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PaperWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = PaperWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "未认证状态下仅支持浏览，完成核验后方可发帖、评论、投票及参与选课评价共建。",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPhotoSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoSourceSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "选择认证照片上传方式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "请上传清晰的「$selectedProofType」照片（含中央美术学院标识，可对姓名与学号打码）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    onClick = {
                        showPhotoSourceSheet = false
                        launchLiveCamera()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("verify_sheet_camera_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = AuroraFuchsia)
                        Column {
                            Text("现场拍照", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("调用相机实时拍摄本校身份证明", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Surface(
                    onClick = {
                        showPhotoSourceSheet = false
                        launchAlbumPicker()
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("verify_sheet_album_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = if (isDarkTheme) AuroraLightIndigo else AuroraIndigo)
                        Column {
                            Text("从相册选择照片", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("上传手机相册中的学生证 / 一卡通 / 通知书 / 毕业证图片", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun AuditStageRow(title: String, done: Boolean, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        done -> FluorescentGreen
                        active -> AuroraIndigo
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            } else if (active) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (active || done) FontWeight.SemiBold else FontWeight.Normal,
            color = if (active || done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
        )
    }
}

private fun createVerificationPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "verification_photos").apply {
        if (!exists()) mkdirs()
    }
    val file = File(dir, "cafa_verify_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@Composable
fun VerificationSuccessScreen(
    uiState: YangmeiUiState,
    onEnterCommunity: () -> Unit,
    onResetDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("verification_success_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 380.dp)
        ) {
            FluidGrayHeroCanvas(
                modifier = Modifier.fillMaxSize(),
                darkVariant = true,
                seed = uiState.currentPersona.avatarSeed
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "央美身份核验通过",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(FluorescentGreen)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "欢迎回到\n杨梅树下。",
                        style = MaterialTheme.typography.displayLarge,
                        color = PaperWhite
                    )
                    Text(
                        text = "你的中央美术学院${uiState.currentPersona.identityType.labelCn}身份已确认。接下来你将以独立匿名形象自由交流。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.80f)
                    )
                }

                // Soft translucent frosted identity card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.08f))
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AnonymousAvatar(seed = uiState.currentPersona.avatarSeed, size = 54.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "匿名身份已激活",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                        Text(
                            text = uiState.currentPersona.nickname,
                            style = MaterialTheme.typography.headlineMedium,
                            color = PaperWhite
                        )
                        Text(
                            text = "中央美术学院 · ${uiState.currentPersona.identityType.labelCn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "已解锁完整权限：发布帖子 · 匿名评论与回复 · 参与投票 · 选课评价共建",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(YangmeiPrimaryGradient)
                    .clickable(onClick = onEnterCommunity)
                    .testTag("verify_success_enter_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "进入杨梅树窝",
                        style = MaterialTheme.typography.titleMedium,
                        color = PaperWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PaperWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            TextButton(
                onClick = onResetDemo,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("重置为未认证访客", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun IdentityChoiceCard(
    type: IdentityType,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fg = if (selected) PaperWhite else MaterialTheme.colorScheme.onSurface
    val sub = if (selected) Color.White.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (selected) Modifier.background(YangmeiPrimaryGradient)
                else Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (type == IdentityType.STUDENT) Icons.Outlined.Badge else Icons.Outlined.School,
                contentDescription = null,
                tint = if (selected) PaperWhite else SlateAccent,
                modifier = Modifier.size(20.dp)
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "已选",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("${type.labelCn}认证", style = MaterialTheme.typography.titleMedium, color = fg)
            Text(description, style = MaterialTheme.typography.bodySmall, color = sub)
        }
    }
}

@Composable
private fun VerifyStepNode(index: String, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = index,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) SlatePrimary else MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
        )
    }
}
