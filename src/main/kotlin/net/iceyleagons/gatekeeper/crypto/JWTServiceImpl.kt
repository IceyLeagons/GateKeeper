package net.iceyleagons.gatekeeper.crypto

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import net.iceyleagons.gatekeeper.SecurityConfig
import net.iceyleagons.gatekeeper.api.JWTService
import net.iceyleagons.gatekeeper.api.KeyService
import org.springframework.stereotype.Service
import java.util.*

@Service
class JWTServiceImpl(val keyService: KeyService) : JWTService {

    /**
     * {@inheritDoc}
     */
    override fun verifyAndParseToken(token: String): Optional<Jws<Claims>> {
        return try {
            Optional.of(
                    Jwts.parserBuilder()
                            .setSigningKey(this.keyService.getKeyPair().public)
                            .build()
                            .parseClaimsJws(token)
            )
        } catch (e: JwtException) {
            Optional.empty()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun createToken(subject: String, expiration: Date, claims: Map<String, Any>): String {
        return Jwts.builder()
                .setSubject(subject).setClaims(claims)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(expiration)
                .signWith(keyService.getKeyPair().private, SecurityConfig.CRYPTO_ALGORITHM)
                .compact()
    }
}