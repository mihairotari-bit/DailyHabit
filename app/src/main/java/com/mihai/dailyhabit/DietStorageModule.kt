package com.mihai.dailyhabit

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DietStorageModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): DietDatabase =
        Room.databaseBuilder(context, DietDatabase::class.java, "diet-plans.db")
            .fallbackToDestructiveMigration(true)
            .build()
}
