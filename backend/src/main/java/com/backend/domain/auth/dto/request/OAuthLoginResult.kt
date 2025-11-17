package com.backend.domain.auth.dto.request

sealed class OAuthLoginResult {
    data class ExistingUser(val access: String, val refresh: String) : OAuthLoginResult()
    data class NewUser(val tempToken: String) : OAuthLoginResult()

    companion object {
        fun existingUser(access: String, refresh: String) = ExistingUser(access, refresh)
        fun newUser(tempToken: String) = NewUser(tempToken)
    }
}