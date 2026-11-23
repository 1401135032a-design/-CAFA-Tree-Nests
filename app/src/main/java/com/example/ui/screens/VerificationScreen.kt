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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Simplified CAFA-only Campus Verification Screen.
 * Verifies only Central Academy of Fine Arts student/alumni membership
 * without requesting college, major, or graduation year.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    uiState: YangmeiUiState,
    onBack: () -> Unit,
    onSubmitVerification: (
        identityType: IdentityType,
        proofDescription: String,
        proofUri: String?
    ) -> Unit,
    onSimulateApprove: () -> Unit,
    onSimulateReject: () -> Unit,
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
    var proofLabel by remember { mutableStateOf(uiState.verificationRecord.proofDescription) }
    var proofUri by remember { mutableStateOf(uiState.verificationRecord.proofUri) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    val proofPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val uriStr = uri.toString()
            proofUri = uriStr
            val label = "已从相册上传央美身份凭证照片"
            proofLabel = label
            // Immediately approve verification once any student ID / card / admission letter photo is uploaded
            onSubmitVerification(selectedType, label, uriStr)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingCameraUri
        if (success && captured != null) {
            val uriStr = captured.toString()
            proofUri = uriStr
            val label = "已现场拍摄央美身份凭证照片"
            proofLabel = label
            // Immediately approve verification once photo is captured
            onSubmitVerification(selectedType, label, uriStr)
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
            // Soft Cool-Slate Gradient Hero
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
                        VerifyStepNode("01", "确认身份", true)
                        VerifyStepNode("02", "拍照/传图", proofUri != null || uiState.verificationStatus != VerificationStatus.UNVERIFIED)
                        VerifyStepNode("03", "立即通过", uiState.verificationStatus == VerificationStatus.VERIFIED)
                    }
                    EditorialHairline()
                }
            }

            if (uiState.verificationStatus == VerificationStatus.REVIEWING) {
                item(key = "verify_reviewing") {
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
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SlatePrimary)
                                )
                                Text(
                                    text = "身份核验中",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "已收到你的「中央美术学院 · ${uiState.verificationRecord.identityType.labelCn}」认证凭证。核验信息与社区匿名形象完全隔离，审核通过后即刻销毁原图。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            EditorialHairline()

                            Text(
                                text = "模拟审核演示",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onSimulateReject,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("verify_sim_reject_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("模拟未通过")
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(YangmeiPrimaryGradient)
                                        .clickable(onClick = onSimulateApprove)
                                        .testTag("verify_sim_approve_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "模拟审核通过",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PaperWhite
                                    )
                                }
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
                            text = "选择你的央美身份",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IdentityChoiceCard(
                                type = IdentityType.STUDENT,
                                description = "学生证 / 学生卡 / 录取通知书",
                                selected = selectedType == IdentityType.STUDENT,
                                onClick = { selectedType = IdentityType.STUDENT },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("verify_type_student")
                            )
                            IdentityChoiceCard(
                                type = IdentityType.ALUMNI,
                                description = "学生证 / 毕业证 / 校友凭证",
                                selected = selectedType == IdentityType.ALUMNI,
                                onClick = { selectedType = IdentityType.ALUMNI },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("verify_type_alumni")
                            )
                        }
                    }
                }

                // Step 2: CAFA Proof Only (No College / Major / Year!)
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

                            // Preview card if a photo has already been picked/captured
                            if (!proofUri.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, FluorescentGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .clickable { showPhotoSourceSheet = true }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = proofUri,
                                        contentDescription = "已选认证照片预览",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
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
                                                text = proofLabel.ifBlank { "已上传认证照片" },
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
                            }

                            Text(
                                text = "支持上传相册照片或现场拍摄学生证、学生卡、录取通知书等本校证明，提交后立即通过认证并解锁发帖。支持对姓名与学号打码。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(YangmeiPrimaryGradient)
                                    .clickable {
                                        if (proofUri != null) {
                                            onSubmitVerification(
                                                selectedType,
                                                proofLabel.ifBlank { "已上传央美身份凭证照片" },
                                                proofUri
                                            )
                                        } else {
                                            showPhotoSourceSheet = true
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
                                        text = "拍照 / 上传照片并立即认证",
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
                                text = "隐私承诺：认证资料仅用于中央美术学院本校边界核验，绝不公开展示，与你的匿名身份完全隔离。",
                                style = MaterialTheme.typography.bodySmall,
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
                    text = "可现场拍摄或从相册上传学生证、学生卡、录取通知书照片，提交后立即通过认证",
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
                            Text("上传手机相册中的学生证 / 学生卡 / 通知书图片", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
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

                // Soft translucent frosted identity card (no harsh black box)
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
                Text("重置为未认证访客（测试演示）", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outlineVariant)
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
