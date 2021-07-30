package school.cesar.myreminderapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text
import school.cesar.myreminderapp.R
import school.cesar.myreminderapp.databinding.ContainerRemindersBinding
import school.cesar.myreminderapp.models.Reminder
import java.text.SimpleDateFormat
import java.util.*

class ReminderRecyclerViewAdapter (private val reminders: MutableList<Reminder>, private val callback: (Int) -> Unit) : RecyclerView.Adapter<ReminderRecyclerViewAdapter.ViewHolder>() {

    var data = reminders

    class ViewHolder (view: ContainerRemindersBinding) : RecyclerView.ViewHolder(view.root) {
        val tvReminderTime: TextView = view.tvReminder
        val tvReminderDescription: TextView = view.tvReminderDescription
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.container_reminders, parent, false)
        val binding = ContainerRemindersBinding.bind(view)
        val viewHolder = ViewHolder(binding)

        viewHolder.itemView.setOnClickListener {
            callback(viewHolder.adapterPosition)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (reminderTime, reminderDescription) = reminders[position]

        val date = Calendar.getInstance()
        date.timeInMillis = reminderTime

        holder.tvReminderTime.text = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(date.time)
        holder.tvReminderDescription.text = reminderDescription
    }

    override fun getItemCount() = reminders.size
}