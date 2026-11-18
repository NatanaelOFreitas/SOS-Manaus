package br.com.sosmanaus.app.entidade

/**
 * ENTIDADE Credenciais: o que o cidadão informa para entrar, junto com as
 * regras que dizem se isso é aceitável antes mesmo de chamar a API.
 */
data class Credenciais(val email: String, val senha: String) {

    /** E-mail sem espaços e em minúsculas, como a API espera. */
    fun normalizadas(): Credenciais = copy(email = email.trim().lowercase())

    fun validar(): ErrosCredenciais = ErrosCredenciais(
        email = when {
            email.isBlank() -> "Informe seu e-mail."
            !REGEX_EMAIL.matches(email.trim()) -> "E-mail inválido."
            else -> null
        },
        senha = when {
            senha.isEmpty() -> "Informe sua senha."
            senha.length < TAMANHO_MINIMO_SENHA -> "A senha deve ter pelo menos $TAMANHO_MINIMO_SENHA caracteres."
            else -> null
        },
    )

    companion object {
        const val TAMANHO_MINIMO_SENHA = 8
        private val REGEX_EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}

data class ErrosCredenciais(val email: String?, val senha: String?) {
    val valido: Boolean get() = email == null && senha == null
}
