package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Investment
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY id DESC")
    fun getAllInvestments(): Flow<List<Investment>>

    @Query("SELECT SUM(investedAmount) FROM investments")
    fun getTotalInvested(): Flow<Double?>

    @Query("SELECT SUM(currentValue) FROM investments")
    fun getTotalCurrentValue(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: Investment): Long

    @Update
    suspend fun updateInvestment(investment: Investment)

    @Delete
    suspend fun deleteInvestment(investment: Investment)

    @Query("DELETE FROM investments")
    suspend fun clearAll()
}
