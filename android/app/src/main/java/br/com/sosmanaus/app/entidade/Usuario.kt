package br.com.sosmanaus.app.entidade

/** Perfis do sistema (documento de requisitos, seção 2). */
enum class Perfil { CIDADAO, GESTOR }

/**
 * ENTIDADE Usuario, como o app enxerga.
 * Kotlin puro: não depende de Android, Compose, Retrofit nem DataStore.
 */
data class Usuario(
    val id: String,
    val nome: String,
    val email: String,
    val perfil: Perfil,
) {
    val primeiroNome: String get() = nome.trim().substringBefore(' ')
}
