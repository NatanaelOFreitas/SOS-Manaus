package br.com.sosmanaus.app.entidade

/** ENTIDADE: em que situação está a sessão do cidadão no aparelho. */
sealed interface EstadoSessao {
    data object Carregando : EstadoSessao
    data object Deslogado : EstadoSessao
    data class Logado(val usuario: Usuario) : EstadoSessao
}
