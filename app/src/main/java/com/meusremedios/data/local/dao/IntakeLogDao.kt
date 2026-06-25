package com.meusremedios.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.meusremedios.data.local.entity.IntakeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: IntakeLogEntity): Long

    @Update
    suspend fun update(log: IntakeLogEntity)

    @Delete
    suspend fun delete(log: IntakeLogEntity)

    @Query("SELECT * FROM intake_logs WHERE date = :date ORDER BY scheduled_at ASC")
    fun observeByDate(date: String): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE medication_id = :medicationId ORDER BY scheduled_at DESC")
    fun observeByMedication(medicationId: Long): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE medication_id = :medicationId AND schedule_time_id = :scheduleTimeId AND date = :date LIMIT 1")
    suspend fun getByMedicationScheduleDate(
        medicationId: Long,
        scheduleTimeId: Long,
        date: String,
    ): IntakeLogEntity?

    @Query("DELETE FROM intake_logs WHERE date < :thresholdDate")
    suspend fun deleteOlderThan(thresholdDate: String): Int
}
