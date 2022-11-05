package net.iceyleagons.gatekeeper.api

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.jetbrains.annotations.Nullable
import java.util.*

/**
 * Manages the assigning and verification of JWT tokens.
 *
 * @see KeyService
 * @author TOTHTOMI
 */
interface JWTService {

    /**
     * Parses the given token. If the signature is valid (our private key has signed it) then the returned Optional will contain the claims of
     * the token. If the token is invalid, an empty optional will be returned.
     *
     * @param token the token to parse & verify
     * @return the resulting Optional with the claims or empty
     */
    fun verifyAndParseToken(token: String): Optional<Jws<Claims>>

    /**
     * Creates a new JWT token and signs it with our private key.
     *
     * @param subject subject of the JWT, in our case it's the primary identity ID
     * @param expiration the expiration date of the token.
     * @param claims the map of claims
     */
    fun createToken(subject: String, expiration: Date, claims: Map<String, Any>): String

}