package school.cesar.myreminderapp.ui.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import school.cesar.myreminderapp.R
import school.cesar.myreminderapp.broadcast.AlarmBroadcastReceiver
import school.cesar.myreminderapp.connectors.DatabaseConnector
import school.cesar.myreminderapp.databinding.ActivityMainBinding
import school.cesar.myreminderapp.models.Reminder
import school.cesar.myreminderapp.ui.adapters.ReminderRecyclerViewAdapter
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var date: Long? = null
    private var hour: Int? = null
    private var min: Int? = null
    private lateinit var futureTime: Calendar
    private lateinit var reminder: Reminder

    private val reminderAdapter by lazy {
        ReminderRecyclerViewAdapter(mutableListOf(), this::removeReminder)
    }

    private val reminderDao by lazy {
        applicationContext.let {
            DatabaseConnector.getInstance(it).reminderDao
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        setupUi()
        setupData()
    }

    private fun setupUi() {
        setupButtons()
    }

    private fun setupRecyclerView() {
        binding.rvReminder.adapter = reminderAdapter
        binding.rvReminder.layoutManager = LinearLayoutManager(this)
    }

    private fun setupButtons() {
        binding.btnAddReminder.setOnClickListener {
            openReminderAppointment()
        }
    }

    private fun removeReminder(position: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                reminderDao.delete(reminderAdapter.data[position])
                withContext(Dispatchers.Main) {
                    reminderAdapter.data.removeAt(position)
                    reminderAdapter.notifyItemRemoved(position)
                    if (reminderAdapter.data.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvReminder.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupData() {
        setupRecyclerView()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                reminderAdapter.data.clear()
                reminderAdapter.data.addAll(reminderDao.getAll().toMutableList())
                if (reminderAdapter.data.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.rvReminder.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun openReminderAppointment() {
        val isSystem24Hour = is24HourFormat(this)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(Calendar.getInstance().get(Calendar.HOUR))
                .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                .setTitleText("Select Appointment time")
                .build()
        picker.show(supportFragmentManager, "tag")

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        val view = layoutInflater.inflate(R.layout.container_description_reminder, null)
        val descriptionDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.Theme_MyReminderApp_CustomDialog)
        descriptionDialogBuilder.apply {
            setView(view)
            setCancelable(true)
        }

        val dialog = descriptionDialogBuilder.create()

        view.findViewById<Button>(R.id.reminder_cancel).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.reminder_confirm).setOnClickListener {
            dialog.dismiss()

            val description = view.findViewById<TextView>(R.id.et_reminder_description).text.toString()
            val isChecked = view.findViewById<CheckBox>(R.id.cb_repeat).isChecked

            setupAlertManager(isChecked, description)

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    //Add logic to insert the reminder
                    reminderDao.insert(reminder)
                    withContext(Dispatchers.Main) {
                        reminderAdapter.data.add(reminder)
                        reminderAdapter.notifyItemInserted(reminderAdapter.data.indexOf(reminder))
                    }
                }
            }
        }

        picker.addOnPositiveButtonClickListener {
            hour = picker.hour
            min = picker.minute

            datePicker.show(supportFragmentManager, "tag")
        }

        datePicker.addOnPositiveButtonClickListener {
            datePicker.selection?.let {
                date = it
                dialog.show()
            }
        }
    }

    private fun setupAlertManager(repeat: Boolean, description: String) {
        futureTime = Calendar.getInstance().apply {
            date?.let {
                timeInMillis = it
                add(Calendar.DAY_OF_MONTH, 1)
            }

            hour?.let {
                set(Calendar.HOUR_OF_DAY, it)
            }

            min?.let {
                set(Calendar.MINUTE, it)
            }

        }

        reminder = Reminder(futureTime.timeInMillis, description)


        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.putExtra("REMINDER", reminder)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val time = SimpleDateFormat("EEEE, MMM d, yyyy - HH:mm a", Locale.getDefault()).format(futureTime.timeInMillis)
        Log.d("MainActivity", "setupAlertManager: $time")

        if (repeat) {
            manager.setExact(AlarmManager.RTC, futureTime.timeInMillis, pendingIntent)
            manager.setRepeating(AlarmManager.RTC, futureTime.timeInMillis, 86400000, pendingIntent)
        } else {
            manager.setExact(AlarmManager.RTC, futureTime.timeInMillis, pendingIntent)
        }
    }
}