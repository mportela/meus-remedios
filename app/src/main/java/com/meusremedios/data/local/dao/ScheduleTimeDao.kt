package com.meusremedios.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.meusremedios.data.local.entity.ScheduleTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleTimeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(scheduleTime: ScheduleTimeEntity): Long

    @Update
    suspend fun update(scheduleTime: ScheduleTimeEntity)

    @Delete
    suspend fun delete(scheduleTime: ScheduleTimeEntity)

    @Query("SELECT * FROM schedule_times WHERE medication_id = :medicationId ORDER BY time_of_day ASC")
    fun observeByMedication(medicationId: Long): Flow<List<ScheduleTimeEntity>>

    @Query("SELECT * FROM schedule_times ORDER BY time_of_day ASC")
    fun observeAll(): Flow<List<ScheduleTimeEntity>>

    @Query("SELECT * FROM schedule_times")
    suspend fun getAll(): List<ScheduleTimeEntity>
}
