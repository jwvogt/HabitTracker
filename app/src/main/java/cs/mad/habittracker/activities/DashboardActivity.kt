package cs.mad.habittracker.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cs.mad.habittracker.R
import cs.mad.habittracker.adapters.DashboardAdapter
import cs.mad.habittracker.databinding.ActivityDashboardBinding
import cs.mad.habittracker.databinding.DialogCreateHabitBinding
import cs.mad.habittracker.databinding.DialogDeleteHabitBinding
import cs.mad.habittracker.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val habitDao by lazy { HabitDatabase.getDatabase(applicationContext).habitDao() }
    private lateinit var habitService: HabitService


    // For swipe
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var colorDrawableBackground: ColorDrawable
    private lateinit var deleteIcon: Drawable



    private var habits: MutableList<Habit> = mutableListOf()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        binding.habitList.adapter = DashboardAdapter(listOf(), habitDao)
        loadHabits()

        viewAdapter = DashboardAdapter(dataSet = habits,habitDao)
        viewManager = LinearLayoutManager(this)

        colorDrawableBackground = ColorDrawable(Color.parseColor("#ff0000"))
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete)!!


        // For Swipe
        binding.habitList.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder2: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDirection: Int) {

                showDeleteDialog(this@DashboardActivity, (viewAdapter as DashboardAdapter).getHabit(viewHolder.adapterPosition))
                loadHabits()
            }

            override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMarginVertical = (viewHolder.itemView.height - deleteIcon.intrinsicHeight) / 2

                if (dX > 0) {
                    colorDrawableBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    deleteIcon.setBounds(itemView.left + iconMarginVertical, itemView.top + iconMarginVertical,
                            itemView.left + iconMarginVertical + deleteIcon.intrinsicWidth, itemView.bottom - iconMarginVertical)
                } else {
                    colorDrawableBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(itemView.right - iconMarginVertical - deleteIcon.intrinsicWidth, itemView.top + iconMarginVertical,
                            itemView.right - iconMarginVertical, itemView.bottom - iconMarginVertical)
                    deleteIcon.level = 0
                }

                colorDrawableBackground.draw(c)

                c.save()

                if (dX > 0)
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                else
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

                deleteIcon.draw(c)

                c.restore()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.habitList)

        habitService = Retrofit.Builder()
                .baseUrl("http://localhost")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HabitService::class.java)

        loadHabits()

        binding.addHabitButton.setOnClickListener {
            lifecycleScope.launch {
                showCreateDialog(it.context, Habit(
                        null,
                        "New Habit",
                        3, "weekly",
                        0,
                        Calendar.getInstance().timeInMillis,
                        "5",
                        "minutes",
                        "12:00PM",
                        "")
                )
                loadFromDb()
                val itemCount = (binding.habitList.adapter as DashboardAdapter).itemCount
                if (itemCount > 0) {
                    binding.habitList.smoothScrollToPosition(itemCount - 1)
                }
            }

        }

        binding.statsButton.setOnClickListener {
            val intent = Intent(
                    this@DashboardActivity,
                    StatsActivity::class.java
            )
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDeleteDialog(context: Context, habit: Habit) {
        val dialogBinding = DialogDeleteHabitBinding.inflate(layoutInflater)
        val customBody = dialogBinding.deleteDialog

            dialogBinding.message.text = "Are you sure you want to delete this habit?"

            AlertDialog.Builder(context)
                    .setView(customBody)
                    .setPositiveButton("Delete") { _, _ ->
                        GlobalScope.launch {
                            deleteHabit(habit)
                            this.launch(Dispatchers.Main) {
                                (binding.habitList.adapter as DashboardAdapter).setData(habitDao.getAll())
                            }
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .create()
                    .show()
    }

    @SuppressLint("SetTextI18n")
    private fun showCreateDialog(context: Context, habit: Habit) {
        val dialogBinding = DialogCreateHabitBinding.inflate(layoutInflater)
        val customBody = dialogBinding.createDialog

        dialogBinding.dailyButton.setOnClickListener {
            habit.interval = "daily"
        }

        dialogBinding.weeklyButton.setOnClickListener {
            habit.interval = "weekly"
        }

        dialogBinding.editName.setText(habit.name)
        dialogBinding.goalEdit.setText(habit.goal.toString())

        AlertDialog.Builder(context)
                .setView(customBody)
                .setPositiveButton("Done") { _, _ ->
                    GlobalScope.launch {
                        habit.name = dialogBinding.editName.text.toString()
                        habit.goal = dialogBinding.goalEdit.text.toString().toInt()
                        addHabit(habit)
                        Log.d("Habit", habit.date_created.toString())
                        this.launch(Dispatchers.Main) {
                            (binding.habitList.adapter as DashboardAdapter).setData(habitDao.getAll())
                        }
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->

                }
                .create()
                .show()
    }

    private fun addHabit(habit: Habit) {
        lifecycleScope.launch {
            habitDao.insert(habit)
            loadFromDb()
        }
    }

    private fun updateHabit(habit: Habit) {
        lifecycleScope.launch {
            habitDao.update(habit)
            loadHabits()
        }
    }

    private fun deleteHabit(habit: Habit) {
        lifecycleScope.launch {
            habitDao.delete(habit)
            loadFromDb()
        }
    }

    private fun loadHabits() {
        lifecycleScope.launch {
            loadFromDb()
        }
    }

    private fun loadFromDb() {
        lifecycleScope.launch {
            (binding.habitList.adapter as DashboardAdapter).setData(habitDao.getAll())
        }

    }
}


