package br.com.sosmanaus.app.fronteira.api

import br.com.sosmanaus.app.controle.portas.AutenticacaoGateway
import br.com.sosmanaus.app.controle.portas.FalhaAutenticacao
import br.com.sosmanaus.app.controle.portas.SessaoAutenticada
import br.com.sosmanaus.app.controle.portas.TipoFalha
import br.com.sosmanaus.app.entidade.Credenciais
import br.com.sosmanaus.app.entidade.Perfil
import br.com.sosmanaus.app.entidade.Usuario
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

/**
 * FRONTEIRA com a API: implementa a porta AutenticacaoGateway.
 *
 * Traduz entidades em DTOs (e vice-versa) e erros HTTP em FalhaAutenticacao,
 * para que nada de HTTP vaze para o controle.
 */
class AutenticacaoApiGateway(private val api: SosManausApi) : AutenticacaoGateway {

    override suspend fun autenticar(credenciais: Credenciais): SessaoAutenticada =
        chamar(seNaoAutorizado = TipoFalha.CREDENCIAIS_INVALIDAS) {
            val resposta = api.login(LoginRequest(credenciais.email, credenciais.senha, CANAL_APP_CIDADAO))
            SessaoAutenticada(resposta.accessToken, resposta.usuario.paraEntidade())
        }

    override suspend fun buscarUsuarioLogado(token: String): Usuario =
        chamar(seNaoAutorizado = TipoFalha.SESSAO_EXPIRADA) {
            api.me("Bearer $token").paraEntidade()
        }

    /** Executa a chamada HTTP e converte qualquer erro técnico em FalhaAutenticacao. */
    private suspend fun <T> chamar(seNaoAutorizado: TipoFalha, bloco: suspend () -> T): T = try {
        bloco()
    } catch (e: HttpException) {
        throw traduzir(e, seNaoAutorizado)
    } catch (e: IOException) {
        throw FalhaAutenticacao(TipoFalha.SEM_CONEXAO, "Não foi possível conectar ao servidor. Verifique sua internet.")
    } catch (e: SerializationException) {
        throw FalhaAutenticacao(TipoFalha.INESPERADA, "Resposta inesperada do servidor.")
    } catch (e: IllegalArgumentException) {
        throw FalhaAutenticacao(TipoFalha.INESPERADA, "Resposta inesperada do servidor.")
    }

    private fun traduzir(e: HttpException, seNaoAutorizado: TipoFalha): FalhaAutenticacao {
        val mensagemServidor = lerMensagem(e)
        return when (e.code()) {
            401 -> FalhaAutenticacao(seNaoAutorizado, mensagemServidor ?: "E-mail ou senha incorretos.")
            403 -> FalhaAutenticacao(TipoFalha.ACESSO_NEGADO, mensagemServidor ?: "Acesso negado.")
            429 -> FalhaAutenticacao(TipoFalha.MUITAS_TENTATIVAS, "Muitas tentativas. Aguarde um minuto e tente novamente.")
            else -> FalhaAutenticacao(TipoFalha.INESPERADA, mensagemServidor ?: "Erro inesperado (código ${e.code()}).")
        }
    }

    /** A API devolve { "message": "texto" } ou { "message": ["texto", ...] }. */
    private fun lerMensagem(e: HttpException): String? = runCatching {
        val corpo = e.response()?.errorBody()?.string().orEmpty()
        val message = ClienteHttp.json.parseToJsonElement(corpo).jsonObject["message"]
        if (message is JsonArray) message.first().jsonPrimitive.content else message?.jsonPrimitive?.content
    }.getOrNull()

    private fun UsuarioDto.paraEntidade() = Usuario(id, nome, email, Perfil.valueOf(perfil))

    private companion object {
        const val CANAL_APP_CIDADAO = "APP_CIDADAO"
    }
}
