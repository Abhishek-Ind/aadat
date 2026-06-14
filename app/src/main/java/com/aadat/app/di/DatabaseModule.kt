package com.aadat.app.di

import android.content.Context
import androidx.room.Room
import com.aadat.app.data.local.AadatDatabase
import com.aadat.app.data.local.dao.HabitCompletionDao
import com.aadat.app.data.local.dao.HabitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AadatDatabase =
        Room.databaseBuilder(context, AadatDatabase::class.java, "aadat_db")
            .build()

    @Provides
    fun provideHabitDao(db: AadatDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitCompletionDao(db: AadatDatabase): HabitCompletionDao = db.habitCompletionDao()
}
