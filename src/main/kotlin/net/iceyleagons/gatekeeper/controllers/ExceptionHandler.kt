package net.iceyleagons.gatekeeper.controllers

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody

@ControllerAdvice
class ExceptionHandler {

    @ResponseBody
    @ExceptionHandler(IllegalStateException::class)
    fun onException(illegalStateException: IllegalStateException): Map<String, String> {
        return mapOf(Pair("error", illegalStateException.message ?: "unknown"))
    }

}