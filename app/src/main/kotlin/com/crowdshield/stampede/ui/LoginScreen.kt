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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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

// ─── Design tokens ───────────────────────────────────────────────────────────
private val BgDark      = Color(0xFF060F1E)
private val BgMid       = Color(0xFF0A1F2F)
private val NodeGreen   = Color(0xFF00E5A0)
private val NodeGreenDim= Color(0x3300E5A0)
private val AccentBlue  = Color(0xFF1E8FFF)
private val AccentBlueDim = Color(0x221E8FFF)
private val GlassBg     = Color(0x18FFFFFF)
private val GlassBorder = Color(0x33FFFFFF)
private val OnGlass     = Color(0xFFE8F4F8)
private val OnGlassHint = Color(0x99E8F4F8)
private val StaffGold   = Color(0xFFFFBB44)
private val StaffGoldDim= Color(0x33FFBB44)

// ─── Login Mode ──────────────────────────────────────────────────────────────
private enum class LoginMode { PUBLIC, STAFF }

// ─── Login Root ──────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    onPublicLogin: () -> Unit,
    onStaffLogin: () -> Unit
) {
    var mode by remember { mutableStateOf(LoginMode.PUBLIC) }

    // Pulse animation for particles & radar rings
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDark, BgMid, BgDark)))
    ) {
        // ── Dynamic background ──────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrowdMeshBackground(pulse, glow)
        }

        // ── Content ─────────────────────────────────────────────────────────
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically { it / 8 })
                    .togetherWith(fadeOut(tween(200)) + slideOutVertically { -it / 8 })
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
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Logo Area
        LogoBadge(glow = glow)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "CrowdShield",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "Keep the crowd safe.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = NodeGreen.copy(alpha = 0.85f),
            letterSpacing = 0.4.sp
        )

        Spacer(modifier = Modifier.height(52.dp))

        // Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(GlassBg)
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Public Access",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnGlassHint,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = {
                        Text("Phone Number", color = OnGlassHint, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = NodeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NodeGreen,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NodeGreen,
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x08FFFFFF)
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NodeGreen,
                        contentColor = Color(0xFF060F1E)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
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

        Spacer(modifier = Modifier.height(28.dp))

        // Forgot text
        Text(
            text = "Forgot your number?",
            fontSize = 13.sp,
            color = OnGlassHint,
            modifier = Modifier.clickable { }
        )

        Spacer(modifier = Modifier.height(44.dp))

        // Staff Access Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(GlassBorder))
            Text(
                text = "  OR  ",
                fontSize = 11.sp,
                color = OnGlassHint,
                letterSpacing = 1.sp
            )
            Box(modifier = Modifier.weight(1f).height(1.dp).background(GlassBorder))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Staff Access button
        Text(
            text = "Are you authorized staff?",
            fontSize = 13.sp,
            color = OnGlassHint
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(StaffGoldDim)
                .border(1.dp, StaffGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .clickable(onClick = onSwitchToStaff)
                .padding(vertical = 14.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = StaffGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Staff Login  →",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StaffGold
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
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
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassBg)
                    .border(1.dp, GlassBorder, CircleShape)
                    .clickable(onClick = onBack)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = OnGlass,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Staff Logo Badge
        LogoBadge(glow = glow, isStaff = true)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "CrowdShield",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(StaffGoldDim)
                .border(1.dp, StaffGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "STAFF PORTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StaffGold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(44.dp))

        // Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(GlassBg)
                .border(
                    1.dp,
                    StaffGold.copy(alpha = 0.25f),
                    RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = staffId,
                    onValueChange = { staffId = it },
                    label = {
                        Text("Staff ID / Email", color = OnGlassHint, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = StaffGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StaffGold,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = StaffGold,
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x08FFFFFF)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text("Password", color = OnGlassHint, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = StaffGold,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = OnGlassHint,
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
                        focusedBorderColor = StaffGold,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = StaffGold,
                        focusedContainerColor = Color(0x12FFFFFF),
                        unfocusedContainerColor = Color(0x08FFFFFF)
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
                        containerColor = StaffGold,
                        contentColor = Color(0xFF1A0E00)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
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
                        fontWeight = FontWeight.ExtraBold
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
                    .background(StaffGold.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Authorized personnel only",
                fontSize = 12.sp,
                color = OnGlassHint,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StaffGold.copy(alpha = 0.7f))
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ─── Logo Badge ───────────────────────────────────────────────────────────────
@Composable
private fun LogoBadge(glow: Float, isStaff: Boolean = false) {
    val color = if (isStaff) StaffGold else NodeGreen
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        color.copy(alpha = 0.18f * glow),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = color.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "CrowdShield",
            tint = color,
            modifier = Modifier.size(40.dp)
        )
    }
}

// ─── Background Canvas ────────────────────────────────────────────────────────
private fun DrawScope.drawCrowdMeshBackground(pulse: Float, glow: Float) {
    val w = size.width
    val h = size.height

    // Grid lines — subtle blueprint feel
    val gridStep = 52.dp.toPx()
    val gridColor = Color(0xFF1E8FFF).copy(alpha = 0.07f)
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

    // Crowd node network — static anchor nodes
    val nodes = listOf(
        Offset(w * 0.15f, h * 0.18f),
        Offset(w * 0.70f, h * 0.10f),
        Offset(w * 0.88f, h * 0.32f),
        Offset(w * 0.25f, h * 0.42f),
        Offset(w * 0.60f, h * 0.45f),
        Offset(w * 0.10f, h * 0.70f),
        Offset(w * 0.80f, h * 0.72f),
        Offset(w * 0.45f, h * 0.82f),
        Offset(w * 0.90f, h * 0.90f)
    )

    // Draw connecting edges between nearby nodes
    val maxEdgeDist = (w * 0.42f)
    for (i in nodes.indices) {
        for (j in (i + 1) until nodes.size) {
            val dx = nodes[i].x - nodes[j].x
            val dy = nodes[i].y - nodes[j].y
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
            if (dist < maxEdgeDist) {
                val alpha = (1f - dist / maxEdgeDist) * 0.18f
                drawLine(
                    color = Color(0xFF00E5A0).copy(alpha = alpha),
                    start = nodes[i],
                    end = nodes[j],
                    strokeWidth = 0.8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 14f), pulse * 22f)
                )
            }
        }
    }

    // Draw nodes themselves
    for ((idx, node) in nodes.withIndex()) {
        val r = (4 + (idx % 3) * 2).dp.toPx()
        drawCircle(
            color = Color(0xFF00E5A0).copy(alpha = 0.15f + 0.12f * glow),
            radius = r * 2.2f,
            center = node
        )
        drawCircle(
            color = Color(0xFF00E5A0).copy(alpha = 0.55f),
            radius = r * 0.7f,
            center = node
        )
    }

    // Radar ring emanating from bottom-center
    val radarCenter = Offset(w * 0.5f, h * 1.15f)
    val maxRadius = h * 0.95f
    for (ring in 0..3) {
        val phase = ((pulse + ring * 0.25f) % 1f)
        val radius = maxRadius * phase
        val alpha = (1f - phase) * 0.12f
        drawCircle(
            color = Color(0xFF1E8FFF).copy(alpha = alpha),
            radius = radius,
            center = radarCenter,
            style = Stroke(width = 1.5f)
        )
    }

    // Accent arc for depth
    val arcPath = Path().apply {
        moveTo(-w * 0.1f, h * 0.35f)
        cubicTo(
            w * 0.2f, h * 0.15f,
            w * 0.8f, h * 0.55f,
            w * 1.1f, h * 0.30f
        )
    }
    drawPath(
        path = arcPath,
        color = Color(0xFF00E5A0).copy(alpha = 0.06f),
        style = Stroke(width = 2.dp.toPx())
    )
}
