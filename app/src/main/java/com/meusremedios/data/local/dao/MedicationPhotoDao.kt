package com.meusremedios.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.meusremedios.data.local.entity.MedicationPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationPhotoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(photo: MedicationPhotoEntity): Long

    @Update
    suspend fun update(photo: MedicationPhotoEntity)

    @Delete
    suspend fun delete(photo: MedicationPhotoEntity)

    @Query("SELECT * FROM medication_photos WHERE medication_id = :medicationId ORDER BY created_at ASC")
    fun observeByMedication(medicationId: Long): Flow<List<MedicationPhotoEntity>>

    @Query("SELECT * FROM medication_photos WHERE medication_id = :medicationId ORDER BY created_at ASC")
    suspend fun getByMedication(medicationId: Long): List<MedicationPhotoEntity>

    @Query("SELECT * FROM medication_photos")
    suspend fun getAll(): List<MedicationPhotoEntity>

    @Query("SELECT * FROM medication_photos WHERE embedding IS NULL")
    suspend fun getWithoutEmbedding(): List<MedicationPhotoEntity>
}
