package net.iceyleagons.gatekeeper.application

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AppRepo : JpaRepository<OAuthApplication, String> {
}