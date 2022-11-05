package net.iceyleagons.gatekeeper.dto

import java.util.*

data class RequestOrigin(val clientId: String, val redirectUri: String, var accessToken: Optional<String> = Optional.empty()) {

    fun asRedirect(): String {
        return if (accessToken.isPresent) "redirect:${redirectUri}#access_token=${accessToken.get()}" else  "redirect:${redirectUri}"
    }
}