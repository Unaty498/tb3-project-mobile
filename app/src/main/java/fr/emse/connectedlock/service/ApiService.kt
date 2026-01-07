package fr.emse.connectedlock.service

import com.squareup.moshi.Json
import fr.emse.connectedlock.data.Badge
import fr.emse.connectedlock.data.Door
import fr.emse.connectedlock.data.User
import fr.emse.connectedlock.data.AccessRule
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "token_type") val tokenType: String = "Bearer"
)

data class ActivateBadgeRequest(val physicallyMapped: Boolean)

interface ApiService {
    @FormUrlEncoded
    @POST("keycloak/realms/connected-lock/protocol/openid-connect/token")
    suspend fun getToken(
        @Field("client_id") clientId: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String
    ): TokenResponse

    @GET("api/me")
    suspend fun getCurrentUser(): User

    @GET("api/badges/user/{userId}")
    suspend fun getBadges(@Path("userId") userId: String): List<Badge>

    @PUT("api/badges/{badgeId}/mapped")
    suspend fun activateBadge(@Path("badgeId") badgeId: String, @Body request: ActivateBadgeRequest)

    @GET("api/doors")
    suspend fun getDoors(): List<Door>

    @GET("api/access-rules/user/{userId}")
    suspend fun getAccess(@Path("userId") userId: String): List<AccessRule>
}
