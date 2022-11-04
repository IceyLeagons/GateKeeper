package net.iceyleagons.gatekeeper.application

import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class OAuthApplication(givenUuid: UUID? = null, givenSecret: String? = null, givenRedirectUri: String? = null) {

    @Id
    val id: String = (givenUuid ?: UUID.randomUUID()).toString()
    val clientSecret: String = givenSecret ?: ""
    val redirectUri: String = givenRedirectUri ?: ""
}