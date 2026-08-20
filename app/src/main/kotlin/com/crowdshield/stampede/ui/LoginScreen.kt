package com.crowdshield.stampede.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ─── Design tokens (Light Safety Theme) ─────────────────────────────────────
private val LightBgMain       = Color(0xFFF6F8FA)
private val LightBgSubtle     = Color(0xFFEEF2F6)
private val LightCardBg       = Color(0xFFFFFFFF)
private val LightCardBorder   = Color(0xFFE2E8F0)
private val TealPrimary       = Color(0xFF0D6E6E)
private val TealPrimaryDark   = Color(0xFF084C4C)
private val TealSubtle        = Color(0xFFE6F4F2)
private val EmeraldAccent     = Color(0xFF059669)
private val EmeraldSubtle     = Color(0xFFECFDF5)
private val TextPrimary       = Color(0xFF0F172A)
private val TextSecondary     = Color(0xFF64748B)
private val TextMuted         = Color(0xFF94A3B8)
private val StaffAmber        = Color(0xFFB45309)
private val StaffAmberBg      = Color(0xFFFFFBEB)
private val StaffAmberBorder  = Color(0xFFFDE68A)
private val StaffAmberDark    = Color(0xFF78350F)

// ─── Login Mode ──────────────────────────────────────────────────────────────
private enum class LoginMode { PUBLIC, STAFF }

// ─── Login Root ──────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onPublicLogin: () -> Unit,
    onStaffLogin: () -> Unit
) {
    var mode by remember { mutableStateOf(LoginMode.PUBLIC) }

    // Subtle gentle animation for background radar & mesh
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(LightBgMain, LightBgSubtle, LightBgMain)))
    ) {
        // ── Dynamic background mesh (Subtle light safety infrastructure) ─────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrowdMeshBackground(pulse, glow)
        }

        // ── Content ─────────────────────────────────────────────────────────
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(tween(250)) + slideInVertically { it / 10 })
                    .togetherWith(fadeOut(tween(180)) + slideOutVertically { -it / 10 })
            },
            label = "loginMode"
        ) { currentMode ->
            when (currentMode) {
                LoginMode.PUBLIC -> PublicLoginPanel(
                    onContinue = onPublicLogin,
                    onSwitchToStaff = { mode = LoginMode.STAFF },
                    glow = glow
                )
                LoginMode.STAFF -> StaffLoginPanel(
                    onSignIn = onStaffLogin,
                    onBack = { mode = LoginMode.PUBLIC },
                    glow = glow
                )
            }
        }
    }
}

// ─── Public Login Panel ───────────────────────────────────────────────────────
@Composable
private fun PublicLoginPanel(
    onContinue: () -> Unit,
    onSwitchToStaff: () -> Unit,
    glow: Float
) {
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo Area
        LogoBadge(glow = glow)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CrowdShield",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(EmeraldAccent)
            )
            Text(
                text = "Keep the crowd safe.",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary,
                letterSpacing = 0.2.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // White Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0x1A0D6E6E),
                    ambientColor = Color(0x0A000000)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightCardBg),
            border = BorderStroke(1.dp, LightCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TealSubtle)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PUBLIC ACCESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = {
                        Text("Phone Number", color = TextSecondary, fontSize = 13.sp)
                    },
                    placeholder = {
                        Text("Enter your phone number", color = TextMuted, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = LightCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = TealPrimary,
                        focusedContainerColor = Color(0xFFFAFCFC),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Forgot text
        Text(
            text = "Forgot your number?",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier
                .clickable { }
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Staff Access Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(LightCardBorder))
            Text(
                text = "   OR   ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Box(modifier = Modifier.weight(1f).height(1.dp).background(LightCardBorder))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Staff Access Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSwitchToStaff),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StaffAmberBg),
            border = BorderStroke(1.dp, StaffAmberBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFDE68A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = StaffAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Staff Login",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StaffAmberDark
                        )
                        Text(
                            text = "Authorized safety personnel",
                            fontSize = 12.sp,
                            color = StaffAmber
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = StaffAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

// ─── Staff Login Panel ────────────────────────────────────────────────────────
@Composable
private fun StaffLoginPanel(
    onSignIn: () -> Unit,
    onBack: () -> Unit,
    glow: Float
) {
    var staffId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LightCardBg)
                    .border(1.dp, LightCardBorder, CircleShape)
                    .clickable(onClick = onBack)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Staff Logo Badge
        LogoBadge(glow = glow, isStaff = true)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CrowdShield",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(StaffAmberBg)
                .border(1.dp, StaffAmberBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                text = "STAFF PORTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StaffAmberDark,
                letterSpacing = 1.8.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // White Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0x1AB45309),
                    ambientColor = Color(0x0A000000)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightCardBg),
            border = BorderStroke(1.dp, StaffAmberBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = staffId,
                    onValueChange = { staffId = it },
                    label = {
                        Text("Staff ID / Email", color = TextSecondary, fontSize = 13.sp)
                    },
                    placeholder = {
                        Text("e.g. STF-8092", color = TextMuted, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = StaffAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StaffAmber,
                        unfocusedBorderColor = LightCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = StaffAmber,
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text("Password", color = TextSecondary, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = StaffAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StaffAmber,
                        unfocusedBorderColor = LightCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = StaffAmber,
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StaffAmberDark,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StaffAmber)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Authorized personnel only",
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StaffAmber)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

// ─── Logo Badge ───────────────────────────────────────────────────────────────
@Composable
private fun LogoBadge(glow: Float, isStaff: Boolean = false) {
    val brandColor = if (isStaff) StaffAmber else TealPrimary
    val containerBg = if (isStaff) StaffAmberBg else TealSubtle

    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(containerBg)
            .border(
                width = 1.5.dp,
                color = brandColor.copy(alpha = 0.25f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "CrowdShield",
            tint = brandColor,
            modifier = Modifier.size(42.dp)
        )
    }
}

// ─── Background Canvas (Subtle Light Theme Grid & Nodes) ───────────────────────
private fun DrawScope.drawCrowdMeshBackground(pulse: Float, glow: Float) {
    val w = size.width
    val h = size.height

    // Grid lines — clean architectural safety blueprint
    val gridStep = 56.dp.toPx()
    val gridColor = Color(0xFF0D6E6E).copy(alpha = 0.035f)
    var x = 0f
    while (x < w) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 0.8f)
        x += gridStep
    }
    var y = 0f
    while (y < h) {
        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.8f)
        y += gridStep
    }

    // Anchor nodes
    val nodes = listOf(
        Offset(w * 0.15f, h * 0.16f),
        Offset(w * 0.72f, h * 0.12f),
        Offset(w * 0.88f, h * 0.30f),
        Offset(w * 0.22f, h * 0.40f),
        Offset(w * 0.65f, h * 0.44f),
        Offset(w * 0.12f, h * 0.68f),
        Offset(w * 0.82f, h * 0.70f),
        Offset(w * 0.48f, h * 0.84f),
        Offset(w * 0.90f, h * 0.90f)
    )

    // Connecting dashed lines
    val maxEdgeDist = (w * 0.42f)
    for (i in nodes.indices) {
        for (j in (i + 1) until nodes.size) {
            val dx = nodes[i].x - nodes[j].x
            val dy = nodes[i].y - nodes[j].y
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
            if (dist < maxEdgeDist) {
                val alpha = (1f - dist / maxEdgeDist) * 0.08f
                drawLine(
                    color = Color(0xFF0D6E6E).copy(alpha = alpha),
                    start = nodes[i],
                    end = nodes[j],
                    strokeWidth = 0.8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 14f), pulse * 22f)
                )
            }
        }
    }

    // Draw nodes
    for ((idx, node) in nodes.withIndex()) {
        val r = (3 + (idx % 3) * 1.5f).dp.toPx()
        drawCircle(
            color = Color(0xFF0D6E6E).copy(alpha = 0.06f + 0.04f * glow),
            radius = r * 2.2f,
            center = node
        )
        drawCircle(
            color = Color(0xFF0D6E6E).copy(alpha = 0.20f),
            radius = r * 0.8f,
            center = node
        )
    }

    // Subtle radar arc
    val radarCenter = Offset(w * 0.5f, h * 1.15f)
    val maxRadius = h * 0.95f
    for (ring in 0..2) {
        val phase = ((pulse + ring * 0.33f) % 1f)
        val radius = maxRadius * phase
        val alpha = (1f - phase) * 0.05f
        drawCircle(
            color = Color(0xFF0D6E6E).copy(alpha = alpha),
            radius = radius,
            center = radarCenter,
            style = Stroke(width = 1.2f)
        )
    }
}
