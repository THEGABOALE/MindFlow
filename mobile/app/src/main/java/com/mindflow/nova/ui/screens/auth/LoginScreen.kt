package com.mindflow.nova.ui.screens.auth

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.mindflow.nova.BuildConfig
import com.mindflow.nova.R
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.session.SessionRepository
import com.mindflow.nova.data.session.SessionResult
import com.mindflow.nova.ui.theme.NovaBackground
import com.mindflow.nova.ui.theme.NovaLoginButton
import com.mindflow.nova.ui.theme.NovaLoginCard
import com.mindflow.nova.ui.theme.NovaLoginFieldBorder
import com.mindflow.nova.ui.theme.NovaText
import com.mindflow.nova.ui.theme.NovaTextSecondary
import kotlinx.coroutines.launch

/**
 * Pantalla de login (wireframe de Figma, página Playground, sección "Login").
 * Cubre los 6 estados del wireframe: vacío, campos completos, error de
 * validación, cargando, conectando con Google y error con Google.
 *
 * El botón de Google usa Credential Manager para pedirle a la cuenta de Google
 * del dispositivo un idToken, que se manda tal cual a POST /api/auth/login/google
 * (mismo backend que ya usa el login por ID). Si el dispositivo no tiene cuenta
 * de Google, el usuario cancela, o el correo no está registrado en ninguna
 * institución, cae en el estado de error del wireframe con un mensaje puntual.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    session: SessionRepository,
    onLoginSuccess: (SessionUser) -> Unit
) {
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGoogleConnecting by remember { mutableStateOf(false) }
    var googleErrorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordFocusRequester = remember { FocusRequester() }
    val canSubmit = loginId.isNotBlank() && password.isNotBlank() && !isSubmitting

    fun submit() {
        if (!canSubmit) return

        keyboardController?.hide()
        isSubmitting = true
        errorMessage = null

        scope.launch {
            when (val result = session.loginWithId(loginId, password)) {
                is SessionResult.Success -> onLoginSuccess(result.user)
                is SessionResult.Rejected -> errorMessage = result.message
                is SessionResult.Failure -> errorMessage = result.message
            }
            isSubmitting = false
        }
    }

    fun attemptGoogleSignIn() {
        isGoogleConnecting = true
        googleErrorMessage = null

        scope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    // false: que muestre cualquier cuenta de Google del dispositivo,
                    // no solo las que ya usaron esta app antes (recién estrenando).
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = credentialManager.getCredential(context = context, request = request)
                val credential = response.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

                    when (val result = session.loginWithGoogle(idToken)) {
                        is SessionResult.Success -> onLoginSuccess(result.user)
                        is SessionResult.Rejected -> googleErrorMessage = result.message
                        is SessionResult.Failure -> googleErrorMessage = result.message
                    }
                } else {
                    googleErrorMessage = "No se pudo leer la cuenta de Google."
                }
            } catch (e: GoogleIdTokenParsingException) {
                googleErrorMessage = "No se pudo leer la cuenta de Google."
            } catch (e: GetCredentialCancellationException) {
                googleErrorMessage = "Cancelaste el inicio de sesión con Google."
            } catch (e: NoCredentialException) {
                googleErrorMessage = "No se encontró ninguna cuenta de Google en este dispositivo."
            } catch (e: GetCredentialException) {
                googleErrorMessage = "No se pudo conectar con Google. Intentá de nuevo."
            }

            isGoogleConnecting = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
    ) {
        Image(
            painter = painterResource(R.drawable.login_wave_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = NovaLoginCard,
                        shape = RoundedCornerShape(topStart = 27.dp, topEnd = 27.dp)
                    )
                    .padding(horizontal = 29.dp, vertical = 28.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.nova_logo),
                    contentDescription = "NOVA",
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Emprende tu vuelo hacia la equidad.",
                    color = NovaText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                GoogleButton(
                    isConnecting = isGoogleConnecting,
                    onClick = ::attemptGoogleSignIn
                )

                if (googleErrorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = googleErrorMessage ?: "",
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x401D1B20))
                    Text(
                        text = "O inicia sesión con",
                        color = NovaText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x401D1B20))
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = loginId,
                    onValueChange = { loginId = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ID de usuario", color = NovaTextSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = loginFieldColors(),
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    placeholder = { Text("Contraseña", color = NovaTextSecondary, fontSize = 13.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    colors = loginFieldColors(),
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            submit()
                        }
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = ::submit,
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(39.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovaLoginButton,
                        contentColor = Color.White,
                        disabledContainerColor = NovaLoginButton.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (isSubmitting) "Cargando..." else "Continuar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "¿No recordás tu contraseña? Contactá a tu colegio",
                    color = Color(0xFF737378),
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    focusedBorderColor = NovaLoginFieldBorder,
    unfocusedBorderColor = NovaLoginFieldBorder,
    focusedTextColor = NovaText,
    unfocusedTextColor = NovaText
)

@Composable
private fun GoogleButton(isConnecting: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(39.dp)
            .background(Color.White, RoundedCornerShape(100.dp))
            .border(1.dp, Color(0x1A1D1B20), RoundedCornerShape(100.dp))
            .then(if (!isConnecting) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isConnecting) "Conectando con Google..." else "Continuar con Google",
                color = NovaText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
