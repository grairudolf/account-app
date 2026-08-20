package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.DiscipleEntity
import com.example.data.repositories.AccountabilityRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntryViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    val disciples: StateFlow<List<DiscipleEntity>> = accountabilityRepository.getDisciplesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun saveDisciple(disciple: DiscipleEntity) {
        viewModelScope.launch {
            accountabilityRepository.saveDisciple(disciple)
        }
    }

    fun updateDisciple(disciple: DiscipleEntity) {
        viewModelScope.launch {
            accountabilityRepository.updateDisciple(disciple)
        }
    }

    fun deleteDisciple(disciple: DiscipleEntity) {
        viewModelScope.launch {
            accountabilityRepository.deleteDisciple(disciple)
        }
    }

    fun deleteDiscipleById(id: String) {
        viewModelScope.launch {
            accountabilityRepository.deleteDiscipleById(id)
        }
    }
}
