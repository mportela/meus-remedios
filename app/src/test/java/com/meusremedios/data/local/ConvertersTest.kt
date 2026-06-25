package com.meusremedios.data.local

import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.PhotoSide
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun localDate_roundTrip() {
        val date = LocalDate.of(2026, 6, 22)
        val stored = converters.localDateToString(date)
        assertEquals(date, converters.stringToLocalDate(stored))
    }

    @Test
    fun localTime_roundTrip() {
        val time = LocalTime.of(8, 30)
        val stored = converters.localTimeToString(time)
        assertEquals(time, converters.stringToLocalTime(stored))
    }

    @Test
    fun instant_roundTrip() {
        val instant = Instant.parse("2026-06-22T11:22:33Z")
        val stored = converters.instantToString(instant)
        assertEquals(instant, converters.stringToInstant(stored))
    }

    @Test
    fun enums_roundTrip() {
        assertEquals(
            PeriodType.RANGED,
            converters.stringToPeriodType(converters.periodTypeToString(PeriodType.RANGED)),
        )
        assertEquals(
            PhotoSide.BACK,
            converters.stringToPhotoSide(converters.photoSideToString(PhotoSide.BACK)),
        )
        assertEquals(
            IntakeStatus.LATE,
            converters.stringToIntakeStatus(converters.intakeStatusToString(IntakeStatus.LATE)),
        )
    }

    @Test
    fun floatArray_roundTrip_preservesValues() {
        val vector = floatArrayOf(0.1f, -2.5f, 3.14159f, 0f, Float.MAX_VALUE, Float.MIN_VALUE)
        val bytes = converters.floatArrayToBytes(vector)
        val restored = converters.bytesToFloatArray(bytes)
        assertArrayEquals(vector, restored, 0f)
    }

    @Test
    fun floatArray_isDeterministic() {
        val vector = floatArrayOf(1f, 2f, 3f)
        assertArrayEquals(
            converters.floatArrayToBytes(vector),
            converters.floatArrayToBytes(vector),
        )
    }

    @Test
    fun nullValues_convertToNull() {
        assertNull(converters.localDateToString(null))
        assertNull(converters.stringToLocalDate(null))
        assertNull(converters.floatArrayToBytes(null))
        assertNull(converters.bytesToFloatArray(null))
    }
}
