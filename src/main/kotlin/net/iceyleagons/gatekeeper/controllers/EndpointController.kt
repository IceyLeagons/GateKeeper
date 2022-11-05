package net.iceyleagons.gatekeeper.controllers

import net.iceyleagons.gatekeeper.api.IdentityService
import net.iceyleagons.gatekeeper.application.AppRepo
import net.iceyleagons.gatekeeper.dto.RequestOrigin
import net.iceyleagons.gatekeeper.user.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.util.*

@RestController
class EndpointController(val identityService: IdentityService, val oAuthApplicationRepository: AppRepo) {

    @GetMapping("/auth/{providerId}/authorize")
    fun onAuthorizeRequest(@PathVariable("providerId") providerId: String,
                           @RequestParam(required = true, name = "redirect_uri") redirectUri: String,
                           @RequestParam(required = true, name = "client_id") clientId: String,
                           model: ModelMap): ModelAndView {
        // client id, redirect uri, state
        val auth = identityService.getAuthUrl(providerId, RequestOrigin(clientId, redirectUri))
        return ModelAndView("redirect:${auth}", model)
    }

    @GetMapping("/auth/{providerId}/link/authorize")
    fun onAuthorizeLinkRequest(@PathVariable("providerId") providerId: String,
                               @RequestParam(required = true, name = "token") token: String,
                               @RequestParam(required = true, name = "redirect_uri") redirectUri: String,
                               @RequestParam(required = true, name = "client_id") clientId: String,
                               model: ModelMap): ModelAndView {

        val auth = identityService.getAuthUrlForLinking(providerId, identityService.getUserFromToken(token).orElseThrow {
            IllegalStateException("Invalid Token!")
        }, RequestOrigin(clientId, redirectUri))
        return ModelAndView("redirect:${auth}", model)
    }

    @GetMapping("/auth/callback")
    fun onCallback(@RequestParam(required = false, name = "error") error: String?,
                   @RequestParam(required = false, name = "code") code: String?,
                   @RequestParam(required = true, name = "state") state: String,
                   model: ModelMap): ModelAndView {

        if (error != null) return ModelAndView()

        val req = identityService.onCallback(code!!, state)
        if (req.isPresent) {
            val redirect = req.get().asRedirect()
            return ModelAndView(redirect, model)
        }

        return ModelAndView()
    }

    @DeleteMapping("/api/currentUser")
    fun deleteUser() {
        // As it's the user's privilege, we don't ask for client secret
        identityService.deleteCurrentUser()
    }

    @GetMapping("/api/currentUser")
    fun getCurrentUser(@RequestParam(required = true, name = "client_secret") clientSecret: String): Optional<User> {
        val currentClientId = getTokenClientId()

        val app = oAuthApplicationRepository.findById(currentClientId).orElseThrow { IllegalStateException("Invalid Token (client_id does not exist!)") }
        if (app.clientSecret != clientSecret) {
            throw java.lang.IllegalStateException("Invalid Client Secret!")
        }

        return identityService.getCurrentUser(true)
    }

    fun getTokenClientId(): String {
        val authToken = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
        val jwt = authToken.credentials as Jwt
        return jwt.getClaimAsString("client_id")
    }
}