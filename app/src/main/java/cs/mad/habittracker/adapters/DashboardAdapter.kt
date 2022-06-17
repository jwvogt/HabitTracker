package cs.mad.habittracker.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import cs.mad.habittracker.activities.HabitActivity
import cs.mad.habittracker.databinding.ItemDashboardHabitBinding
import cs.mad.habittracker.entities.Habit
import cs.mad.habittracker.entities.HabitDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DashboardAdapter(private var dataSet: List<Habit>, private val dao: HabitDao) :
    RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    init {
        setData(dataSet)
    }

    class ViewHolder(bind: ItemDashboardHabitBinding) : RecyclerView.ViewHolder(bind.root) {
        val binding: ItemDashboardHabitBinding = bind
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardHabitBinding.inflate(
            LayoutInflater.from(viewGroup.context),
            viewGroup,
            false
        )
        binding.root.minimumHeight = viewGroup.height / 4
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]

        viewHolder.binding.habitName.text = item.name
        viewHolder.binding.goal.text = "Goal: ${item.goal} ${item.interval}"
        viewHolder.binding.completed.text = "${item.timesPerformed} completed"

        viewHolder.binding.root.setOnClickListener {
            val intent = Intent(
                    viewHolder.itemView.context,
                    HabitActivity::class.java
            )
            intent.putExtra("id", item.myId.toString())
            intent.putExtra("count", item.timesPerformed.toString())
            viewHolder.itemView.context.startActivity(intent)
        }

        viewHolder.binding.root.setOnLongClickListener {
            showDeleteDialog(it.context, item)
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDeleteDialog(context: Context, habit: Habit) {
        val customTitle = TextView(context)
        customTitle.text = "Are you sure you want to delete ${habit.name}?"

        AlertDialog.Builder(context)
            .setCustomTitle(customTitle)
            .setPositiveButton("Delete") { _, _ ->
                GlobalScope.launch {
                    dao.delete(habit)
                    this.launch(Dispatchers.Main) {
                        setData(dao.getAll())
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
            .show()
    }

    fun addDemo() {
        if (dataSet.isEmpty()) {
            val demoHabits = mutableListOf<Habit>()
            demoHabits.add(
                    Habit(null,
                            "Practice Piano",
                            3,
                            "daily",
                            12,
                            1618900045640,
                            "15",
                            "minutes",
                            "11:00AM",
                            ""
                    )
            )


            demoHabits.add(
                    Habit(null,
                            "Study Math",
                            7,
                            "weekly",
                            48,
                            1611900045640,
                            "10",
                            "minutes",
                            "11:00AM",
                            ""
                    )
            )

            demoHabits.add(
                    Habit(null,
                            "Jogging",
                            5,
                            "weekly",
                            75,
                            1411900045640,
                            "15",
                            "minutes",
                            "11:00AM",
                            ""
                    )
            )

            setData(demoHabits)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun getHabit(position: Int) : Habit {
        return dataSet[position]
    }


    fun setData(items: List<Habit>) {
        dataSet = items
        notifyDataSetChanged()
    }
}


