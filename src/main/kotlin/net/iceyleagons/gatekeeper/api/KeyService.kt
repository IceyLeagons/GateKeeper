package net.iceyleagons.gatekeeper.api

import java.security.KeyPair

interface KeyService {

    fun getKeyPair(): KeyPair

}