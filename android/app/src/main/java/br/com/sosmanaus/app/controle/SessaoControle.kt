package br.com.sosmanaus.app.controle

import br.com.sosmanaus.app.controle.portas.ArmazenamentoSessao
import br.com.sosmanaus.app.controle.portas.AutenticacaoGateway
import br.com.sosmanaus.app.controle.portas.FalhaAutenticacao
import br.com.sosmanaus.app.controle.portas.TipoFalha
import br.com.sosmanaus.app.entidade.Credenciais
import br.com.sosmanaus.app.entidade.EstadoSessao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ResultadoLogin {
    data object Sucesso : ResultadoLogin
    data class Falha(val mensagem: String) : ResultadoLogin
}

/**
 * CONTROLE da sessão (casos de uso "Fazer login", "Restaurar sessão" e "Sair").
 *
 * Existe uma única instância no app (criada no AppContainer). Coordena a
 * API e o armazenamento local pelas portas e publica o estado da sessão
 * para a fronteira (telas) observar.
 */
class SessaoControle(
    private val gateway: AutenticacaoGateway,
    private val armazenamento: ArmazenamentoSessao,
) {
    private val _estado = MutableStateFlow<EstadoSessao>(EstadoSessao.Carregando)
    val estado: StateFlow<EstadoSessao> = _estado.asStateFlow()

    /** Ao abrir o app: se houver token salvo e ainda válido, entra direto. */
    suspend fun restaurar() {
        if (_estado.value !is EstadoSessao.Carregando) return

        val token = armazenamento.lerToken()
        if (token == null) {
            _estado.value = EstadoSessao.Deslogado
            return
        }

        _estado.value = try {
            EstadoSessao.Logado(gateway.buscarUsuarioLogado(token))
        } catch (falha: FalhaAutenticacao) {
            // Token vencido ou conta desativada: descarta. Sem internet: mantém o token para depois.
            if (falha.tipo == TipoFalha.SESSAO_EXPIRADA) armazenamento.limpar()
            EstadoSessao.Deslogado
        }
    }

    suspend fun entrar(credenciais: Credenciais): ResultadoLogin = try {
        val sessao = gateway.autenticar(credenciais.normalizadas())
        armazenamento.salvarToken(sessao.token)
        _estado.value = EstadoSessao.Logado(sessao.usuario)
        ResultadoLogin.Sucesso
    } catch (falha: FalhaAutenticacao) {
        ResultadoLogin.Falha(falha.message ?: "Não foi possível entrar. Tente novamente.")
    }

    suspend fun sair() {
        armazenamento.limpar()
        _estado.value = EstadoSessao.Deslogado
    }
}
