package net.iceyleagons.gatekeeper.providers

import net.iceyleagons.gatekeeper.api.IdentityProvider
import net.iceyleagons.gatekeeper.dto.AccessTokenResponse
import net.iceyleagons.gatekeeper.dto.TokenResponse
import net.iceyleagons.gatekeeper.user.Identity
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.util.*
import java.util.concurrent.TimeUnit

class Spotify(environment: Environment) : IdentityProvider(
        "spotify",
        "https://logospng.org/download/spotify/logo-spotify-icon-4096.png",
        environment
) {

    override fun getFullAuthUrl(state: String): String {
        return getDefaultAuthUrl(state).build().encode().toUriString()
    }

    override fun requestTokens(code: String): TokenResponse {
        val restTemplate = RestTemplate()
        val auth = Base64.getEncoder().encodeToString("${clientId}:${clientSecret}".encodeToByteArray())

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("grant_type", "authorization_code")
        map.add("code", code)
        map.add("redirect_uri", redirectUri)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.add("Authorization", "Basic $auth")

        val entity = HttpEntity<MultiValueMap<String, String>>(map, headers)
        return restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, TokenResponse::class.java).body ?: throw IllegalStateException("No response")
    }

    private fun requestTokenFromRefreshToken(refreshToken: String): AccessTokenResponse {
        val restTemplate = RestTemplate()
        val auth = Base64.getEncoder().encodeToString("${clientId}:${clientSecret}".encodeToByteArray())

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("grant_type", "refresh_token")
        map.add("refresh_token", refreshToken)

        val entity = HttpEntity<MultiValueMap<String, String>>(map, getHeaders(Pair("Authorization", "Basic $auth")))
        return restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, AccessTokenResponse::class.java).body ?: throw IllegalStateException("No response")
    }

    override fun fetchProfileData(accessToken: String): Map<String, Any> {
        val data = getResponse("https://api.spotify.com/v1/me", accessToken)
        return mapOf(
                Pair("id", data["id"] as String),
                Pair("name", data["display_name"] as String),
                Pair("email", data["email"] as String)
        )
    }

    override fun refreshAccessToken(identity: Identity) {
        identity.providerRefreshToken?.let {
            val renewed = requestTokenFromRefreshToken(it)

            identity.providerAccessToken = renewed.accessToken
            identity.providerAccessTokenExpires = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(renewed.expiresIn)
        }
    }
}