package school.cesar.myreminderapp.connectors

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import school.cesar.myreminderapp.dao.ReminderDao
import school.cesar.myreminderapp.models.Reminder

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
abstract class DatabaseConnector: RoomDatabase(){
    abstract val reminderDao: ReminderDao

    companion object {
        private var INSTANCE: DatabaseConnector? = null
        fun getInstance(context: Context) : DatabaseConnector {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext,
                    DatabaseConnector::class.java,
                    "reminder_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}