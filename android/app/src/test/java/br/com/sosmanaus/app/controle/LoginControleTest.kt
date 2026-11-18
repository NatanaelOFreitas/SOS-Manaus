package br.com.sosmanaus.app.controle

import br.com.sosmanaus.app.controle.portas.FalhaAutenticacao
import br.com.sosmanaus.app.controle.portas.SessaoAutenticada
import br.com.sosmanaus.app.controle.portas.TipoFalha
import br.com.sosmanaus.app.entidade.Perfil
import br.com.sosmanaus.app.entidade.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Testes do CONTROLE da tela de login (ViewModel) com as portas falsas. */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginControleTest {

    private val gateway = GatewayFalso()
    private val controle = LoginControle(SessaoControle(gateway, ArmazenamentoFalso()))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `campos invalidos nao chegam na API`() {
        controle.entrar()

        assertTrue(gateway.credenciaisRecebidas.isEmpty())
        assertEquals("Informe seu e-mail.", controle.ui.value.erroEmail)
        assertEquals("Informe sua senha.", controle.ui.value.erroSenha)
    }

    @Test
    fun `digitar apaga o erro do campo`() {
        controle.entrar()
        controle.aoAlterarEmail("m")
        assertNull(controle.ui.value.erroEmail)
    }

    @Test
    fun `login com sucesso termina sem erro`() {
        gateway.respostaLogin = Result.success(
            SessaoAutenticada("tk", Usuario("1", "Maria Silva", "cidadao@sosmanaus.com", Perfil.CIDADAO)),
        )
        controle.aoAlterarEmail("cidadao@sosmanaus.com")
        controle.aoAlterarSenha("sos12345")

        controle.entrar()

        assertFalse(controle.ui.value.carregando)
        assertNull(controle.ui.value.erroGeral)
    }

    @Test
    fun `mensagem do servidor aparece na falha`() {
        gateway.respostaLogin = Result.failure(
            FalhaAutenticacao(TipoFalha.ACESSO_NEGADO, "Esta conta é de gestor. Acesse pelo painel da prefeitura."),
        )
        controle.aoAlterarEmail("gestor@sosmanaus.com")
        controle.aoAlterarSenha("sos12345")

        controle.entrar()

        assertEquals("Esta conta é de gestor. Acesse pelo painel da prefeitura.", controle.ui.value.erroGeral)
        assertFalse(controle.ui.value.carregando)
    }

    @Test
    fun `alterna a visibilidade da senha`() {
        controle.alternarSenhaVisivel()
        assertTrue(controle.ui.value.senhaVisivel)
    }
}
