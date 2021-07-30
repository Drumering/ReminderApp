package school.cesar.myreminderapp.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import school.cesar.myreminderapp.R

class NotificationHelper {

    companion object {

        private fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "foreground"
                val descriptionText = "foreground application"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("0", name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        private fun genNotification(context: Context, description: String): Notification {

            createNotificationChannel(context)

            val builder = NotificationCompat.Builder(context, "0").apply {
                setSmallIcon(R.drawable.ic_notification_base)
                setContentTitle("My Reminder App")
                setContentText(description)
                setAutoCancel(true)
            }

            return builder.build()
        }

        fun sendNotification(context: Context, description: String) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(0, genNotification(context, description))
        }
    }
}