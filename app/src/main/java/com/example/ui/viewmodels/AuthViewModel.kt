package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.UserEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.data.repositories.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = userRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun continueAsGuest() {
        viewModelScope.launch {
            userRepository.signInAsGuest()
        }
    }

    fun signInWithAccount(id: String, fullName: String, email: String, migrateLocalData: Boolean) {
        viewModelScope.launch {
            if (migrateLocalData) {
                accountabilityRepository.migrateGuestDataToAccount(id)
            }
            userRepository.signInWithAccount(id, fullName, email)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
        }
    }
}
