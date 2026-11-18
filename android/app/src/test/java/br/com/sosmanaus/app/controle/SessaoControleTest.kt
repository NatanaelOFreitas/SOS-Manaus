package br.com.sosmanaus.app.controle

import br.com.sosmanaus.app.controle.portas.FalhaAutenticacao
import br.com.sosmanaus.app.controle.portas.SessaoAutenticada
import br.com.sosmanaus.app.controle.portas.TipoFalha
import br.com.sosmanaus.app.entidade.Credenciais
import br.com.sosmanaus.app.entidade.EstadoSessao
import br.com.sosmanaus.app.entidade.Perfil
import br.com.sosmanaus.app.entidade.Usuario
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes do CONTROLE de sessão com as portas falsas. */
class SessaoControleTest {

    private val maria = Usuario("1", "Maria Silva", "cidadao@sosmanaus.com", Perfil.CIDADAO)

    @Test
    fun `sem token salvo vai para deslogado`() = runTest {
        val controle = SessaoControle(GatewayFalso(), ArmazenamentoFalso(token = null))
        controle.restaurar()
        assertEquals(EstadoSessao.Deslogado, controle.estado.value)
    }

    @Test
    fun `token valido restaura a sessao`() = runTest {
        val gateway = GatewayFalso().apply { respostaMe = Result.success(maria) }
        val controle = SessaoControle(gateway, ArmazenamentoFalso(token = "abc"))
        controle.restaurar()
        assertEquals(EstadoSessao.Logado(maria), controle.estado.value)
    }

    @Test
    fun `token expirado e apagado do aparelho`() = runTest {
        val gateway = GatewayFalso().apply {
            respostaMe = Result.failure(FalhaAutenticacao(TipoFalha.SESSAO_EXPIRADA, "Sessão inválida."))
        }
        val armazenamento = ArmazenamentoFalso(token = "vencido")
        val controle = SessaoControle(gateway, armazenamento)

        controle.restaurar()

        assertEquals(EstadoSessao.Deslogado, controle.estado.value)
        assertNull(armazenamento.token)
    }

    @Test
    fun `sem internet mantem o token para tentar depois`() = runTest {
        val gateway = GatewayFalso().apply {
            respostaMe = Result.failure(FalhaAutenticacao(TipoFalha.SEM_CONEXAO, "Sem conexão."))
        }
        val armazenamento = ArmazenamentoFalso(token = "abc")
        SessaoControle(gateway, armazenamento).restaurar()
        assertEquals("abc", armazenamento.token)
    }

    @Test
    fun `login com sucesso salva o token e muda o estado`() = runTest {
        val gateway = GatewayFalso().apply { respostaLogin = Result.success(SessaoAutenticada("tk", maria)) }
        val armazenamento = ArmazenamentoFalso()
        val controle = SessaoControle(gateway, armazenamento)

        val resultado = controle.entrar(Credenciais(" Cidadao@SosManaus.com ", "sos12345"))

        assertEquals(ResultadoLogin.Sucesso, resultado)
        assertEquals("tk", armazenamento.token)
        assertEquals("cidadao@sosmanaus.com", gateway.credenciaisRecebidas.single().email)
        assertTrue(controle.estado.value is EstadoSessao.Logado)
    }

    @Test
    fun `sair limpa o token`() = runTest {
        val armazenamento = ArmazenamentoFalso(token = "tk")
        val controle = SessaoControle(GatewayFalso(), armazenamento)
        controle.sair()
        assertNull(armazenamento.token)
        assertEquals(EstadoSessao.Deslogado, controle.estado.value)
    }
}
