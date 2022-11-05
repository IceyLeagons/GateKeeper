package net.iceyleagons.gatekeeper

import net.iceyleagons.gatekeeper.api.IdentityProvider
import net.iceyleagons.gatekeeper.api.IdentityService
import net.iceyleagons.gatekeeper.api.JWTService
import net.iceyleagons.gatekeeper.application.AppRepo
import net.iceyleagons.gatekeeper.dto.RequestOrigin
import net.iceyleagons.gatekeeper.providers.Google
import net.iceyleagons.gatekeeper.providers.Microsoft
import net.iceyleagons.gatekeeper.providers.Spotify
import net.iceyleagons.gatekeeper.user.User
import net.iceyleagons.gatekeeper.user.UserRepository
import org.springframework.core.env.Environment
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


@Service
class IdentityServiceImpl(
        env: Environment,
        val userRepository: UserRepository,
        val oAuthApplicationRepository: AppRepo,
        val jwtService: JWTService
) : IdentityService {

    val identityProviders: MutableMap<String, IdentityProvider> = HashMap()
    val requestOrigins: MutableMap<String, RequestOrigin> = HashMap()
    val authStates: MutableMap<String, IdentityProvider> = HashMap() // logins
    val linkingStates: MutableMap<String, Pair<IdentityProvider, User>> = HashMap() // linkings

    init {
        registerIdentityProvider(Google(env))
        registerIdentityProvider(Spotify(env))
        registerIdentityProvider(Microsoft(env))
    }

    private fun registerIdentityProvider(identityProvider: IdentityProvider) {
        identityProviders[identityProvider.name] = identityProvider
    }

    override fun onCallback(code: String, state: String): Optional<RequestOrigin> {
        if (authStates.containsKey(state)) {
            authStates[state]?.let {
                // Login --> return JWT to our APIs
                val resp = it.requestTokens(code)
                val identity = resp.asIdentity(it)

                val id = "oauth2|${it.name}|${identity.data["id"]}"
                if (userRepository.existsById(id)) {
                    val origin = requestOrigins[state]!!
                    val user = userRepository.findById(id).get()
                    val expires = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
                    val token = toToken(userRepository.save(user), expires, origin.clientId)

                    requestOrigins.remove(state)
                    origin.accessToken = Optional.of(token)
                    return@onCallback Optional.of(origin)
                }

                val origin = requestOrigins[state]!!
                val user = User(id,  identity.data["email"] as String, identity.data["name"] as String, identity)
                val expires = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
                val token = toToken(userRepository.save(user), expires, origin.clientId)


                requestOrigins.remove(state)

                origin.accessToken = Optional.of(token)
                return@onCallback Optional.of(origin)
            }
        }

        if (linkingStates.containsKey(state)) {
            linkingStates[state]?.let {
                // Linking --> DB job
                val ip = it.first
                val currentUser = it.second

                val resp = ip.requestTokens(code)

                userRepository.findById(currentUser.id).ifPresent { user ->
                    user.addIdentity(resp.asIdentity(ip))
                    userRepository.save(user)
                }

                return@onCallback Optional.ofNullable(requestOrigins[state])
            }
        }

        throw IllegalStateException("Invalid state")
    }

    private fun assertRedirectUri(origin: RequestOrigin) {
        if (!oAuthApplicationRepository.existsById(origin.clientId)) throw IllegalStateException("Invalid ClientId!")
        val app = oAuthApplicationRepository.findById(origin.clientId).get()

        if (app.redirectUri != origin.redirectUri) throw IllegalStateException("Illegal RedirectUri!")
    }

    override fun getAuthUrl(identityProviderId: String, origin: RequestOrigin): String {
        assertRedirectUri(origin)

        val prov = identityProviders[identityProviderId] ?: throw IllegalArgumentException("No identiry provider found with id: $identityProviderId .")
        val state = getState()

        requestOrigins[state]= origin
        authStates[state] = prov
        return prov.getFullAuthUrl(state)
    }

    override fun getAuthUrlForLinking(identityProviderId: String, user: User, origin: RequestOrigin): String {
        assertRedirectUri(origin)

        val prov = identityProviders[identityProviderId] ?: throw IllegalArgumentException("No identity provider found with id: $identityProviderId .")
        for (identity in user.getIdentities()) {
            if (identity.providerId == identityProviderId) {
                throw IllegalStateException("Identity from provider is already linked to user!")
            }
        }

        val state = getState()

        requestOrigins[state]= origin
        linkingStates[state] = Pair(prov, user)
        return prov.getFullAuthUrl(state)
    }

    override fun getCurrentUser(refreshTokens: Boolean): Optional<User> {
        return try {
            val auth = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
            val jwt = auth.credentials as Jwt
            val id = jwt.claims["id"] as String

            if (refreshTokens) refreshAccessTokens(userRepository.findById(id)) else userRepository.findById(id)
        } catch (e: Exception) {
            Optional.empty()
        }
    }

    override fun deleteCurrentUser() {
        val user = getCurrentUser(refreshTokens = false).orElseThrow { IllegalStateException("User not found!") }
        userRepository.delete(user)
    }

    override fun getUserFromToken(token: String): Optional<User> {
        val claims = jwtService.verifyAndParseToken(token).orElse(null) ?: return Optional.empty()
        return userRepository.findById(claims.body["id"] as String)
    }

    override fun getProviders(): Map<String, IdentityProvider> = identityProviders

    private fun refreshAccessTokens(user: Optional<User>): Optional<User> {
        if (user.isEmpty) return Optional.empty()

        user.get().getIdentities().forEach { it ->
            if (System.currentTimeMillis() >= it.providerAccessTokenExpires) {
                identityProviders[it.providerId]?.refreshAccessToken(it)
            }
        }

        return user
    }

    private fun getState(): String {
        val state = UUID.randomUUID().toString()
        if (authStates.containsKey(state) || linkingStates.containsKey(state)) {
            return getState()
        }

        return state
    }

    private fun toToken(user: User, expires: Long, clientId: String): String {
        // Possibly do refresh token here as well!
        return jwtService.createToken(
                user.id,
                Date(expires),
                mapOf(
                        Pair("id", user.id),
                        Pair("email", user.email),
                        Pair("primary_identity_provider", user.primaryIdentity.providerId),
                        Pair("client_id", clientId)
                )
        )
    }
}