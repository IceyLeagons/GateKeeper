package net.iceyleagons.gatekeeper.user

import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class GateKeeperUserDetailsService(val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String?): User {
        return userRepository.findById(username!!).orElseThrow {
            UsernameNotFoundException("User with id $username not found!")
        }
    }
}