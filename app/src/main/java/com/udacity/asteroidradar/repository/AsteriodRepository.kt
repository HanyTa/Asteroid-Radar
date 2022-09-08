package com.udacity.asteroidradar.repository

import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.getNextSevenDaysFormattedDates
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.asDatabaseModel
import com.udacity.asteroidradar.database.AsteroidDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AsteroidRepository(private val database: AsteroidDatabase) {

    val dayPicture = database.asteroidDao.getDayPicture()
    val asteroidData = database.asteroidDao.getAsteroids(
        getNextSevenDaysFormattedDates()[0],
        getNextSevenDaysFormattedDates()[getNextSevenDaysFormattedDates().size - 1]
    )
    val todayAsteroids = database.asteroidDao.getTodayAsteroids(getNextSevenDaysFormattedDates()[0])

    suspend fun refreshAsteroidData() {
        withContext(Dispatchers.IO) {
            val pictureOfDay = Network.asteroidService.getPictureOfDay(Constants.API_KEY).await()
            database.asteroidDao.insertPictureOfDay(pictureOfDay)
            val queryMap = HashMap<String, String>()

            queryMap["start_date"] = getNextSevenDaysFormattedDates()[0];
            queryMap["end_date"] =
                getNextSevenDaysFormattedDates()[getNextSevenDaysFormattedDates().size - 1];
            queryMap["api_key"] = Constants.API_KEY;
            val asteroidFeeds: String = Network.asteroidService.getAsteroidFeeds(queryMap).await();
            val asteroidList = parseAsteroidsJsonResult(JSONObject(asteroidFeeds))
            database.asteroidDao.insertAllAsteroids(*asteroidList.asDatabaseModel())
        }
    }
}