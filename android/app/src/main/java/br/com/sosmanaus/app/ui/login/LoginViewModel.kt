package br.com.sosmanaus.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.sosmanaus.app.data.AuthRepository
import br.com.sosmanaus.app.data.ResultadoLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val senha: String = "",
    val senhaVisivel: Boolean = false,
    val erroEmail: String? = null,
    val erroSenha: String? = null,
    val erroGeral: String? = null,
    val carregando: Boolean = false,
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun onEmailChange(valor: String) =
        _ui.update { it.copy(email = valor, erroEmail = null, erroGeral = null) }

    fun onSenhaChange(valor: String) =
        _ui.update { it.copy(senha = valor, erroSenha = null, erroGeral = null) }

    fun alternarSenhaVisivel() =
        _ui.update { it.copy(senhaVisivel = !it.senhaVisivel) }

    fun entrar() {
        val estado = _ui.value
        if (estado.carregando) return

        val erroEmail = LoginValidator.validarEmail(estado.email)
        val erroSenha = LoginValidator.validarSenha(estado.senha)
        if (erroEmail != null || erroSenha != null) {
            _ui.update { it.copy(erroEmail = erroEmail, erroSenha = erroSenha) }
            return
        }

        _ui.update { it.copy(carregando = true, erroGeral = null) }
        viewModelScope.launch {
            val resultado = authRepository.entrar(estado.email.trim().lowercase(), estado.senha)
            _ui.update {
                when (resultado) {
                    // Em caso de sucesso, o AuthRepository troca a sessão e a MainActivity abre a tela inicial
                    is ResultadoLogin.Sucesso -> it.copy(carregando = false)
                    is ResultadoLogin.Falha -> it.copy(carregando = false, erroGeral = resultado.mensagem)
                }
            }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository) = viewModelFactory {
            initializer { LoginViewModel(authRepository) }
        }
    }
}
