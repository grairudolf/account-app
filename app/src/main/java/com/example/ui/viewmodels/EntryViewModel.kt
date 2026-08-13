package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.repositories.AccountabilityRepository
import kotlinx.coroutines.launch

class EntryViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    fun saveEntry(context: Context, entry: AccountabilityEntryEntity) {
        viewModelScope.launch {
            accountabilityRepository.saveEntry(entry)
            accountabilityRepository.logNotification(
                context = context,
                title = "Entry Logged",
                message = "Logged transaction for ${entry.domainId.uppercase()} on ${entry.dateIso}",
                type = "ENTRY"
            )
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            accountabilityRepository.deleteEntry(id)
        }
    }
}
