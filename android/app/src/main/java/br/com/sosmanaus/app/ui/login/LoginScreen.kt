package br.com.sosmanaus.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sosmanaus.app.ui.theme.Azul
import br.com.sosmanaus.app.ui.theme.Borda
import br.com.sosmanaus.app.ui.theme.Branco
import br.com.sosmanaus.app.ui.theme.Cartao
import br.com.sosmanaus.app.ui.theme.Erro
import br.com.sosmanaus.app.ui.theme.ErroFundo
import br.com.sosmanaus.app.ui.theme.Fundo
import br.com.sosmanaus.app.ui.theme.SosManausTheme
import br.com.sosmanaus.app.ui.theme.Texto
import br.com.sosmanaus.app.ui.theme.TextoSuave
import br.com.sosmanaus.app.ui.theme.Verde
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun aviso(texto: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(texto)
        }
    }

    LoginConteudo(
        ui = ui,
        snackbar = snackbar,
        onEmailChange = viewModel::onEmailChange,
        onSenhaChange = viewModel::onSenhaChange,
        onAlternarSenha = viewModel::alternarSenhaVisivel,
        onEntrar = viewModel::entrar,
        onCriarConta = { aviso("O cadastro será liberado na próxima etapa do projeto.") },
        onEsqueciSenha = { aviso("A recuperação de senha será liberada em breve.") },
    )
}

@Composable
private fun LoginConteudo(
    ui: LoginUiState,
    snackbar: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onSenhaChange: (String) -> Unit,
    onAlternarSenha: () -> Unit,
    onEntrar: () -> Unit,
    onCriarConta: () -> Unit,
    onEsqueciSenha: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = Fundo,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
          val alturaTela = maxHeight
          // Centraliza o cartão na tela, mas permite rolar quando o teclado aparece
          Box(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .heightIn(min = alturaTela)
                .padding(horizontal = 20.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center,
          ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color(0x22000000)),
                shape = RoundedCornerShape(32.dp),
                color = Cartao,
            ) {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 36.dp)) {
                    Cabecalho()
                    Spacer(Modifier.height(28.dp))

                    AbasEntrarCriar(onCriarConta = onCriarConta)
                    Spacer(Modifier.height(24.dp))

                    if (ui.erroGeral != null) {
                        AlertaErro(ui.erroGeral)
                        Spacer(Modifier.height(16.dp))
                    }

                    Campo(
                        rotulo = "E-mail",
                        valor = ui.email,
                        onValorChange = onEmailChange,
                        placeholder = "voce@email.com",
                        icone = Icons.Outlined.MailOutline,
                        erro = ui.erroEmail,
                        habilitado = !ui.carregando,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    )
                    Spacer(Modifier.height(18.dp))

                    Campo(
                        rotulo = "Senha",
                        valor = ui.senha,
                        onValorChange = onSenhaChange,
                        placeholder = "••••••••",
                        icone = Icons.Outlined.Lock,
                        erro = ui.erroSenha,
                        habilitado = !ui.carregando,
                        visualTransformation = if (ui.senhaVisivel) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onEntrar()
                            },
                        ),
                        trailing = {
                            IconButton(onClick = onAlternarSenha) {
                                Icon(
                                    imageVector = if (ui.senhaVisivel) Icons.Outlined.VisibilityOff
                                    else Icons.Outlined.Visibility,
                                    contentDescription = if (ui.senhaVisivel) "Ocultar senha" else "Mostrar senha",
                                    tint = TextoSuave,
                                )
                            }
                        },
                    )

                    TextButton(
                        onClick = onEsqueciSenha,
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(
                            "Esqueci minha senha",
                            color = Azul,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onEntrar()
                        },
                        enabled = !ui.carregando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = Verde),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Verde,
                            disabledContainerColor = Verde.copy(alpha = 0.7f),
                        ),
                    ) {
                        if (ui.carregando) {
                            CircularProgressIndicator(
                                color = Branco,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp),
                            )
                        } else {
                            Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Branco)
                        }
                    }
                }
            }
          }
        }
    }
}

@Composable
private fun Cabecalho() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Verde),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Place, contentDescription = null, tint = Branco, modifier = Modifier.size(28.dp))
            }
            // Ponto azul do logo
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Azul)
                    .border(2.dp, Cartao, CircleShape),
            )
        }
        Spacer(Modifier.size(14.dp))
        Column {
            Text("SOS Manaus", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Texto)
            Text("Cidade conectada, cuidado compartilhado", fontSize = 12.sp, color = TextoSuave)
        }
    }
}

/** Abas do protótipo. "Entrar" fica selecionada; "Criar conta" será a próxima tela. */
@Composable
private fun AbasEntrarCriar(onCriarConta: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Branco)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Aba("Entrar", selecionada = true, onClick = {}, modifier = Modifier.weight(1f))
        Aba("Criar conta", selecionada = false, onClick = onCriarConta, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Aba(texto: String, selecionada: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selecionada) Verde else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            texto,
            color = if (selecionada) Branco else TextoSuave,
            fontWeight = if (selecionada) FontWeight.Bold else FontWeight.SemiBold,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun Campo(
    rotulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    placeholder: String,
    icone: ImageVector,
    erro: String?,
    habilitado: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column {
        Text(rotulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Texto)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextoSuave.copy(alpha = 0.7f)) },
            leadingIcon = { Icon(icone, contentDescription = null, tint = TextoSuave) },
            trailingIcon = trailing,
            isError = erro != null,
            supportingText = erro?.let { { Text(it) } },
            singleLine = true,
            enabled = habilitado,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Branco,
                unfocusedContainerColor = Branco,
                disabledContainerColor = Branco,
                errorContainerColor = Branco,
                focusedBorderColor = Verde,
                unfocusedBorderColor = Borda,
                cursorColor = Verde,
                focusedTextColor = Texto,
                unfocusedTextColor = Texto,
            ),
        )
    }
}

@Composable
private fun AlertaErro(mensagem: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ErroFundo)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Erro, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(8.dp))
        Text(mensagem, color = Erro, fontSize = 14.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF1F4F2, heightDp = 760)
@Composable
private fun LoginPreview() {
    SosManausTheme {
        LoginConteudo(
            ui = LoginUiState(),
            snackbar = remember { SnackbarHostState() },
            onEmailChange = {},
            onSenhaChange = {},
            onAlternarSenha = {},
            onEntrar = {},
            onCriarConta = {},
            onEsqueciSenha = {},
        )
    }
}
