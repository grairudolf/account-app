package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.CustomDomainEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.domain.models.AccountabilityDomainModel
import com.example.domain.models.DomainType
import com.example.domain.models.PredefinedDomains
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class DomainsViewModel(
    private val accountabilityRepository: AccountabilityRepository,
    private val userRepository: com.example.data.repositories.UserRepository? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allDomainsFlow: StateFlow<List<AccountabilityDomainModel>> = combine(
        accountabilityRepository.customDomainsFlow,
        _searchQuery
    ) { customDomains, query ->
        val predefined = PredefinedDomains.ALL
        val mappedCustom = customDomains.map {
            AccountabilityDomainModel(
                id = it.id,
                type = DomainType.CUSTOM,
                titleKey = it.name,
                descKey = it.description,
                iconName = it.iconName,
                isCustom = true,
                measurementUnit = it.measurementType
            )
        }
        val combined = predefined + mappedCustom
        val q = query.trim()
        if (q.isBlank()) {
            combined
        } else {
            combined.filter { domain ->
                domain.id.contains(q, ignoreCase = true) ||
                domain.titleKey.contains(q, ignoreCase = true) ||
                domain.descKey.contains(q, ignoreCase = true) ||
                domain.measurementUnit.contains(q, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredefinedDomains.ALL)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addCustomDomain(name: String, description: String, iconName: String, measurementType: String) {
        viewModelScope.launch {
            val user = userRepository?.getOrCreateGuestUser()
            val userId = user?.id ?: "guest_user"
            val domain = CustomDomainEntity(
                id = "custom_${UUID.randomUUID()}",
                userId = userId,
                name = name,
                description = description,
                iconName = iconName,
                measurementType = measurementType
            )
            accountabilityRepository.saveCustomDomain(domain)
        }
    }

    fun deleteCustomDomain(id: String) {
        viewModelScope.launch {
            accountabilityRepository.deleteCustomDomain(id)
        }
    }
}
