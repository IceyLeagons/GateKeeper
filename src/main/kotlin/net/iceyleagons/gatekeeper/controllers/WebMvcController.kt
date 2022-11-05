package net.iceyleagons.gatekeeper.controllers

import net.iceyleagons.gatekeeper.api.IdentityService
import net.iceyleagons.gatekeeper.application.AppRepo
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class WebMvcController(val oAuthApplicationRepository: AppRepo, val identityService: IdentityService) {

    @RequestMapping("/link")
    fun link(@RequestParam(required = true, name = "state") state: String,
             @RequestParam(required = true, name = "redirect_uri") redirectUri: String,
             @RequestParam(required = true, name = "token") token: String,
             @RequestParam(required = true, name = "client_id") clientId: String,
             model: Model): String {

        if (!oAuthApplicationRepository.existsById(clientId)) {
            model.addAttribute("error", "Invalid client_id!")
            return "login"
        }

        if (oAuthApplicationRepository.findById(clientId).get().redirectUri != redirectUri) {
            model.addAttribute("error", "Invalid redirect_uri!")
            return "login"
        }

        val userO = identityService.getUserFromToken(token)
        if (userO.isEmpty) {
            model.addAttribute("error", "Invalid token!")
            return "login"
        }

        val user = userO.get()
        val allProviders = identityService.getProviders().toMutableMap()
        user.getIdentities().forEach {
            allProviders.remove(it.providerId)
        }

        model.addAttribute("providers", allProviders.values.sortedBy { it.name })
        model.addAttribute("state", state)
        model.addAttribute("redirect_uri", redirectUri)
        model.addAttribute("client_id", clientId)
        model.addAttribute("token", token)
        return "link"
    }

    @RequestMapping("/authorize")
    fun index(@RequestParam(required = true, name = "state") state: String,
              @RequestParam(required = true, name = "redirect_uri") redirectUri: String,
              @RequestParam(required = true, name = "client_id") clientId: String,
              model: Model): String {

        if (!oAuthApplicationRepository.existsById(clientId)) {
            model.addAttribute("error", "Invalid client_id!")
            return "login"
        }

        if (oAuthApplicationRepository.findById(clientId).get().redirectUri != redirectUri) {
            model.addAttribute("error", "Invalid redirect_uri!")
            return "login"
        }

        model.addAttribute("providers", identityService.getProviders().values.sortedBy { it.name })

        model.addAttribute("state", state)
        model.addAttribute("redirect_uri", redirectUri)
        model.addAttribute("client_id", clientId)
        return "login"
    }
}