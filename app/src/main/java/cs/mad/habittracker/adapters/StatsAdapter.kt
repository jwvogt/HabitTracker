package cs.mad.habittracker.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color.RED
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import cs.mad.habittracker.databinding.DialogStatInfoBinding
import cs.mad.habittracker.databinding.ItemStatBinding
import cs.mad.habittracker.entities.Habit
import cs.mad.habittracker.entities.HabitDao
import java.util.*


class StatsAdapter(private var dataSet: List<Habit>, private val dao: HabitDao) :
        RecyclerView.Adapter<StatsAdapter.ViewHolder>() {

    init {
        setData(dataSet)
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(bind: ItemStatBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding: ItemStatBinding = bind
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStatBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
        )
        binding.root.minimumHeight = viewGroup.height / 4
        return ViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]
        val goal = item.goal.toFloat()
        val dateCreated = item.date_created
        val interval = item.interval
        val timesPerformed = item.timesPerformed.toFloat()

        var stat = 0.toFloat()

        // date logic
        val now = Calendar.getInstance().timeInMillis
        val daysTracked = ((now - dateCreated) / (1000 * 60 * 60 * 24)).toInt()  // 1000 milli per second, 60 sec per min, 60 min per hour, 24 hours per day

        Log.d("days tracked", daysTracked.toString())

        if (daysTracked == 0) {
            stat = (timesPerformed / goal) * 100
        }
        else {
            if (interval == "daily") {
                stat = (timesPerformed / (goal * daysTracked)) * 100
            } else if (interval == "weekly") {
                stat = if (daysTracked <= 7) {
                    (timesPerformed / goal) * 100
                } else {
                    (timesPerformed / (goal * (daysTracked / 7))) * 100
                }
            }
        }

        if (stat < 100) {
            viewHolder.binding.cardBackground.setCardBackgroundColor(RED)

        }
        viewHolder.binding.statName.text = item.name
//        viewHolder.binding.statGoal.text = "Goal: ${goal.toInt()} $interval"
        viewHolder.binding.timesCompleted.text = "Completed ${item.timesPerformed} times"
        viewHolder.binding.statPercent.text = "$stat%"
//        viewHolder.binding.daysTrackedLabel.text = "Days Tracked: $daysTracked"

        viewHolder.binding.root.setOnClickListener {
            val inflater: LayoutInflater = LayoutInflater.from(it.context)

            showStatDialog(inflater, it.context, item)
        }
    }


    private fun showStatDialog(inflater: LayoutInflater, context: Context, habit: Habit) {
        val dialogBinding = DialogStatInfoBinding.inflate(inflater)
        val customBody = dialogBinding.statDialog

        val dateCreated = habit.date_created

        val now = Calendar.getInstance().timeInMillis
        val daysTracked = ((now - dateCreated) / (1000 * 60 * 60 * 24)).toInt()

        var stat = 0.toFloat()

        if (daysTracked == 0) {
            stat = (habit.timesPerformed.toFloat() / habit.goal) * 100
        }
        else {
            if (habit.interval == "daily") {
                stat = (habit.timesPerformed.toFloat() / (habit.goal.toFloat() * daysTracked)) * 100
            } else if (habit.interval == "weekly") {
                stat = if (daysTracked <= 7) {
                    (habit.timesPerformed.toFloat() / habit.goal.toFloat()) * 100
                } else {
                    (habit.timesPerformed.toFloat() / (habit.goal.toFloat() * (daysTracked / 7))) * 100
                }
            }
        }

        if (stat < 100) {
            dialogBinding.adviceLabel.text = "Don't give up!!"
        }
        else {
            dialogBinding.adviceLabel.text = "Great work!!"
        }

        dialogBinding.statName.text = habit.name
        dialogBinding.goalDisplay.text = habit.goal.toString()
        dialogBinding.intervalDisplay.text = habit.interval
        dialogBinding.timesPerformedDisplay.text = habit.timesPerformed.toString()
        dialogBinding.daysTrackedDisplay.text = daysTracked.toString()
        dialogBinding.percentageDisplay.text = "${stat}%"

        AlertDialog.Builder(context)
                .setView(customBody)
                .setPositiveButton("Done") { _, _ -> }
                .create()
                .show()
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun setData(items: List<Habit>) {
        dataSet = items
        notifyDataSetChanged()
    }
}