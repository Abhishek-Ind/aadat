package com.aadat.app.worker

import android.content.Context
import androidx.work.*
import com.aadat.app.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleNotification(habit: Habit) {
        val hour = habit.notificationTimeHour ?: return
        val minute = habit.notificationTimeMinute ?: return

        cancelNotification(habit.id)

        val now = LocalDateTime.now()
        val target = now.toLocalDate().atTime(LocalTime.of(hour, minute))
        val targetDateTime = if (target.isAfter(now)) target else target.plusDays(1)
        val initialDelay = java.time.Duration.between(now, targetDateTime).toMinutes()

        val inputData = workDataOf(
            HabitNotificationWorker.HABIT_ID_KEY to habit.id,
            HabitNotificationWorker.HABIT_NAME_KEY to habit.name
        )

        val workRequest = PeriodicWorkRequestBuilder<HabitNotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag("habit_notification_${habit.id}")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "habit_notification_${habit.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelNotification(habitId: String) {
        workManager.cancelUniqueWork("habit_notification_$habitId")
    }

    fun cancelAllNotifications() {
        workManager.cancelAllWork()
    }
}
