package net.iceyleagons.gatekeeper.application

import java.security.SecureRandom
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class OAuthApplication(givenUuid: UUID? = null, givenSecret: String? = null, givenRedirectUri: String? = null) {

    @Id
    val id: String = (givenUuid ?: UUID.randomUUID()).toString()
    val clientSecret: String = givenSecret ?: ""
    val redirectUri: String = givenRedirectUri ?: ""

    companion object {

        /**
         * Generate a 32 length secret for an OAuth application using SecureRandom
         *
         * @return the generated secret
         */
        fun generateSecret(): String {
            val random = SecureRandom()
            val sb = StringBuffer()

            while (sb.length < 32) {
                sb.append(Integer.toHexString(random.nextInt()))
            }
            return sb.toString().substring(0, 32)
        }
    }
}