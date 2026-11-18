package br.com.sosmanaus.app.controle.portas

/**
 * PORTA para guardar o token no aparelho.
 * Implementada na fronteira por `fronteira/armazenamento/ArmazenamentoSessaoDataStore`.
 */
interface ArmazenamentoSessao {
    suspend fun lerToken(): String?
    suspend fun salvarToken(token: String)
    suspend fun limpar()
}
