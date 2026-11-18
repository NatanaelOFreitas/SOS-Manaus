package br.com.sosmanaus.app.fronteira.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.sosmanaus.app.controle.LoginControle
import br.com.sosmanaus.app.controle.SessaoControle
import br.com.sosmanaus.app.entidade.EstadoSessao
import br.com.sosmanaus.app.fronteira.ui.home.HomeScreen
import br.com.sosmanaus.app.fronteira.ui.login.LoginScreen
import kotlinx.coroutines.launch

/**
 * FRONTEIRA: decide qual tela mostrar conforme o estado publicado pelo SessaoControle.
 */
@Composable
fun NavegacaoSosManaus(sessao: SessaoControle) {
    val estado by sessao.estado.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Ao abrir o app, tenta restaurar o login salvo
    LaunchedEffect(Unit) { sessao.restaurar() }

    when (val atual = estado) {
        EstadoSessao.Carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        EstadoSessao.Deslogado -> {
            val controle: LoginControle = viewModel(factory = LoginControle.fabrica(sessao))
            LoginScreen(controle)
        }

        is EstadoSessao.Logado -> HomeScreen(
            usuario = atual.usuario,
            onSair = { scope.launch { sessao.sair() } },
        )
    }
}
