package br.com.sosmanaus.app.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface SosManausApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(@Header("Authorization") bearer: String): UsuarioDto
}

@Serializable
data class LoginRequest(val email: String, val senha: String)

@Serializable
data class LoginResponse(val accessToken: String, val usuario: UsuarioDto)

@Serializable
data class UsuarioDto(
    val id: String,
    val nome: String,
    val email: String,
    val perfil: String,
)
