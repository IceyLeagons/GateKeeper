package net.iceyleagons.gatekeeper

import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@Configuration
@EnableWebSecurity
class SecurityConfig {

    companion object {
        val CRYPTO_ALGORITHM = SignatureAlgorithm.PS256
        val CRYPTO_PROVIDER = "BC"
    }

}