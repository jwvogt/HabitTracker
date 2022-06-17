package cs.mad.habittracker.activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.SystemClock
import android.provider.AlarmClock
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cs.mad.habittracker.databinding.ActivityHabitBinding
import cs.mad.habittracker.entities.Habit
import cs.mad.habittracker.entities.HabitDatabase
import kotlinx.coroutines.launch


class HabitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHabitBinding
    private val habitDao by lazy { HabitDatabase.getDatabase(applicationContext).habitDao() }
    //private lateinit var habit: Habit
    var habit:Habit = Habit(
            null,
            "New Habit",
            3,
            "weekly",
            0,
            java.util.Calendar.getInstance().timeInMillis,
            "5",
            "minutes",
            "12:00PM",
            ""
    )

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitBinding.inflate(layoutInflater)
        val view = binding.root
        //For the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(view)

        binding.buttonEditTitle.setOnClickListener {
            val customTitle = TextView(it.context)
            val customBody = EditText(it.context)

            customTitle.text = "Edit Habit Name"
            customBody.setText(habit.name)

            AlertDialog.Builder(it.context)
                    .setCustomTitle(customTitle)
                    .setView(customBody)
                    .setPositiveButton("Done") { _, _ ->
                        habit.name = customBody.text.toString()
                        binding.habitSetTitle.text = habit.name
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .create()
                    .show()
        }



        binding.notesText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                print(s)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                habit.notes = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                print(s)
            }
        })

        binding.timerTimeEntered.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                print(s)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                habit.timerTime = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                print(s)
            }
        })


        lifecycleScope.launch {
            habit = habitDao.getHabit(intent.getStringExtra("id")!!) // *

            title = "Habit"

            binding.habitSetTitle.text = habit.name

            var count = habit.timesPerformed
            var goal = habit.goal
            val timerTime = habit.timerTime
            val reminderTime = habit.reminderTime
            habit.notes

            displayHabitData(habit)

            binding.remindersOffButton.setOnClickListener {
                val text = "Feature Coming Soon!"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()

            }

            binding.remindersOnButton.setOnClickListener {
                val text = "Feature Coming Soon!"
                val duration = Toast.LENGTH_SHORT

                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()

            }

            binding.reminderTimeEntered.text = reminderTime
            binding.timerTimeEntered.setText(timerTime)

            if (habit.interval == "daily") {

                binding.intervalRadioGroup.check(binding.goalButtonDaily.id)
            } else if (habit.interval == "weekly") {

                binding.intervalRadioGroup.check(binding.goalButtonWeekly.id)
            }

            if (habit.timerInterval == "minutes") {

                binding.timerIntervalButtons.check(binding.minutesButton.id)
            } else if (habit.timerInterval == "seconds") {

                binding.timerIntervalButtons.check(binding.secondsButton.id)
            }

            binding.goalIncrementButton.setOnClickListener {
                if (goal < 100) {
                    goal++
                    habit.goal = goal
                    binding.goalEdit.text = goal.toString()

                }
            }

            binding.goalDecrementButton.setOnClickListener {
                if (goal > 0) {
                    goal--
                    habit.goal = goal
                    binding.goalEdit.text = goal.toString()

                }
            }

            binding.completeIncrementButton.setOnClickListener {
                count++
                habit.timesPerformed = count
                binding.completedCount.text = count.toString()

            }

            binding.completeDecrementButton.setOnClickListener {
                count--
                habit.timesPerformed = count
                binding.completedCount.text = count.toString()

            }

            binding.timerLaunchButton.setOnClickListener {

                var time = binding.timerTimeEntered.text.toString().toInt()
                if(binding.minutesButton.isChecked) {
                    time *= 60
                }
                try {
                    val intent = Intent(AlarmClock.ACTION_SET_TIMER)
                            .putExtra(AlarmClock.EXTRA_LENGTH, time)
                            .putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    binding.timerLaunchButton.text = "You don't have the Clock app!"
                }
            }

                //https://www.journaldev.com/9976/android-date-time-picker-dialog
                binding.reminderTimeEdit.setOnClickListener {

                    val mHour: Int = Calendar.HOUR
                    val mMinute: Int = Calendar.MINUTE
                    var amOrPm = "AM"

                    val timePickerDialog = TimePickerDialog(
                            it.context,
                            { _,
                                                hourOfDay,
                                                minute ->
                                var hours = hourOfDay
                                var minutes = minute.toString()
                                if (hours >= 12) {
                                    amOrPm= "PM"
                                }
                                if (hours > 12) {
                                    hours %= 12
                                }
                                if (hours == 0) {
                                    hours = 12
                                }
                                if (minutes == "0") {
                                    minutes = "00"
                                }
                                binding.reminderTimeEntered.text = "$hours:$minutes$amOrPm"
                                habit.reminderTime = "$hours:$minutes$amOrPm"
                            },
                            mHour,
                            mMinute,
                            false

                    )
                    timePickerDialog.show()
                }
            }





            binding.goalButtonDaily.setOnClickListener {
                habit.interval = "daily"

            }

            binding.goalButtonWeekly.setOnClickListener {
                habit.interval = "weekly"

            }

            binding.minutesButton.setOnClickListener {
                habit.timerInterval = "minutes"
            }

            binding.secondsButton.setOnClickListener {
                habit.timerInterval = "seconds"
            }







            val dayMultiplier: Int = if (habit.interval == "weekly") 7 else 1

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

            alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1000 * 60 * 24 * dayMultiplier,
                    PendingIntent.getService(
                            applicationContext,
                            1, //Id for the request, this number is probably wrong
                            intent,
                            PendingIntent.FLAG_NO_CREATE
                    )
            )


        }












    override fun onPause() {
        updateHabit(habit)
        super.onPause()

    }

    private fun displayHabitData(habit: Habit) {
        binding.completedCount.text = habit.timesPerformed.toString()
        binding.goalEdit.text = habit.goal.toString()
        binding.notesText.setText(habit.notes)
    }

    private fun updateHabit(habit: Habit) {
        lifecycleScope.launch {
            habitDao.update(habit)
            displayHabitData(habit)

        }
    }




}


