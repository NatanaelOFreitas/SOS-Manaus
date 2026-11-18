package br.com.sosmanaus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.sosmanaus.app.fronteira.ui.NavegacaoSosManaus
import br.com.sosmanaus.app.fronteira.ui.tema.SosManausTheme

/** Ponto de entrada do Android: só aplica o tema e entrega o controle de sessão à navegação. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessao = (application as SosManausApplication).container.sessaoControle

        setContent {
            SosManausTheme {
                NavegacaoSosManaus(sessao)
            }
        }
    }
}
