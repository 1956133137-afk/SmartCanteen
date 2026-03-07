package com.example.smartcanteen.di

import android.content.Context
import androidx.room.Room
import com.example.smartcanteen.data.local.AppDatabase
import com.example.smartcanteen.data.local.WhitelistDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_canteen.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWhitelistDao(database: AppDatabase): WhitelistDao {
        return database.whitelistDao()
    }
}