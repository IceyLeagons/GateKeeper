package net.iceyleagons.gatekeeper.user

import com.fasterxml.jackson.annotation.JsonIgnore

data class Identity(
        val providerId: String = "",

        var providerAccessToken: String? = null,
        var providerAccessTokenExpires: Long = 0L,

        @JsonIgnore
        var providerRefreshToken: String? = null,
        val data: Map<String, Any> = mutableMapOf()
)