package school.cesar.myreminderapp.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "reminder_table")
data class Reminder(@PrimaryKey val reminderTime: Long, val reminderDescription: String) : Parcelable
