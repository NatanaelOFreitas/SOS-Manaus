package br.com.sosmanaus.app.controle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.sosmanaus.app.entidade.Credenciais
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tudo o que a tela de login precisa desenhar. */
data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val senhaVisivel: Boolean = false,
    val erroEmail: String? = null,
    val erroSenha: String? = null,
    val erroGeral: String? = null,
    val carregando: Boolean = false,
)

/**
 * CONTROLE da tela de login.
 *
 * Recebe os eventos da fronteira (digitar, tocar em Entrar), usa a entidade
 * Credenciais para validar e delega o login ao SessaoControle. É um ViewModel
 * do Android só para sobreviver à rotação de tela; não conhece Compose.
 */
class LoginControle(private val sessao: SessaoControle) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun aoAlterarEmail(valor: String) =
        _ui.update { it.copy(email = valor, erroEmail = null, erroGeral = null) }

    fun aoAlterarSenha(valor: String) =
        _ui.update { it.copy(senha = valor, erroSenha = null, erroGeral = null) }

    fun alternarSenhaVisivel() =
        _ui.update { it.copy(senhaVisivel = !it.senhaVisivel) }

    fun entrar() {
        val atual = _ui.value
        if (atual.carregando) return

        val credenciais = Credenciais(atual.email, atual.senha)
        val erros = credenciais.validar() // regra da entidade
        if (!erros.valido) {
            _ui.update { it.copy(erroEmail = erros.email, erroSenha = erros.senha) }
            return
        }

        _ui.update { it.copy(carregando = true, erroGeral = null) }
        viewModelScope.launch {
            val resultado = sessao.entrar(credenciais)
            _ui.update {
                when (resultado) {
                    // No sucesso o SessaoControle muda o estado e a navegação abre a tela inicial
                    ResultadoLogin.Sucesso -> it.copy(carregando = false)
                    is ResultadoLogin.Falha -> it.copy(carregando = false, erroGeral = resultado.mensagem)
                }
            }
        }
    }

    companion object {
        fun fabrica(sessao: SessaoControle) = viewModelFactory {
            initializer { LoginControle(sessao) }
        }
    }
}
