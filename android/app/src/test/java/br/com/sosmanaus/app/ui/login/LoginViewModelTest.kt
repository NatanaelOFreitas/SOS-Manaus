package br.com.sosmanaus.app.ui.login

import br.com.sosmanaus.app.data.AuthRepository
import br.com.sosmanaus.app.data.EstadoSessao
import br.com.sosmanaus.app.data.ResultadoLogin
import br.com.sosmanaus.app.data.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private class FakeAuthRepository(var resultado: ResultadoLogin) : AuthRepository {
        var chamadas = 0
        var ultimoEmail: String? = null
        override val sessao: StateFlow<EstadoSessao> = MutableStateFlow(EstadoSessao.Deslogado)
        override suspend fun restaurarSessao() = Unit
        override suspend fun sair() = Unit
        override suspend fun entrar(email: String, senha: String): ResultadoLogin {
            chamadas++
            ultimoEmail = email
            return resultado
        }
    }

    private val usuario = Usuario("1", "Maria Silva", "cidadao@sosmanaus.com", "CIDADAO")

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `nao chama a API quando os campos sao invalidos`() = runTest {
        val repo = FakeAuthRepository(ResultadoLogin.Sucesso(usuario))
        val vm = LoginViewModel(repo)

        vm.entrar()

        assertEquals(0, repo.chamadas)
        assertEquals("Informe seu e-mail.", vm.ui.value.erroEmail)
        assertEquals("Informe sua senha.", vm.ui.value.erroSenha)
    }

    @Test
    fun `envia email normalizado e termina sem erro no sucesso`() = runTest {
        val repo = FakeAuthRepository(ResultadoLogin.Sucesso(usuario))
        val vm = LoginViewModel(repo)

        vm.onEmailChange("  Cidadao@SosManaus.com ")
        vm.onSenhaChange("sos12345")
        vm.entrar()

        assertEquals(1, repo.chamadas)
        assertEquals("cidadao@sosmanaus.com", repo.ultimoEmail)
        assertFalse(vm.ui.value.carregando)
        assertNull(vm.ui.value.erroGeral)
    }

    @Test
    fun `mostra a mensagem do servidor na falha`() = runTest {
        val repo = FakeAuthRepository(ResultadoLogin.Falha("E-mail ou senha incorretos."))
        val vm = LoginViewModel(repo)

        vm.onEmailChange("cidadao@sosmanaus.com")
        vm.onSenhaChange("senhaerrada")
        vm.entrar()

        assertEquals("E-mail ou senha incorretos.", vm.ui.value.erroGeral)
        assertFalse(vm.ui.value.carregando)
    }
}
