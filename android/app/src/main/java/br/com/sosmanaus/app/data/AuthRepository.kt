package br.com.sosmanaus.app.data

import br.com.sosmanaus.app.data.local.TokenStorage
import br.com.sosmanaus.app.data.remote.LoginRequest
import br.com.sosmanaus.app.data.remote.NetworkModule
import br.com.sosmanaus.app.data.remote.SosManausApi
import br.com.sosmanaus.app.data.remote.UsuarioDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

data class Usuario(val id: String, val nome: String, val email: String, val perfil: String)

sealed interface EstadoSessao {
    data object Carregando : EstadoSessao
    data object Deslogado : EstadoSessao
    data class Logado(val usuario: Usuario) : EstadoSessao
}

sealed interface ResultadoLogin {
    data class Sucesso(val usuario: Usuario) : ResultadoLogin
    data class Falha(val mensagem: String) : ResultadoLogin
}

interface AuthRepository {
    val sessao: StateFlow<EstadoSessao>
    suspend fun restaurarSessao()
    suspend fun entrar(email: String, senha: String): ResultadoLogin
    suspend fun sair()
}

class AuthRepositoryImpl(
    private val api: SosManausApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    private val _sessao = MutableStateFlow<EstadoSessao>(EstadoSessao.Carregando)
    override val sessao: StateFlow<EstadoSessao> = _sessao.asStateFlow()

    override suspend fun restaurarSessao() {
        if (_sessao.value !is EstadoSessao.Carregando) return

        val token = tokenStorage.ler()
        if (token == null) {
            _sessao.value = EstadoSessao.Deslogado
            return
        }

        _sessao.value = try {
            EstadoSessao.Logado(api.me("Bearer $token").paraDominio())
        } catch (e: HttpException) {
            if (e.code() == 401) tokenStorage.limpar() // token expirado ou inválido
            EstadoSessao.Deslogado
        } catch (e: IOException) {
            EstadoSessao.Deslogado
        }
    }

    override suspend fun entrar(email: String, senha: String): ResultadoLogin {
        return try {
            val resposta = api.login(LoginRequest(email, senha))
            val usuario = resposta.usuario.paraDominio()

            // O app Android é exclusivo do cidadão; gestores usam o painel web
            if (usuario.perfil != PERFIL_CIDADAO) {
                return ResultadoLogin.Falha("Esta conta é de gestor. Acesse pelo painel web.")
            }

            tokenStorage.salvar(resposta.accessToken)
            _sessao.value = EstadoSessao.Logado(usuario)
            ResultadoLogin.Sucesso(usuario)
        } catch (e: HttpException) {
            ResultadoLogin.Falha(mensagemDeErro(e))
        } catch (e: IOException) {
            ResultadoLogin.Falha("Não foi possível conectar ao servidor. Verifique sua internet.")
        } catch (e: SerializationException) {
            ResultadoLogin.Falha("Resposta inesperada do servidor.")
        }
    }

    override suspend fun sair() {
        tokenStorage.limpar()
        _sessao.value = EstadoSessao.Deslogado
    }

    /** Lê o campo "message" que o NestJS devolve (texto ou lista de textos). */
    private fun mensagemDeErro(e: HttpException): String {
        if (e.code() == 429) return "Muitas tentativas. Aguarde um minuto e tente novamente."

        val doServidor = runCatching {
            val corpo = e.response()?.errorBody()?.string().orEmpty()
            val message = NetworkModule.json.parseToJsonElement(corpo).jsonObject["message"]
            if (message is JsonArray) message.first().jsonPrimitive.content
            else message?.jsonPrimitive?.content
        }.getOrNull()

        return doServidor ?: when (e.code()) {
            401 -> "E-mail ou senha incorretos."
            else -> "Ocorreu um erro inesperado (código ${e.code()})."
        }
    }

    private fun UsuarioDto.paraDominio() = Usuario(id, nome, email, perfil)

    private companion object {
        const val PERFIL_CIDADAO = "CIDADAO"
    }
}
