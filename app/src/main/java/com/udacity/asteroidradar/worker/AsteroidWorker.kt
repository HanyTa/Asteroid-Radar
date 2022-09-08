package com.udacity.asteroidradar.worker

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class AsteroidWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {


    companion object {
        const val WORK_NAME = "AsteroidWorker"
    }


    override suspend fun doWork(): Result {
        val database = AsteroidDatabase.getInstance(applicationContext)
        val repository = AsteroidRepository(database)

        return try {
            repository.refreshAsteroidData()
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val currentTime = calendar.time
            val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
            }
            database.asteroidDao.deletePastAsteroids(dateFormat.format(currentTime))

            return Result.success()
        } catch (ex: HttpException) {
            Result.retry()
        }
    }
}