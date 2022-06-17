package cs.mad.habittracker.activities

import android.app.ActionBar
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cs.mad.habittracker.adapters.StatsAdapter
import cs.mad.habittracker.databinding.ActivityStatsBinding
import cs.mad.habittracker.entities.HabitDatabase
import cs.mad.habittracker.entities.HabitService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding
    private val habitDao by lazy { HabitDatabase.getDatabase(applicationContext).habitDao() }
    private lateinit var habitService: HabitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        val view = binding.root
        // For the action bar
        ActionBar.DISPLAY_HOME_AS_UP

        title = "Stats"

        setContentView(view)

        habitService = Retrofit.Builder()
                .baseUrl("http://localhost")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HabitService::class.java)

        binding.statsList.adapter = StatsAdapter(listOf(), habitDao)

        loadHabits()

        binding.statsList.setOnClickListener {

        }

    }



    private fun loadHabits() {
        lifecycleScope.launch {
            loadFromDb()
        }
    }

    private fun loadFromDb() {
        lifecycleScope.launch {
            (binding.statsList.adapter as StatsAdapter).setData(habitDao.getAll())
        }

    }

}