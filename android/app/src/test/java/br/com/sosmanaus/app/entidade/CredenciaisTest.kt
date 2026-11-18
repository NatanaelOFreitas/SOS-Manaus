package br.com.sosmanaus.app.entidade

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes da ENTIDADE Credenciais: regras puras, sem Android. */
class CredenciaisTest {

    @Test
    fun `campos vazios geram os dois erros`() {
        val erros = Credenciais("  ", "").validar()
        assertEquals("Informe seu e-mail.", erros.email)
        assertEquals("Informe sua senha.", erros.senha)
        assertFalse(erros.valido)
    }

    @Test
    fun `email sem dominio e invalido`() {
        assertEquals("E-mail inválido.", Credenciais("maria@", "sos12345").validar().email)
    }

    @Test
    fun `senha curta e invalida`() {
        assertEquals(
            "A senha deve ter pelo menos 6 caracteres.",
            Credenciais("maria@sosmanaus.com", "123").validar().senha,
        )
    }

    @Test
    fun `credenciais corretas passam`() {
        val erros = Credenciais("maria@sosmanaus.com", "sos12345").validar()
        assertNull(erros.email)
        assertTrue(erros.valido)
    }

    @Test
    fun `normalizacao tira espacos e deixa o email minusculo`() {
        assertEquals("maria@sosmanaus.com", Credenciais("  Maria@SosManaus.com ", "x").normalizadas().email)
    }

    @Test
    fun `primeiro nome do usuario`() {
        assertEquals("Maria", Usuario("1", "Maria Silva", "m@x.com", Perfil.CIDADAO).primeiroNome)
    }
}
