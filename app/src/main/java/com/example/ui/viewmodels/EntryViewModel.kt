package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.repositories.AccountabilityRepository
import kotlinx.coroutines.launch

class EntryViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    fun saveEntry(entry: AccountabilityEntryEntity) {
        viewModelScope.launch {
            accountabilityRepository.saveEntry(entry)
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            accountabilityRepository.deleteEntry(id)
        }
    }
}
