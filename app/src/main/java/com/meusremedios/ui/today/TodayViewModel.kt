package com.meusremedios.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusremedios.domain.model.DailyReport
import com.meusremedios.domain.usecase.ObserveDailyReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val report: DailyReport? = null,
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    observeDailyReport: ObserveDailyReportUseCase,
    clock: Clock,
) : ViewModel() {

    val today: LocalDate = LocalDate.now(clock)
    private val selectedDate = MutableStateFlow(today)
    val selectedDateState: StateFlow<LocalDate> = selectedDate.asStateFlow()

    val uiState: StateFlow<TodayUiState> =
        selectedDate.flatMapLatest { date ->
            observeDailyReport(date).map { report ->
                TodayUiState(report = report, isLoading = false)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState(),
        )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun goToPreviousDay() {
        selectedDate.value = selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        selectedDate.value = selectedDate.value.plusDays(1)
    }
}
