package school.cesar.myreminderapp.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import school.cesar.myreminderapp.connectors.DatabaseConnector
import school.cesar.myreminderapp.helpers.NotificationHelper
import school.cesar.myreminderapp.models.Reminder
import kotlin.concurrent.thread

class AlarmBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val reminder = intent?.getParcelableExtra<Reminder>("DESCRIPTION")

            NotificationHelper.sendNotification(it, reminder?.reminderDescription ?: "")
            val reminderDao = DatabaseConnector.getInstance(it).reminderDao
            reminder?.let { item ->
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        reminderDao.delete(item)
                    }
                }
            }
        }
    }
}