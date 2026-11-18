package br.com.sosmanaus.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sosmanaus.app.data.Usuario
import br.com.sosmanaus.app.ui.theme.Fundo
import br.com.sosmanaus.app.ui.theme.Texto
import br.com.sosmanaus.app.ui.theme.TextoSuave
import br.com.sosmanaus.app.ui.theme.Verde

/** Tela provisória pós-login — será substituída pela Tela Inicial do protótipo. */
@Composable
fun HomeScreen(usuario: Usuario, onSair: () -> Unit) {
    Scaffold(containerColor = Fundo) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Verde, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Olá, ${usuario.nome.substringBefore(' ')}!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Texto)
            Text("Login realizado com sucesso.", color = TextoSuave, modifier = Modifier.padding(top = 6.dp))
            Text(usuario.email, color = TextoSuave, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = onSair,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Verde),
            ) {
                Text("Sair", fontWeight = FontWeight.Bold)
            }
        }
    }
}
