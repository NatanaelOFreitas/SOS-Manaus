package br.com.sosmanaus.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "sessao")

/** Guarda o token JWT no armazenamento privado do app (DataStore). */
class TokenStorage(private val context: Context) {
    private val chaveToken = stringPreferencesKey("token")

    suspend fun ler(): String? = context.dataStore.data.first()[chaveToken]

    suspend fun salvar(token: String) {
        context.dataStore.edit { it[chaveToken] = token }
    }

    suspend fun limpar() {
        context.dataStore.edit { it.remove(chaveToken) }
    }
}
