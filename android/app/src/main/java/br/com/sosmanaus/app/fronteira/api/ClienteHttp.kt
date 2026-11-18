package br.com.sosmanaus.app.fronteira.api

import br.com.sosmanaus.app.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Monta o cliente HTTP (OkHttp + Retrofit) usado pela fronteira de API. */
object ClienteHttp {
    val json = Json { ignoreUnknownKeys = true }

    fun criarApi(urlBase: String): SosManausApi {
        // BASIC registra só método, URL e status; nunca o corpo, que contém a senha
        val log = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val cliente = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(log)
            .build()

        return Retrofit.Builder()
            .baseUrl(urlBase)
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SosManausApi::class.java)
    }
}
