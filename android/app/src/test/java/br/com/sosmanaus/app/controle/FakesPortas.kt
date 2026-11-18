package br.com.sosmanaus.app.controle

import br.com.sosmanaus.app.controle.portas.ArmazenamentoSessao
import br.com.sosmanaus.app.controle.portas.AutenticacaoGateway
import br.com.sosmanaus.app.controle.portas.SessaoAutenticada
import br.com.sosmanaus.app.entidade.Credenciais
import br.com.sosmanaus.app.entidade.Usuario

/**
 * Implementações falsas das portas. Permitem testar os controles sem
 * rede e sem Android, o que só é possível porque o controle depende das
 * interfaces, e não do Retrofit ou do DataStore.
 */
class GatewayFalso : AutenticacaoGateway {
    var respostaLogin: Result<SessaoAutenticada> = Result.failure(IllegalStateException("não configurado"))
    var respostaMe: Result<Usuario> = Result.failure(IllegalStateException("não configurado"))
    val credenciaisRecebidas = mutableListOf<Credenciais>()

    override suspend fun autenticar(credenciais: Credenciais): SessaoAutenticada {
        credenciaisRecebidas += credenciais
        return respostaLogin.getOrElse { throw it }
    }

    override suspend fun buscarUsuarioLogado(token: String): Usuario =
        respostaMe.getOrElse { throw it }
}

class ArmazenamentoFalso(var token: String? = null) : ArmazenamentoSessao {
    override suspend fun lerToken() = token
    override suspend fun salvarToken(token: String) {
        this.token = token
    }
    override suspend fun limpar() {
        token = null
    }
}
