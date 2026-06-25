package com.meusremedios.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.meusremedios.data.local.entity.AppSettingsEntity
import com.meusremedios.data.local.entity.IntakeLogEntity
import com.meusremedios.data.local.entity.MedicationEntity
import com.meusremedios.data.local.entity.MedicationPhotoEntity
import com.meusremedios.data.local.entity.ScheduleTimeEntity
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.PhotoSide
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DaoTest {
    private lateinit var db: MeusRemediosDatabase

    @Before
    fun setUp() {
        db =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                MeusRemediosDatabase::class.java,
            ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun medication(name: String) =
        MedicationEntity(
            name = name,
            dosage = null,
            notes = null,
            periodType = PeriodType.CONTINUOUS,
            startDate = null,
            endDate = null,
            remindersEnabled = true,
            createdAt = "2026-06-22T10:00:00Z",
        )

    @Test
    fun insertAndGetMedication_preservesData() =
        runTest {
            val id = db.medicationDao().insert(medication("Dipirona"))
            val loaded = db.medicationDao().getById(id)
            assertEquals("Dipirona", loaded?.name)
            assertEquals(id, loaded?.id)
        }

    @Test
    fun observeAll_isOrderedByName() =
        runTest {
            db.medicationDao().insert(medication("Zinco"))
            db.medicationDao().insert(medication("Aspirina"))
            val list = db.medicationDao().observeAll().first()
            assertEquals(listOf("Aspirina", "Zinco"), list.map { it.name })
        }

    @Test
    fun observeAll_emitsOnInsert() =
        runTest {
            db.medicationDao().observeAll().test {
                assertEquals(emptyList<MedicationEntity>(), awaitItem())
                db.medicationDao().insert(medication("Paracetamol"))
                assertEquals(listOf("Paracetamol"), awaitItem().map { it.name })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun deletingMedication_cascadesPhotos() =
        runTest {
            val medId = db.medicationDao().insert(medication("Losartana"))
            db.medicationPhotoDao().insert(
                MedicationPhotoEntity(
                    medicationId = medId,
                    filePath = "/data/photo.jpg",
                    side = PhotoSide.FRONT,
                    embedding = floatArrayOf(1f, 2f, 3f),
                    dominantColorLab = floatArrayOf(50f, 0f, 0f),
                    aspectRatio = 1.5f,
                    createdAt = "2026-06-22T10:00:00Z",
                ),
            )
            assertEquals(1, db.medicationPhotoDao().getByMedication(medId).size)

            db.medicationDao().delete(db.medicationDao().getById(medId)!!)
            assertTrue(db.medicationPhotoDao().getByMedication(medId).isEmpty())
        }

    @Test
    fun photoEmbedding_roundTripsThroughBlob() =
        runTest {
            val medId = db.medicationDao().insert(medication("Omeprazol"))
            val embedding = floatArrayOf(0.5f, -1.25f, 3.0f)
            db.medicationPhotoDao().insert(
                MedicationPhotoEntity(
                    medicationId = medId,
                    filePath = "/data/o.jpg",
                    side = PhotoSide.BACK,
                    embedding = embedding,
                    dominantColorLab = null,
                    aspectRatio = null,
                    createdAt = "2026-06-22T10:00:00Z",
                ),
            )
            val loaded = db.medicationPhotoDao().getByMedication(medId).single()
            assertArrayEquals(embedding, loaded.embedding, 0f)
            assertNull(loaded.dominantColorLab)
        }

    @Test
    fun getWithoutEmbedding_returnsOnlyPhotosWithNullEmbedding() =
        runTest {
            val medId = db.medicationDao().insert(medication("Losartana"))
            db.medicationPhotoDao().insert(
                MedicationPhotoEntity(
                    medicationId = medId,
                    filePath = "/data/pill_with.jpg",
                    side = PhotoSide.FRONT,
                    embedding = floatArrayOf(0.1f, 0.2f),
                    dominantColorLab = null,
                    aspectRatio = null,
                    createdAt = "2026-06-24T10:00:00Z",
                ),
            )
            db.medicationPhotoDao().insert(
                MedicationPhotoEntity(
                    medicationId = medId,
                    filePath = "/data/pill_without.jpg",
                    side = PhotoSide.BACK,
                    embedding = null,
                    dominantColorLab = null,
                    aspectRatio = null,
                    createdAt = "2026-06-24T10:01:00Z",
                ),
            )

            val result = db.medicationPhotoDao().getWithoutEmbedding()

            assertEquals(1, result.size)
            assertEquals("/data/pill_without.jpg", result.single().filePath)
        }

    @Test
    fun update_persistsEmbeddingChange() =
        runTest {
            val medId = db.medicationDao().insert(medication("Atenolol"))
            val photoId =
                db.medicationPhotoDao().insert(
                    MedicationPhotoEntity(
                        medicationId = medId,
                        filePath = "/data/atenolol.jpg",
                        side = PhotoSide.FRONT,
                        embedding = null,
                        dominantColorLab = null,
                        aspectRatio = null,
                        createdAt = "2026-06-24T10:00:00Z",
                    ),
                )
            val saved = db.medicationPhotoDao().getByMedication(medId).single()
            val newEmbedding = floatArrayOf(0.9f, 0.1f)
            db.medicationPhotoDao().update(saved.copy(embedding = newEmbedding))

            val updated = db.medicationPhotoDao().getByMedication(medId).single()
            assertArrayEquals(newEmbedding, updated.embedding, 0f)
        }

    @Test
    fun scheduleTime_recoveredByMedication() =
        runTest {
            val medId = db.medicationDao().insert(medication("Metformina"))
            db.scheduleTimeDao().insert(
                ScheduleTimeEntity(
                    medicationId = medId,
                    timeOfDay = "08:00",
                    daysOfWeekMask = 0b111_1111,
                ),
            )
            val list = db.scheduleTimeDao().observeByMedication(medId).first()
            assertEquals("08:00", list.single().timeOfDay)
        }

    @Test
    fun intakeLog_filteredByDate() =
        runTest {
            val medId = db.medicationDao().insert(medication("Enalapril"))
            db.intakeLogDao().insert(intakeLog(medId, "2026-06-22"))
            db.intakeLogDao().insert(intakeLog(medId, "2026-06-23"))

            val onDate = db.intakeLogDao().observeByDate("2026-06-22").first()
            assertEquals(1, onDate.size)
            assertEquals("2026-06-22", onDate.single().date)
        }

    @Test
    fun intakeLog_deleteOlderThan_removesPastLogs() =
        runTest {
            val medId = db.medicationDao().insert(medication("Sinvastatina"))
            db.intakeLogDao().insert(intakeLog(medId, "2026-01-01"))
            db.intakeLogDao().insert(intakeLog(medId, "2026-06-22"))

            val removed = db.intakeLogDao().deleteOlderThan("2026-06-01")
            assertEquals(1, removed)
            assertEquals(1, db.intakeLogDao().observeByMedication(medId).first().size)
        }

    @Test
    fun appSettings_upsertReplacesSingleton() =
        runTest {
            val dao = db.appSettingsDao()
            assertNull(dao.get())

            dao.upsert(settings(retention = 90))
            dao.upsert(settings(retention = 30))

            val loaded = dao.get()
            assertEquals(30, loaded?.historyRetentionDays)
            assertEquals(AppSettingsEntity.SINGLETON_ID, loaded?.id)
        }

    private fun intakeLog(
        medicationId: Long,
        date: String,
    ) = IntakeLogEntity(
        medicationId = medicationId,
        scheduleTimeId = null,
        date = date,
        scheduledAt = "${date}T08:00:00Z",
        takenAt = null,
        status = IntakeStatus.PENDING,
    )

    private fun settings(retention: Int) =
        AppSettingsEntity(
            historyRetentionDays = retention,
            autoCapture = false,
            remindersGlobal = true,
            reminderLeadMinutes = 1,
            fontScale = null,
            highContrast = null,
        )
}
