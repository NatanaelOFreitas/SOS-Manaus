package br.com.sosmanaus.app.controle.portas

import br.com.sosmanaus.app.entidade.Credenciais
import br.com.sosmanaus.app.entidade.Usuario

/**
 * PORTA usada pelo controle para falar com o sistema externo (a API).
 * O controle não sabe que existe HTTP, Retrofit ou JSON; quem implementa
 * isso é a fronteira `fronteira/api/AutenticacaoApiGateway`.
 */
interface AutenticacaoGateway {
    /** @throws FalhaAutenticacao */
    suspend fun autenticar(credenciais: Credenciais): SessaoAutenticada

    /** @throws FalhaAutenticacao */
    suspend fun buscarUsuarioLogado(token: String): Usuario
}

data class SessaoAutenticada(val token: String, val usuario: Usuario)

enum class TipoFalha { CREDENCIAIS_INVALIDAS, ACESSO_NEGADO, SESSAO_EXPIRADA, MUITAS_TENTATIVAS, SEM_CONEXAO, INESPERADA }

class FalhaAutenticacao(val tipo: TipoFalha, mensagem: String) : Exception(mensagem)
