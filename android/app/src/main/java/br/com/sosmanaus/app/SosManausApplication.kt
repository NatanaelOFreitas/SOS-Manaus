package br.com.sosmanaus.app

import android.app.Application
import android.content.Context
import br.com.sosmanaus.app.controle.SessaoControle
import br.com.sosmanaus.app.fronteira.api.AutenticacaoApiGateway
import br.com.sosmanaus.app.fronteira.api.ClienteHttp
import br.com.sosmanaus.app.fronteira.armazenamento.ArmazenamentoSessaoDataStore

class SosManausApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/**
 * Raiz de composição (injeção de dependências manual).
 *
 * É o único lugar que conhece as três camadas ao mesmo tempo: cria as
 * implementações da fronteira (API, DataStore) e as entrega ao controle
 * através das portas.
 */
class AppContainer(context: Context) {
    private val api = ClienteHttp.criarApi(BuildConfig.API_URL)

    val sessaoControle = SessaoControle(
        gateway = AutenticacaoApiGateway(api),
        armazenamento = ArmazenamentoSessaoDataStore(context.applicationContext),
    )
}
