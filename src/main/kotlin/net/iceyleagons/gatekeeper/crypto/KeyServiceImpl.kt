package net.iceyleagons.gatekeeper.crypto

import io.jsonwebtoken.security.Keys
import net.iceyleagons.gatekeeper.SecurityConfig
import net.iceyleagons.gatekeeper.api.KeyService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyFactory
import java.security.KeyPair
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

@Service
class KeyServiceImpl(@Value("\${gatekeeper.security.keysFolder}") keysFolder: String) : KeyService {

    private val keyPair: KeyPair
    override fun getKeyPair(): KeyPair = keyPair

    init {
        val path = Path.of(keysFolder)
        if (!Files.exists(path)) {
            Files.createDirectory(path)
        }

        keyPair = loadOrCreateKeyPair(path.resolve("public.key"), path.resolve("private.key"))
    }

    /**
     * Checks whether our keys exist. If they don't we will create them via the JWT library.
     * The crypto algorithm to be used is defined in SecurityConfig
     *
     * @param publicPath path to the public key
     * @param privatePath path to the private key
     *
     * @return the loaded or created KeyPair
     */
    private fun loadOrCreateKeyPair(publicPath: Path, privatePath: Path): KeyPair {
        if (!Files.exists(publicPath) || !Files.exists(privatePath)) {
            val kp = Keys.keyPairFor(SecurityConfig.CRYPTO_ALGORITHM)
            return saveKeyPair(kp, Files.createFile(publicPath), Files.createFile(privatePath))
        }

        return loadKeyPair(publicPath, privatePath)
    }

    /**
     * Loads a keypair from the given public & private key paths.
     *
     * @param privatePath path to the private key file
     * @param publicPath path to the public key file
     * @return the resulting KeyPair
     */
    private fun loadKeyPair(publicPath: Path, privatePath: Path): KeyPair {
        val decoder = Base64.getDecoder()
        val encPub = decoder.decode(Files.readAllBytes(publicPath))
        val encPriv = decoder.decode(Files.readAllBytes(privatePath))

        val factory = KeyFactory.getInstance(SecurityConfig.CRYPTO_ALGORITHM.jcaName, SecurityConfig.CRYPTO_PROVIDER)
        val pub = factory.generatePublic(X509EncodedKeySpec(encPub))
        val priv = factory.generatePrivate(PKCS8EncodedKeySpec(encPriv))

        return KeyPair(pub, priv)
    }

    /**
     * Saves the given keypair to the given file paths.
     *
     * @param keyPair the KeyPair to save
     * @param publicPath the path where the public key should be stored
     * @param privatePath the path where the private ke should be stored
     */
    private fun saveKeyPair(keyPair: KeyPair, publicPath: Path, privatePath: Path): KeyPair {
        val encoder = Base64.getEncoder()

        val pub = X509EncodedKeySpec(keyPair.public.encoded)
        val priv = PKCS8EncodedKeySpec(keyPair.private.encoded)

        Files.writeString(publicPath, encoder.encodeToString(pub.encoded))
        Files.writeString(privatePath, encoder.encodeToString(priv.encoded))

        return keyPair
    }
}