package com.example.favdish.application

import android.app.Application
import com.example.favdish.model.database.FavDishRepository
import com.example.favdish.model.database.FavDishRoomDatabase

/*
The idea of this class is to define the variables we want to use throughout the application
 */
class FavDishApplication: Application() {
    //we want the database to be loaded only when its needed, and not on the start of the app
    private val database by lazy { FavDishRoomDatabase.getDatabase(this) }
    //setting up repository, passing to it a Dao object
    val repository by lazy { FavDishRepository(database.favDishDao()) }
}