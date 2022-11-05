package net.iceyleagons.gatekeeper

import io.jsonwebtoken.lang.RuntimeEnvironment
import net.iceyleagons.gatekeeper.application.AppRepo
import net.iceyleagons.gatekeeper.application.OAuthApplication
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.security.Security
import java.util.*

@SpringBootApplication
class GatekeeperApplication(appRepo: AppRepo) {
    init {
        val app = OAuthApplication(UUID.randomUUID(), OAuthApplication.generateSecret(), "http://localhost:8081/")
        appRepo.save(app)
    }
}

fun main(args: Array<String>) {
    Security.addProvider(BouncyCastleProvider())
    RuntimeEnvironment.enableBouncyCastleIfPossible()
    runApplication<GatekeeperApplication>(*args)
}
