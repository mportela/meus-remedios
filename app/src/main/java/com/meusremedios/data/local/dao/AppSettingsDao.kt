package com.meusremedios.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meusremedios.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = :id")
    fun observe(id: Int = AppSettingsEntity.SINGLETON_ID): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = :id")
    suspend fun get(id: Int = AppSettingsEntity.SINGLETON_ID): AppSettingsEntity?
}
