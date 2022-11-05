package net.iceyleagons.gatekeeper.crypto

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import net.iceyleagons.gatekeeper.api.JWTService
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException

class GateKeeperJWTDecoder(private val jwtService: JWTService) : JwtDecoder {

    override fun decode(token: String?): Jwt {
        if (token == null) throw JwtException("Token cannot be null!")

        val opt = jwtService.verifyAndParseToken(token)
        return mapToSpringJwt(opt.orElseThrow { JwtException("Invalid auth token!") }, token)
    }

    private fun mapToSpringJwt(claims: Jws<Claims>, token: String): Jwt {
        val body = claims.body
        return Jwt(token, body.issuedAt.toInstant(), body.expiration.toInstant(), claims.header, body)
    }
}