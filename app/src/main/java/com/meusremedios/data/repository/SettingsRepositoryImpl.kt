package com.meusremedios.data.repository

import com.meusremedios.data.local.dao.AppSettingsDao
import com.meusremedios.data.local.mapper.toDomain
import com.meusremedios.data.local.mapper.toEntity
import com.meusremedios.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val appSettingsDao: AppSettingsDao,
) : SettingsRepository {

    override fun observe(): Flow<AppSettings> =
        appSettingsDao.observe().map { it?.toDomain() ?: AppSettings() }

    override suspend fun get(): AppSettings =
        appSettingsDao.get()?.toDomain() ?: AppSettings()

    override suspend fun update(settings: AppSettings) =
        appSettingsDao.upsert(settings.toEntity())
}
