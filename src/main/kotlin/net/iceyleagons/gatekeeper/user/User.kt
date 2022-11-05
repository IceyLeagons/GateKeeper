package net.iceyleagons.gatekeeper.user

import com.fasterxml.jackson.annotation.JsonIgnore
import net.iceyleagons.gatekeeper.user.converter.IdentityConverter
import net.iceyleagons.gatekeeper.user.converter.IdentityListConverter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.*

@Entity
@Table(name = "users")
class User(givenId: String? = null, givenEmail: String? = null, givenDisplayName: String? = null, givenIdentity: Identity? = null) : UserDetails {

    @Id
    @Column(name = "id", length = 16, unique = true, nullable = false)
    val id: String = givenId ?: ""

    val email: String = givenEmail ?: ""
    val displayName: String = givenDisplayName ?: ""

    @JsonIgnore
    @Convert(converter = IdentityConverter::class)
    val primaryIdentity: Identity = givenIdentity ?: Identity()

    @JsonIgnore
    @Convert(converter = IdentityListConverter::class)
    val connectedIdentities: MutableList<Identity> = ArrayList()

    @JsonIgnore
    fun hasIdentityFromProvider(provider: String): Boolean {
        return getIdentities().any {
            it.providerId == provider
        }
    }

    fun getIdentities(): List<Identity> {
        return listOf(primaryIdentity, *connectedIdentities.toTypedArray())
    }

    fun addIdentity(identity: Identity) {
        connectedIdentities.add(identity)
    }

    // These are not implemented, because we don't need these. These are just here so we can use Spring's default security system easily.
    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = arrayListOf()
    @JsonIgnore
    override fun getPassword(): String = ""
    @JsonIgnore
    override fun getUsername(): String = id

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean = true
    @JsonIgnore
    override fun isAccountNonLocked(): Boolean = true
    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean = true
    @JsonIgnore
    override fun isEnabled(): Boolean = true
}