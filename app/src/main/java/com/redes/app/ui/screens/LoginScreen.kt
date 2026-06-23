package com.redes.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redes.app.R
import com.redes.app.ui.auth.AuthUiState
import com.redes.app.ui.theme.RedesAccent
import com.redes.app.ui.theme.RedesBrand
import com.redes.app.ui.theme.RedesConnected
import com.redes.app.ui.theme.RedesErrorSoft
import com.redes.app.ui.theme.RedesNight
import com.redes.app.ui.theme.RedesNightDeep
import com.redes.app.ui.theme.RedesNightSoft

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var entered by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { entered = true }
    LaunchedEffect(uiState.currentUser) {
        if (uiState.currentUser != null) isSuccess = true
    }

    val hasInput = uiState.email.isNotEmpty() || uiState.password.isNotEmpty()

    // ── Borde de la card ──────────────────────────────────────────────────────
    val borderTransition = rememberInfiniteTransition(label = "border")
    val dashPhase by borderTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (uiState.isSubmitting) 1400 else 2200, easing = LinearEasing)
        ),
        label = "phase",
    )
    val borderAlpha by borderTransition.animateFloat(
        initialValue = if (uiState.isSubmitting) 0.55f else 0.38f,
        targetValue = if (uiState.isSubmitting) 0.92f else 0.68f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (uiState.isSubmitting) 700 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )

    // ── Glow + escala del logo ────────────────────────────────────────────────
    val logoGlowAlpha by borderTransition.animateFloat(
        initialValue = if (uiState.isSubmitting) 0.35f else 0.18f,
        targetValue = if (uiState.isSubmitting || isSuccess) 0.65f else 0.30f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (uiState.isSubmitting) 800 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logoGlow",
    )
    val logoScale by borderTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState.isSubmitting) 1.09f else 1.02f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = if (uiState.isSubmitting) 800 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logoScale",
    )

    // ── Orbes flotantes (4) — mismo concepto que el web ──────────────────────
    val orbTransition = rememberInfiniteTransition(label = "orbs")

    // Orb 1: centro superior — azul brand
    val orb1Alpha by orbTransition.animateFloat(0.18f, 0.40f,
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o1a")
    val orb1Dx by orbTransition.animateFloat(-0.04f, 0.04f,
        infiniteRepeatable(tween(5400, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o1x")
    val orb1Dy by orbTransition.animateFloat(-0.025f, 0.025f,
        infiniteRepeatable(tween(4800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o1y")

    // Orb 2: inferior derecho — sky blue
    val orb2Alpha by orbTransition.animateFloat(0.10f, 0.26f,
        infiniteRepeatable(tween(5600, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o2a")
    val orb2Dx by orbTransition.animateFloat(0.02f, -0.045f,
        infiniteRepeatable(tween(6200, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o2x")
    val orb2Dy by orbTransition.animateFloat(0.03f, -0.03f,
        infiniteRepeatable(tween(5800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o2y")

    // Orb 3: izquierdo medio — púrpura profundo
    val orb3Alpha by orbTransition.animateFloat(0.09f, 0.22f,
        infiniteRepeatable(tween(3800, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o3a")
    val orb3Dx by orbTransition.animateFloat(-0.02f, 0.03f,
        infiniteRepeatable(tween(4400, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o3x")
    val orb3Dy by orbTransition.animateFloat(-0.02f, 0.035f,
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o3y")

    // Orb 4: superior derecho — brand suave
    val orb4Alpha by orbTransition.animateFloat(0.12f, 0.28f,
        infiniteRepeatable(tween(6000, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o4a")
    val orb4Dy by orbTransition.animateFloat(-0.04f, 0.02f,
        infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse), "o4y")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RedesNight, RedesNightSoft, RedesNightDeep)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        // ── Capa de orbes animados ────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Orb 1 — centro superior, brand blue
            val r1 = size.width * 0.58f
            val c1 = Offset(size.width * (0.5f + orb1Dx), size.height * (0.28f + orb1Dy))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RedesBrand.copy(alpha = orb1Alpha), Color.Transparent),
                    radius = r1, center = c1,
                ),
                radius = r1, center = c1,
            )
            // Orb 2 — inferior derecho, sky blue
            val r2 = size.width * 0.52f
            val c2 = Offset(size.width * (0.85f + orb2Dx), size.height * (0.78f + orb2Dy))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RedesAccent.copy(alpha = orb2Alpha), Color.Transparent),
                    radius = r2, center = c2,
                ),
                radius = r2, center = c2,
            )
            // Orb 3 — izquierdo medio, deep purple
            val r3 = size.width * 0.38f
            val c3 = Offset(size.width * (0.08f + orb3Dx), size.height * (0.55f + orb3Dy))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2F2850).copy(alpha = orb3Alpha * 1.8f), Color.Transparent),
                    radius = r3, center = c3,
                ),
                radius = r3, center = c3,
            )
            // Orb 4 — superior derecho, brand suave
            val r4 = size.width * 0.42f
            val c4 = Offset(size.width * 0.88f, size.height * (0.18f + orb4Dy))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RedesBrand.copy(alpha = orb4Alpha * 0.75f), Color.Transparent),
                    radius = r4, center = c4,
                ),
                radius = r4, center = c4,
            )
        }

        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(animationSpec = tween(350)) +
                slideInVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                    initialOffsetY = { 48 },
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ── Card con borde animado por estado ────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            drawContent()
                            val cornerPx = 28.dp.toPx()
                            val sw = when {
                                isSuccess || uiState.isSubmitting || uiState.errorMessage != null -> 1.8.dp.toPx()
                                else -> 1.dp.toPx()
                            }
                            val perimeter = 2f * (size.width + size.height)
                            when {
                                // connected → borde verde completo
                                isSuccess -> drawRoundRect(
                                    color = RedesConnected.copy(alpha = 0.82f),
                                    cornerRadius = CornerRadius(cornerPx),
                                    style = Stroke(sw),
                                )
                                // error → borde rojo
                                uiState.errorMessage != null -> drawRoundRect(
                                    color = RedesErrorSoft.copy(alpha = 0.85f),
                                    cornerRadius = CornerRadius(cornerPx),
                                    style = Stroke(sw),
                                )
                                // loading → dash rápido
                                uiState.isSubmitting -> {
                                    val dl = perimeter * 0.12f
                                    drawRoundRect(
                                        color = RedesAccent.copy(alpha = borderAlpha),
                                        cornerRadius = CornerRadius(cornerPx),
                                        style = Stroke(
                                            width = sw,
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(dl, perimeter - dl),
                                                phase = -dashPhase * perimeter,
                                            ),
                                        ),
                                    )
                                }
                                // con input → dash lento sutil
                                hasInput -> {
                                    val dl = perimeter * 0.05f
                                    drawRoundRect(
                                        color = RedesAccent.copy(alpha = borderAlpha * 0.6f),
                                        cornerRadius = CornerRadius(cornerPx),
                                        style = Stroke(
                                            width = sw,
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(dl, perimeter - dl),
                                                phase = -dashPhase * perimeter,
                                            ),
                                        ),
                                    )
                                }
                                // idle → borde blanco muy tenue
                                else -> drawRoundRect(
                                    color = Color(0x14FFFFFF),
                                    cornerRadius = CornerRadius(cornerPx),
                                    style = Stroke(sw),
                                )
                            }
                        }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xDD11131A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp, vertical = 28.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // ── Logo dentro del card ──────────────────────────
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(88.dp),
                            ) {
                                // Halo animado — cambia a verde en éxito
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    if (isSuccess) RedesConnected.copy(alpha = logoGlowAlpha)
                                                    else RedesAccent.copy(alpha = logoGlowAlpha * 0.85f),
                                                    Color.Transparent,
                                                )
                                            )
                                        )
                                )
                                // Círculo blanco sólido + logo agrandado (padding 10dp)
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .graphicsLayer { scaleX = logoScale; scaleY = logoScale }
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.redes_logo),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "REDES",
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 7.sp),
                                fontWeight = FontWeight.Light,
                                color = Color(0xFF7A90A8),
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = stringResource(R.string.login_title),
                                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = (-0.3).sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.login_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFCFD8E6),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = onEmailChanged,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.email_label)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = null,
                                        tint = if (uiState.emailError != null) RedesErrorSoft else Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next,
                                ),
                                singleLine = true,
                                isError = uiState.emailError != null,
                                supportingText = uiState.emailError?.let { { Text(it) } },
                                enabled = !uiState.isSubmitting && !isSuccess,
                                shape = RoundedCornerShape(16.dp),
                                colors = outlinedFieldColors(),
                            )
                            Text(
                                text = stringResource(R.string.login_email_hint),
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, start = 4.dp),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = onPasswordChanged,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.password_label)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = if (uiState.passwordError != null) RedesErrorSoft else Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        enabled = !uiState.isSubmitting && !isSuccess,
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                            tint = if (uiState.passwordError != null) RedesErrorSoft else Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                ),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                isError = uiState.passwordError != null,
                                supportingText = uiState.passwordError?.let { { Text(it) } },
                                enabled = !uiState.isSubmitting && !isSuccess,
                                shape = RoundedCornerShape(16.dp),
                                colors = outlinedFieldColors(),
                            )
                            Text(
                                text = stringResource(R.string.login_password_hint),
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp, start = 4.dp),
                            )
                            if (uiState.errorMessage != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    color = Color(0x18F87171),
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Warning,
                                            contentDescription = null,
                                            tint = RedesErrorSoft,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = uiState.errorMessage,
                                            color = RedesErrorSoft,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(
                                color = Color(0x10FFFFFF),
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                            Button(
                                onClick = onLoginClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = !uiState.isSubmitting && !isSuccess,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSuccess) RedesConnected else RedesAccent,
                                    contentColor = Color.White,
                                    disabledContainerColor = if (isSuccess) RedesConnected.copy(alpha = 0.75f) else Color(0xFF2A4A6B),
                                    disabledContentColor = Color(0x80FFFFFF),
                                ),
                            ) {
                                when {
                                    isSuccess -> {
                                        Text(
                                            text = "✓  Acceso concedido",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    uiState.isSubmitting -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White,
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = stringResource(R.string.login_loading),
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = stringResource(R.string.login_button),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.login_footer),
                                color = Color(0x80E2E8F0),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF0D1017),
    unfocusedContainerColor = Color(0xFF0D1017),
    disabledContainerColor = Color(0xFF0D1017),
    focusedBorderColor = RedesAccent,
    unfocusedBorderColor = Color(0x20FFFFFF),
    errorBorderColor = RedesErrorSoft,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Color(0x80FFFFFF),
    focusedLabelColor = RedesAccent,
    unfocusedLabelColor = Color(0xFF94A3B8),
    disabledLabelColor = Color(0x50FFFFFF),
    errorLabelColor = RedesErrorSoft,
    focusedSupportingTextColor = Color(0xFF94A3B8),
    unfocusedSupportingTextColor = Color(0xFF94A3B8),
    errorSupportingTextColor = RedesErrorSoft,
    cursorColor = Color.White,
    disabledBorderColor = Color(0x14FFFFFF),
)
