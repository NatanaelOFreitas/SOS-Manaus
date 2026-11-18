package br.com.sosmanaus.app.fronteira.armazenamento

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.sosmanaus.app.controle.portas.ArmazenamentoSessao
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "sessao")

/**
 * FRONTEIRA com o armazenamento do aparelho: implementa a porta
 * ArmazenamentoSessao usando DataStore (área privada do app).
 */
class ArmazenamentoSessaoDataStore(private val context: Context) : ArmazenamentoSessao {
    private val chaveToken = stringPreferencesKey("token")

    override suspend fun lerToken(): String? = context.dataStore.data.first()[chaveToken]

    override suspend fun salvarToken(token: String) {
        context.dataStore.edit { it[chaveToken] = token }
    }

    override suspend fun limpar() {
        context.dataStore.edit { it.remove(chaveToken) }
    }
}
