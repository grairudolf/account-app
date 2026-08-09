package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.GoalEntity
import com.example.data.repositories.AccountabilityRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class GoalsViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    private val _selectedFrequency = MutableStateFlow("ALL") // ALL, DAILY, WEEKLY, MONTHLY
    val selectedFrequency: StateFlow<String> = _selectedFrequency.asStateFlow()

    val goalsWithProgressFlow: StateFlow<List<GoalWithProgress>> = combine(
        accountabilityRepository.allGoalsFlow,
        accountabilityRepository.allEntriesFlow,
        _selectedFrequency
    ) { goals, entries, freq ->
        val filteredGoals = if (freq == "ALL") goals else goals.filter { it.frequency == freq }
        filteredGoals.map { goal ->
            val progress = accountabilityRepository.calculateGoalProgress(goal, entries)
            val pct = if (goal.targetValue > 0) {
                ((progress / goal.targetValue) * 100).toInt().coerceIn(0, 100)
            } else 0
            GoalWithProgress(goal, progress, pct)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onFrequencySelected(frequency: String) {
        _selectedFrequency.value = frequency
    }

    fun addGoal(
        userId: String,
        domainId: String,
        title: String,
        targetValue: Double,
        unit: String,
        frequency: String,
        startDateIso: String
    ) {
        viewModelScope.launch {
            val goal = GoalEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                domainId = domainId,
                title = title,
                targetValue = targetValue,
                unit = unit,
                frequency = frequency,
                startDateIso = startDateIso
            )
            accountabilityRepository.saveGoal(goal)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            accountabilityRepository.deleteGoal(id)
        }
    }
}
