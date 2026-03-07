package com.example.smartcanteen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(whitelist: WhitelistEntity)

    @Query("DELETE FROM whitelist_table WHERE studentId = :studentId")
    suspend fun deleteByStudentId(studentId: String)

    @Query("DELETE FROM whitelist_table")
    suspend fun clearAll()

    @Query("SELECT * FROM whitelist_table")
    fun getAllWhitelist(): Flow<List<WhitelistEntity>>
}