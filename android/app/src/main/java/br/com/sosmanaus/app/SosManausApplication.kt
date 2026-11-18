package br.com.sosmanaus.app

import android.app.Application
import android.content.Context
import br.com.sosmanaus.app.data.AuthRepository
import br.com.sosmanaus.app.data.AuthRepositoryImpl
import br.com.sosmanaus.app.data.local.TokenStorage
import br.com.sosmanaus.app.data.remote.NetworkModule

class SosManausApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Injeção de dependências manual: cria uma única instância de cada serviço. */
class AppContainer(context: Context) {
    private val api = NetworkModule.criarApi(BuildConfig.API_URL)
    private val tokenStorage = TokenStorage(context)

    val authRepository: AuthRepository = AuthRepositoryImpl(api, tokenStorage)
}
