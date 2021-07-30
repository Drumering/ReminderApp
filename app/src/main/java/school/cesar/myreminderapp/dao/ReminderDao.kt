package school.cesar.myreminderapp.dao

import androidx.room.*
import school.cesar.myreminderapp.models.Reminder

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminder_table")
    suspend fun getAll(): List<Reminder>
}