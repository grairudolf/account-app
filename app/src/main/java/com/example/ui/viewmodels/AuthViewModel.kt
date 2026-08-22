package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.UserEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.data.repositories.UserRepository
import com.example.services.auth.FirebaseAuthHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    val userRepository: UserRepository,
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            userRepository.getOrCreateGuestUser()
        }
    }

    val currentUser: StateFlow<UserEntity?> = userRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun hasCompletedAuthPrompt(): Boolean = userRepository.hasCompletedAuthPrompt

    fun continueAsGuest() {
        viewModelScope.launch {
            userRepository.signInAsGuest()
        }
    }

    fun signInWithAccount(
        id: String,
        fullName: String,
        email: String,
        profileImageUri: String? = null,
        localAssembly: String = "",
        migrateLocalData: Boolean = true
    ) {
        viewModelScope.launch {
            if (migrateLocalData) {
                accountabilityRepository.migrateGuestDataToAccount(id)
            }
            userRepository.signInWithAccount(id, fullName, email, profileImageUri, localAssembly)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            FirebaseAuthHelper.signOut()
            userRepository.signOut()
        }
    }
}
