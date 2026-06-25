package com.meusremedios.ui.today

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.domain.usecase.FakeIntakeLogRepository
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.MarkIntakeSkippedUseCase
import com.meusremedios.domain.usecase.MarkIntakeTakenUseCase
import com.meusremedios.domain.usecase.ObserveDailyReportUseCase
import com.meusremedios.ui.theme.MeusRemediosTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.LocalTime

/**
 * Testes Compose/Robolectric de [TodayScreen].
 * Verifica que a tela do dia renderiza com fakes de repositório.
 */
@RunWith(RobolectricTestRunner::class)
class TodayScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val clock = Clock.systemDefaultZone()
    private val medications = FakeMedicationRepository()
    private val schedules = FakeScheduleRepository()
    private val intakeLogs = FakeIntakeLogRepository()

    private fun viewModel() = TodayViewModel(
        observeDailyReport = ObserveDailyReportUseCase(medications, schedules, intakeLogs, clock),
        markIntakeTakenUseCase = MarkIntakeTakenUseCase(intakeLogs, clock),
        markIntakeSkippedUseCase = MarkIntakeSkippedUseCase(intakeLogs, clock),
        clock = clock,
    )

    @Test
    fun todayScreen_rendersTitleHoje() {
        composeRule.setContent {
            MeusRemediosTheme {
                TodayScreen(onOpenMedication = {}, viewModel = viewModel())
            }
        }

        composeRule.onNodeWithText("Hoje").assertIsDisplayed()
    }

    @Test
    fun todayScreen_rendersDoseCard_whenMedicationSeeded() {
        runBlocking {
            val id = medications.add(Medication(name = "Losartana"))
            schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))
        }

        composeRule.setContent {
            MeusRemediosTheme {
                TodayScreen(onOpenMedication = {}, viewModel = viewModel())
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Losartana").assertIsDisplayed()
    }
}
