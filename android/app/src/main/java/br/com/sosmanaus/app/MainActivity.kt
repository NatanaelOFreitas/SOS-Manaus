package br.com.sosmanaus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.sosmanaus.app.data.EstadoSessao
import br.com.sosmanaus.app.ui.home.HomeScreen
import br.com.sosmanaus.app.ui.login.LoginScreen
import br.com.sosmanaus.app.ui.login.LoginViewModel
import br.com.sosmanaus.app.ui.theme.SosManausTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authRepository = (application as SosManausApplication).container.authRepository

        setContent {
            SosManausTheme {
                val sessao by authRepository.sessao.collectAsStateWithLifecycle()
                val scope = rememberCoroutineScope()

                // Restaura o login salvo ao abrir o app
                LaunchedEffect(Unit) { authRepository.restaurarSessao() }

                when (val estado = sessao) {
                    EstadoSessao.Carregando -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    EstadoSessao.Deslogado -> {
                        val vm: LoginViewModel = viewModel(factory = LoginViewModel.factory(authRepository))
                        LoginScreen(vm)
                    }

                    is EstadoSessao.Logado -> HomeScreen(
                        usuario = estado.usuario,
                        onSair = { scope.launch { authRepository.sair() } },
                    )
                }
            }
        }
    }
}
