package cs.mad.habittracker.entities

import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.room.*
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import java.time.Duration
import java.util.*
import java.util.Calendar
import java.util.Date

data class WebHabits(val habits: List<Habit>)

@Entity
data class Habit(
        @PrimaryKey(autoGenerate = true)
        val myId: Long?,
        @SerializedName("id")
        var name: String,
        var goal: Int,
        var interval: String,
        var timesPerformed: Int,
        var date_created: Long,
        var timerTime: String,
        var timerInterval: String,
        var reminderTime: String,
        var notes: String


)

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit")
    suspend fun getAll(): List<Habit>

    @Query("SELECT * FROM habit WHERE myId = :habitID")
    suspend fun getHabit(habitID: String): Habit



    @Query("DELETE FROM habit WHERE myID NOT NULL")
    suspend fun deleteFromWeb()

    @Insert
    suspend fun insert(vararg habit: Habit)

    @Insert
    suspend fun insert(habits: List<Habit>)

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)
}

interface HabitService {
    @GET("habits")
    fun getAll(): Call<WebHabits>
}