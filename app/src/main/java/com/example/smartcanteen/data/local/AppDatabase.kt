package com.example.smartcanteen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WhitelistEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun whitelistDao(): WhitelistDao
}