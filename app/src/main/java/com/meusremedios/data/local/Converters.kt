package com.meusremedios.data.local

import androidx.room.TypeConverter
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.PhotoSide
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Conversores de tipos de domínio para formatos persistíveis pelo Room.
 * Datas/horas em texto ISO-8601, `FloatArray` em BLOB (big-endian) e enums por
 * nome. Todas as conversões são reversíveis e determinísticas.
 */
class Converters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun instantToString(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun stringToInstant(value: String?): Instant? = value?.let(Instant::parse)

    @TypeConverter
    fun periodTypeToString(value: PeriodType?): String? = value?.name

    @TypeConverter
    fun stringToPeriodType(value: String?): PeriodType? = value?.let(PeriodType::valueOf)

    @TypeConverter
    fun photoSideToString(value: PhotoSide?): String? = value?.name

    @TypeConverter
    fun stringToPhotoSide(value: String?): PhotoSide? = value?.let(PhotoSide::valueOf)

    @TypeConverter
    fun intakeStatusToString(value: IntakeStatus?): String? = value?.name

    @TypeConverter
    fun stringToIntakeStatus(value: String?): IntakeStatus? = value?.let(IntakeStatus::valueOf)

    @TypeConverter
    fun floatArrayToBytes(value: FloatArray?): ByteArray? {
        if (value == null) return null
        val buffer =
            ByteBuffer.allocate(value.size * Float.SIZE_BYTES)
                .order(ByteOrder.BIG_ENDIAN)
        value.forEach(buffer::putFloat)
        return buffer.array()
    }

    @TypeConverter
    fun bytesToFloatArray(value: ByteArray?): FloatArray? {
        if (value == null) return null
        val buffer = ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN)
        val result = FloatArray(value.size / Float.SIZE_BYTES)
        for (i in result.indices) {
            result[i] = buffer.float
        }
        return result
    }
}
