package com.redes.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.redes.app.R
import com.redes.app.ui.auth.AuthUiState
import com.redes.app.ui.theme.RedesAccent
import com.redes.app.ui.theme.RedesBlue
import com.redes.app.ui.theme.RedesCardDark
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
    val statusText = when {
        uiState.isSubmitting -> stringResource(R.string.login_status_loading)
        uiState.errorMessage != null -> stringResource(R.string.login_status_error)
        else -> stringResource(R.string.login_status_idle)
    }
    val statusColor = when {
        uiState.isSubmitting -> RedesAccent
        uiState.errorMessage != null -> RedesErrorSoft
        else -> Color(0xFFB9D6FF)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(RedesNight, RedesNightSoft, RedesNightDeep)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x3330518C), Color.Transparent),
                        radius = 900f
                    )
                )
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = RedesCardDark
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    color = Color(0x16000000),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .background(Color(0x10FFFFFF))
                        .padding(14.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.redes_logo),
                        contentDescription = "Logo REDES",
                        modifier = Modifier.width(68.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCFD8E6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(28.dp))
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.email_label)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    singleLine = true,
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError?.let { { Text(it) } },
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(16.dp),
                    colors = outlinedFieldColors(),
                )
                Text(
                    text = stringResource(R.string.login_email_hint),
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.password_label)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError?.let { { Text(it) } },
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(16.dp),
                    colors = outlinedFieldColors(),
                )
                Text(
                    text = stringResource(R.string.login_password_hint),
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                )
                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color(0x18F87171),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = RedesErrorSoft,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedesBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = stringResource(R.string.login_loading))
                    } else {
                        Text(text = stringResource(R.string.login_button))
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.login_footer),
                    color = Color(0xB3E2E8F0),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF0D1017),
    unfocusedContainerColor = Color(0xFF0D1017),
    disabledContainerColor = Color(0xFF0D1017),
    focusedBorderColor = Color(0xFF4D9DE0),
    unfocusedBorderColor = Color(0x26FFFFFF),
    errorBorderColor = RedesErrorSoft,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFFCFD8E6),
    unfocusedLabelColor = Color(0xFF94A3B8),
    errorLabelColor = RedesErrorSoft,
    focusedSupportingTextColor = Color(0xFF94A3B8),
    unfocusedSupportingTextColor = Color(0xFF94A3B8),
    errorSupportingTextColor = RedesErrorSoft,
    cursorColor = Color.White,
)
