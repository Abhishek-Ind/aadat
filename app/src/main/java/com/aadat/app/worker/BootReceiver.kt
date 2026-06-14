package com.aadat.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aadat.app.data.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val habits = habitRepository.getAllActiveHabits().firstOrNull() ?: return@launch
            habits.filter { it.notificationTimeHour != null }.forEach { habit ->
                notificationScheduler.scheduleNotification(habit)
            }
        }
    }
}
