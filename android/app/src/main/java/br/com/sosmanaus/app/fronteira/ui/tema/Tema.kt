package br.com.sosmanaus.app.fronteira.ui.tema

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta baseada no protótipo do Magic Patterns (verde amazônico + azul)
val Verde = Color(0xFF155E4B)
val VerdeClaro = Color(0xFFE3EFEA)
val Azul = Color(0xFF1A6FA8)
val Fundo = Color(0xFFF1F4F2)
val Cartao = Color(0xFFF6F8F7)
val Branco = Color(0xFFFFFFFF)
val Texto = Color(0xFF13201B)
val TextoSuave = Color(0xFF5C6B65)
val Borda = Color(0xFFDDE3E0)
val Erro = Color(0xFFC62828)
val ErroFundo = Color(0xFFFDECEA)

private val Cores = lightColorScheme(
    primary = Verde,
    onPrimary = Branco,
    secondary = Azul,
    onSecondary = Branco,
    background = Fundo,
    onBackground = Texto,
    surface = Cartao,
    onSurface = Texto,
    onSurfaceVariant = TextoSuave,
    outline = Borda,
    error = Erro,
)

@Composable
fun SosManausTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Cores, content = content)
}
