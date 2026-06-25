package com.meusremedios.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.meusremedios.data.local.MeusRemediosDatabase
import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.PeriodType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class RepositoryTest {
    private lateinit var db: MeusRemediosDatabase
    private lateinit var medicationRepository: MedicationRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                MeusRemediosDatabase::class.java,
            ).allowMainThreadQueries().build()
        medicationRepository = MedicationRepositoryImpl(db.medicationDao())
        settingsRepository = SettingsRepositoryImpl(db.appSettingsDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun add_thenObserve_mapsToDomain() =
        runTest {
            val id =
                medicationRepository.add(
                    Medication(
                        name = "Dipirona",
                        dosage = "500 mg",
                        periodType = PeriodType.RANGED,
                        startDate = LocalDate.of(2026, 6, 1),
                        endDate = LocalDate.of(2026, 6, 30),
                    ),
                )

            val loaded = medicationRepository.getById(id)
            assertEquals("Dipirona", loaded?.name)
            assertEquals("500 mg", loaded?.dosage)
            assertEquals(PeriodType.RANGED, loaded?.periodType)
            assertEquals(LocalDate.of(2026, 6, 1), loaded?.startDate)
        }

    @Test
    fun settings_returnsDefaults_whenEmpty() =
        runTest {
            val defaults = settingsRepository.get()
            assertEquals(AppSettings.DEFAULT_RETENTION_DAYS, defaults.historyRetentionDays)
            assertEquals(AppSettings.DEFAULT_REMINDER_LEAD_MINUTES, defaults.reminderLeadMinutes)
            assertEquals(true, defaults.remindersGlobal)
        }

    @Test
    fun settings_persistsUpdate() =
        runTest {
            settingsRepository.update(AppSettings(historyRetentionDays = 30, autoCapture = true))
            val loaded = settingsRepository.observe().first()
            assertEquals(30, loaded.historyRetentionDays)
            assertEquals(true, loaded.autoCapture)
        }
}
