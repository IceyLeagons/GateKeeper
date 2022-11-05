package net.iceyleagons.gatekeeper.api

import java.security.KeyPair

/**
 * Manages the cryptographic keys for GateKeeper
 * @author TOTHTOMI
 */
interface KeyService {

    /**
     * @return the KeyPair to use for JWT signing
     */
    fun getKeyPair(): KeyPair

}