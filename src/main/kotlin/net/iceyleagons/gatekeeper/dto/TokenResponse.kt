package net.iceyleagons.gatekeeper.dto

import com.fasterxml.jackson.annotation.JsonProperty
import net.iceyleagons.gatekeeper.api.IdentityProvider
import net.iceyleagons.gatekeeper.user.Identity
import java.util.concurrent.TimeUnit

class TokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,

        @JsonProperty("expires_in")
        accessTokenExpires: Long,

        @JsonProperty("refresh_token")
        val refreshToken: String
) {

    val accessTokenExpires: Long = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(accessTokenExpires)

    fun asIdentity(identityProvider: IdentityProvider): Identity {
        return Identity(
                identityProvider.name,
                accessToken,
                accessTokenExpires,
                refreshToken,
                identityProvider.fetchProfileData(accessToken)
        )
    }
}