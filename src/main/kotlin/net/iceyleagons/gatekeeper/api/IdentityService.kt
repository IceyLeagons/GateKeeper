package net.iceyleagons.gatekeeper.api

import net.iceyleagons.gatekeeper.dto.RequestOrigin
import net.iceyleagons.gatekeeper.user.User
import java.util.*

interface IdentityService {

    fun onCallback(code: String, state: String): Optional<RequestOrigin>

    fun getAuthUrl(identityProviderId: String, origin: RequestOrigin): String

    fun getAuthUrlForLinking(identityProviderId: String, user: User, origin: RequestOrigin): String

    fun deleteCurrentUser()

    fun getCurrentUser(refreshTokens: Boolean): Optional<User>

    fun getUserFromToken(token: String): Optional<User>

    fun getProviders(): Map<String, IdentityProvider>

}