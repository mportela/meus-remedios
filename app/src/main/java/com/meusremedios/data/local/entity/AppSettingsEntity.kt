package com.meusremedios.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Entidade Room das configurações do app (registro único, id = 1). */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    @ColumnInfo(name = "history_retention_days")
    val historyRetentionDays: Int,
    @ColumnInfo(name = "auto_capture")
    val autoCapture: Boolean,
    @ColumnInfo(name = "reminders_global")
    val remindersGlobal: Boolean,
    @ColumnInfo(name = "reminder_lead_minutes")
    val reminderLeadMinutes: Int,
    @ColumnInfo(name = "font_scale")
    val fontScale: Float?,
    @ColumnInfo(name = "high_contrast")
    val highContrast: Boolean?,
) {
    companion object {
        const val SINGLETON_ID: Int = 1
    }
}
