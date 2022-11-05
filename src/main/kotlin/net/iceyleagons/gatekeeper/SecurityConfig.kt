package net.iceyleagons.gatekeeper

import io.jsonwebtoken.SignatureAlgorithm
import net.iceyleagons.gatekeeper.api.JWTService
import net.iceyleagons.gatekeeper.crypto.GateKeeperJWTDecoder
import net.iceyleagons.gatekeeper.user.GateKeeperUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig(val jwtService: JWTService, val customUserDetailsService: GateKeeperUserDetailsService) {

    companion object {
        val CRYPTO_ALGORITHM = SignatureAlgorithm.PS256
        val CRYPTO_PROVIDER = "BC"
    }

    @Bean
    fun authenticationManager(conf: AuthenticationConfiguration): AuthenticationManager {
        return conf.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return GateKeeperJWTDecoder(jwtService)
    }

    @Bean
    fun corsConfig(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val conf = CorsConfiguration().applyPermitDefaultValues()

        source.registerCorsConfiguration("/**", conf)
        return source
    }

    @Bean
    fun securityFilter(http: HttpSecurity): SecurityFilterChain {
        return http
                .csrf().disable()
                .cors().configurationSource(corsConfig())
                .and().userDetailsService(customUserDetailsService)
                .httpBasic().disable()
                .formLogin().disable()
                .authorizeRequests()
                .antMatchers("/auth/**", "/authorize", "/link").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt().decoder(jwtDecoder()).and().and()
                .build()
    }

}