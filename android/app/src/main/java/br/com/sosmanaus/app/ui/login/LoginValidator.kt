package br.com.sosmanaus.app.ui.login

/** Validações locais, feitas antes de chamar a API. */
object LoginValidator {
    private val regexEmail = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    const val TAMANHO_MINIMO_SENHA = 6

    fun validarEmail(email: String): String? = when {
        email.isBlank() -> "Informe seu e-mail."
        !regexEmail.matches(email.trim()) -> "E-mail inválido."
        else -> null
    }

    fun validarSenha(senha: String): String? = when {
        senha.isEmpty() -> "Informe sua senha."
        senha.length < TAMANHO_MINIMO_SENHA -> "A senha deve ter pelo menos $TAMANHO_MINIMO_SENHA caracteres."
        else -> null
    }
}
