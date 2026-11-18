package br.com.sosmanaus.app.ui.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginValidatorTest {

    @Test
    fun `email vazio retorna erro`() {
        assertEquals("Informe seu e-mail.", LoginValidator.validarEmail("  "))
    }

    @Test
    fun `email sem dominio e invalido`() {
        assertEquals("E-mail inválido.", LoginValidator.validarEmail("maria@"))
    }

    @Test
    fun `email valido passa`() {
        assertNull(LoginValidator.validarEmail("cidadao@sosmanaus.com"))
    }

    @Test
    fun `senha curta retorna erro`() {
        assertEquals("A senha deve ter pelo menos 6 caracteres.", LoginValidator.validarSenha("123"))
    }

    @Test
    fun `senha valida passa`() {
        assertNull(LoginValidator.validarSenha("sos12345"))
    }
}
