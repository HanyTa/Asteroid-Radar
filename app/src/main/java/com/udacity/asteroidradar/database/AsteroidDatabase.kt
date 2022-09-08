package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import retrofit2.http.DELETE

@Dao
interface AsteroidDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPictureOfDay(pictureOfDay: PictureOfDay)

    @Query("SELECT * FROM day_picture ORDER BY title DESC LIMIT 1")
    fun getDayPicture() : LiveData<PictureOfDay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllAsteroids(vararg asteroid: Asteroid)

    @Query("SELECT * FROM asteroid WHERE closeApproachDate BETWEEN :startDate AND :endDate ORDER BY closeApproachDate DESC")
    fun getAsteroids(startDate: String,endDate: String) : LiveData<List<Asteroid>>

    @Query("SELECT * FROM asteroid WHERE closeApproachDate = :startDate ORDER BY closeApproachDate DESC")
    fun getTodayAsteroids(startDate: String) : LiveData<List<Asteroid>>

    @Query("DELETE FROM asteroid WHERE closeApproachDate < :startDate")
    fun deletePastAsteroids(startDate: String)
}

@Database(entities = [Asteroid::class, PictureOfDay::class], version = 1)
abstract class AsteroidDatabase : RoomDatabase() {

    abstract val asteroidDao: AsteroidDao

    companion object Instance {
        @Volatile
        private lateinit var INSTANCE: AsteroidDatabase
        fun getInstance(context: Context): AsteroidDatabase {
            synchronized(this) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AsteroidDatabase::class.java,
                        "asteroids"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}