package com.meusremedios.data.repository

import com.meusremedios.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/** Mediação para as configurações do app (singleton, com valores padrão). */
interface SettingsRepository {
    /** Emite as configurações atuais, com os valores padrão se ainda não houver registro. */
    fun observe(): Flow<AppSettings>

    suspend fun get(): AppSettings

    suspend fun update(settings: AppSettings)
}
