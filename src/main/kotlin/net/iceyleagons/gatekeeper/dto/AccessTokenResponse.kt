package net.iceyleagons.gatekeeper.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AccessTokenResponse(
        @JsonProperty("access_token") val accessToken: String,
        @JsonProperty("expires_in") val expiresIn: Long
)