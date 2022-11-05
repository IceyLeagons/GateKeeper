package net.iceyleagons.gatekeeper.api

import net.iceyleagons.gatekeeper.dto.TokenResponse
import net.iceyleagons.gatekeeper.user.Identity
import org.json.JSONObject
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.http.*
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

abstract class IdentityProvider(val name: String, val icon: String, environment: Environment) {

    protected val redirectUri: String = environment["gatekeeper.identityCallbackUri"]!!
    protected val authorizationUrl: String = environment["gatekeeper.identityProviders.${name}.authUrl"]!!
    protected val scopes: String = environment["gatekeeper.identityProviders.${name}.scopes"]!!
    protected val tokenUrl: String = environment["gatekeeper.identityProviders.${name}.tokenUrl"]!!
    protected val clientId: String = environment["gatekeeper.identityProviders.${name}.clientId"]!!
    protected val clientSecret: String = environment["gatekeeper.identityProviders.${name}.clientSecret"]!!

    abstract fun getFullAuthUrl(state: String): String

    abstract fun requestTokens(code: String): TokenResponse

    abstract fun fetchProfileData(accessToken: String): Map<String, Any>

    abstract fun refreshAccessToken(identity: Identity)


    fun getDefaultAuthUrl(state: String): UriComponentsBuilder {
        return UriComponentsBuilder.newInstance()
                .uri(URI(authorizationUrl))
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", scopes)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
    }

    fun getResponse(endpoint: String, accessToken: String): JSONObject {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.add("Authorization", "Bearer $accessToken")

        val restTemplate = RestTemplate()
        val entity = HttpEntity<MultiValueMap<String, String>>(headers)

        val response: ResponseEntity<String> = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String::class.java)
        return JSONObject(response.body)
    }

    fun getHeaders(vararg pair: Pair<String, String>): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        pair.forEach {
            headers.add(it.first, it.second)
        }

        return headers
    }
}