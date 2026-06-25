package com.meusremedios.notifications

import android.content.Context
import com.meusremedios.domain.usecase.MarkIntakeTakenUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotificationActionHandlerTest {

    private lateinit var markIntakeTakenUseCase: MarkIntakeTakenUseCase
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var context: Context
    private lateinit var handler: NotificationActionHandler

    @Before
    fun setUp() {
        markIntakeTakenUseCase = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)
        context = mockk(relaxed = true)
        every { context.startActivity(any()) } just runs
        handler = NotificationActionHandler(context, markIntakeTakenUseCase, notificationHelper)
    }

    @Test
    fun `handleMarkTaken with scheduleTimeId calls use case and cancels notification`() = runTest {
        handler.handleMarkTaken(
            medicationId = 1L,
            medicationName = "Losartana",
            scheduleTimeId = 5L,
            timeLabel = "08:00",
            scheduledAtIso = "2026-06-24T08:00:00Z",
            dateIso = "2026-06-24",
            notificationId = 5,
        )

        coVerify { markIntakeTakenUseCase(any<com.meusremedios.domain.model.ScheduledDose>(), LocalDate.parse("2026-06-24")) }
        verify { notificationHelper.cancel(5) }
    }

    @Test
    fun `handleMarkTaken with absent scheduleTimeId uses ad-hoc overload`() = runTest {
        handler.handleMarkTaken(
            medicationId = 1L,
            medicationName = "Losartana",
            scheduleTimeId = -1L,
            timeLabel = "08:00",
            scheduledAtIso = "2026-06-24T08:00:00Z",
            dateIso = "2026-06-24",
            notificationId = 5,
        )

        coVerify { markIntakeTakenUseCase(eq(1L), LocalDate.parse("2026-06-24")) }
        verify { notificationHelper.cancel(5) }
    }

    @Test
    fun `handleSnooze schedules new alarm and cancels current notification`() {
        handler.handleSnooze(
            notificationId = 5,
            medicationId = 1L,
            medicationName = "Losartana",
            scheduleTimeId = 5L,
            timeLabel = "08:00",
            scheduledAt = "2026-06-24T08:00:00Z",
            date = "2026-06-24",
        )

        verify { notificationHelper.scheduleDoseAlarm(eq(5), any(), eq(1L), any(), eq(5L), any(), any(), any()) }
        verify { notificationHelper.cancel(5) }
    }

}
