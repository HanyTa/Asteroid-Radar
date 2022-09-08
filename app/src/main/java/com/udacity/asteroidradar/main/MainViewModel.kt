package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidType
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AsteroidDatabase.getInstance(application)
    private val repository = AsteroidRepository(database)

    val dayPic: LiveData<PictureOfDay> = repository.dayPicture
    private val asteroidTypeFilter = MutableLiveData(AsteroidType.SHOW_WEEKLY)

    val asteroidFeeds: LiveData<List<Asteroid>> = Transformations.switchMap(asteroidTypeFilter) {
        when (it) {
            AsteroidType.SHOW_WEEKLY -> {
                repository.asteroidData
            }
            else -> {
                repository.todayAsteroids
            }
        }
    }
    private val _navigateToDetails = MutableLiveData<Asteroid?>()
    val navigateToDetails: LiveData<Asteroid?>
        get() = _navigateToDetails

    init {
        getAsteroidsFeeds()
    }

    private fun getAsteroidsFeeds() {
        viewModelScope.launch {
            repository.refreshAsteroidData()
        }
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToDetails.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToDetails.value = null
    }

    fun filterDate(asteroidType: AsteroidType) {
        filterAsteroids(
            asteroidType
        )
    }

    private fun filterAsteroids(asteroidType: AsteroidType) {
        asteroidTypeFilter.value = asteroidType
    }

}